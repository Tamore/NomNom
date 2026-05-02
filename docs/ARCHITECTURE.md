# NomNom — Architecture

A beginner-friendly explanation of how the app is structured,
why decisions were made, and how data flows through the system.

---

## Big Picture

```
┌────────────────────────────────────────────────┐
│         Android App (Jetpack Compose)          │
│  Screens → ViewModels → SupabaseService        │
└────────────────────────┬───────────────────────┘
                         │ HTTPS REST / Gemini API
         ┌───────────────┴────────────────┐
         │                                │
┌────────▼────────┐             ┌─────────▼────────┐
│  Supabase       │             │  Google Gemini   │
│  (Auth + DB)    │             │  (AI extraction) │
│  PostgreSQL     │             │  gemini-2.0-flash│
│  RLS policies   │             └──────────────────┘
└─────────────────┘
```

---

## Android Folder Structure

```
android-app/app/src/main/
├── AndroidManifest.xml                   ← declares INTERNET permission
└── java/com/nomnom/
    ├── app/
    │   └── MainActivity.kt              ← app entry point, root NavHost
    ├── data/
    │   ├── model/
    │   │   └── Models.kt                ← all data classes (Recipe, User,
    │   │                                   Collection, AiExtractedRecipe, ...)
    │   ├── service/
    │   │   └── SupabaseService.kt       ← ALL HTTP calls live here
    │   │                                   (Supabase REST + Gemini API)
    │   └── viewmodel/
    │       ├── AuthViewModel.kt         ← login, signup, logout, session
    │       ├── RecipeViewModel.kt       ← fetch/create/update/delete recipes
    │       ├── CollectionViewModel.kt   ← fetch/create/delete collections
    │       └── AiImportViewModel.kt     ← AI extraction state
    └── ui/
        └── screens/
            ├── auth/
            │   ├── LoginScreen.kt
            │   └── SignupScreen.kt
            ├── home/
            │   └── HomeScreen.kt        ← bottom tab bar + inner NavHost
            ├── recipe/
            │   ├── RecipeListScreen.kt  ← list of all recipes + AI import banner
            │   ├── RecipeDetailScreen.kt
            │   ├── AddRecipeScreen.kt   ← supports AI prefill
            │   └── EditRecipeScreen.kt
            ├── collections/
            │   └── CollectionsScreen.kt
            ├── suggest/
            │   └── SuggestScreen.kt    ← "What Should I Eat?"
            └── ai/
                └── AiImportScreen.kt   ← paste URL/text → Gemini extraction
```

---

## MVVM Pattern (Used Everywhere)

```
Screen (UI)
  │  user taps button
  ▼
ViewModel
  │  calls suspend function on Dispatchers.IO
  ▼
SupabaseService
  │  OkHttp HTTP request
  ▼
Supabase / Gemini API
  │  JSON response
  ▼
ViewModel (updates StateFlow)
  │
  ▼
Screen re-renders automatically (Compose observes StateFlow)
```

**Why MVVM?**
- Screens don't know about HTTP — easy to test
- ViewModels survive screen rotations
- StateFlow is reactive — UI updates automatically when data changes
- One ViewModel can be shared across multiple screens (e.g. RecipeViewModel used in list AND detail)

---

## Navigation Structure

The app has **two NavHosts**:

```
MainActivity (Root NavHost)
  ├── "login"   → LoginScreen
  ├── "signup"  → SignupScreen
  └── "home"    → HomeScreen
                    │
                    └── HomeScreen (Inner NavHost + Bottom Tab Bar)
                          ├── "recipe_list"           → RecipeListScreen
                          ├── "recipe_detail/{id}"    → RecipeDetailScreen
                          ├── "add_recipe"            → AddRecipeScreen
                          ├── "edit_recipe/{id}"      → EditRecipeScreen
                          ├── "ai_import"             → AiImportScreen
                          ├── "collections"           → CollectionsScreen
                          └── "suggest"               → SuggestScreen
```

