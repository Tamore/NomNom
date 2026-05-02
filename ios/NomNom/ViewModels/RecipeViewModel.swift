import Foundation
import SwiftUI

@MainActor
class RecipeViewModel: ObservableObject {
    @Published var recipes: [Recipe] = []
    @Published var selectedRecipe: Recipe?
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private let supabaseService = SupabaseService.shared
    var authToken: String = ""
    var userId: String = ""
    
    // MARK: - Fetch Recipes
    
    func fetchRecipes() async {
        isLoading = true
        errorMessage = nil
        
        do {
            recipes = try await supabaseService.fetchRecipes(token: authToken)
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch recipes: \(error.localizedDescription)"
            isLoading = false
        }
    }
    
    func fetchRecipe(id: String) async {
        isLoading = true
        errorMessage = nil
        
        do {
            selectedRecipe = try await supabaseService.fetchRecipe(id: id, token: authToken)
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch recipe: \(error.localizedDescription)"
            isLoading = false
        }
    }
    
    // MARK: - Create Recipe
    
    func createRecipe(title: String, ingredients: [String], steps: [String],
                      sourceUrl: String? = nil, sourceType: String? = nil,
                      prepTimeMinutes: Int? = nil, cookTimeMinutes: Int? = nil,
                      servings: Int? = nil, notes: String? = nil) async {
        isLoading = true
        errorMessage = nil
        
        let input = RecipeInput(
            title: title,
            ingredients: ingredients,
            steps: steps,
            sourceUrl: sourceUrl,
            sourceType: sourceType,
            prepTimeMinutes: prepTimeMinutes,
            cookTimeMinutes: cookTimeMinutes,
            servings: servings,
            notes: notes
        )
        
        do {
            let newRecipe = try await supabaseService.createRecipe(recipe: input, userId: userId, token: authToken)
            recipes.insert(newRecipe, at: 0) // Add to top of list
            isLoading = false
        } catch {
            errorMessage = "Failed to create recipe: \(error.localizedDescription)"
            isLoading = false
        }
    }
    
    // MARK: - Update Recipe
    
    func updateRecipe(id: String, title: String, ingredients: [String], steps: [String],
                      sourceUrl: String? = nil, sourceType: String? = nil,
                      prepTimeMinutes: Int? = nil, cookTimeMinutes: Int? = nil,
                      servings: Int? = nil, notes: String? = nil) async {
        isLoading = true
        errorMessage = nil
        
        let input = RecipeInput(
            title: title,
            ingredients: ingredients,
            steps: steps,
            sourceUrl: sourceUrl,
            sourceType: sourceType,
            prepTimeMinutes: prepTimeMinutes,
            cookTimeMinutes: cookTimeMinutes,
            servings: servings,
            notes: notes
        )
        
        do {
            let updatedRecipe = try await supabaseService.updateRecipe(id: id, recipe: input, token: authToken)
            
            // Update in local list
            if let index = recipes.firstIndex(where: { $0.id == id }) {
                recipes[index] = updatedRecipe
            }
            
            selectedRecipe = updatedRecipe
            isLoading = false
        } catch {
            errorMessage = "Failed to update recipe: \(error.localizedDescription)"
            isLoading = false
        }
    }
    
    // MARK: - Delete Recipe
    
    func deleteRecipe(id: String) async {
        isLoading = true
        errorMessage = nil
        
        do {
            try await supabaseService.deleteRecipe(id: id, token: authToken)
            
            // Remove from local list
            recipes.removeAll { $0.id == id }
            selectedRecipe = nil
            isLoading = false
        } catch {
            errorMessage = "Failed to delete recipe: \(error.localizedDescription)"
            isLoading = false
        }
    }
}
