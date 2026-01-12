package com.example.recipes.data.local

import androidx.room.*
import com.example.recipes.data.model.FavoriteRecipe
import com.example.recipes.data.model.HiddenRecipe
import com.example.recipes.data.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query(
        """
        SELECT * FROM recipes 
        WHERE (ownerId IS NULL OR (:userId IS NOT NULL AND ownerId = :userId))
        AND (:userId IS NULL OR id NOT IN (SELECT recipeId FROM hidden_recipes WHERE userId = :userId))
        ORDER BY id DESC
        """
    )
    fun getAllRecipes(userId: String?): Flow<List<Recipe>>

    @Query(
        """
        SELECT * FROM recipes 
        WHERE id = :id AND (ownerId IS NULL OR (:userId IS NOT NULL AND ownerId = :userId))
        AND (:userId IS NULL OR id NOT IN (SELECT recipeId FROM hidden_recipes WHERE userId = :userId))
        """
    )
    suspend fun getRecipeById(id: Long, userId: String?): Recipe?

    @Query(
        """
        SELECT * FROM recipes 
        WHERE category = :category 
        AND (ownerId IS NULL OR (:userId IS NOT NULL AND ownerId = :userId))
        AND (:userId IS NULL OR id NOT IN (SELECT recipeId FROM hidden_recipes WHERE userId = :userId))
        ORDER BY id DESC
        """
    )
    fun getRecipesByCategory(category: String, userId: String?): Flow<List<Recipe>>

    @Query(
        """
        SELECT recipes.* FROM recipes 
        INNER JOIN favorite_recipes ON recipes.id = favorite_recipes.recipeId
        WHERE favorite_recipes.userId = :userId
        AND (recipes.ownerId IS NULL OR recipes.ownerId = :userId)
        AND recipes.id NOT IN (SELECT recipeId FROM hidden_recipes WHERE userId = :userId)
        ORDER BY recipes.id DESC
        """
    )
    fun getFavoriteRecipes(userId: String): Flow<List<Recipe>>

    @Query(
        """
        SELECT * FROM recipes 
        WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND (ownerId IS NULL OR (:userId IS NOT NULL AND ownerId = :userId))
        AND (:userId IS NULL OR id NOT IN (SELECT recipeId FROM hidden_recipes WHERE userId = :userId))
        ORDER BY id DESC
        """
    )
    fun searchRecipes(query: String, userId: String?): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteRecipe(favoriteRecipe: FavoriteRecipe)

    @Query("DELETE FROM favorite_recipes WHERE userId = :userId AND recipeId = :recipeId")
    suspend fun removeFavoriteRecipe(userId: String, recipeId: Long)

    @Query("SELECT recipeId FROM favorite_recipes WHERE userId = :userId")
    fun getFavoriteIds(userId: String): Flow<List<Long>>

    @Query("SELECT * FROM recipes WHERE ownerId = :userId ORDER BY id DESC")
    fun getUserRecipes(userId: String): Flow<List<Recipe>>

    @Query(
        """
        SELECT COUNT(*) > 0 FROM recipes 
        WHERE (ownerId IS NULL OR (:userId IS NOT NULL AND ownerId = :userId))
        AND (:userId IS NULL OR id NOT IN (SELECT recipeId FROM hidden_recipes WHERE userId = :userId))
        """
    )
    suspend fun hasRecipes(userId: String?): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun hideRecipe(hiddenRecipe: HiddenRecipe)

    @Query("DELETE FROM hidden_recipes WHERE userId = :userId AND recipeId = :recipeId")
    suspend fun unhideRecipe(userId: String, recipeId: Long)
}
