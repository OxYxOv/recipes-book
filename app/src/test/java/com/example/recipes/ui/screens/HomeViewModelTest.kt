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
        whenever(repository.getAllRecipes()).thenReturn(flowOf(recipes))

        // When
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // Then
        assertEquals(recipes, viewModel.recipes.value)
        verify(repository).getAllRecipes()
    }

    @Test
    fun `loadRecipesByCategory should filter by category`() = runTest {
        // Given
        val category = "breakfast"
        val recipes = listOf(testRecipe)
        whenever(repository.getAllRecipes()).thenReturn(flowOf(emptyList()))
        whenever(repository.getRecipesByCategory(category)).thenReturn(flowOf(recipes))

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.loadRecipesByCategory(category)
        advanceUntilIdle()

        // Then
        assertEquals(recipes, viewModel.recipes.value)
        verify(repository).getRecipesByCategory(category)
    }

    @Test
    fun `searchRecipes should filter recipes by query`() = runTest {
        // Given
        val query = "Test"
        val recipes = listOf(testRecipe)
        whenever(repository.getAllRecipes()).thenReturn(flowOf(emptyList()))
        whenever(repository.searchRecipes(query)).thenReturn(flowOf(recipes))

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.searchRecipes(query)
        advanceUntilIdle()

        // Then
        assertEquals(recipes, viewModel.recipes.value)
        verify(repository).searchRecipes(query)
    }

    @Test
    fun `searchRecipes with blank query should load all recipes`() = runTest {
        // Given
        val recipes = listOf(testRecipe)
        whenever(repository.getAllRecipes()).thenReturn(flowOf(recipes))

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.searchRecipes("")
        advanceUntilIdle()

        // Then
        // getAllRecipes is called twice: in init and in searchRecipes
        verify(repository).getAllRecipes()
    }

    @Test
    fun `toggleFavorite should call repository`() = runTest {
        // Given
        val recipeId = 1L
        val isFavorite = true
        whenever(repository.getAllRecipes()).thenReturn(flowOf(emptyList()))

        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.toggleFavorite(recipeId, isFavorite)

        // Then
        verify(repository).toggleFavorite(recipeId, isFavorite)
    }

    @Test
    fun `initial state should be empty list`() = runTest {
        // Given
        whenever(repository.getAllRecipes()).thenReturn(flowOf(emptyList()))

        // When
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.recipes.value.isEmpty())
    }
}
