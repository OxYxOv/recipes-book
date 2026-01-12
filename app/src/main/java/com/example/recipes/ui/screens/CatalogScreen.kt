package com.example.recipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipes.data.local.RecipeDatabase
import com.example.recipes.data.local.UserPreferencesManager
import com.example.recipes.data.remote.RetrofitClient
import com.example.recipes.data.repository.RecipeRepository
import com.example.recipes.ui.components.RecipeCard
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(
    onRecipeClick: (Long) -> Unit,
    viewModel: CatalogViewModel = viewModel(
        factory = CatalogViewModelFactory(
            RecipeRepository(
                RecipeDatabase.getDatabase(LocalContext.current).recipeDao(),
                RetrofitClient.recipeApiService
            )
        )
    )
) {
    val recipes by viewModel.recipes.collectAsState()
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { UserPreferencesManager(LocalContext.current) }
    val isLoggedIn by preferencesManager.isLoggedIn.collectAsState(initial = false)
    val userEmail by preferencesManager.userEmail.collectAsState(initial = null)
    var showAuthDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userEmail) {
        viewModel.loadRecipes(userEmail)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Text(
                text = "Каталог рецептов",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Recipes list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            items(recipes) { recipe ->
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
                    Text("Понял")
                }
            },
            title = { Text("Требуется вход") },
            text = { Text("Авторизуйтесь, чтобы управлять избранным.") }
        )
    }
}
