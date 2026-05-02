package com.nomnom.data.service

import com.nomnom.data.model.AiExtractedRecipe
import com.nomnom.data.model.Recipe
import com.nomnom.data.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SupabaseService {
    companion object {
        private val SUPABASE_URL = com.nomnom.app.BuildConfig.SUPABASE_URL
        private val ANON_KEY     = com.nomnom.app.BuildConfig.SUPABASE_ANON_KEY
        private val GEMINI_API_KEY = com.nomnom.app.BuildConfig.GEMINI_API_KEY
        private val GEMINI_URL   = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$GEMINI_API_KEY"

        private val httpClient = OkHttpClient()
        private val json = Json { ignoreUnknownKeys = true }

        // Reads the real Supabase error message from the response body
        private fun apiError(prefix: String, code: Int, body: String): Exception {
            val msg = try {
                val err = Json { ignoreUnknownKeys = true }
                    .decodeFromString<SupabaseError>(body)
                err.message ?: err.msg ?: err.error ?: body
            } catch (e: Exception) { body.take(200).ifBlank { "HTTP $code" } }
            return Exception("$prefix: $msg (HTTP $code)")
        }

        suspend fun signup(email: String, password: String): Triple<User, String, String> {
            val url = "$SUPABASE_URL/auth/v1/signup"
            
            val requestBody = SignupRequest(email, password)
            val jsonBody = json.encodeToString(SignupRequest.serializer(), requestBody)
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(url)
                .post(jsonBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .build()
            
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (!response.isSuccessful) {
                // Extract real Supabase error message from JSON body
                val msg = try {
                    val err = json.decodeFromString<SupabaseError>(responseBody)
                    err.msg ?: err.message ?: response.message
                } catch (e: Exception) { response.message }
                throw Exception(msg)
            }
            
            val authResponse = json.decodeFromString(AuthResponse.serializer(), responseBody)
            val token = authResponse.accessToken
                ?: throw Exception("Email confirmation required — please check your inbox")
            val refreshToken = authResponse.refreshToken ?: ""
            
            val user = User(
                id = authResponse.user.id,
                email = authResponse.user.email,
                createdAt = System.currentTimeMillis().toString()
            )
            
            return Triple(user, token, refreshToken)
        }
        
        suspend fun login(email: String, password: String): Triple<User, String, String> {
            val url = "$SUPABASE_URL/auth/v1/token?grant_type=password"
            
            val requestBody = LoginRequest(email, password)
            val jsonBody = json.encodeToString(LoginRequest.serializer(), requestBody)
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(url)
                .post(jsonBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .build()
            
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (!response.isSuccessful) {
                val msg = try {
                    val err = json.decodeFromString<SupabaseError>(responseBody)
                    err.msg ?: err.message ?: response.message
                } catch (e: Exception) { response.message }
                throw Exception(msg)
            }
            val authResponse = json.decodeFromString(AuthResponse.serializer(), responseBody)
            val token = authResponse.accessToken
                ?: throw Exception("Login failed — no token returned")
            val refreshToken = authResponse.refreshToken ?: ""
            
            val user = User(
                id = authResponse.user.id,
                email = authResponse.user.email,
                createdAt = System.currentTimeMillis().toString()
            )
            
            return Triple(user, token, refreshToken)
        }

        suspend fun refreshSession(refreshToken: String): Pair<String, String> {
            val url = "$SUPABASE_URL/auth/v1/token?grant_type=refresh_token"
            
            val requestBody = """{"refresh_token":"$refreshToken"}"""
            val jsonBody = requestBody.toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(url)
                .post(jsonBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .build()
            
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (!response.isSuccessful) throw Exception("Session refresh failed")
            
            val authResponse = json.decodeFromString(AuthResponse.serializer(), responseBody)
            val newToken = authResponse.accessToken ?: throw Exception("No new access token")
            val newRefreshToken = authResponse.refreshToken ?: ""
            
            return Pair(newToken, newRefreshToken)
        }
        
        suspend fun fetchRecipes(userId: String, token: String): List<Recipe> {
            val filter = if (userId.isNotBlank()) "or=(user_id.eq.$userId,is_global.eq.true)" else "is_global.eq.true"
            val url = "$SUPABASE_URL/rest/v1/recipes?$filter&order=created_at.desc"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .build()
            
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) throw apiError("fetchRecipes", response.code, body)
            return json.decodeFromString<List<Recipe>>(body)
        }
        
        suspend fun createRecipe(recipe: RecipeInput, userId: String, token: String): Recipe {
            val url = "$SUPABASE_URL/rest/v1/recipes"
            
            val createRequest = RecipeCreateRequest(
                user_id = userId,
                title = recipe.title,
                ingredients = recipe.ingredients,
                steps = recipe.steps,
                source_url = recipe.sourceUrl,
                source_type = recipe.sourceType,
                prep_time_minutes = recipe.prepTimeMinutes,
                cook_time_minutes = recipe.cookTimeMinutes,
                servings = recipe.servings,
                notes = recipe.notes,
                tags = recipe.tags,
                is_global = recipe.is_global
            )
            
            val jsonBody = json.encodeToString(RecipeCreateRequest.serializer(), createRequest)
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(url)
                .post(jsonBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Prefer", "return=representation")   // ask Supabase to return the created row
                .build()
            
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) throw apiError("createRecipe", response.code, body)
            val recipes = json.decodeFromString<List<Recipe>>(body)
            return recipes.firstOrNull() ?: throw Exception("Recipe not returned after create")
        }
        
        suspend fun fetchRecipe(id: String, token: String): Recipe {
            val url = "$SUPABASE_URL/rest/v1/recipes?id=eq.$id"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .build()
            
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) throw apiError("fetchRecipe", response.code, body)
            val recipes = json.decodeFromString<List<Recipe>>(body)
            return recipes.firstOrNull() ?: throw Exception("Recipe not found")
        }
        
        suspend fun updateRecipe(id: String, recipe: RecipeInput, token: String): Recipe {
            val url = "$SUPABASE_URL/rest/v1/recipes?id=eq.$id"
            
            val updateRequest = RecipeUpdateRequest(
                title = recipe.title,
                ingredients = recipe.ingredients,
                steps = recipe.steps,
                source_url = recipe.sourceUrl,
                source_type = recipe.sourceType,
                prep_time_minutes = recipe.prepTimeMinutes,
                cook_time_minutes = recipe.cookTimeMinutes,
                servings = recipe.servings,
                notes = recipe.notes,
                tags = recipe.tags
            )
            
            val jsonBody = json.encodeToString(RecipeUpdateRequest.serializer(), updateRequest)
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(url)
                .patch(jsonBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .build()
            
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) throw apiError("updateRecipe", response.code, body)
            val recipes = json.decodeFromString<List<Recipe>>(body)
            return recipes.firstOrNull() ?: throw Exception("Recipe not found after update")
        }

        suspend fun updateEmail(email: String, token: String) {
            val url = "$SUPABASE_URL/auth/v1/user"
            val requestBody = """{"email":"$email"}""".toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("apikey", ANON_KEY)
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) throw apiError("updateEmail", response.code, response.body?.string() ?: "")
        }

        suspend fun updatePassword(password: String, token: String) {
            val url = "$SUPABASE_URL/auth/v1/user"
            val requestBody = """{"password":"$password"}""".toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("apikey", ANON_KEY)
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) throw apiError("updatePassword", response.code, response.body?.string() ?: "")
        }

        suspend fun getProfile(userId: String): com.nomnom.data.model.UserProfile? {
            val url = "$SUPABASE_URL/rest/v1/users?id=eq.$userId"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", ANON_KEY)
                .build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) return null
            return json.decodeFromString<List<com.nomnom.data.model.UserProfile>>(body).firstOrNull()
        }

        suspend fun updateProfile(userId: String, username: String, avatarId: String, token: String) {
            val url = "$SUPABASE_URL/rest/v1/users?id=eq.$userId"
            val updateReq = ProfileUpdateRequest(username, avatarId)
            val jsonBody = json.encodeToString(ProfileUpdateRequest.serializer(), updateReq)
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .patch(jsonBody)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("apikey", ANON_KEY)
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) throw apiError("updateProfile", response.code, response.body?.string() ?: "")
        }
        
        suspend fun deleteRecipe(id: String, token: String) {
            val url = "$SUPABASE_URL/rest/v1/recipes?id=eq.$id"
            
            val request = Request.Builder()
                .url(url)
                .delete()
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .build()
            
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val body = response.body?.string() ?: ""
                throw apiError("deleteRecipe", response.code, body)
            }
        }

        // ── Collections ────────────────────────────────────────────────────

        suspend fun fetchCollections(token: String): List<com.nomnom.data.model.RecipeCollection> {
            val url = "$SUPABASE_URL/rest/v1/collections?order=created_at.desc"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Failed to fetch collections: ${response.message}")
            val body = response.body?.string() ?: throw Exception("Empty response")
            return json.decodeFromString(body)
        }

        suspend fun createCollection(
            name: String,
            description: String?,
            userId: String,
            token: String
        ): com.nomnom.data.model.RecipeCollection {
            val url = "$SUPABASE_URL/rest/v1/collections"
            val payload = CollectionCreateRequest(user_id = userId, name = name, description = description)
            val jsonBody = json.encodeToString(CollectionCreateRequest.serializer(), payload)
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(jsonBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Prefer", "return=representation")
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Failed to create collection: ${response.message}")
            val body = response.body?.string() ?: throw Exception("Empty response")
            val list = json.decodeFromString<List<com.nomnom.data.model.RecipeCollection>>(body)
            return list.first()
        }

        suspend fun deleteCollection(id: String, token: String) {
            val url = "$SUPABASE_URL/rest/v1/collections?id=eq.$id"
            val request = Request.Builder()
                .url(url)
                .delete()
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val body = response.body?.string() ?: ""
                throw apiError("deleteCollection", response.code, body)
            }
        }

        suspend fun renameCollection(id: String, newName: String, token: String) {
            val url = "$SUPABASE_URL/rest/v1/collections?id=eq.$id"
            val payload = """{"name":"$newName"}"""
            val body = payload.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val rb = response.body?.string() ?: ""
                throw apiError("renameCollection", response.code, rb)
            }
        }

        /** Fetch all recipe IDs that belong to a collection */
        suspend fun fetchCollectionRecipeIds(collectionId: String, token: String): List<String> {
            val url = "$SUPABASE_URL/rest/v1/recipe_collections?collection_id=eq.$collectionId&select=recipe_id"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) throw apiError("fetchCollectionRecipes", response.code, body)
            return json.decodeFromString<List<CollectionRecipeRow>>(body).map { it.recipe_id }
        }

        /** Add a recipe to a collection (INSERT into recipe_collections) */
        suspend fun addRecipeToCollection(collectionId: String, recipeId: String, token: String) {
            val url      = "$SUPABASE_URL/rest/v1/recipe_collections"
            val payload  = """{"collection_id":"$collectionId","recipe_id":"$recipeId"}"""
            val body     = payload.toRequestBody("application/json".toMediaType())
            val request  = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val rb = response.body?.string() ?: ""
                throw apiError("addRecipeToCollection", response.code, rb)
            }
        }

        /** Remove a recipe from a collection (DELETE from recipe_collections) */
        suspend fun removeRecipeFromCollection(collectionId: String, recipeId: String, token: String) {
            val url     = "$SUPABASE_URL/rest/v1/recipe_collections?collection_id=eq.$collectionId&recipe_id=eq.$recipeId"
            val request = Request.Builder()
                .url(url)
                .delete()
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val rb = response.body?.string() ?: ""
                throw apiError("removeRecipeFromCollection", response.code, rb)
            }
        }

        // ── AI Recipe Extraction (direct Gemini API call) ───────────────────

        suspend fun extractRecipe(input: String, token: String): AiExtractedRecipe {
            val prompt = """Extract a recipe from the text below. Return ONLY valid JSON, no markdown, no explanation:
{"title":"Name","ingredients":["ingredient with quantity"],"steps":["step"],"prepTimeMinutes":10,"cookTimeMinutes":20,"servings":4,"notes":null}
Text: ${input.take(4000)}"""

            val escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
            val reqJson = """{"contents":[{"parts":[{"text":"$escapedPrompt"}]}],"generationConfig":{"temperature":0.1,"maxOutputTokens":2048}}"""
            val body = reqJson.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(GEMINI_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()
            val response = httpClient.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (!response.isSuccessful) throw apiError("extractRecipe", response.code, respBody)
            val geminiResp = json.decodeFromString<GeminiResponse>(respBody)
            val rawText = geminiResp.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty Gemini response")
            val cleanJson = rawText
                .replace(Regex("```json\\n?"), "")
                .replace(Regex("```\\n?"), "")
                .trim()
            return json.decodeFromString<AiExtractedRecipe>(cleanJson)
        }
    }
}

