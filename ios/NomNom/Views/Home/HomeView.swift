import SwiftUI

struct HomeView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            // Recipes Tab
            RecipeListView()
                .tabItem {
                    Image(systemName: "book.fill")
                    Text("Recipes")
                }
                .tag(0)
            
            // Collections Tab
            NavigationStack {
                VStack {
                    HStack {
                        Text("Collections")
                            .font(.system(size: 28, weight: .bold))
                        Spacer()
                        Button(action: {}) {
                            Image(systemName: "plus.circle.fill")
                                .font(.title2)
                                .foregroundColor(.orange)
                        }
                    }
                    .padding()
                    
                    // Placeholder for collections
                    VStack(spacing: 20) {
                        Image(systemName: "folder.fill")
                            .font(.system(size: 48))
                            .foregroundColor(.gray)
                        
                        Text("No collections yet")
                            .font(.headline)
                        
                        Text("Create collections to organize your recipes")
                            .font(.caption)
                            .foregroundColor(.gray)
                            .multilineTextAlignment(.center)
                        
                        Button(action: {}) {
                            Text("Create Collection")
                                .font(.headline)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.orange)
                                .foregroundColor(.white)
                                .cornerRadius(8)
                        }
                        .padding(.top, 20)
                    }
                    .padding()
                    
                    Spacer()
                }
            }
            .tabItem {
                Image(systemName: "folder.fill")
                Text("Collections")
            }
            .tag(1)
            
            // What to Eat Tab
            NavigationStack {
                VStack {
                    Text("What Should I Eat?")
                        .font(.system(size: 28, weight: .bold))
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding()
                    
                    // Placeholder for suggestion feature
                    VStack(spacing: 20) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 48))
                            .foregroundColor(.orange)
                        
                        Text("Get recipe suggestions")
                            .font(.headline)
                        
                        Text("We'll help you decide what to cook based on your mood and ingredients")
                            .font(.caption)
                            .foregroundColor(.gray)
                            .multilineTextAlignment(.center)
                        
                        Button(action: {}) {
                            Text("Get Suggestions")
                                .font(.headline)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.orange)
                                .foregroundColor(.white)
                                .cornerRadius(8)
                        }
                        .padding(.top, 20)
                    }
                    .padding()
                    
                    Spacer()
                }
            }
            .tabItem {
                Image(systemName: "sparkles")
                Text("Suggest")
            }
            .tag(2)
            
            // Profile Tab
            NavigationStack {
                VStack {
                    HStack {
                        Text("Profile")
                            .font(.system(size: 28, weight: .bold))
                        Spacer()
                    }
                    .padding()
                    
                    VStack(spacing: 15) {
                        HStack {
                            Text("Email")
                                .foregroundColor(.gray)
                            Spacer()
                            Text(authViewModel.currentUser?.email ?? "Unknown")
                                .fontWeight(.semibold)
                        }
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(8)
                        
                        Spacer()
                        
                        Button(action: {
                            authViewModel.logout()
                        }) {
                            Text("Logout")
                                .font(.headline)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.red.opacity(0.1))
                                .foregroundColor(.red)
                                .cornerRadius(8)
                        }
                    }
                    .padding()
                    
                    Spacer()
                }
            }
            .tabItem {
                Image(systemName: "person.fill")
                Text("Profile")
            }
            .tag(3)
        }
        .tint(.orange)
    }
}

#Preview {
    HomeView()
        .environmentObject(AuthViewModel())
}
