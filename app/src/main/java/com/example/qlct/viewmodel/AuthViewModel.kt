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

// Quản lý phiên đăng nhập trong RAM và công bố trạng thái bất biến cho AuthScreen.
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    // Chỉ ViewModel được ghi MutableStateFlow; UI chỉ có quyền quan sát StateFlow.
    private val mutableState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = mutableState.asStateFlow()

    init { load() }

    /**
     * Đọc trạng thái tài khoản trong coroutine và chọn đăng nhập hoặc tạo tài khoản.
     * Nếu đọc lỗi, UI giữ trạng thái tải kèm thông báo để người dùng thử lại.
     */
    fun load() {
        if (uiState.value.busy) return
        mutableState.value = AuthUiState()
        viewModelScope.launch {
            try { mutableState.value = AuthUiState(loading = false, setup = !repository.hasAccount()) }
            catch (cancel: CancellationException) { throw cancel }
            catch (_: Exception) { mutableState.value = AuthUiState(error = "Không đọc được tài khoản. Nhấn Thử lại.") }
        }
    }

    /**
     * Chặn gửi lặp, kiểm tra dữ liệu rồi gọi đăng ký hoặc đăng nhập theo setup.
     * Kết quả cập nhật username/userId để MainActivity chuyển màn hình; lỗi được đưa vào uiState.
     */
    fun submit(username: String, password: String, confirmation: String) {
        val current = uiState.value
        // Chặn gửi khi đang tải, đang xác thực hoặc đã có phiên để tránh tạo nhiều yêu cầu song song.
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
            // Lệnh hủy coroutine phải được ném tiếp để ViewModel dừng đúng khi lifecycle kết thúc.
            } catch (cancel: CancellationException) { throw cancel }
            catch (error: Exception) {
                mutableState.value = current.copy(busy = false, error = if (error is IllegalArgumentException)
                    error.message else "Không thể thực hiện. Vui lòng thử lại.")
            }
        }
    }

    /**
     * Xóa thông báo lỗi khi người dùng chỉnh lại dữ liệu hoặc bắt đầu thao tác khác.
     */
    fun clearError() { mutableState.value = uiState.value.copy(error = null) }
    /**
     * Chuyển giữa đăng nhập và đăng ký khi không có thao tác xác thực đang chạy.
     */
    fun toggleSetup() {
        if (!uiState.value.busy) mutableState.value = uiState.value.copy(setup = !uiState.value.setup, error = null)
    }
    /**
     * Xóa phiên trong bộ nhớ và trở về đăng nhập; tài khoản và giao dịch trong Room vẫn còn.
     */
    fun logout() { mutableState.value = AuthUiState(loading = false) }

    companion object {
        /**
         * Tạo Factory truyền AuthRepository vào ViewModel thông qua cơ chế quản lý lifecycle.
         */
        fun factory(repository: AuthRepository) = viewModelFactory { initializer { AuthViewModel(repository) } }
    }
}
