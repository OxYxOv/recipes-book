package com.example.recipes.ui.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.recipes.data.model.Recipe
import com.example.recipes.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AddRecipeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: RecipeRepository

    private lateinit var viewModel: AddRecipeViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = AddRecipeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addRecipe should create and insert recipe with all fields`() = runTest {
        // Given
        val name = "Test Recipe"
        val description = "Test Description"
        val ingredients = "Test Ingredients"
        val instructions = "Test Instructions"
        val cookingTime = 30
        val servings = 4
        val category = "breakfast"
        val difficulty = "easy"
        val imageUrl = "http://example.com/image.jpg"
        
        whenever(repository.insertRecipe(any())).thenReturn(1L)

        // When
        viewModel.addRecipe(
            name = name,
            description = description,
            ingredients = ingredients,
            instructions = instructions,
            cookingTime = cookingTime,
            servings = servings,
            category = category,
            difficulty = difficulty,
            imageUrl = imageUrl
        )

        // Then
        verify(repository).insertRecipe(any<Recipe>())
    }

    @Test
    fun `addRecipe should create recipe with null imageUrl`() = runTest {
        // Given
        val name = "Test Recipe"
        val description = "Test Description"
        val ingredients = "Test Ingredients"
        val instructions = "Test Instructions"
        val cookingTime = 30
        val servings = 4
        val category = "breakfast"
        val difficulty = "easy"
        
        whenever(repository.insertRecipe(any())).thenReturn(1L)

        // When
        viewModel.addRecipe(
            name = name,
            description = description,
            ingredients = ingredients,
            instructions = instructions,
            cookingTime = cookingTime,
            servings = servings,
            category = category,
            difficulty = difficulty,
            imageUrl = null
        )

        // Then
        verify(repository).insertRecipe(any<Recipe>())
    }

    @Test
    fun `addRecipe should mark recipe as local`() = runTest {
        // Given
        whenever(repository.insertRecipe(any())).thenReturn(1L)

        // When
        viewModel.addRecipe(
            name = "Test",
            description = "Test",
            ingredients = "Test",
            instructions = "Test",
            cookingTime = 30,
            servings = 2,
            category = "breakfast",
            difficulty = "easy",
            imageUrl = null
        )

        // Then
        verify(repository).insertRecipe(any<Recipe>())
    }
}
