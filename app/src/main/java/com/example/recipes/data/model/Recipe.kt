package com.example.recipes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val ingredients: String,
    val instructions: String,
    val imageUrl: String? = null,
    val category: String,
    val cookingTime: Int,
    val difficulty: String,
    val servings: Int = 1,
    val isFavorite: Boolean = false,
    val isLocal: Boolean = true,
    val ownerId: String? = null,
    @SerializedName("remote_id")
    val remoteId: String? = null
)
