package com.example.ticketti.ui.theme.login

data class LoginUiState (
    val username:String="",
    val password:String="",
    val isLoading:Boolean=false,
    val error:String? =null
)