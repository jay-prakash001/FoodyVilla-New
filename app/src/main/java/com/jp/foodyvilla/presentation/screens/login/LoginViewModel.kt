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


    private val _loginUiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val loginUiState = _loginUiState.asStateFlow()


    init {
        println("Login status : ${authRepo.isLoggedIn()}")
    }

    fun signInWithSupabase(idToken: String) {
        viewModelScope.launch {
            authRepo.signInWithSupabase(idToken).collectLatest {
                println("Login Res $it")
            }
        }
    }
}


