package com.example.qlct

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlct.data.ExpenseDatabase
import com.example.qlct.data.ExpenseRepository
import com.example.qlct.ui.ExpenseScreen
import com.example.qlct.ui.ExpenseViewModel
import com.example.qlct.data.AuthRepository
import com.example.qlct.data.DemoDataSeeder
import com.example.qlct.ui.AuthViewModel
import com.example.qlct.ui.AuthScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        val database = ExpenseDatabase.getInstance(applicationContext)
        val authRepository = AuthRepository(database.accountDao(), DemoDataSeeder(database))
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else
                lightColorScheme(primary = Color(0xFF7560C6), secondary = Color(0xFF9C8BD9),
                    background = Color(0xFFF6F5FA), surface = Color.White,
                    surfaceVariant = Color(0xFFF0EDF8), onSurface = Color(0xFF29263A))) {
                val auth: AuthViewModel = viewModel(factory = AuthViewModel.factory(authRepository))
                val authState by auth.state.collectAsStateWithLifecycle()
                if (authState.username == null) AuthScreen(auth)
                else {
                    key(authState.userId) {
                        val repository = ExpenseRepository(database.dao(), authState.userId!!)
                        val model: ExpenseViewModel = viewModel(key = "expenses_${authState.userId}", factory = ExpenseViewModel.factory(repository))
                        ExpenseScreen(model, authState.username!!, auth::logout)
                    }
                }
            }
        }
    }
}
