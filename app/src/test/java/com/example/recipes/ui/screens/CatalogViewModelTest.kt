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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: RecipeRepository

    private lateinit var viewModel: CatalogViewModel
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
    fun `init should load all recipes`() = runTest {
        // Given
        val recipes = listOf(testRecipe)
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(recipes))

        // When
        viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        // Then
        assertEquals(recipes, viewModel.recipes.value)
        verify(repository).getAllRecipes(null)
    }

    @Test
    fun `init should start with empty list`() = runTest {
        // Given
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(emptyList()))

        // When
        viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.recipes.value.isEmpty())
        verify(repository).getAllRecipes(null)
    }

    @Test
    fun `toggleFavorite should call repository`() = runTest {
        // Given
        val recipeId = 1L
        val isFavorite = true
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(emptyList()))

        viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.toggleFavorite(userId, recipeId, isFavorite)

        // Then
        verify(repository).toggleFavorite(userId, recipeId, isFavorite)
    }

    @Test
    fun `recipes should update when repository emits new data`() = runTest {
        // Given
        val recipes1 = listOf(testRecipe)
        val recipes2 = listOf(testRecipe, testRecipe.copy(id = 2, name = "Recipe 2"))
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(recipes1))

        viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        // First emission
        assertEquals(recipes1, viewModel.recipes.value)

        // Update mock for second emission
        whenever(repository.getAllRecipes(any())).thenReturn(flowOf(recipes2))
        
        // Create new view model to trigger reload
        viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        // Then
        assertEquals(recipes2, viewModel.recipes.value)
    }
}
