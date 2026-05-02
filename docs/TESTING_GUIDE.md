# NomNom — Comprehensive Testing Guide

Use this checklist to verify that all features of the NomNom app are functioning correctly.

---

## 🔐 Authentication
- [ ] **Login**: Can sign in with a valid email/password.
- [ ] **Signup**: Can create a new account with matching passwords (min 6 chars).
- [ ] **Validation**: Login/Signup buttons are disabled until the form is valid.
- [ ] **Persistence**: App remembers the user after closing and reopening.
- [ ] **Logout**: Can successfully sign out from the Profile screen.

## 🥘 Recipe Management
- [ ] **Add Recipe**: Can manually create a recipe with title, ingredients, and steps.
- [ ] **Edit Recipe**: Can modify an existing recipe and save changes.
- [ ] **Delete Recipe**: Can delete a recipe (verify it disappears from the list).
- [ ] **Images**: Recipe images load correctly in the list and details view.
- [ ] **Tags**: Can add/remove tags (Quick, Healthy, etc.) from a recipe.

## 🪄 AI Ingestion (Gemini)
- [ ] **Paste & Extract**: Pasting a messy recipe text correctly extracts title, ingredients, and steps.
- [ ] **Auto-Fill**: Extracted data correctly populates the "Add Recipe" multi-step form.
- [ ] **Error Handling**: Friendly error message appears if the AI fails to parse the text.

## 📁 Collections & Organization
- [ ] **Create Collection**: Can create a new collection (e.g., "Breakfast").
- [ ] **Add to Collection**: Can add recipes to a specific collection.
- [ ] **Filtering**: Selecting a tag or collection correctly filters the main recipe list.

## 🛒 Shopping & Suggestions
- [ ] **Shopping List**: Selecting recipes and clicking "Shop" generates an aggregated list of ingredients.
- [ ] **Ingredient Checking**: Can check/uncheck items in the shopping list (with strikethrough effect).
- [ ] **Smart Suggestions**: The "Suggest" tab provides recipe ideas based on your library.

## 📊 Profile & Premium
- [ ] **Statistics**: The Profile tab shows accurate charts for recipe categories.
- [ ] **Pro Identity**: Premium "PRO" badges appear on recipe cards (if enabled).
- [ ] **Haptic Feedback**: Subtle vibrations occur when saving or deleting.

## 🌐 Stability & Offline
- [ ] **Splash Screen**: Branded splash screen appears on startup.
- [ ] **Offline Mode**: An "Offline" banner appears when the internet is disconnected.
- [ ] **Sharing**: The "Share" button generates a beautifully formatted recipe text for external apps.

---

## ✅ Final Build Verification
- [ ] **Android Studio**: Run `./gradlew :app:compileDebugKotlin` — Should result in `BUILD SUCCESSFUL`.
- [ ] **Logs**: Check `adb logcat` for any `FATAL EXCEPTION` during app navigation.
