# Getting Started with NomNom

Welcome to **NomNom**, your smart, AI-powered recipe companion. This guide will help you set up the project and get it running on your machine.

---

## 🚀 Prerequisites

Before you begin, ensure you have the following installed:

*   **Android Studio** (Ladybug or newer)
*   **Android SDK API 34+**
*   **Java JDK 17+**
*   **Supabase Account** (for backend & auth)
*   **Google Gemini API Key** (for AI recipe ingestion)

---

## 🛠 Step 1: Backend Setup (Supabase)

1.  **Create a Project**: Go to [Supabase](https://supabase.com) and create a new project named `nomnom`.
2.  **Run Schema**: In the Supabase SQL Editor, run the contents of `backend/supabase_schema.sql` to create the necessary tables and RLS policies.
3.  **Get Credentials**: Go to Project Settings -> API and copy your **Project URL** and **Anon Key**.

---

## 🔑 Step 2: Environment Configuration

In the Android project, you need to provide your API keys.

1.  Open `android-app/app/build.gradle.kts` (or a `BuildConfig` if configured).
2.  Alternatively, locate `SupabaseService.kt` and `RecipeViewModel.kt` to ensure the URLs and Keys are correctly referenced.
3.  Ensure your **Gemini API Key** is added to your local environment or the appropriate configuration file.

---

## 📱 Step 3: Run the App

1.  **Open Project**: Open the `android-app` folder in Android Studio.
2.  **Sync Gradle**: Wait for the Gradle sync to complete.
3.  **Select Device**: Choose an emulator (API 34 recommended) or a physical device.
4.  **Launch**: Click the green **Run** button.

---

## 📂 Project Structure

*   **`android-app/`**: The full source code for the Android application.
    *   `app/src/main/java/com/nomnom/ui/`: All UI screens and components (Jetpack Compose).
    *   `app/src/main/java/com/nomnom/data/`: ViewModels, Models, and Services (Supabase, AI).
*   **`docs/`**: Documentation, architecture details, and build history.
*   **`backend/`**: Database schemas and server-side configurations.

---

## 🧪 Testing

Refer to the [TESTING_GUIDE.md](TESTING_GUIDE.md) for a comprehensive checklist of features to verify.

---

## 🎨 Design System

NomNom uses the **Culinary Serenity** design system:
*   **Primary Color**: Sage Green (`#4A6549`)
*   **Background**: Warm Linen (`#FAF9F6`)
*   **Typography**: Noto Serif (Titles) & Plus Jakarta Sans (Body)

---

Need help? Check the [ARCHITECTURE.md](ARCHITECTURE.md) for deeper technical insights.
