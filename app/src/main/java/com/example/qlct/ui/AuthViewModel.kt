package com.example.qlct.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.qlct.data.AccountValidator
import com.example.qlct.data.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val loading: Boolean = true,
    val setup: Boolean = false,
    val busy: Boolean = false,
    val username: String? = null,
    val userId: Int? = null,
    val error: String? = null
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthState())
    val state = mutableState.asStateFlow()

    init { load() }

    fun load() {
        if (state.value.busy) return
        mutableState.value = AuthState()
        viewModelScope.launch {
            try { mutableState.value = AuthState(loading = false, setup = !repository.hasAccount()) }
            catch (cancel: CancellationException) { throw cancel }
            catch (_: Exception) { mutableState.value = AuthState(error = "Không đọc được tài khoản. Nhấn Thử lại.") }
        }
    }

    fun submit(username: String, password: String, confirmation: String) {
        val current = state.value
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

    fun clearError() { mutableState.value = state.value.copy(error = null) }
    fun toggleSetup() {
        if (!state.value.busy) mutableState.value = state.value.copy(setup = !state.value.setup, error = null)
    }
    fun logout() { mutableState.value = AuthState(loading = false) }

    companion object {
        fun factory(repository: AuthRepository) = viewModelFactory { initializer { AuthViewModel(repository) } }
    }
}
