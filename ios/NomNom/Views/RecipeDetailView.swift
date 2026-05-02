import SwiftUI

struct RecipeDetailView: View {
    let recipe: Recipe
    let viewModel: RecipeViewModel
    @State private var isEditing = false
    @Environment(\.dismiss) var dismiss
    @State private var showDeleteAlert = false
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Title
                    Text(recipe.title)
                        .font(.title)
                        .fontWeight(.bold)
                    
                    // Metadata Row
                    HStack(spacing: 20) {
                        if let prepTime = recipe.prepTimeMinutes {
                            VStack(alignment: .center, spacing: 4) {
                                Image(systemName: "timer")
                                    .font(.system(size: 16))
                                    .foregroundColor(.orange)
                                Text("\(prepTime) min")
                                    .font(.caption)
                                Text("Prep")
                                    .font(.caption2)
                                    .foregroundColor(.gray)
                            }
                        }
                        
                        if let cookTime = recipe.cookTimeMinutes {
                            VStack(alignment: .center, spacing: 4) {
                                Image(systemName: "flame")
                                    .font(.system(size: 16))
                                    .foregroundColor(.orange)
                                Text("\(cookTime) min")
                                    .font(.caption)
                                Text("Cook")
                                    .font(.caption2)
                                    .foregroundColor(.gray)
                            }
                        }
                        
                        if let servings = recipe.servings {
                            VStack(alignment: .center, spacing: 4) {
                                Image(systemName: "person.2")
                                    .font(.system(size: 16))
                                    .foregroundColor(.orange)
                                Text("\(servings)")
                                    .font(.caption)
                                Text("Servings")
                                    .font(.caption2)
                                    .foregroundColor(.gray)
                            }
                        }
                        
                        Spacer()
                    }
                    .padding(.vertical, 12)
                    .padding(.horizontal, 16)
                    .background(Color(.systemGray6))
                    .cornerRadius(8)
                    
                    // Ingredients
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Ingredients")
                            .font(.headline)
                        
                        VStack(alignment: .leading, spacing: 8) {
                            ForEach(recipe.ingredients, id: \.self) { ingredient in
                                HStack(spacing: 8) {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(.orange)
                                    Text(ingredient)
                                }
                            }
                        }
                    }
                    
                    Divider()
                    
                    // Steps
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Steps")
                            .font(.headline)
                        
                        VStack(alignment: .leading, spacing: 12) {
                            ForEach(Array(recipe.steps.enumerated()), id: \.offset) { index, step in
                                HStack(alignment: .top, spacing: 12) {
                                    Text("\(index + 1).")
                                        .fontWeight(.bold)
                                        .foregroundColor(.orange)
                                    Text(step)
                                }
                            }
                        }
                    }
                    
                    if let notes = recipe.notes, !notes.isEmpty {
                        Divider()
                        
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Notes")
                                .font(.headline)
                            Text(notes)
                                .foregroundColor(.gray)
                        }
                    }
                    
                    if let sourceUrl = recipe.sourceUrl, !sourceUrl.isEmpty {
                        Divider()
                        
                        Link("View Original Recipe", destination: URL(string: sourceUrl) ?? URL(fileURLWithPath: ""))
                            .foregroundColor(.orange)
                    }
                    
                    Spacer()
                }
                .padding()
            }
            .navigationTitle("Recipe")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button("Edit", action: { isEditing = true })
                        Button("Delete", role: .destructive, action: { showDeleteAlert = true })
                    } label: {
                        Image(systemName: "ellipsis.circle")
                            .foregroundColor(.orange)
                    }
                }
            }
        }
        .sheet(isPresented: $isEditing) {
            EditRecipeView(recipe: recipe, viewModel: viewModel, isPresented: $isEditing)
        }
        .alert("Delete Recipe", isPresented: $showDeleteAlert) {
            Button("Delete", role: .destructive) {
                Task {
                    await viewModel.deleteRecipe(id: recipe.id)
                    dismiss()
                }
            }
            Button("Cancel", role: .cancel) { }
        } message: {
            Text("Are you sure you want to delete this recipe?")
        }
    }
}

#Preview {
    let sampleRecipe = Recipe(
        id: "1",
        userId: "user1",
        title: "Pasta Carbonara",
        ingredients: ["Spaghetti", "Eggs", "Bacon"],
        steps: ["Cook pasta", "Fry bacon", "Mix everything"],
        sourceUrl: nil,
        sourceType: nil,
        prepTimeMinutes: 15,
        cookTimeMinutes: 20,
        servings: 4,
        notes: "Classic Italian recipe",
        tags: [],
        createdAt: Date(),
        updatedAt: Date()
    )
    
    RecipeDetailView(recipe: sampleRecipe, viewModel: RecipeViewModel())
}
