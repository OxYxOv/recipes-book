package com.example.recipes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val recipeId: Long,
    private val repository: RecipeRepository
) : ViewModel() {
    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe

    init {
        loadRecipe()
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            _recipe.value = repository.getRecipeById(recipeId)
        }
    }

    suspend fun toggleFavorite(isFavorite: Boolean) {
        repository.toggleFavorite(recipeId, isFavorite)
        loadRecipe()
    }
}

class RecipeDetailViewModelFactory(
    private val recipeId: Long,
    private val repository: RecipeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailViewModel(recipeId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
