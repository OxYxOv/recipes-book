// kotlin
package com.example.recipes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    val initialHighlightedCategory = remember(categories) {
        categories.firstOrNull { it.id != "all" }?.id ?: "all"
    }
    var highlightedCategory by remember { mutableStateOf(initialHighlightedCategory) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val preferencesManager = remember { UserPreferencesManager(context) }
    val isLoggedIn by preferencesManager.isLoggedIn.collectAsState(initial = false)
    val userEmail by preferencesManager.userEmail.collectAsState(initial = null)
    var showAuthDialog by remember { mutableStateOf(false) }

    val orderedRecipes = remember(recipes, categories) {
        val order = categories.map { it.id }
        recipes.sortedWith(
            compareBy(
                { idRec -> order.indexOf(idRec.category).takeIf { idx -> idx >= 0 } ?: Int.MAX_VALUE },
                { it.id * -1 }
            )
        )
    }
    val firstVisibleCategory by remember {
        derivedStateOf {
            orderedRecipes.getOrNull(listState.firstVisibleItemIndex)?.category ?: highlightedCategory
        }
    }

    LaunchedEffect(firstVisibleCategory) {
        highlightedCategory = firstVisibleCategory
    }

    LaunchedEffect(userEmail, selectedCategory) {
        if (selectedCategory == "all") {
            viewModel.loadAllRecipes(userEmail)
        } else {
            viewModel.loadRecipesByCategory(selectedCategory, userEmail)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchRecipes(it, userEmail)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text("Поиск рецептов...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { focusManager.clearFocus() }
                    ),
                    supportingText = { Text("Введите название или ингредиент") },
                    isError = searchQuery.length > 50,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = highlightedCategory == category.id,
                    onClick = {
                        selectedCategory = category.id
                        if (category.id != "all") {
                            val targetIndex = orderedRecipes.indexOfFirst { it.category == category.id }
                            if (targetIndex in orderedRecipes.indices) {
                                scope.launch { listState.animateScrollToItem(targetIndex, scrollOffset = 0) }
                            }
                        }
                        highlightedCategory = category.id
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
