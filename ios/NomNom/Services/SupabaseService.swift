import Foundation

// MARK: - Supabase Service for API calls
class SupabaseService {
    static let shared = SupabaseService()
    
    // TODO: Replace with your Supabase URL and Anon Key
    private let supabaseURL = "https://unsozonnlyveccnuiigh.supabase.co"
    private let anonKey = "sb_publishable_MChaYMtpuEUOYeQH1eWBxg_OuH91BoB"
    
    private init() {}
    
    // MARK: - Authentication
    
    /// Sign up a new user with email and password
    func signup(email: String, password: String) async throws -> (user: User, token: String) {
        let endpoint = "\(supabaseURL)/auth/v1/signup"
        var request = URLRequest(url: URL(string: endpoint)!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        
        let body = ["email": email, "password": password]
        request.httpBody = try JSONEncoder().encode(body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw NSError(domain: "Auth Error", code: -1, userInfo: [NSLocalizedDescriptionKey: "Signup failed"])
        }
        
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        let authResponse = try decoder.decode(AuthResponse.self, from: data)
        
        let user = User(
            id: authResponse.user.id,
            email: authResponse.user.email,
            createdAt: Date()
        )
        
        return (user, authResponse.session.access_token)
    }
    
    /// Login user with email and password
    func login(email: String, password: String) async throws -> (user: User, token: String) {
        let endpoint = "\(supabaseURL)/auth/v1/token?grant_type=password"
        var request = URLRequest(url: URL(string: endpoint)!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        
        let body = ["email": email, "password": password]
        request.httpBody = try JSONEncoder().encode(body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw NSError(domain: "Auth Error", code: -1, userInfo: [NSLocalizedDescriptionKey: "Login failed"])
        }
        
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        let authResponse = try decoder.decode(AuthResponse.self, from: data)
        
        let user = User(
            id: authResponse.user.id,
            email: authResponse.user.email,
            createdAt: Date()
        )
        
        return (user, authResponse.session.access_token)
    }
    
    // MARK: - Recipe Operations
    
    /// Fetch all recipes for the logged-in user
    func fetchRecipes(token: String) async throws -> [Recipe] {
        let endpoint = "\(supabaseURL)/rest/v1/recipes?order=created_at.desc"
        var request = URLRequest(url: URL(string: endpoint)!)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw NSError(domain: "Recipe Error", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to fetch recipes"])
        }
        
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        let recipes = try decoder.decode([Recipe].self, from: data)
        return recipes
    }
    
    /// Create a new recipe
    func createRecipe(recipe: RecipeInput, userId: String, token: String) async throws -> Recipe {
        let endpoint = "\(supabaseURL)/rest/v1/recipes"
        var request = URLRequest(url: URL(string: endpoint)!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        
        let createRequest = RecipeCreateRequest(
            user_id: userId,
            title: recipe.title,
            ingredients: recipe.ingredients,
            steps: recipe.steps,
            source_url: recipe.sourceUrl,
            source_type: recipe.sourceType,
            prep_time_minutes: recipe.prepTimeMinutes,
            cook_time_minutes: recipe.cookTimeMinutes,
            servings: recipe.servings,
            notes: recipe.notes
        )
        
        request.httpBody = try JSONEncoder().encode(createRequest)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 201 else {
            throw NSError(domain: "Recipe Error", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to create recipe"])
        }
        
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        let createdRecipe = try decoder.decode(Recipe.self, from: data)
        return createdRecipe
    }
    
    /// Update an existing recipe
    func updateRecipe(id: String, recipe: RecipeInput, token: String) async throws -> Recipe {
        let endpoint = "\(supabaseURL)/rest/v1/recipes?id=eq.\(id)"
        var request = URLRequest(url: URL(string: endpoint)!)
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        
        let updateRequest = RecipeUpdateRequest(
            title: recipe.title,
            ingredients: recipe.ingredients,
            steps: recipe.steps,
            source_url: recipe.sourceUrl,
            source_type: recipe.sourceType,
            prep_time_minutes: recipe.prepTimeMinutes,
            cook_time_minutes: recipe.cookTimeMinutes,
            servings: recipe.servings,
            notes: recipe.notes
        )
        
        request.httpBody = try JSONEncoder().encode(updateRequest)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw NSError(domain: "Recipe Error", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to update recipe"])
        }
        
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        let recipes = try decoder.decode([Recipe].self, from: data)
        
        guard let recipe = recipes.first else {
            throw NSError(domain: "Recipe Error", code: -1, userInfo: [NSLocalizedDescriptionKey: "Recipe not found"])
        }
        
        return recipe
    }
    
    /// Delete a recipe
    func deleteRecipe(id: String, token: String) async throws {
        let endpoint = "\(supabaseURL)/rest/v1/recipes?id=eq.\(id)"
        var request = URLRequest(url: URL(string: endpoint)!)
        request.httpMethod = "DELETE"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        
        let (_, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 204 else {
            throw NSError(domain: "Recipe Error", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to delete recipe"])
        }
    }
    
    // MARK: - Helper Methods
    
    /// Make an authenticated API request
    private func authenticatedRequest(to endpoint: String, method: String = "GET", token: String, body: Data? = nil) throws -> URLRequest {
        var request = URLRequest(url: URL(string: "\(supabaseURL)/rest/v1\(endpoint)")!)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.httpBody = body
        return request
    }
}

// MARK: - Response Models
struct AuthResponse: Codable {
    struct User: Codable {
        let id: String
        let email: String
    }
    
    struct Session: Codable {
        let access_token: String
        let refresh_token: String
    }
    
    let user: User
    let session: Session
}

/// Input model for creating/updating recipes (user-facing)
struct RecipeInput {
    var title: String
    var ingredients: [String]
    var steps: [String]
    var sourceUrl: String?
    var sourceType: String?
    var prepTimeMinutes: Int?
    var cookTimeMinutes: Int?
    var servings: Int?
    var notes: String?
}

/// Request model for creating recipe (API)
struct RecipeCreateRequest: Codable {
    let user_id: String
    let title: String
    let ingredients: [String]
    let steps: [String]
    let source_url: String?
    let source_type: String?
    let prep_time_minutes: Int?
    let cook_time_minutes: Int?
    let servings: Int?
    let notes: String?
}

/// Request model for updating recipe (API)
struct RecipeUpdateRequest: Codable {
    let title: String
    let ingredients: [String]
    let steps: [String]
    let source_url: String?
    let source_type: String?
    let prep_time_minutes: Int?
    let cook_time_minutes: Int?
    let servings: Int?
    let notes: String?
}
