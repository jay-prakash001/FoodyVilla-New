package com.jp.foodyvilla.data.repo

import android.content.Context
import android.system.Os.remove
import android.util.Log
import com.jp.foodyvilla.data.model.login.LoginResponse
import com.jp.foodyvilla.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.client.utils.EmptyContent.headers
import io.ktor.http.headers
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put // 👈 CRITICAL IMPORT
import kotlin.time.ExperimentalTime

class AuthRepo(
    private val supabase: SupabaseClient,
    private val context: Context
) {

    // client id : 936302589972-bjm4nnn6sie66eaqalfi0l6re6ja7fju.apps.googleusercontent.com
    fun logout(): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)

        try {
            supabase.auth.signOut()
            supabase.auth.clearSession() // 👈 ensures local session removed
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }

    @OptIn(ExperimentalTime::class, InternalAPI::class)
    fun loginWithOtp(
        phone: String,
        otp: String? = null
    ): Flow<UiState<String>> = flow {

        emit(UiState.Loading)

        try {
            val response = supabase.functions.invoke("login") {
                body = """
        {
            "phone": "$phone"
            ${if (otp != null) """, "otp": "$otp"""" else ""}
        }
    """.trimIndent()
            }

            val bodyString = response.bodyAsText()

            val loginResponse = Json {
                ignoreUnknownKeys = true
            }.decodeFromString<LoginResponse>(bodyString)

            println("Parsed response $loginResponse")

            // ❌ API failure
            if (!loginResponse.success) {
                emit(UiState.Error(Exception(loginResponse.message)))
                return@flow
            }

            // ✅ OTP sent (no otp provided)
            if (otp == null) {
                emit(UiState.Success("OTP_SENT"))
                return@flow
            }

            // ✅ Login success
            val accessToken = loginResponse.access_token
                ?: throw Exception("Missing access token")

            val refreshToken = loginResponse.refresh_token
                ?: throw Exception("Missing refresh token")

            val expiresIn = loginResponse.expires_in ?: 3600L

            val session = UserSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = expiresIn,
                tokenType = "bearer",
                user = null
            )

            supabase.auth.importSession(session)
            supabase.auth.refreshCurrentSession()
            emit(UiState.Success("LOGIN_SUCCESS"))

        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }



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