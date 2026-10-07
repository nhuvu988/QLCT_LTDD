package com.example.qlct.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthScreen(model: AuthViewModel) {
    val state by model.state.collectAsStateWithLifecycle()
    // Credentials deliberately stay out of saved instance state and persistent storage.
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF7560C6), Color(0xFFF6F5FA))))
            .safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("QLCT", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Text("Quản lý chi tiêu cá nhân", color = Color.White)
        Spacer(Modifier.height(28.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (state.loading) {
                    if (state.error == null) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                    else {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = model::load) { Text("Thử lại") }
                    }
                } else {
                    Text(if (state.setup) "Tạo tài khoản" else "Đăng nhập", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(if (state.setup) "Thiết lập tài khoản để mở ứng dụng trên thiết bị này."
                        else "Đăng nhập để xem và quản lý giao dịch của bạn.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(username, { username = it; model.clearError() },
                        label = { Text("Tên đăng nhập") }, singleLine = true, enabled = !state.busy,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(password, { password = it; model.clearError() },
                        label = { Text("Mật khẩu") }, singleLine = true, enabled = !state.busy,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = { TextButton(onClick = { showPassword = !showPassword }) { Text(if (showPassword) "Ẩn" else "Hiện") } },
                        modifier = Modifier.fillMaxWidth())
                    if (state.setup) OutlinedTextField(confirmation, { confirmation = it; model.clearError() },
                        label = { Text("Xác nhận mật khẩu") }, singleLine = true, enabled = !state.busy,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = { model.submit(username, password, confirmation) }, enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth()) {
                        if (state.busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text(if (state.setup) "Tạo tài khoản và tiếp tục" else "Đăng nhập")
                    }
                    Text(if (state.setup) "Tên đăng nhập: 3–32 ký tự không dấu. Mật khẩu: 8–128 ký tự."
                        else "Tài khoản được lưu trên thiết bị; không cần Internet.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = {
                        password = ""; confirmation = ""; model.toggleSetup()
                    }, enabled = !state.busy, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(if (state.setup) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký")
                    }
                }
            }
        }
    }
}
