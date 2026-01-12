package com.example.recipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val preferencesManager = remember { UserPreferencesManager(context) }
    val isLoggedIn by preferencesManager.isLoggedIn.collectAsState(initial = false)
    val userEmail by preferencesManager.userEmail.collectAsState(initial = null)
    var showTimePicker by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isLoggedIn, userEmail) {
        showAuthDialog = !isLoggedIn || userEmail.isNullOrBlank()
    }

    val nameError = name.isBlank()
    val descError = description.isBlank()
    val ingredientError = ingredients.isBlank()
    val instructionError = instructions.isBlank()
    val cookingError = cookingTime.isBlank()
    val servingsError = servings.isBlank()

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
                isError = attemptedSave && nameError,
                supportingText = { if (attemptedSave && nameError) Text("Название обязательно") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                placeholder = { Text("Кратко опишите блюдо") },
                isError = attemptedSave && descError,
                supportingText = { if (attemptedSave && descError) Text("Добавьте описание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ингредиенты (по одному на строку)") },
                placeholder = { Text("Молоко, яйца, соль ...") },
                isError = attemptedSave && ingredientError,
                supportingText = { if (attemptedSave && ingredientError) Text("Введите ингредиенты") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Инструкции приготовления") },
                placeholder = { Text("Шаги приготовления по порядку") },
                isError = attemptedSave && instructionError,
                supportingText = { if (attemptedSave && instructionError) Text("Добавьте инструкции") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5
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
                    isError = attemptedSave && cookingError,
                    supportingText = { if (attemptedSave && cookingError) Text("Укажите время приготовления") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        TextButton(onClick = { showTimePicker = true }) {
                            Text("Выбрать")
                        }
                    }
                )

                OutlinedTextField(
                    value = servings,
                    onValueChange = { input ->
                        servings = input.filter { it.isDigit() }.take(SERVINGS_MAX_LENGTH)
                    },
                    label = { Text("Порции") },
                    placeholder = { Text("Например, 2") },
                    isError = attemptedSave && servingsError,
                    supportingText = { if (attemptedSave && servingsError) Text("Укажите количество порций") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
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
                        placeholder = { Text("Выберите категорию") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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
                        placeholder = { Text("Выберите сложность") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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
                placeholder = { Text("Если оставить пустым, добавится картинка по умолчанию") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    attemptedSave = true
                    if (!isLoggedIn || userEmail.isNullOrBlank()) {
                        onAuthRequired()
                        return@Button
                    }
                    if (nameError || descError || ingredientError || instructionError || cookingError || servingsError) {
                        return@Button
                    }
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
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isLoggedIn && !userEmail.isNullOrBlank()
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
