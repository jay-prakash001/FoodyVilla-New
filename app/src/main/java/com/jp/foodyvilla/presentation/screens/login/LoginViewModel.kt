package com.jp.foodyvilla.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla.data.repo.AuthRepo
import com.jp.foodyvilla.presentation.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepo: AuthRepo) : ViewModel() {


    private val _loginUiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val loginUiState = _loginUiState.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()
    private val _otp = MutableStateFlow("")
    val otp = _phoneNumber.asStateFlow()

    fun updatePhone(newValue: String) {
        _phoneNumber.value = newValue
    }

    fun updateOtp(newValue: String) {
        _otp.value = newValue
    }

    init {
        println("Login status : ${authRepo.isLoggedIn()}")
    }

    fun login() {
        viewModelScope.launch {
            authRepo.loginWithOtp(phoneNumber.value, otp.value).collectLatest {
                _loginUiState.value = it
            }

        }
    }


    fun signInWithSupabase(idToken: String) {
        viewModelScope.launch {
            authRepo.signInWithSupabase(idToken).collectLatest {
                println("Login Res $it")
            }
        }
    }
}


