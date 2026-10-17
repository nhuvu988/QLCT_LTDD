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
    /**
     * Khởi tạo Room và các Repository, sau đó dựng giao diện Compose.
     * Chưa đăng nhập thì mở AuthScreen; đã đăng nhập thì mở ExpenseScreen theo userId.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Cho phép vẽ sát mép màn hình; các màn hình Compose tự chừa khoảng trống cho thanh hệ thống.
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
                // Thu thập StateFlow theo lifecycle; khi state đổi, Compose dựng lại phần giao diện liên quan.
                val authState by auth.uiState.collectAsStateWithLifecycle()
                if (authState.username == null) AuthScreen(auth)
                else {
                    // Đổi khóa Compose khi đổi tài khoản để tách trạng thái giao diện giữa các người dùng.
                    // ViewModel chi tiêu cũng có khóa chứa userId và Repository chỉ truy vấn tài khoản đó.
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
