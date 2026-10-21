package com.example.qlct

import com.example.qlct.view.theme.QlctTheme
import androidx.compose.runtime.getValue
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
            QlctTheme {
                val auth: AuthViewModel = viewModel(factory = AuthViewModel.factory(authRepository))
                val authState by auth.uiState.collectAsStateWithLifecycle()
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
