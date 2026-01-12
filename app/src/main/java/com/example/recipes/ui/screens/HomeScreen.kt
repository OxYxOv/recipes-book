package com.example.recipes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipes.data.local.RecipeDatabase
import com.example.recipes.data.local.UserPreferencesManager
import com.example.recipes.data.model.Category
import com.example.recipes.data.remote.RetrofitClient
import com.example.recipes.data.repository.RecipeRepository
import com.example.recipes.ui.components.RecipeCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecipeClick: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            RecipeRepository(
                RecipeDatabase.getDatabase(LocalContext.current).recipeDao(),
                RetrofitClient.recipeApiService
            )
        )
    )
) {
    val recipes by viewModel.recipes.collectAsState()
    val categories = remember {
        listOf(
            Category("all", "Все", "🍽️"),
            Category("breakfast", "Завтрак", "🥞"),
            Category("lunch", "Обед", "🍲"),
            Category("dinner", "Ужин", "🍖"),
            Category("dessert", "Десерт", "🍰"),
            Category("snack", "Закуска", "🥗")
        )
    }
    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val listState = rememberLazyListState()
    val preferencesManager = remember { UserPreferencesManager(LocalContext.current) }
    val isLoggedIn by preferencesManager.isLoggedIn.collectAsState(initial = false)
    val userEmail by preferencesManager.userEmail.collectAsState(initial = null)
    var showAuthDialog by remember { mutableStateOf(false) }

    val orderedRecipes = remember(recipes, categories) {
        val order = categories.map { it.id }
        recipes.sortedWith(
            compareBy({ order.indexOf(it.category).takeIf { idx -> idx >= 0 } ?: Int.MAX_VALUE }, { it.id * -1 })
        )
    }
    val firstVisibleCategory by remember {
        derivedStateOf {
            orderedRecipes.getOrNull(listState.firstVisibleItemIndex)?.category ?: selectedCategory
        }
    }

    LaunchedEffect(firstVisibleCategory) {
        selectedCategory = firstVisibleCategory
    }

    LaunchedEffect(userEmail) {
        if (selectedCategory == "all") {
            viewModel.loadAllRecipes(userEmail)
        } else {
            viewModel.loadRecipesByCategory(selectedCategory, userEmail)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Книга рецептов",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        viewModel.searchRecipes(it, userEmail)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Поиск рецептов...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = androidx.compose.ui.text.input.KeyboardActions(
                        onSearch = { focusManager.clearFocus() }
                    ),
                    supportingText = { Text("Введите название или ингредиент") },
                    isError = searchQuery.length > 50,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            }
        }

        // Categories
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                        selected = selectedCategory == category.id,
                        onClick = {
                            selectedCategory = category.id
                            if (category.id == "all") {
                                viewModel.loadAllRecipes(userEmail)
                            } else {
                                viewModel.loadRecipesByCategory(category.id, userEmail)
                            }
                        },
                        label = { Text("${category.icon} ${category.name}") }
                    )
            }
        }

        // Recipes list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            state = listState
        ) {
            items(orderedRecipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                    onFavoriteClick = {
                        if (!isLoggedIn || userEmail.isNullOrBlank()) {
                            showAuthDialog = true
                        } else {
                            scope.launch {
                                viewModel.toggleFavorite(userEmail, recipe.id, !recipe.isFavorite)
                            }
                        }
                    }
                )
            }

            if (recipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Рецепты не найдены",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            confirmButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Хорошо")
                }
            },
            title = { Text("Требуется авторизация") },
            text = { Text("Войдите в профиль, чтобы добавлять рецепты в избранное.") }
        )
    }
}
