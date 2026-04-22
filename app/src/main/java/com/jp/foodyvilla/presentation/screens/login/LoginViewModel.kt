package com.jp.foodyvilla.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jp.foodyvilla.data.model.user.UserProfile
import com.jp.foodyvilla.data.repo.AuthRepo
import com.jp.foodyvilla.data.repo.UserRepository
import com.jp.foodyvilla.presentation.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepo: AuthRepo, private val userRepository: UserRepository) :
    ViewModel() {



    init {
        viewModelScope.launch {

            val user = userRepository.getCurrentUserProfile()
            println("ViewModel user $user")
        }
    }

    private val _loginUiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val loginUiState = _loginUiState.asStateFlow()


    private val _getOtpState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val getOtpState = _getOtpState.asStateFlow()

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

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        println("Login status : ${authRepo.isLoggedIn()}")
        isLoggedIn()
viewModelScope.launch {
    userRepository.getCurrentUserProfile().collectLatest {
        println("User $it")
    }
}

    }


    private fun isLoggedIn() {
        viewModelScope.launch {
            _isLoggedIn.value = authRepo.isLoggedIn()
        }
    }

    fun login() {
        viewModelScope.launch {
            authRepo.loginWithOtp("+91${phoneNumber.value}").collectLatest {

                println("Login res $it")
                _getOtpState.value = it

            }

        }
    }

    fun login(otp: String? = null) {
        viewModelScope.launch {
            authRepo.loginWithOtp("+91${phoneNumber.value}", otp).collectLatest {

                println("Login res $it")
                _loginUiState.value = it
                if (it is UiState.Success) {
                    _isLoggedIn.value = true
                }
            }

        }
    }

    private val _userState = MutableStateFlow<UiState<UserProfile>>(UiState.Idle)
    val user = _userState.asStateFlow()
    fun getUserProfile() {
        viewModelScope.launch {
            try {
                userRepository.getCurrentUserProfile().collectLatest {


                    _userState.value = it

                }
            } catch (e: Exception) {

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


