package com.example.recipes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.repository.RecipeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: RecipeRepository) : ViewModel() {
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes
    private var loadJob: Job? = null

    init {
        loadAllRecipes(null)
    }

    fun loadAllRecipes(userId: String?) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.getAllRecipes(userId).collect { recipes ->
                _recipes.value = recipes
            }
        }
    }

    fun loadRecipesByCategory(category: String, userId: String?) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.getRecipesByCategory(category, userId).collect { recipes ->
                _recipes.value = recipes
            }
        }
    }

    fun searchRecipes(query: String, userId: String?) {
        if (query.isBlank()) {
            loadAllRecipes(userId)
            return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.searchRecipes(query, userId).collect { recipes ->
                _recipes.value = recipes
            }
        }
    }

    suspend fun toggleFavorite(userId: String?, id: Long, isFavorite: Boolean) {
        if (!userId.isNullOrBlank()) {
            repository.toggleFavorite(userId, id, isFavorite)
        }
    }
}

class HomeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
