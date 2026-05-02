package com.nomnom.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.nomnom.app.ui.theme.*
import com.nomnom.data.viewmodel.AuthViewModel
import com.nomnom.data.viewmodel.RecipeViewModel
import com.nomnom.ui.components.NomNomButton
import com.nomnom.ui.components.NomNomTextField
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

private enum class ProfileMode { Main, PersonalInfo, Dietary, Account, Help }

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    recipeViewModel: RecipeViewModel,
    onLogout: () -> Unit
) {
    var mode by remember { mutableStateOf(ProfileMode.Main) }
    val haptic = LocalHapticFeedback.current
    
    val username by authViewModel.username.collectAsState()
    val avatarId by authViewModel.avatarId.collectAsState()
    val email by authViewModel.userEmail.collectAsState()
    val isPremium by authViewModel.isPremium.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    val avatars = listOf(
        "avatar_avocado" to com.nomnom.app.R.drawable.avatar_avocado,
        "avatar_broccoli" to com.nomnom.app.R.drawable.avatar_broccoli,
        "avatar_pineapple" to com.nomnom.app.R.drawable.avatar_pineapple,
        "avatar_strawberry" to com.nomnom.app.R.drawable.avatar_strawberry,
        "avatar_pepper" to com.nomnom.app.R.drawable.avatar_pepper
    )

    val currentAvatarRes = avatars.find { it.first == avatarId }?.second ?: avatars[0].second

    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            if (targetState == ProfileMode.Main) {
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            } else {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            }
        },
        label = "profile_navigation"
    ) { currentMode ->
        when (currentMode) {
            ProfileMode.Main -> MainProfileView(
                username = username.ifBlank { email.split("@").firstOrNull() ?: "Chef" },
                avatarRes = currentAvatarRes,
                email = email,
                isPremium = isPremium,
                onModeChange = { mode = it },
                onLogout = onLogout
            )
            ProfileMode.PersonalInfo -> PersonalInfoView(
                initialUsername = username,
                currentAvatarId = avatarId,
                avatars = avatars,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onSave = { name, avId ->
                    authViewModel.updateProfile(name, avId)
                    if (errorMessage == null && !isLoading) mode = ProfileMode.Main
                },
                onBack = { mode = ProfileMode.Main; authViewModel.clearError() }
            )
            ProfileMode.Dietary -> DietaryPreferencesView(
                onBack = { mode = ProfileMode.Main }
            )
            ProfileMode.Account -> AccountSettingsView(
                initialEmail = email,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onUpdateEmail = { authViewModel.updateEmail(it) },
                onUpdatePassword = { authViewModel.updatePassword(it) },
                onBack = { mode = ProfileMode.Main; authViewModel.clearError() }
            )
            ProfileMode.Help -> HelpSupportView(
                onBack = { mode = ProfileMode.Main }
            )
        }
    }
}

