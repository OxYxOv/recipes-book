package com.example.recipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recipes.data.local.RecipeDatabase
import com.example.recipes.data.local.UserPreferencesManager
import com.example.recipes.data.remote.RetrofitClient
import com.example.recipes.data.repository.RecipeRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import android.app.TimePickerDialog
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.focusRequester

private const val COOKING_TIME_MAX_LENGTH = 4
private const val SERVINGS_MAX_LENGTH = 3

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddRecipeScreen(
    onRecipeAdded: () -> Unit,
    onAuthRequired: () -> Unit = {},
    viewModel: AddRecipeViewModel = viewModel(
        factory = AddRecipeViewModelFactory(
            RecipeRepository(
                RecipeDatabase.getDatabase(LocalContext.current).recipeDao(),
                RetrofitClient.recipeApiService
            )
        )
    )
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var cookingTime by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("breakfast") }
    var difficulty by remember { mutableStateOf("easy") }
    var imageUrl by remember { mutableStateOf("") }
    var attemptedSubmit by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val preferencesManager = remember { UserPreferencesManager(context) }
    val isLoggedIn by preferencesManager.isLoggedIn.collectAsState(initial = false)
    val userEmail by preferencesManager.userEmail.collectAsState(initial = null)
    var showTimePicker by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    // Only check auth status on first load, not continuously
    LaunchedEffect(Unit) {
        if (!isLoggedIn || userEmail.isNullOrBlank()) {
            showAuthDialog = true
        }
    }

    val nameError = attemptedSubmit && name.isBlank()
    val descError = attemptedSubmit && description.isBlank()
    val ingredientError = attemptedSubmit && ingredients.isBlank()
    val instructionError = attemptedSubmit && instructions.isBlank()
    val cookingError = attemptedSubmit && cookingTime.isBlank()
    val servingsError = attemptedSubmit && servings.isBlank()
    
    // Helper function to check if form is valid
    fun isFormValid(): Boolean {
        return name.isNotBlank() && 
               description.isNotBlank() && 
               ingredients.isNotBlank() && 
               instructions.isNotBlank() && 
               cookingTime.isNotBlank() && 
               servings.isNotBlank()
    }

    val categories = listOf(
        "breakfast" to "Завтрак",
        "lunch" to "Обед",
        "dinner" to "Ужин",
        "dessert" to "Десерт",
        "snack" to "Закуска"
    )

    val difficulties = listOf(
        "easy" to "Легко",
        "medium" to "Средне",
        "hard" to "Сложно"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (showAuthDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAuthDialog = false
                    onAuthRequired()
                },
                confirmButton = {
                    TextButton(onClick = {
                        showAuthDialog = false
                        onAuthRequired()
                    }) { Text("Перейти к входу") }
                },
                title = { Text("Требуется авторизация") },
                text = { Text("Войдите или зарегистрируйтесь, чтобы добавлять собственные рецепты.") }
            )
        }
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Text(
                text = "Добавить рецепт",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название рецепта") },
                placeholder = { Text("Например, Паста Карбонара") },
                isError = nameError,
                supportingText = { if (nameError) Text("Название обязательно") else Text("Введите название вашего рецепта") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                placeholder = { Text("Кратко опишите блюдо") },
                isError = descError,
                supportingText = { if (descError) Text("Добавьте описание") else Text("Опишите вкус и особенности блюда") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ингредиенты (по одному на строку)") },
                placeholder = { Text("Например:\n300г муки\n2 яйца\n100мл молока") },
                isError = ingredientError,
                supportingText = { if (ingredientError) Text("Введите ингредиенты") else Text("Укажите каждый ингредиент с новой строки") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Инструкции приготовления") },
                placeholder = { Text("Например:\n1. Смешайте муку и яйца\n2. Добавьте молоко\n3. Выпекайте 20 минут") },
                isError = instructionError,
                supportingText = { if (instructionError) Text("Добавьте инструкции") else Text("Опишите пошагово процесс приготовления") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = cookingTime,
                    onValueChange = { input ->
                        cookingTime = input.filter { it.isDigit() }.take(COOKING_TIME_MAX_LENGTH)
                    },
                    label = { Text("Время (мин)") },
                    placeholder = { Text("Например, 30") },
                    isError = cookingError,
                    supportingText = { if (cookingError) Text("Укажите время") else Text("Время приготовления в минутах") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Выбрать время приготовления"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = servings,
                    onValueChange = { input ->
                        servings = input.filter { it.isDigit() }.take(SERVINGS_MAX_LENGTH)
                    },
                    label = { Text("Порции") },
                    placeholder = { Text("Например, 4") },
                    isError = servingsError,
                    supportingText = { if (servingsError) Text("Укажите порции") else Text("Количество порций") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.first == category }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
                        placeholder = { Text("Выберите категорию блюда") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        supportingText = { Text("Тип приема пищи для этого блюда") }
                    )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                category = id
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Difficulty dropdown
            var difficultyExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                    expanded = difficultyExpanded,
                    onExpandedChange = { difficultyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = difficulties.find { it.first == difficulty }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Сложность") },
                        placeholder = { Text("Выберите уровень сложности") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        supportingText = { Text("Насколько сложно приготовить это блюдо") }
                    )
                ExposedDropdownMenu(
                    expanded = difficultyExpanded,
                    onDismissRequest = { difficultyExpanded = false }
                ) {
                    difficulties.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                difficulty = id
                                difficultyExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL изображения (необязательно)") },
                placeholder = { Text("https://example.com/image.jpg") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Если оставить пустым, будет использовано изображение по умолчанию") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    attemptedSubmit = true
                    if (isLoggedIn && !userEmail.isNullOrBlank() && isFormValid()) {
                        scope.launch {
                            viewModel.addRecipe(
                                name = name,
                                description = description,
                                ingredients = ingredients,
                                instructions = instructions,
                                cookingTime = cookingTime.toIntOrNull() ?: 0,
                                servings = servings.toIntOrNull() ?: 1,
                                category = category,
                                difficulty = difficulty,
                                imageUrl = imageUrl.ifBlank { null },
                                ownerId = userEmail!!
                            )
                            onRecipeAdded()
                        }
                    } else if (!isLoggedIn || userEmail.isNullOrBlank()) {
                        onAuthRequired()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить рецепт")
            }
        }
    }

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val totalMinutes = hourOfDay * 60 + minute
                cookingTime = totalMinutes.toString()
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).apply {
            setOnDismissListener { showTimePicker = false }
            show()
        }
    }
}
