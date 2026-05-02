import SwiftUI

struct RecipeListView: View {
    @StateObject private var viewModel = RecipeViewModel()
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var showingAddRecipe = false
    @State private var searchText = ""
    
    var filteredRecipes: [Recipe] {
        if searchText.isEmpty {
            return viewModel.recipes
        }
        return viewModel.recipes.filter { recipe in
            recipe.title.localizedCaseInsensitiveContains(searchText)
        }
    }
    
    var body: some View {
        NavigationStack {
            ZStack {
                if viewModel.isLoading && viewModel.recipes.isEmpty {
                    ProgressView()
                } else if viewModel.recipes.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "book.closed")
                            .font(.system(size: 60))
                            .foregroundColor(.gray)
                        Text("No Recipes Yet")
                            .font(.headline)
                        Text("Start by adding your first recipe")
                            .foregroundColor(.gray)
                    }
                } else {
                    List {
                        ForEach(filteredRecipes) { recipe in
                            NavigationLink(destination: RecipeDetailView(recipe: recipe, viewModel: viewModel)) {
                                VStack(alignment: .leading, spacing: 8) {
                                    Text(recipe.title)
                                        .font(.headline)
                                    
                                    HStack(spacing: 16) {
                                        if let prepTime = recipe.prepTimeMinutes {
                                            Label("\(prepTime) min", systemImage: "timer")
                                                .font(.caption)
                                                .foregroundColor(.gray)
                                        }
                                        
                                        if let cookTime = recipe.cookTimeMinutes {
                                            Label("\(cookTime) min", systemImage: "flame")
                                                .font(.caption)
                                                .foregroundColor(.gray)
                                        }
                                        
                                        if let servings = recipe.servings {
                                            Label("\(servings) servings", systemImage: "person.2")
                                                .font(.caption)
                                                .foregroundColor(.gray)
                                        }
                                    }
                                }
                                .padding(.vertical, 8)
                            }
                        }
                    }
                }
            }
            .searchable(text: $searchText, prompt: "Search recipes")
            .navigationTitle("My Recipes")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { showingAddRecipe = true }) {
                        Image(systemName: "plus.circle.fill")
                            .font(.system(size: 20))
                            .foregroundColor(.orange)
                    }
                }
            }
            .sheet(isPresented: $showingAddRecipe) {
                AddRecipeView(viewModel: viewModel, isPresented: $showingAddRecipe)
            }
            .task {
                viewModel.authToken = authViewModel.authToken
                viewModel.userId = authViewModel.currentUser?.id ?? ""
                await viewModel.fetchRecipes()
            }
            .alert("Error", isPresented: .constant(viewModel.errorMessage != nil)) {
                Button("OK") { viewModel.errorMessage = nil }
            } message: {
                Text(viewModel.errorMessage ?? "")
            }
        }
    }
}

#Preview {
    RecipeListView()
        .environmentObject(AuthViewModel())
}
