import Foundation

// MARK: - User Model
struct User: Identifiable, Codable {
    let id: String
    let email: String
    let createdAt: Date
    
    enum CodingKeys: String, CodingKey {
        case id
        case email
        case createdAt = "created_at"
    }
}

// MARK: - Recipe Model
struct Recipe: Identifiable, Codable {
    let id: String
    let userId: String
    let title: String
    let ingredients: [String]
    let steps: [String]
    let sourceUrl: String?
    let sourceType: String? // 'instagram', 'youtube', 'text', 'custom'
    let prepTimeMinutes: Int?
    let cookTimeMinutes: Int?
    let servings: Int?
    let notes: String?
    let tags: [String]
    let createdAt: Date
    let updatedAt: Date
    
    enum CodingKeys: String, CodingKey {
        case id
        case userId = "user_id"
        case title
        case ingredients
        case steps
        case sourceUrl = "source_url"
        case sourceType = "source_type"
        case prepTimeMinutes = "prep_time_minutes"
        case cookTimeMinutes = "cook_time_minutes"
        case servings
        case notes
        case tags
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
}

// MARK: - Collection Model
struct RecipeCollection: Identifiable, Codable {
    let id: String
    let userId: String
    let name: String
    let description: String?
    let createdAt: Date
    
    enum CodingKeys: String, CodingKey {
        case id
        case userId = "user_id"
        case name
        case description
        case createdAt = "created_at"
    }
}

// MARK: - Tag Model
struct RecipeTag: Identifiable, Codable {
    let id: String
    let userId: String
    let name: String
    let color: String?
    let createdAt: Date
    
    enum CodingKeys: String, CodingKey {
        case id
        case userId = "user_id"
        case name
        case color
        case createdAt = "created_at"
    }
}