@Serializable
data class SignupRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token")
    val accessToken: String? = null,        // null when email confirmation is required
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    val user: AuthUser
) {
    @Serializable
    data class AuthUser(
        val id: String,
        val email: String
    )
}


@Serializable
data class RecipeInput(
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val sourceUrl: String? = null,
    val sourceType: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int? = null,
    val notes: String? = null,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val is_global: Boolean = false
)

@Serializable
data class RecipeCreateRequest(
    val user_id: String,
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val source_url: String? = null,
    val source_type: String? = null,
    val prep_time_minutes: Int? = null,
    val cook_time_minutes: Int? = null,
    val servings: Int? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val is_global: Boolean = false
)

@Serializable
data class RecipeUpdateRequest(
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val source_url: String? = null,
    val source_type: String? = null,
    val prep_time_minutes: Int? = null,
    val cook_time_minutes: Int? = null,
    val servings: Int? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class CollectionCreateRequest(
    val user_id: String,
    val name: String,
    val description: String? = null
)

@Serializable
data class CollectionRecipeRow(
    val recipe_id: String
)

@Serializable
data class ProfileUpdateRequest(
    val username: String,
    @SerialName("avatar_id")
    val avatarId: String
)

// Parses error bodies returned by Supabase, e.g. {"msg":"User already registered","code":422}
@Serializable
data class SupabaseError(
    val msg: String? = null,
    val message: String? = null,
    val error: String? = null,
    val error_description: String? = null
)

// ── Gemini API response models ───────────────────────────────────────────
@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart> = emptyList()
)

@Serializable
data class GeminiPart(
    val text: String = ""
)
