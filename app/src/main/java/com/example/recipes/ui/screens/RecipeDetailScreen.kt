package com.example.recipes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.recipes.data.local.RecipeDatabase
import com.example.recipes.data.local.UserPreferencesManager
import com.example.recipes.data.model.DEFAULT_RECIPE_IMAGE
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.remote.RetrofitClient
import com.example.recipes.data.repository.RecipeRepository
import com.example.recipes.ui.components.AnimatedFavoriteButton
import com.example.recipes.ui.components.ProgressBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val preferencesManager = remember { UserPreferencesManager(context) }
    val userEmailState by preferencesManager.userEmail.collectAsState(initial = null)
    val isLoggedIn by preferencesManager.isLoggedIn.collectAsState(initial = false)
    val viewModel: RecipeDetailViewModel = viewModel(
        key = "recipe-$recipeId-${userEmailState ?: "guest"}",
        factory = RecipeDetailViewModelFactory(
            recipeId,
            RecipeRepository(
                RecipeDatabase.getDatabase(LocalContext.current).recipeDao(),
                RetrofitClient.recipeApiService
            ),
            userEmailState
        )
    )
    val recipe by viewModel.recipe.collectAsState()
    val userEmail = userEmailState
    val scope = rememberCoroutineScope()
    var showAuthDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editableRecipe by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали рецепта") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    recipe?.let { r ->
                        AnimatedFavoriteButton(
                            isFavorite = r.isFavorite,
                            onClick = {
                                if (!isLoggedIn || userEmail.isNullOrBlank()) {
                                    showAuthDialog = true
                                } else {
                                    scope.launch {
                                        viewModel.toggleFavorite(!r.isFavorite)
                                    }
                                }
                            }
                        )
                        if (r.ownerId != null && r.ownerId == userEmail) {
                            IconButton(onClick = { showEditDialog = true; editableRecipe = r }) {
                                Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                            }
                        }
                        if (isLoggedIn && !userEmail.isNullOrBlank() &&
                            (r.ownerId == null || r.ownerId == userEmail)
                        ) {
                            val deleteDescription = if (r.ownerId == null) "Скрыть рецепт" else "Удалить рецепт"
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = deleteDescription)
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        recipe?.let { r ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Recipe image
                Box {
                    AsyncImage(
                        model = r.imageUrl ?: DEFAULT_RECIPE_IMAGE,
                        contentDescription = r.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Recipe info
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = r.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        ProgressBadge(difficulty = r.difficulty)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = r.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recipe stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoChip(
                            icon = Icons.Default.AccessTime,
                            label = "${r.cookingTime} мин"
                        )
                        InfoChip(
                            icon = Icons.Default.Restaurant,
                            label = "${r.servings} порций"
                        )
                        InfoChip(
                            icon = Icons.Default.Category,
                            label = r.category
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Ingredients
                    Text(
                        text = "Ингредиенты",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = r.ingredients,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Instructions
                    Text(
                        text = "Инструкции",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = r.instructions,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            confirmButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Понятно")
                }
            },
            title = { Text("Необходим вход") },
            text = { Text("Авторизуйтесь, чтобы добавлять рецепт в избранное.") }
        )
    }

    if (showDeleteDialog) {
        val hideOnly = recipe?.ownerId == null
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch {
                        val deleted = viewModel.deleteRecipe()
                        if (deleted) onNavigateBack()
                    }
                }) {
                    Text(if (hideOnly) "Скрыть" else "Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            },
            title = { Text(if (hideOnly) "Скрыть рецепт?" else "Удалить рецепт?") },
            text = {
                Text(
                    if (hideOnly) "Рецепт будет скрыт из вашего каталога, но останется доступным для других пользователей."
                    else "Это действие нельзя отменить."
                )
            }
        )
    }

    if (showEditDialog && editableRecipe != null) {
        val recipeToEdit = editableRecipe!!
        var name by remember(recipeToEdit.id) { mutableStateOf(recipeToEdit.name) }
        var description by remember(recipeToEdit.id) { mutableStateOf(recipeToEdit.description) }
        var ingredients by remember(recipeToEdit.id) { mutableStateOf(recipeToEdit.ingredients) }
        var instructions by remember(recipeToEdit.id) { mutableStateOf(recipeToEdit.instructions) }
        var cookingTime by remember(recipeToEdit.id) { mutableStateOf(recipeToEdit.cookingTime.toString()) }
        var servings by remember(recipeToEdit.id) { mutableStateOf(recipeToEdit.servings.toString()) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    scope.launch {
                        viewModel.updateRecipe(
                            recipeToEdit.copy(
                                name = name,
                                description = description,
                                ingredients = ingredients,
                                instructions = instructions,
                                cookingTime = cookingTime.toIntOrNull() ?: recipeToEdit.cookingTime,
                                servings = servings.toIntOrNull() ?: recipeToEdit.servings
                            )
                        )
                    }
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Отмена") }
            },
            title = { Text("Редактирование рецепта") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") })
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание") })
                    OutlinedTextField(value = ingredients, onValueChange = { ingredients = it }, label = { Text("Ингредиенты") })
                    OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Инструкции") })
                    OutlinedTextField(
                        value = cookingTime,
                        onValueChange = { cookingTime = it.filter { c -> c.isDigit() } },
                        label = { Text("Время (мин)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = servings,
                        onValueChange = { servings = it.filter { c -> c.isDigit() } },
                        label = { Text("Порции") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        )
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
