package com.example.recipes.data.repository

import com.example.recipes.data.local.RecipeDao
import com.example.recipes.data.model.DEFAULT_RECIPE_IMAGE
import com.example.recipes.data.model.FavoriteRecipe
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.remote.RecipeApiService
import kotlinx.coroutines.flow.*

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val apiService: RecipeApiService
) {
    fun getAllRecipes(userId: String?): Flow<List<Recipe>> =
        combine(recipeDao.getAllRecipes(userId), favoriteIds(userId)) { recipes, favorites ->
            recipes.withFavorites(favorites)
        }

    fun getFavoriteRecipes(userId: String): Flow<List<Recipe>> =
        combine(recipeDao.getFavoriteRecipes(userId), favoriteIds(userId)) { recipes, favorites ->
            recipes.withFavorites(favorites)
        }

    fun searchRecipes(query: String, userId: String?): Flow<List<Recipe>> =
        combine(recipeDao.searchRecipes(query, userId), favoriteIds(userId)) { recipes, favorites ->
            recipes.withFavorites(favorites)
        }

    fun getRecipesByCategory(category: String, userId: String?): Flow<List<Recipe>> =
        combine(recipeDao.getRecipesByCategory(category, userId), favoriteIds(userId)) { recipes, favorites ->
            recipes.withFavorites(favorites)
        }

    fun getUserRecipes(userId: String): Flow<List<Recipe>> =
        combine(recipeDao.getUserRecipes(userId), favoriteIds(userId)) { recipes, favorites ->
            recipes.withFavorites(favorites)
        }

    suspend fun getRecipeById(id: Long, userId: String?): Recipe? {
        val recipe = recipeDao.getRecipeById(id, userId) ?: return null
        val favorites = favoriteIds(userId).first()
        return recipe.copy(isFavorite = favorites.contains(id))
    }

    suspend fun insertRecipe(recipe: Recipe): Long =
        recipeDao.insertRecipe(
            recipe.copy(
                imageUrl = recipe.imageUrl?.takeIf { it.isNotBlank() } ?: DEFAULT_RECIPE_IMAGE
            )
        )

    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)

    suspend fun toggleFavorite(userId: String, id: Long, isFavorite: Boolean) {
        if (isFavorite) {
            recipeDao.addFavoriteRecipe(FavoriteRecipe(userId = userId, recipeId = id))
        } else {
            recipeDao.removeFavoriteRecipe(userId = userId, recipeId = id)
        }
    }

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

    private fun favoriteIds(userId: String?): Flow<Set<Long>> =
        if (userId.isNullOrBlank()) flowOf(emptySet())
        else recipeDao.getFavoriteIds(userId).map { it.toSet() }

    private fun List<Recipe>.withFavorites(favorites: Set<Long>): List<Recipe> =
        map { recipe -> recipe.copy(isFavorite = favorites.contains(recipe.id)) }
}
