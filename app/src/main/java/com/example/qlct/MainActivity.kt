package com.example.qlct

import com.example.qlct.view.theme.QlctTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.isSystemInDarkTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.key
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlct.model.database.DatabaseInitializer
import com.example.qlct.model.database.DemoDataSeeder
import com.example.qlct.model.database.ExpenseDatabase
import com.example.qlct.model.repository.AuthRepository
import com.example.qlct.model.repository.ExpenseRepository
import com.example.qlct.view.auth.AuthScreen
import com.example.qlct.view.expense.ExpenseScreen
import com.example.qlct.viewmodel.AuthViewModel
import com.example.qlct.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        val database = ExpenseDatabase.getInstance(applicationContext)
        val authRepository = AuthRepository(
            dao = database.accountDao(),
            demoSeeder = DemoDataSeeder(database),
            initializer = DatabaseInitializer(database)
        )
        setContent {
            val preferences = remember { getSharedPreferences("appearance", MODE_PRIVATE) }
            var savedDarkMode by remember {
                mutableStateOf<Boolean?>(if (preferences.contains("dark_mode")) preferences.getBoolean("dark_mode", false) else null)
            }
            val darkTheme = savedDarkMode ?: isSystemInDarkTheme()
            val toggleTheme: () -> Unit = {
                savedDarkMode = !darkTheme
                preferences.edit().putBoolean("dark_mode", !darkTheme).apply()
            }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !darkTheme
            }
            QlctTheme(darkTheme = darkTheme) {
                val auth: AuthViewModel = viewModel(factory = AuthViewModel.factory(authRepository))
                val authState by auth.uiState.collectAsStateWithLifecycle()
                if (authState.username == null) AuthScreen(auth, darkTheme, toggleTheme)
                else {
                    key(authState.userId) {
                        val repository = ExpenseRepository(database.dao(), authState.userId!!)
                        val model: ExpenseViewModel = viewModel(key = "expenses_${authState.userId}", factory = ExpenseViewModel.factory(repository))
                        ExpenseScreen(model, authState.username!!, auth::logout, darkTheme, toggleTheme)
                    }
                }
            }
        }
    }
}
