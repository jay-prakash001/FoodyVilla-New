package com.jp.foodyvilla.di

import androidx.lifecycle.viewmodel.compose.viewModel
import com.jp.foodyvilla.data.repo.AuthRepo
import com.jp.foodyvilla.data.repo.CartRepository
import com.jp.foodyvilla.data.repo.LocationRepository
import com.jp.foodyvilla.data.repo.OfferRepo
import com.jp.foodyvilla.data.repo.OrderRepository
import com.jp.foodyvilla.data.repo.ProductRepo
import com.jp.foodyvilla.data.repo.ReviewRepository
import com.jp.foodyvilla.data.repo.UserRepository
import com.jp.foodyvilla.presentation.screens.detail.DetailViewModel
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla.presentation.screens.login.LoginViewModel
import com.jp.foodyvilla.presentation.screens.menu.MenuViewModel
import com.jp.foodyvilla.presentation.screens.offers.OffersViewModel
import com.jp.foodyvilla.presentation.screens.reviews.ReviewsViewModel
import com.russhwolf.settings.Settings
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.NonCancellable.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val appModule = module{

    single {
        createSupabaseClient(
            supabaseUrl = "https://mzeajzfhjovwyuotiywx.supabase.co",
            supabaseKey = "sb_publishable_C0Dz4fVE-_YjQIHLHqMbQQ_EWWuskzq"
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Functions)
            install(Realtime)


            httpEngine = OkHttp.create()
        }
    }


    single { OfferRepo(get()) }
    single { ProductRepo(get()) }
    single{ ReviewRepository(get()) }
    single{ AuthRepo(get(), androidContext()) }
    single { UserRepository(get()) }
    single{ CartRepository(get()) }
    single{ OrderRepository(get()) }
    single{ LocationRepository(androidContext()) }
    viewModel {
        HomeViewModel(get(), get(), get(),get(), get())
    }
    viewModel{
        OffersViewModel(get())
    }

    viewModel{
        DetailViewModel(get(),get())
    }

    viewModel{
        MenuViewModel(get())
    }

    viewModel{
        ReviewsViewModel(get())
    }

    viewModel{
        LoginViewModel(get(), get())
    }

}