package com.jp.foodyvilla.data.repo

import android.content.Context
import android.util.Log
import com.jp.foodyvilla.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.github.jan.supabase.auth.providers.Google
class AuthRepo(
    private val supabase: SupabaseClient,
    private val context: Context
) {

    // client id : 936302589972-bjm4nnn6sie66eaqalfi0l6re6ja7fju.apps.googleusercontent.com

    fun signInWithSupabase(idToken: String): Flow<UiState<UserInfo>> = flow {

        println("Token : $idToken")
        emit(UiState.Loading)
        try {
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                emit(UiState.Success(user))
            } else {
                emit(UiState.Error(Exception("User not found")))
            }
            Log.d("SupabaseAuth", "Logged in: ${user?.email}")

        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Error: ${e.message}")
            emit(UiState.Error(e))
        }
    }

    fun isLoggedIn(): Boolean {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()

            println("Token: $token")
            println(supabase.auth.currentUserOrNull())
            println(token)
            if (token.isNullOrEmpty()) {
                return false
            } else {
                return true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }


}