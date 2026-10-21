package com.example.qlct.viewmodel.state

data class AuthUiState(
    val loading: Boolean = true,
    val setup: Boolean = false,
    val busy: Boolean = false,
    val username: String? = null,
    val userId: Int? = null,
    val error: String? = null
)
