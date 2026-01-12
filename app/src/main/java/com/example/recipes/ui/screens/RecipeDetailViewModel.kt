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
    private val repository: RecipeRepository,
    private val userId: String?
) : ViewModel() {
    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe

    init {
        loadRecipe()
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            _recipe.value = repository.getRecipeById(recipeId, userId)
        }
    }

    suspend fun toggleFavorite(isFavorite: Boolean) {
        if (!userId.isNullOrBlank()) {
            repository.toggleFavorite(userId, recipeId, isFavorite)
            loadRecipe()
        }
    }

    suspend fun deleteRecipe(): Boolean {
        val currentRecipe = _recipe.value ?: return false
        return when {
            currentRecipe.ownerId != null && currentRecipe.ownerId == userId -> {
                repository.deleteRecipe(currentRecipe)
                true
            }
            currentRecipe.ownerId == null && !userId.isNullOrBlank() -> {
                repository.hideRecipe(userId, currentRecipe.id)
                true
            }
            else -> false
        }
    }

    suspend fun updateRecipe(updated: Recipe) {
        if (updated.ownerId != null && updated.ownerId == userId) {
            repository.updateRecipe(updated)
            loadRecipe()
        }
    }
}

class RecipeDetailViewModelFactory(
    private val recipeId: Long,
    private val repository: RecipeRepository,
    private val userId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailViewModel(recipeId, repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
