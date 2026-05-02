package com.nomnom.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nomnom.app.ui.theme.*
import com.nomnom.data.viewmodel.AuthViewModel
import com.nomnom.ui.screens.auth.LoginScreen
import com.nomnom.ui.screens.auth.SignupScreen
import com.nomnom.ui.screens.home.HomeScreen

// ── Root navigation routes ──────────────────────────────────────────────
private object RootRoutes {
    const val SPLASH = "splash"
    const val LOGIN  = "login"
    const val SIGNUP = "signup"
    const val HOME   = "home"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val isAuthReady by authViewModel.isAuthReady.collectAsState()

            splashScreen.setKeepOnScreenCondition { !isAuthReady }

            NomNomTheme {
                val isLoggedIn      by authViewModel.isLoggedIn.collectAsState()
                val isAuthReady     by authViewModel.isAuthReady.collectAsState()
                val isSessionExpired by authViewModel.isSessionExpired.collectAsState()

                val navController = rememberNavController()
                val context       = LocalContext.current

                // Auto-redirect when session expires (401 from any API)
                LaunchedEffect(isSessionExpired) {
                    if (isSessionExpired) {
                        authViewModel.clearSessionExpired()
                        Toast.makeText(context, "Session expired — please log in again", Toast.LENGTH_LONG).show()
                        navController.navigate(RootRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                // Once auth is confirmed, navigate to the correct screen
                LaunchedEffect(isAuthReady, isLoggedIn) {
                    if (isAuthReady) {
                        if (isLoggedIn) {
                            navController.navigate(RootRoutes.HOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            navController.navigate(RootRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                NavHost(
                    navController    = navController,
                    startDestination = RootRoutes.SPLASH   // always start here
                ) {
                    // ── Splash / loading ──────────────────────────────────
                    composable(RootRoutes.SPLASH) {
                        SplashScreen()
                    }

                    composable(RootRoutes.LOGIN) {
                        LoginScreen(
                            viewModel          = authViewModel,
                            onNavigateToSignup = { navController.navigate(RootRoutes.SIGNUP) }
                        )
                    }

                    composable(RootRoutes.SIGNUP) {
                        SignupScreen(
                            viewModel         = authViewModel,
                            onNavigateToLogin = {
                                navController.navigate(RootRoutes.LOGIN) {
                                    popUpTo(RootRoutes.LOGIN) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(RootRoutes.HOME) {
                        HomeScreen(authViewModel = authViewModel)
                    }
                }
            }
        }
    }
}

// ── Animated splash — shown for < 500 ms while DataStore loads ─────────────
@Composable
private fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulse by infiniteTransition.animateFloat(
        initialValue  = 0.9f,
        targetValue   = 1.05f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splashPulse"
    )

    Box(
        modifier        = Modifier
            .fillMaxSize()
            .background(CSSurface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🍳", fontSize = 72.sp, modifier = Modifier.scale(pulse))
            Spacer(Modifier.height(12.dp))
            Row {
                Text("Nom", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = CSOnSurface)
                Text("Nom", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = CSSage)
            }
        }
    }
}

