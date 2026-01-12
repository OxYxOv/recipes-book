package com.example.recipes.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "favorite_recipes",
    primaryKeys = ["userId", "recipeId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FavoriteRecipe(
    val userId: String,
    val recipeId: Long
)
