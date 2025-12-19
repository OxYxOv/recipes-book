package com.example.recipes.ui.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: RecipeRepository

    private lateinit var viewModel: RecipeDetailViewModel

    private val testRecipeId = 1L
    private val testRecipe = Recipe(
        id = testRecipeId,
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
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load recipe`() = runTest {
        // Given
        whenever(repository.getRecipeById(testRecipeId)).thenReturn(testRecipe)

        // When
        viewModel = RecipeDetailViewModel(testRecipeId, repository)
        advanceUntilIdle()

        // Then
        assertEquals(testRecipe, viewModel.recipe.value)
        verify(repository).getRecipeById(testRecipeId)
    }

    @Test
    fun `init should set null when recipe not found`() = runTest {
        // Given
        whenever(repository.getRecipeById(testRecipeId)).thenReturn(null)

        // When
        viewModel = RecipeDetailViewModel(testRecipeId, repository)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.recipe.value)
        verify(repository).getRecipeById(testRecipeId)
    }

    @Test
    fun `toggleFavorite should update favorite status and reload`() = runTest {
        // Given
        val isFavorite = true
        whenever(repository.getRecipeById(testRecipeId)).thenReturn(testRecipe)
        
        viewModel = RecipeDetailViewModel(testRecipeId, repository)
        advanceUntilIdle()

        val updatedRecipe = testRecipe.copy(isFavorite = true)
        whenever(repository.getRecipeById(testRecipeId)).thenReturn(updatedRecipe)

        // When
        viewModel.toggleFavorite(isFavorite)
        advanceUntilIdle()

        // Then
        verify(repository).toggleFavorite(testRecipeId, isFavorite)
        // getRecipeById should be called twice: init and after toggle
        verify(repository).getRecipeById(testRecipeId)
    }


}
