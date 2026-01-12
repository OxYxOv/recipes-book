package com.example.recipes.ui.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: RecipeRepository

    private lateinit var viewModel: HomeViewModel
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
    fun `loadAllRecipes should update recipes state`() = runTest {
        // Given
        val recipes = listOf(testRecipe)
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(recipes))

        // When
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // Then
        assertEquals(recipes, viewModel.recipes.value)
        verify(repository).getAllRecipes(null)
    }

    @Test
    fun `loadRecipesByCategory should filter by category`() = runTest {
        // Given
        val category = "breakfast"
        val recipes = listOf(testRecipe)
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(emptyList()))
        whenever(repository.getRecipesByCategory(category, null)).thenReturn(flowOf(recipes))

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.loadRecipesByCategory(category, null)
        advanceUntilIdle()

        // Then
        assertEquals(recipes, viewModel.recipes.value)
        verify(repository).getRecipesByCategory(category, null)
    }

    @Test
    fun `searchRecipes should filter recipes by query`() = runTest {
        // Given
        val query = "Test"
        val recipes = listOf(testRecipe)
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(emptyList()))
        whenever(repository.searchRecipes(query, null)).thenReturn(flowOf(recipes))

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.searchRecipes(query, null)
        advanceUntilIdle()

        // Then
        assertEquals(recipes, viewModel.recipes.value)
        verify(repository).searchRecipes(query, null)
    }

    @Test
    fun `searchRecipes with blank query should load all recipes`() = runTest {
        // Given
        val recipes = listOf(testRecipe)
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(recipes))

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.searchRecipes("", null)
        advanceUntilIdle()

        // Then
        // getAllRecipes is called twice: in init and in searchRecipes
        verify(repository, times(2)).getAllRecipes(null)
    }

    @Test
    fun `toggleFavorite should call repository`() = runTest {
        // Given
        val recipeId = 1L
        val isFavorite = true
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(emptyList()))

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.toggleFavorite(userId, recipeId, isFavorite)

        // Then
        verify(repository).toggleFavorite(userId, recipeId, isFavorite)
    }

    @Test
    fun `initial state should be empty list`() = runTest {
        // Given
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(emptyList()))

        // When
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.recipes.value.isEmpty())
    }
}
