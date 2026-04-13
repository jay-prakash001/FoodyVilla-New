package com.jp.foodyvilla.di

import androidx.lifecycle.viewmodel.compose.viewModel
import com.jp.foodyvilla.data.repo.OfferRepo
import com.jp.foodyvilla.data.repo.ProductRepo
import com.jp.foodyvilla.data.repo.ReviewRepository
import com.jp.foodyvilla.presentation.screens.detail.DetailViewModel
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.menu.MenuViewModel
import com.jp.foodyvilla.presentation.screens.offers.OffersViewModel
import com.jp.foodyvilla.presentation.screens.reviews.ReviewsViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.NonCancellable.get
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{

    single{
        createSupabaseClient(
            supabaseUrl = "https://mzeajzfhjovwyuotiywx.supabase.co",
            supabaseKey = "sb_publishable_C0Dz4fVE-_YjQIHLHqMbQQ_EWWuskzq"
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            httpEngine = OkHttp.create()
        }
    }


    single { OfferRepo(get()) }
    single { ProductRepo(get()) }
    single{ ReviewRepository(get()) }
    viewModel {
        HomeViewModel(get(), get())
    }
    viewModel{
        OffersViewModel(get())
    }

    viewModel{
        DetailViewModel(get())
    }

    viewModel{
        MenuViewModel(get())
    }

    viewModel{
        ReviewsViewModel(get())
    }

}