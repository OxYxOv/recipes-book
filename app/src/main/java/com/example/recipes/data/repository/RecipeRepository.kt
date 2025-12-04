package com.example.recipes.data.repository

import com.example.recipes.data.local.RecipeDao
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.remote.RecipeApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val apiService: RecipeApiService
) {
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()

    fun getFavoriteRecipes(): Flow<List<Recipe>> = recipeDao.getFavoriteRecipes()

    fun searchRecipes(query: String): Flow<List<Recipe>> = recipeDao.searchRecipes(query)

    fun getRecipesByCategory(category: String): Flow<List<Recipe>> = 
        recipeDao.getRecipesByCategory(category)

    suspend fun getRecipeById(id: Long): Recipe? = recipeDao.getRecipeById(id)

    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insertRecipe(recipe)

    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = 
        recipeDao.updateFavoriteStatus(id, isFavorite)

    fun syncRecipesFromApi(): Flow<Result<List<Recipe>>> = flow {
        try {
            val recipes = apiService.getRecipes()
            recipes.forEach { recipe ->
                recipeDao.insertRecipe(recipe.copy(isLocal = false))
            }
            emit(Result.success(recipes))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
