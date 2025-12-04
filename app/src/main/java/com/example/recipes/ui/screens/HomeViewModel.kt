package com.example.recipes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: RecipeRepository) : ViewModel() {
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    init {
        loadAllRecipes()
    }

    fun loadAllRecipes() {
        viewModelScope.launch {
            repository.getAllRecipes().collect { recipes ->
                _recipes.value = recipes
            }
        }
    }

    fun loadRecipesByCategory(category: String) {
        viewModelScope.launch {
            repository.getRecipesByCategory(category).collect { recipes ->
                _recipes.value = recipes
            }
        }
    }

    fun searchRecipes(query: String) {
        if (query.isBlank()) {
            loadAllRecipes()
            return
        }
        viewModelScope.launch {
            repository.searchRecipes(query).collect { recipes ->
                _recipes.value = recipes
            }
        }
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        repository.toggleFavorite(id, isFavorite)
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
