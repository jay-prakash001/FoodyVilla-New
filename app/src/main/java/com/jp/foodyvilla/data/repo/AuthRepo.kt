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
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
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




    @OptIn(ExperimentalTime::class)
    fun loginWithOtp(
        phone: String,
        otp: String? = null
    ): Flow<UiState<String>> = flow {

        emit(UiState.Loading)

        try {
            // ── 1. Invoke the edge function ───────────────────────────────
            val response = supabase.functions.invoke(
                function = "login",
                body = buildJsonObject {
                    put("phone", phone)
                    otp?.let { put("otp", it) }
                }
            )

            // ── 2. Read body — FIX: use bodyAsText() not bodyBytes ────────
            //    `bodyBytes` is not a property on Ktor's HttpResponse.
            //    `bodyAsText()` is the correct suspend extension (ktor-client-core 3.x).
            val bodyString = response.bodyAsText()

            // ── 3. Parse JSON ─────────────────────────────────────────────
            val json = Json.parseToJsonElement(bodyString).jsonObject

            val success = json["success"]?.jsonPrimitive?.boolean ?: false
            //            FIX: `.booleanOrNull` → `.boolean` (non-nullable after null-check)
            //            Both work, but `.boolean` throws on wrong type which is desirable.

            if (!success) {
                val message = json["message"]?.jsonPrimitive?.content ?: "Unknown error"
                emit(UiState.Error(Exception(message)))
                return@flow
            }

            // ── 4. OTP sent path (no otp provided) ───────────────────────
            if (otp == null) {
                emit(UiState.Success("OTP_SENT"))
                return@flow
            }

            // ── 5. Login success path — parse session ─────────────────────
            val sessionJson = json["session"]?.jsonObject
                ?: throw Exception("Session data missing")

            val expiresIn = sessionJson["expires_in"]
                ?.jsonPrimitive
                // FIX: Edge functions can return expires_in as Int or Long in JSON.
                // Using `long` directly throws if value is a float; parse via content instead.
                ?.content
                ?.toLongOrNull()
                ?: 3600L

            val userSession = UserSession(
                accessToken  = sessionJson["access_token"]?.jsonPrimitive?.content  ?: "",
                refreshToken = sessionJson["refresh_token"]?.jsonPrimitive?.content ?: "",
                expiresIn    = expiresIn,
                tokenType    = sessionJson["token_type"]?.jsonPrimitive?.content    ?: "bearer",
                // FIX: tokenType was hardcoded "bearer" — read it from JSON or default to "bearer"
                user         = null
            )

            // importSession is correct API for supabase-kt 3.x
            supabase.auth.importSession(userSession)

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