package com.nomnom.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class User(
    val id: String,
    val email: String,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class UserProfile(
    val id: String,
    val username: String? = null,
    @SerialName("avatar_id")
    val avatarId: String? = null
)

@Serializable
data class Recipe(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("source_url")
    val sourceUrl: String? = null,
    @SerialName("source_type")
    val sourceType: String? = null,
    @SerialName("prep_time_minutes")
    val prepTimeMinutes: Int? = null,
    @SerialName("cook_time_minutes")
    val cookTimeMinutes: Int? = null,
    val servings: Int? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("is_global")
    val isGlobal: Boolean = false
)

@Serializable
data class RecipeCollection(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val description: String? = null,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class RecipeTag(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val color: String? = null,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class AiExtractedRecipe(
    val title: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int? = null,
    val notes: String? = null
)
