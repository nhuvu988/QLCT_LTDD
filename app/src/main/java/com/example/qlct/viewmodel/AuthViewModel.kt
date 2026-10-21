package com.example.qlct.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.qlct.model.repository.AuthRepository
import com.example.qlct.model.validation.AccountValidator
import com.example.qlct.viewmodel.state.AuthUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = mutableState.asStateFlow()

    init { load() }

    fun load() {
        if (uiState.value.busy) return
        mutableState.value = AuthUiState()
        viewModelScope.launch {
            try { mutableState.value = AuthUiState(loading = false, setup = !repository.hasAccount()) }
            catch (cancel: CancellationException) { throw cancel }
            catch (_: Exception) { mutableState.value = AuthUiState(error = "Không đọc được tài khoản. Nhấn Thử lại.") }
        }
    }

    fun submit(username: String, password: String, confirmation: String) {
        val current = uiState.value
        if (current.loading || current.busy || current.username != null) return
        val error = AccountValidator.error(username, password, confirmation.takeIf { current.setup })
        if (error != null) { mutableState.value = current.copy(error = error); return }
        mutableState.value = current.copy(busy = true, error = null)
        viewModelScope.launch {
            try {
                val account = if (current.setup) repository.register(username, password, confirmation)
                    else repository.login(username, password)
                mutableState.value = current.copy(busy = false, username = account?.username, userId = account?.id, setup = false,
                    error = if (account == null) "Tên đăng nhập hoặc mật khẩu không đúng." else null)
            } catch (cancel: CancellationException) { throw cancel }
            catch (error: Exception) {
                mutableState.value = current.copy(busy = false, error = if (error is IllegalArgumentException)
                    error.message else "Không thể thực hiện. Vui lòng thử lại.")
            }
        }
    }

    fun clearError() { mutableState.value = uiState.value.copy(error = null) }
    fun toggleSetup() {
        if (!uiState.value.busy) mutableState.value = uiState.value.copy(setup = !uiState.value.setup, error = null)
    }
    fun logout() { mutableState.value = AuthUiState(loading = false) }

    companion object {
        fun factory(repository: AuthRepository) = viewModelFactory { initializer { AuthViewModel(repository) } }
    }
}
