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
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeRepositoryTest {

    @Mock
    private lateinit var recipeDao: RecipeDao

    @Mock
    private lateinit var apiService: RecipeApiService

    private lateinit var repository: RecipeRepository
    private val userId = "user@test.com"

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
        isFavorite = false,
        ownerId = userId
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
        whenever(recipeDao.getAllRecipes(userId)).thenReturn(flowOf(recipes))
        whenever(recipeDao.getFavoriteIds(userId)).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.getAllRecipes(userId).first()

        // Then
        assertEquals(recipes, result)
        verify(recipeDao).getAllRecipes(userId)
    }

    @Test
    fun `getFavoriteRecipes should return only favorite recipes`() = runTest {
        // Given
        val favoriteRecipe = testRecipe.copy(isFavorite = true, id = 2)
        val favorites = listOf(favoriteRecipe)
        whenever(recipeDao.getFavoriteRecipes(userId)).thenReturn(flowOf(favorites))
        whenever(recipeDao.getFavoriteIds(userId)).thenReturn(flowOf(listOf(favoriteRecipe.id)))

        // When
        val result = repository.getFavoriteRecipes(userId).first()

        // Then
        assertEquals(favorites, result)
        assertTrue(result.all { it.isFavorite })
        verify(recipeDao).getFavoriteRecipes(userId)
    }

    @Test
    fun `searchRecipes should return matching recipes`() = runTest {
        // Given
        val query = "Test"
        val recipes = listOf(testRecipe)
        whenever(recipeDao.searchRecipes(query, userId)).thenReturn(flowOf(recipes))
        whenever(recipeDao.getFavoriteIds(userId)).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.searchRecipes(query, userId).first()

        // Then
        assertEquals(recipes, result)
        verify(recipeDao).searchRecipes(query, userId)
    }

    @Test
    fun `getRecipesByCategory should return recipes of specific category`() = runTest {
        // Given
        val category = "breakfast"
        val recipes = listOf(testRecipe)
        whenever(recipeDao.getRecipesByCategory(category, userId)).thenReturn(flowOf(recipes))
        whenever(recipeDao.getFavoriteIds(userId)).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.getRecipesByCategory(category, userId).first()

        // Then
        assertEquals(recipes, result)
        assertEquals(category, result.first().category)
        verify(recipeDao).getRecipesByCategory(category, userId)
    }

    @Test
    fun `getRecipeById should return recipe when exists`() = runTest {
        // Given
        val recipeId = 1L
        whenever(recipeDao.getRecipeById(recipeId, userId)).thenReturn(testRecipe)
        whenever(recipeDao.getFavoriteIds(userId)).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.getRecipeById(recipeId, userId)

        // Then
        assertEquals(testRecipe, result)
        verify(recipeDao).getRecipeById(recipeId, userId)
    }

    @Test
    fun `getRecipeById should return null when not exists`() = runTest {
        // Given
        val recipeId = 999L
        whenever(recipeDao.getRecipeById(recipeId, userId)).thenReturn(null)
        whenever(recipeDao.getFavoriteIds(userId)).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.getRecipeById(recipeId, userId)

        // Then
        assertNull(result)
        verify(recipeDao).getRecipeById(recipeId, userId)
    }

    @Test
    fun `insertRecipe should return inserted recipe id`() = runTest {
        // Given
        val recipeId = 1L
        whenever(recipeDao.insertRecipe(any())).thenReturn(recipeId)

        // When
        val result = repository.insertRecipe(testRecipe)

        // Then
        assertEquals(recipeId, result)
        verify(recipeDao).insertRecipe(any())
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
        repository.toggleFavorite(userId, recipeId, isFavorite)

        // Then
        verify(recipeDao).addFavoriteRecipe(any())
    }

    @Test
    fun `syncRecipesFromApi should insert recipes on success`() = runTest {
        // Given
        val apiRecipes = listOf(testRecipe)
        whenever(apiService.getRecipes()).thenReturn(apiRecipes)
        whenever(recipeDao.insertRecipe(any())).thenReturn(1L)

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
