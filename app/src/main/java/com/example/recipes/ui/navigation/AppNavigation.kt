package com.example.recipes.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipes.ui.screens.*

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem(Screen.Home.route, Icons.Default.Home, "Главная")
    object Catalog : BottomNavItem(Screen.Catalog.route, Icons.Default.MenuBook, "Каталог")
    object AddRecipe : BottomNavItem(Screen.AddRecipe.route, Icons.Default.Add, "Добавить")
    object Profile : BottomNavItem(Screen.Profile.route, Icons.Default.Person, "Профиль")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Catalog,
        BottomNavItem.AddRecipe,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavigationGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onRecipeClick = { recipeId ->
                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
            })
        }

        composable(Screen.Catalog.route) {
            CatalogScreen(onRecipeClick = { recipeId ->
                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
            })
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            RecipeDetailScreen(
                recipeId = recipeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddRecipe.route) {
            AddRecipeScreen(onRecipeAdded = {
                navController.popBackStack()
            }, onAuthRequired = {
                navController.navigate(Screen.Profile.route)
            })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onRecipeClick = { recipeId ->
                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
            })
        }
    }
}
