package com.example.recipes.data.remote

import com.example.recipes.data.model.Recipe
import retrofit2.http.*

interface RecipeApiService {
    @GET("recipes")
    suspend fun getRecipes(): List<Recipe>

    @GET("recipes/{id}")
    suspend fun getRecipeById(@Path("id") id: String): Recipe

    @GET("recipes/category/{category}")
    suspend fun getRecipesByCategory(@Path("category") category: String): List<Recipe>

    @POST("recipes")
    suspend fun createRecipe(@Body recipe: Recipe): Recipe

    @PUT("recipes/{id}")
    suspend fun updateRecipe(@Path("id") id: String, @Body recipe: Recipe): Recipe

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: String)
}
