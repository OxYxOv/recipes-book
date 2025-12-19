package com.example.recipes.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.recipes.data.model.Recipe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class RecipeDaoTest {

    private lateinit var recipeDao: RecipeDao
    private lateinit var database: RecipeDatabase

    private val testRecipe1 = Recipe(
        id = 1,
        name = "Pasta",
        description = "Italian pasta dish",
        ingredients = "Pasta, tomato, basil",
        instructions = "Cook pasta, add sauce",
        category = "lunch",
        cookingTime = 30,
        difficulty = "easy",
        servings = 2,
        isFavorite = false
    )

    private val testRecipe2 = Recipe(
        id = 2,
        name = "Pizza",
        description = "Italian pizza",
        ingredients = "Dough, cheese, tomato",
        instructions = "Make dough, add toppings, bake",
        category = "dinner",
        cookingTime = 45,
        difficulty = "medium",
        servings = 4,
        isFavorite = true
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            RecipeDatabase::class.java
        ).allowMainThreadQueries().build()
        recipeDao = database.recipeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetRecipe() = runTest {
        // When
        val id = recipeDao.insertRecipe(testRecipe1)
        val recipe = recipeDao.getRecipeById(id)

        // Then
        assertNotNull(recipe)
        assertEquals(testRecipe1.name, recipe?.name)
        assertEquals(testRecipe1.description, recipe?.description)
    }

    @Test
    @Throws(Exception::class)
    fun getAllRecipes() = runTest {
        // Given
        recipeDao.insertRecipe(testRecipe1)
        recipeDao.insertRecipe(testRecipe2)

        // When
        val recipes = recipeDao.getAllRecipes().first()

        // Then
        assertEquals(2, recipes.size)
        assertTrue(recipes.any { it.name == testRecipe1.name })
        assertTrue(recipes.any { it.name == testRecipe2.name })
    }

    @Test
    @Throws(Exception::class)
    fun getRecipesByCategory() = runTest {
        // Given
        recipeDao.insertRecipe(testRecipe1)
        recipeDao.insertRecipe(testRecipe2)

        // When
        val lunchRecipes = recipeDao.getRecipesByCategory("lunch").first()

        // Then
        assertEquals(1, lunchRecipes.size)
        assertEquals("Pasta", lunchRecipes.first().name)
        assertEquals("lunch", lunchRecipes.first().category)
    }

    @Test
    @Throws(Exception::class)
    fun getFavoriteRecipes() = runTest {
        // Given
        recipeDao.insertRecipe(testRecipe1)
        recipeDao.insertRecipe(testRecipe2)

        // When
        val favorites = recipeDao.getFavoriteRecipes().first()

        // Then
        assertEquals(1, favorites.size)
        assertEquals("Pizza", favorites.first().name)
        assertTrue(favorites.first().isFavorite)
    }

    @Test
    @Throws(Exception::class)
    fun searchRecipes() = runTest {
        // Given
        recipeDao.insertRecipe(testRecipe1)
        recipeDao.insertRecipe(testRecipe2)

        // When
        val results = recipeDao.searchRecipes("pasta").first()

        // Then
        assertEquals(1, results.size)
        assertEquals("Pasta", results.first().name)
    }

    @Test
    @Throws(Exception::class)
    fun updateRecipe() = runTest {
        // Given
        val id = recipeDao.insertRecipe(testRecipe1)
        val updatedRecipe = testRecipe1.copy(id = id, name = "Updated Pasta")

        // When
        recipeDao.updateRecipe(updatedRecipe)
        val recipe = recipeDao.getRecipeById(id)

        // Then
        assertEquals("Updated Pasta", recipe?.name)
    }

    @Test
    @Throws(Exception::class)
    fun deleteRecipe() = runTest {
        // Given
        val id = recipeDao.insertRecipe(testRecipe1)

        // When
        recipeDao.deleteRecipe(testRecipe1.copy(id = id))
        val recipe = recipeDao.getRecipeById(id)

        // Then
        assertNull(recipe)
    }

    @Test
    @Throws(Exception::class)
    fun updateFavoriteStatus() = runTest {
        // Given
        val id = recipeDao.insertRecipe(testRecipe1)
        assertFalse(recipeDao.getRecipeById(id)?.isFavorite ?: true)

        // When
        recipeDao.updateFavoriteStatus(id, true)
        val recipe = recipeDao.getRecipeById(id)

        // Then
        assertTrue(recipe?.isFavorite ?: false)
    }

    @Test
    @Throws(Exception::class)
    fun searchRecipesByDescription() = runTest {
        // Given
        recipeDao.insertRecipe(testRecipe1)
        recipeDao.insertRecipe(testRecipe2)

        // When - search by description
        val results = recipeDao.searchRecipes("Italian").first()

        // Then
        assertEquals(2, results.size)
    }

    @Test
    @Throws(Exception::class)
    fun insertReplace() = runTest {
        // Given
        val id = recipeDao.insertRecipe(testRecipe1)
        val replacementRecipe = testRecipe1.copy(id = id, name = "Replaced Recipe")

        // When - insert with same ID (should replace)
        recipeDao.insertRecipe(replacementRecipe)
        val recipe = recipeDao.getRecipeById(id)

        // Then
        assertEquals("Replaced Recipe", recipe?.name)
        assertEquals(1, recipeDao.getAllRecipes().first().size)
    }
}
