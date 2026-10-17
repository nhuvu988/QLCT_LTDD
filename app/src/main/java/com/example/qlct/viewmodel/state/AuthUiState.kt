package com.example.qlct.viewmodel.state

// Ảnh chụp trạng thái màn hình xác thực, được thay bằng copy khi có thay đổi.
data class AuthUiState(
    val loading: Boolean = true,
    // true là đăng ký; false là đăng nhập. loading đọc dữ liệu ban đầu, busy là đang gửi xác thực.
    val setup: Boolean = false,
    val busy: Boolean = false,
    // username/userId khác null sau xác thực thành công; phiên không được lưu lâu dài vào Room.
    val username: String? = null,
    val userId: Int? = null,
    val error: String? = null
)
