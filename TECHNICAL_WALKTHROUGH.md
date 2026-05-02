# Technical Walkthrough: NomNom Architecture

This document provides an in-depth look at the technical decisions and engineering patterns implemented in NomNom. It is intended for technical reviewers, recruiters, and developers.

---

## 🧠 AI-Powered Recipe Ingestion
The core innovation of NomNom is its ability to turn "messy" data into structured culinary assets.

### The Problem
Cooking blogs are often cluttered with advertisements, personal anecdotes, and inconsistent formatting, making it hard to copy-paste ingredients or steps into a standard app.

### The Solution: Gemini 2.0 Integration
NomNom utilizes the **Google Gemini 2.0 Flash API** via a specialized service (`SupabaseService.kt`). 
*   **Prompt Engineering**: We use a detailed system prompt that instructs Gemini to act as a "Culinary Data Engineer."
*   **JSON Enforcement**: The prompt strictly enforces a JSON schema, which the app then parses into a strongly-typed `AiExtractedRecipe` Kotlin object.
*   **Error Handling**: If the AI output is malformed, the app provides a graceful fallback, allowing the user to manually edit the pre-filled form.

---

## 🏗 Modular Architecture
The app follows modern Android development best practices:

*   **Jetpack Compose**: The entire UI is built with a declarative, state-driven approach.
*   **MVVM (Model-View-ViewModel)**:
    *   **ViewModels**: Manage state and UI logic (e.g., `AuthViewModel`, `RecipeViewModel`).
    *   **Services**: Handle networking and external API calls (`SupabaseService`).
    *   **Repositories (Contextual)**: Data fetching logic is encapsulated to allow for future caching or offline-first synchronization.
*   **Unidirectional Data Flow (UDF)**: State flows down from ViewModels to Composables, and events flow up.

---

## 🔐 Secure & Scalable Backend
We chose **Supabase** for its robust PostgreSQL foundation and built-in security features.

*   **Auth & Profiles**: Uses Supabase Auth for secure login. Custom user profiles (usernames, avatars) are stored in a `public.users` table, synced with the auth metadata.
*   **Row Level Security (RLS)**: Recipes are protected at the database level. Users can only edit or delete their own recipes, while "Admin" recipes are globally viewable.
*   **RESTful Sync**: The app uses `OkHttp` and `Kotlin Serialization` to communicate with Supabase's PostgREST interface.

---

## 🎨 Advanced UI/UX Patterns
NomNom is designed to feel premium and "alive."

*   **Glassmorphism**: The "Collections" screen uses custom backgrounds and blurred surfaces to create depth.
*   **Haptic Feedback**: Meaningful haptics are integrated into primary actions (like rolling the "Suggest" dice or adding a recipe) to provide physical confirmation.
*   **Motion Layouts**: Seamless transitions between profile modes (Main -> Personal Info) using `AnimatedContent`.
*   **Custom Design Tokens**: A curated palette of HSL-derived colors (`CSSage`, `CSLinen`, `CSTerracotta`) ensures a calming, professional aesthetic.

---

## 📱 Performance & Connectivity
*   **Image Handling**: Uses **Coil** for asynchronous image loading and memory-efficient caching.
*   **Network Awareness**: The app monitors the device's connectivity state, providing visual cues when offline and preventing actions that require a server sync.
*   **Session Longevity**: Implemented proactive token refreshing to ensure the user stays logged in without frustrating re-authentications.
