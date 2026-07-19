package com.jp.foodyvilla.data.repo

import com.jp.foodyvilla.data.model.Category
import com.jp.foodyvilla.data.model.Outlet
import com.jp.foodyvilla.data.model.OutletMenuItem
import com.jp.foodyvilla.data.model.Review
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepo(private val client: SupabaseClient) {

    fun getOutletsWithMenu(): Flow<List<Outlet>> = flow {
        try {
            val res = client
                .from("outlets")
                .select(
                    Columns.raw(
                        """
            *,
            outlet_menu_items(
                *,
                product_catalog(
                    *,
                    categories!inner(*)
                )
            )
            """.trimIndent()
                    )
                ) {
                    filter {
                        eq("is_active", true)
                        eq("outlet_menu_items.product_catalog.categories.is_active", true)
                    }
                }
                .decodeList<Outlet>()


            emit(res)
        } catch (e: Exception) {

            println(" product fetch error $e")
            emit(emptyList())
        }
    }

    fun getCategories(): Flow<List<com.jp.foodyvilla.data.model.Category>> = flow {
        try {
            val res = client
                .from("categories")
                .select {
                    filter {
                        eq("is_active", true)
                    }
                }
                .decodeList<com.jp.foodyvilla.data.model.Category>()
            emit(res)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getOutletById(id: Long): Flow<Outlet?> = flow {
        try {
            val res = client
                .from("outlets")
                .select(
                    Columns.raw(
                        """
                        *,
                        menu_items:outlet_menu_items (*, product_catalog (*, categories!inner(*)))
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("id", id)
                        eq("is_active", true)
                        eq("outlet_menu_items.product_catalog.categories.is_active", true)
                    }
                }
                .decodeSingleOrNull<Outlet>()

            emit(res)
        } catch (e: Exception) {
            emit(null)
        }
    }
    
    fun getProductReviews(productId: Long): Flow<List<Review>> = flow {
        try {
            val res = client
                .from("reviews")
                .select {
                    filter {
                        eq("product_id", productId)
                    }
                }
                .decodeList<Review>()
            emit(res)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getProductById(id: Long): Flow<OutletMenuItem?> = flow {
        try {
            val res = client
                .from("outlet_menu_items")
                .select(
                    Columns.raw(
                        """
                        *,
                        product_catalog (*, categories!inner(*)),
                        outlets (*)
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("id", id)
                        eq("product_catalog.categories.is_active", true)
                    }
                }
                .decodeSingleOrNull<OutletMenuItem>()

            if (res?.outlets?.is_active == true) {
                emit(res)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }
}
