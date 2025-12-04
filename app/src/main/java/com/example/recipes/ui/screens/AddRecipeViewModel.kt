package com.example.recipes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.repository.RecipeRepository

class AddRecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
    suspend fun addRecipe(
        name: String,
        description: String,
        ingredients: String,
        instructions: String,
        cookingTime: Int,
        servings: Int,
        category: String,
        difficulty: String,
        imageUrl: String?
    ) {
        val recipe = Recipe(
            name = name,
            description = description,
            ingredients = ingredients,
            instructions = instructions,
            cookingTime = cookingTime,
            servings = servings,
            category = category,
            difficulty = difficulty,
            imageUrl = imageUrl,
            isLocal = true
        )
        repository.insertRecipe(recipe)
    }
}

class AddRecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddRecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddRecipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
