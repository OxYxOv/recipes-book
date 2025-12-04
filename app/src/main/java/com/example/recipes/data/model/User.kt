package com.example.recipes.data.model

data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val token: String? = null
)
