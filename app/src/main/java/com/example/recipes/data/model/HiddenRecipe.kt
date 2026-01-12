package com.example.recipes.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "hidden_recipes",
    primaryKeys = ["userId", "recipeId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId"), Index("userId")]
)
data class HiddenRecipe(
    val userId: String,
    val recipeId: Long
)
