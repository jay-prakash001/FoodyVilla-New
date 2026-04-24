package com.jp.foodyvilla.data.repo

import com.jp.foodyvilla.data.model.user.UserProfile
import com.jp.foodyvilla.presentation.utils.UiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepository(private val supabase: SupabaseClient) {

     fun getCurrentUserProfile(): Flow<UiState<UserProfile>> = flow {
        val session = supabase.auth.currentSessionOrNull()


        if(session == null){
            emit(UiState.Error(Exception("user not logged in")))
            return@flow
        }
        val user = session.user
        val authUserId = user?.id

        if(authUserId == null){
            emit(UiState.Error(Exception("user not logged in")))
            return@flow
        }
        val response = supabase.postgrest["users"]
            .select {
                filter {
                    eq("auth_user_id", authUserId)
                }
            }
            .decodeSingleOrNull<UserProfile>()

        println("User $response")
         if(response == null){
             emit(UiState.Error(Exception("User Not found")))
             return@flow
         }
       emit(UiState.Success(response!!))
    }


    suspend fun updateFcmToken(
        fcmToken: String
    ) {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User not logged in")

        try {
            supabase.from("users").update(
                {
                    set("fcm_token", fcmToken)
                }
            ) {
                filter {
                    eq("auth_user_id", userId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}