import SwiftUI

// MARK: - Authentication ViewModel
class AuthViewModel: ObservableObject {
    @Published var isLoggedIn = false
    @Published var currentUser: User?
    @Published var authToken: String?
    @Published var errorMessage: String?
    @Published var isLoading = false
    
    // MARK: - Authentication Methods
    
    func signup(email: String, password: String) async {
        isLoading = true
        errorMessage = nil
        
        do {
            let (user, token) = try await SupabaseService.shared.signup(email: email, password: password)
            
            // Save to UserDefaults (in production, use Keychain)
            saveCredentials(user: user, token: token)
            
            await MainActor.run {
                self.currentUser = user
                self.authToken = token
                self.isLoggedIn = true
                self.isLoading = false
            }
        } catch {
            await MainActor.run {
                self.errorMessage = "Signup failed: \(error.localizedDescription)"
                self.isLoading = false
            }
        }
    }
    
    func login(email: String, password: String) async {
        isLoading = true
        errorMessage = nil
        
        do {
            let (user, token) = try await SupabaseService.shared.login(email: email, password: password)
            
            // Save to UserDefaults (in production, use Keychain)
            saveCredentials(user: user, token: token)
            
            await MainActor.run {
                self.currentUser = user
                self.authToken = token
                self.isLoggedIn = true
                self.isLoading = false
            }
        } catch {
            await MainActor.run {
                self.errorMessage = "Login failed: \(error.localizedDescription)"
                self.isLoading = false
            }
        }
    }
    
    func logout() {
        isLoggedIn = false
        currentUser = nil
        authToken = nil
        UserDefaults.standard.removeObject(forKey: "authToken")
        UserDefaults.standard.removeObject(forKey: "userId")
        UserDefaults.standard.removeObject(forKey: "userEmail")
    }
    
    // MARK: - Private Methods
    
    private func saveCredentials(user: User, token: String) {
        UserDefaults.standard.set(token, forKey: "authToken")
        UserDefaults.standard.set(user.id, forKey: "userId")
        UserDefaults.standard.set(user.email, forKey: "userEmail")
    }
    
    func loadCredentials() {
        if let token = UserDefaults.standard.string(forKey: "authToken"),
           let userId = UserDefaults.standard.string(forKey: "userId"),
           let email = UserDefaults.standard.string(forKey: "userEmail") {
            self.authToken = token
            self.currentUser = User(id: userId, email: email, createdAt: Date())
            self.isLoggedIn = true
        }
    }
}