**Why two NavHosts?**
- The root one handles the auth flow (login → home)
- The inner one handles navigation *within* the app
- The bottom tab bar only shows inside HomeScreen — not on login/signup
- `LaunchedEffect(isLoggedIn)` in MainActivity reacts to logout from anywhere

---

## Database Schema (Supabase PostgreSQL)

```sql
users
  id         UUID (PK, from Supabase Auth)
  email      VARCHAR UNIQUE
  created_at TIMESTAMP

recipes
  id                UUID (PK)
  user_id           UUID  ← links to auth.users (no FK constraint, RLS enforces)
  title             VARCHAR
  ingredients       TEXT[]
  steps             TEXT[]
  source_url        VARCHAR nullable
  source_type       VARCHAR nullable
  prep_time_minutes INT nullable
  cook_time_minutes INT nullable
  servings          INT nullable
  notes             TEXT nullable
  created_at        TIMESTAMP
  updated_at        TIMESTAMP

collections
  id          UUID (PK)
  user_id     UUID
  name        VARCHAR
  description TEXT nullable
  created_at  TIMESTAMP

recipe_collections  ← junction table (not yet used in UI)
  recipe_id     UUID FK → recipes.id
  collection_id UUID FK → collections.id
  PRIMARY KEY (recipe_id, collection_id)
```

**Row Level Security (RLS)**
Each table has a policy: `user_id = auth.uid()`
This means users automatically only see/edit/delete their own data.
No extra backend code needed.

---

## Data Flow Examples

### Login Flow
```
LoginScreen
  → user enters email + password → taps Login
  → AuthViewModel.login(email, password)
  → SupabaseService.login() → POST /auth/v1/token
  → Supabase returns { access_token, user.id }
  → AuthViewModel saves token + userId to DataStore
  → isLoggedIn StateFlow emits true
  → MainActivity LaunchedEffect navigates to "home"
```

### Create Recipe Flow
```
AddRecipeScreen
  → user fills form → taps Save
  → RecipeViewModel.createRecipe(title, ingredients, ...)
  → SupabaseService.createRecipe() → POST /rest/v1/recipes
  → Supabase inserts row (RLS checks user_id = auth.uid())
  → RecipeViewModel.fetchRecipes() refreshes list
  → recipes StateFlow emits updated list
  → RecipeListScreen re-renders with new recipe
```

### AI Import Flow
```
AiImportScreen
  → user pastes URL or text → taps Extract
  → AiImportViewModel.extractRecipe(input)
  → SupabaseService.extractRecipe()
      → builds Gemini prompt with strict JSON schema
      → POST to Gemini API (gemini-2.0-flash)
      → strips ```json fences from response
      → decodes JSON into AiExtractedRecipe
  → AiImportScreen shows preview card
  → user taps "Use This Recipe"
  → HomeScreen sets aiPrefill state
  → navigates to AddRecipeScreen(prefill = aiExtractedRecipe)
  → form auto-fills all fields
  → user taps Save → normal create recipe flow
```

---

## Key Technical Decisions

| Decision | Why |
|---|---|
| OkHttp + kotlinx.serialization | Lightweight, no extra SDK dependencies |
| No Supabase Kotlin SDK | Gives full control over requests, easier to debug |
| DataStore (not SharedPreferences) | Modern, async, type-safe |
| Direct Gemini API call (not Edge Function) | Simpler, no JWT issues, easier quota debugging |
| Gemini prompt with strict JSON schema | Dramatically reduces hallucinated formats |
| FK constraint removed, RLS only | Avoids constraint violations on insert while keeping security |
| ViewModels at HomeScreen level | Survive tab switches; single source of truth for data |

---

## Security Notes

| Area | Current State | Production Fix |
|---|---|---|
| Gemini API key | Hardcoded in `SupabaseService.kt` | Move to Supabase Edge Function |
| Auth token storage | Android DataStore (plaintext) | Upgrade to Android Keystore |
| Token refresh | Not implemented — expires after 1h | Add refresh token flow |
| Supabase Anon key | In code (acceptable for mobile) | Stays in code — safe with RLS |
