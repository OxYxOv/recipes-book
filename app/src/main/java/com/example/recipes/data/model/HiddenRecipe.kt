package com.example.recipes.data.model

import androidx.room.Entity

@Entity(
    tableName = "hidden_recipes",
    primaryKeys = ["userId", "recipeId"]
)
data class HiddenRecipe(
    val userId: String,
    val recipeId: Long
)
