package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.SchoolRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainSchoolPlatformScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.signOutFromSchoolPlatform
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SchoolViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember(context) {
                context.getSharedPreferences("myschool_prefs", Context.MODE_PRIVATE)
            }
            var isDarkTheme by rememberSaveable {
                mutableStateOf(prefs.getBoolean("dark_theme", false))
            }
            var hasSeenOnboarding by rememberSaveable {
                mutableStateOf(prefs.getBoolean("onboarding_completed", false))
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                if (!hasSeenOnboarding) {
                    OnboardingScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = {
                            isDarkTheme = !isDarkTheme
                            prefs.edit().putBoolean("dark_theme", isDarkTheme).apply()
                        },
                        onFinishOnboarding = {
                            hasSeenOnboarding = true
                            prefs.edit().putBoolean("onboarding_completed", true).apply()
                        }
                    )
                } else {
                    SchoolAuthGate(
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = {
                            isDarkTheme = !isDarkTheme
                            prefs.edit().putBoolean("dark_theme", isDarkTheme).apply()
                        },
                        onReopenOnboarding = {
                            hasSeenOnboarding = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SchoolAuthGate(
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onReopenOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = onToggleDarkTheme,
            onReopenOnboarding = onReopenOnboarding,
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        val schoolViewModel: SchoolViewModel = viewModel(
            key = user.uid,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY]) {
                        "APPLICATION_KEY missing from CreationExtras"
                    }
                    val databaseId = app.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(databaseId)
                    SchoolViewModel(SchoolRepository(db))
                }
            }
        )

        MainSchoolPlatformScreen(
            viewModel = schoolViewModel,
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = onToggleDarkTheme,
            onSignOut = {
                signOutFromSchoolPlatform(
                    context = context,
                    onSignOutComplete = { currentUser = null },
                    scope = coroutineScope
                )
            }
        )
    }
}
