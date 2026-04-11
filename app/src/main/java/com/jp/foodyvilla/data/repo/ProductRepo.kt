package com.jp.foodyvilla.data.repo

import com.jp.foodyvilla.data.model.FoodItem
import com.jp.foodyvilla.data.model.OfferFood
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
class ProductRepo(private val client : SupabaseClient) {

    fun getProducts(): Flow<List<FoodItem>> = flow {
        try {
            val res = client
                .from("products")
                .select()
                .decodeList<FoodItem>()

            emit(res)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun getProductById(id: Int): Flow<FoodItem?> = flow {
        try {
            val res = client
                .from("products")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<FoodItem>()

            emit(res)

        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }
}