package com.example.recipes.data.repository

import com.example.recipes.data.local.RecipeDao
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.remote.RecipeApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeRepositoryTest {

    @Mock
    private lateinit var recipeDao: RecipeDao

    @Mock
    private lateinit var apiService: RecipeApiService

    private lateinit var repository: RecipeRepository

    private val testRecipe = Recipe(
        id = 1,
        name = "Test Recipe",
        description = "Test Description",
        ingredients = "Test Ingredients",
        instructions = "Test Instructions",
        category = "breakfast",
        cookingTime = 30,
        difficulty = "easy",
        servings = 2,
        isFavorite = false
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = RecipeRepository(recipeDao, apiService)
    }

    @Test
    fun `getAllRecipes should return flow from dao`() = runTest {
        // Given
        val recipes = listOf(testRecipe)
        whenever(recipeDao.getAllRecipes()).thenReturn(flowOf(recipes))

        // When
        val result = repository.getAllRecipes().first()

        // Then
        assertEquals(recipes, result)
        verify(recipeDao).getAllRecipes()
    }

    @Test
    fun `getFavoriteRecipes should return only favorite recipes`() = runTest {
        // Given
        val favoriteRecipe = testRecipe.copy(isFavorite = true)
        val favorites = listOf(favoriteRecipe)
        whenever(recipeDao.getFavoriteRecipes()).thenReturn(flowOf(favorites))

        // When
        val result = repository.getFavoriteRecipes().first()

        // Then
        assertEquals(favorites, result)
        assertTrue(result.all { it.isFavorite })
        verify(recipeDao).getFavoriteRecipes()
    }

    @Test
    fun `searchRecipes should return matching recipes`() = runTest {
        // Given
        val query = "Test"
        val recipes = listOf(testRecipe)
        whenever(recipeDao.searchRecipes(query)).thenReturn(flowOf(recipes))

        // When
        val result = repository.searchRecipes(query).first()

        // Then
        assertEquals(recipes, result)
        verify(recipeDao).searchRecipes(query)
    }

    @Test
    fun `getRecipesByCategory should return recipes of specific category`() = runTest {
        // Given
        val category = "breakfast"
        val recipes = listOf(testRecipe)
        whenever(recipeDao.getRecipesByCategory(category)).thenReturn(flowOf(recipes))

        // When
        val result = repository.getRecipesByCategory(category).first()

        // Then
        assertEquals(recipes, result)
        assertEquals(category, result.first().category)
        verify(recipeDao).getRecipesByCategory(category)
    }

    @Test
    fun `getRecipeById should return recipe when exists`() = runTest {
        // Given
        val recipeId = 1L
        whenever(recipeDao.getRecipeById(recipeId)).thenReturn(testRecipe)

        // When
        val result = repository.getRecipeById(recipeId)

        // Then
        assertEquals(testRecipe, result)
        verify(recipeDao).getRecipeById(recipeId)
    }

    @Test
    fun `getRecipeById should return null when not exists`() = runTest {
        // Given
        val recipeId = 999L
        whenever(recipeDao.getRecipeById(recipeId)).thenReturn(null)

        // When
        val result = repository.getRecipeById(recipeId)

        // Then
        assertNull(result)
        verify(recipeDao).getRecipeById(recipeId)
    }

    @Test
    fun `insertRecipe should return inserted recipe id`() = runTest {
        // Given
        val recipeId = 1L
        whenever(recipeDao.insertRecipe(testRecipe)).thenReturn(recipeId)

        // When
        val result = repository.insertRecipe(testRecipe)

        // Then
        assertEquals(recipeId, result)
        verify(recipeDao).insertRecipe(testRecipe)
    }

    @Test
    fun `updateRecipe should call dao update`() = runTest {
        // Given
        val updatedRecipe = testRecipe.copy(name = "Updated Name")

        // When
        repository.updateRecipe(updatedRecipe)

        // Then
        verify(recipeDao).updateRecipe(updatedRecipe)
    }

    @Test
    fun `deleteRecipe should call dao delete`() = runTest {
        // When
        repository.deleteRecipe(testRecipe)

        // Then
        verify(recipeDao).deleteRecipe(testRecipe)
    }

    @Test
    fun `toggleFavorite should update favorite status`() = runTest {
        // Given
        val recipeId = 1L
        val isFavorite = true

        // When
        repository.toggleFavorite(recipeId, isFavorite)

        // Then
        verify(recipeDao).updateFavoriteStatus(recipeId, isFavorite)
    }

    @Test
    fun `syncRecipesFromApi should insert recipes on success`() = runTest {
        // Given
        val apiRecipes = listOf(testRecipe)
        whenever(apiService.getRecipes()).thenReturn(apiRecipes)
        whenever(recipeDao.insertRecipe(testRecipe.copy(isLocal = false))).thenReturn(1L)

        // When
        val result = repository.syncRecipesFromApi().first()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(apiRecipes, result.getOrNull())
        verify(apiService).getRecipes()
    }

    @Test
    fun `syncRecipesFromApi should return failure on exception`() = runTest {
        // Given
        val exception = Exception("Network error")
        whenever(apiService.getRecipes()).thenThrow(exception)

        // When
        val result = repository.syncRecipesFromApi().first()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
        verify(apiService).getRecipes()
    }
}
