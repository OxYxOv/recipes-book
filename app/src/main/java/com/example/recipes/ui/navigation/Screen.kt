package com.example.recipes.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Catalog : Screen("catalog")
    object RecipeDetail : Screen("recipe/{recipeId}") {
        fun createRoute(recipeId: Long) = "recipe/$recipeId"
    }
    object AddRecipe : Screen("add_recipe")
    object Profile : Screen("profile")
}