@Composable
private fun MainProfileView(
    username: String,
    avatarRes: Int,
    email: String,
    isPremium: Boolean,
    onModeChange: (ProfileMode) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NomNomBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // Profile Header
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(NomNomSurface)
                    .border(3.dp, NomNomRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            }
            if (isPremium) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CSGold)
                        .border(2.dp, NomNomBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💎", fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = username,
            style = MaterialTheme.typography.headlineSmall,
            color = NomNomWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = email,
            color = NomNomTextSub,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(40.dp))

        // Settings Menu
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(NomNomSurface)
                .padding(vertical = 8.dp)
        ) {
            ProfileMenuItem(icon = Icons.Default.Person, label = "Personal Information", onClick = { onModeChange(ProfileMode.PersonalInfo) })
            Divider(modifier = Modifier.padding(horizontal = 24.dp), color = NomNomDivider.copy(alpha = 0.5f))
            ProfileMenuItem(icon = Icons.Default.Restaurant, label = "Dietary Preferences", onClick = { onModeChange(ProfileMode.Dietary) })
            Divider(modifier = Modifier.padding(horizontal = 24.dp), color = NomNomDivider.copy(alpha = 0.5f))
            ProfileMenuItem(icon = Icons.Default.Settings, label = "Account Settings", onClick = { onModeChange(ProfileMode.Account) })
            Divider(modifier = Modifier.padding(horizontal = 24.dp), color = NomNomDivider.copy(alpha = 0.5f))
            ProfileMenuItem(icon = Icons.Default.HelpOutline, label = "Help & Support", onClick = { onModeChange(ProfileMode.Help) })
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NomNomSurface, contentColor = NomNomRed)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PersonalInfoView(
    initialUsername: String,
    currentAvatarId: String,
    avatars: List<Pair<String, Int>>,
    isLoading: Boolean,
    errorMessage: String?,
    onSave: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf(initialUsername) }
    var avatarId by remember { mutableStateOf(currentAvatarId) }
    
    Column(
        modifier = Modifier.fillMaxSize().background(NomNomBg).padding(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NomNomWhite) }
            Text("Personal Information", style = MaterialTheme.typography.titleLarge, color = NomNomWhite)
        }

        Spacer(Modifier.height(32.dp))

        Text("Profile Picture", color = NomNomTextSub, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            avatars.forEach { (id, res) ->
                Box(
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(if (avatarId == id) NomNomRed.copy(alpha = 0.2f) else NomNomSurface).border(width = if (avatarId == id) 2.dp else 0.dp, color = NomNomRed, shape = CircleShape).clickable { avatarId = id },
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(id = res), contentDescription = null, modifier = Modifier.fillMaxSize().padding(10.dp), contentScale = ContentScale.Fit)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        InfoField(label = "Username", value = username, onValueChange = { username = it }, placeholder = "Your display name")
        
        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(errorMessage, color = NomNomRed, fontSize = 14.sp)
        }

        Spacer(Modifier.weight(1f))
        NomNomButton(onClick = { onSave(username, avatarId) }, modifier = Modifier.fillMaxWidth().height(56.dp), enabled = !isLoading) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NomNomWhite, strokeWidth = 2.dp)
            else Text("Save Profile", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DietaryPreferencesView(onBack: () -> Unit) {
    val options = listOf("Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Low Carb", "Keto", "Paleo")
    val selected = remember { mutableStateListOf<String>() }

    Column(modifier = Modifier.fillMaxSize().background(NomNomBg).padding(24.dp)) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NomNomWhite) }
            Text("Dietary Preferences", style = MaterialTheme.typography.titleLarge, color = NomNomWhite)
        }
        Spacer(Modifier.height(24.dp))
        Text("Select your dietary requirements to help us suggest better recipes.", color = NomNomTextSub, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))

        options.forEach { option ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { if (selected.contains(option)) selected.remove(option) else selected.add(option) }.padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(option, color = NomNomWhite, modifier = Modifier.weight(1f), fontSize = 16.sp)
                Checkbox(
                    checked = selected.contains(option),
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(checkedColor = NomNomRed, uncheckedColor = NomNomTextSub)
                )
            }
            Divider(color = NomNomDivider.copy(alpha = 0.3f))
        }

        Spacer(Modifier.weight(1f))
        NomNomButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Save Preferences", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AccountSettingsView(
    initialEmail: String,
    isLoading: Boolean,
    errorMessage: String?,
    onUpdateEmail: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(NomNomBg).padding(24.dp)) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NomNomWhite) }
            Text("Account Settings", style = MaterialTheme.typography.titleLarge, color = NomNomWhite)
        }
        Spacer(Modifier.height(32.dp))

        InfoField(label = "Email Address", value = email, onValueChange = { email = it }, placeholder = "your@email.com")
        Spacer(Modifier.height(16.dp))
        NomNomButton(onClick = { onUpdateEmail(email) }, modifier = Modifier.fillMaxWidth().height(48.dp), enabled = !isLoading, containerColor = NomNomSurface) {
            Text("Update Email", fontSize = 14.sp)
        }

        Spacer(Modifier.height(40.dp))

        InfoField(label = "New Password", value = password, onValueChange = { password = it }, placeholder = "••••••••", isPassword = true)
        Spacer(Modifier.height(16.dp))
        NomNomButton(onClick = { onUpdatePassword(password) }, modifier = Modifier.fillMaxWidth().height(48.dp), enabled = !isLoading && password.length >= 6, containerColor = NomNomSurface) {
            Text("Change Password", fontSize = 14.sp)
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(errorMessage, color = NomNomRed, fontSize = 14.sp)
        }
    }
}

@Composable
private fun HelpSupportView(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(NomNomBg).padding(24.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NomNomWhite) }
            Text("Help & Support", style = MaterialTheme.typography.titleLarge, color = NomNomWhite)
        }
        Spacer(Modifier.height(32.dp))

        val faqs = listOf(
            "How do I add a recipe?" to "Tap the '+' button on the Recipes screen.",
            "Can I use NomNom offline?" to "Yes, all your saved recipes are available without internet.",
            "How do I upgrade to Pro?" to "Go to the Profile screen and tap 'Go NomNom Pro'.",
            "What is AI Import?" to "It uses Gemini to extract recipes from websites or photos."
        )

        faqs.forEach { (q, a) ->
            Text(q, color = NomNomWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(a, color = NomNomTextSub, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
        }

        Spacer(Modifier.height(32.dp))
        NomNomButton(onClick = { /* Open link */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Contact Support")
        }
    }
}

@Composable
private fun InfoField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, isPassword: Boolean = false) {
    Column {
        Text(label, color = NomNomWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        NomNomTextField(value = value, onValueChange = onValueChange, placeholder = placeholder, modifier = Modifier.fillMaxWidth(), visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None)
    }
}

@Composable
private fun ProfileMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(NomNomBg), contentAlignment = Alignment.Center) { Icon(icon, null, tint = NomNomTextSub, modifier = Modifier.size(20.dp)) }
        Text(text = label, color = NomNomWhite, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = NomNomDivider, modifier = Modifier.size(20.dp))
    }
}
