import SwiftUI

struct AddRecipeView: View {
    let viewModel: RecipeViewModel
    @Binding var isPresented: Bool
    
    @State private var title = ""
    @State private var ingredients: [String] = [""]
    @State private var steps: [String] = [""]
    @State private var sourceUrl = ""
    @State private var sourceType = ""
    @State private var prepTimeMinutes: String = ""
    @State private var cookTimeMinutes: String = ""
    @State private var servings: String = ""
    @State private var notes = ""
    
    var isFormValid: Bool {
        !title.trimmingCharacters(in: .whitespaces).isEmpty &&
        !ingredients.filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty }.isEmpty &&
        !steps.filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty }.isEmpty
    }
    
    var body: some View {
        NavigationStack {
            Form {
                // Basic Info Section
                Section("Recipe Title") {
                    TextField("e.g. Pasta Carbonara", text: $title)
                }
                
                Section("Cooking Times") {
                    HStack {
                        Label("Prep Time (min)", systemImage: "timer")
                        Spacer()
                        TextField("0", text: $prepTimeMinutes)
                            .keyboardType(.numberPad)
                            .frame(width: 60)
                    }
                    
                    HStack {
                        Label("Cook Time (min)", systemImage: "flame")
                        Spacer()
                        TextField("0", text: $cookTimeMinutes)
                            .keyboardType(.numberPad)
                            .frame(width: 60)
                    }
                    
                    HStack {
                        Label("Servings", systemImage: "person.2")
                        Spacer()
                        TextField("0", text: $servings)
                            .keyboardType(.numberPad)
                            .frame(width: 60)
                    }
                }
                
                // Ingredients Section
                Section("Ingredients") {
                    ForEach(0..<ingredients.count, id: \.self) { index in
                        HStack {
                            TextField("Ingredient \(index + 1)", text: $ingredients[index])
                            
                            if ingredients.count > 1 {
                                Button(action: { ingredients.remove(at: index) }) {
                                    Image(systemName: "xmark.circle.fill")
                                        .foregroundColor(.red)
                                }
                            }
                        }
                    }
                    
                    Button(action: { ingredients.append("") }) {
                        Label("Add Ingredient", systemImage: "plus.circle.fill")
                            .foregroundColor(.orange)
                    }
                }
                
                // Steps Section
                Section("Steps") {
                    ForEach(0..<steps.count, id: \.self) { index in
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Step \(index + 1)")
                                .font(.caption)
                                .foregroundColor(.gray)
                            
                            TextEditor(text: $steps[index])
                                .frame(minHeight: 60)
                                .overlay(RoundedRectangle(cornerRadius: 4)
                                    .stroke(Color(.systemGray3), lineWidth: 1))
                        }
                        
                        if steps.count > 1 {
                            Button(action: { steps.remove(at: index) }) {
                                Label("Remove Step", systemImage: "xmark.circle.fill")
                                    .foregroundColor(.red)
                            }
                        }
                    }
                    
                    Button(action: { steps.append("") }) {
                        Label("Add Step", systemImage: "plus.circle.fill")
                            .foregroundColor(.orange)
                    }
                }
                
                // Optional Details Section
                Section("Optional Details") {
                    TextField("Source URL", text: $sourceUrl)
                        .keyboardType(.URL)
                    
                    TextField("Source Type (Website, Book, etc.)", text: $sourceType)
                    
                    TextEditor(text: $notes)
                        .frame(minHeight: 80)
                        .overlay(RoundedRectangle(cornerRadius: 4)
                            .stroke(Color(.systemGray3), lineWidth: 1))
                }
            }
            .navigationTitle("Add Recipe")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        isPresented = false
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        saveRecipe()
                    }
                    .disabled(!isFormValid || viewModel.isLoading)
                    .foregroundColor(.orange)
                }
            }
        }
    }
    
    private func saveRecipe() {
        Task {
            let cleanedIngredients = ingredients.filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty }
            let cleanedSteps = steps.filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty }
            let cleanedNotes = notes.trimmingCharacters(in: .whitespaces)
            let cleanedSourceUrl = sourceUrl.trimmingCharacters(in: .whitespaces)
            let cleanedSourceType = sourceType.trimmingCharacters(in: .whitespaces)
            
            await viewModel.createRecipe(
                title: title,
                ingredients: cleanedIngredients,
                steps: cleanedSteps,
                sourceUrl: cleanedSourceUrl.isEmpty ? nil : cleanedSourceUrl,
                sourceType: cleanedSourceType.isEmpty ? nil : cleanedSourceType,
                prepTimeMinutes: Int(prepTimeMinutes),
                cookTimeMinutes: Int(cookTimeMinutes),
                servings: Int(servings),
                notes: cleanedNotes.isEmpty ? nil : cleanedNotes
            )
            
            isPresented = false
        }
    }
}

#Preview {
    @StateObject var viewModel = RecipeViewModel()
    @State var isPresented = true
    
    return AddRecipeView(viewModel: viewModel, isPresented: $isPresented)
}
