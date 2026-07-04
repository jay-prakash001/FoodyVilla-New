package com.jp.foodyvilla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.jp.foodyvilla.presentation.navigation.FoodyVillaNavGraph
import com.jp.foodyvilla.presentation.screens.home.HomeViewModel

import com.jp.foodyvilla.presentation.utils.HideSystemBars
import com.jp.foodyvilla.ui.theme.AppTheme
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlin.getValue

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Checkout.preload(applicationContext)
        enableEdgeToEdge()
        setContent {
            AppTheme(dynamicColor = false) {
                HideSystemBars()
//
////                MobileLoginScreen { }
//
////                OtpVerificationScreen {  }
                FoodyVillaNavGraph()
//            val context = LocalContext.current
//                GoogleSignInScreen()


//                CheckoutScreen(viewModel = viewModel)

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Checkout.clearUserData(this)
    }
    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {


        viewModel.onPaymentSuccess(
            razorpayPaymentId = razorpayPaymentId,
            razorpayOrderId = paymentData?.orderId,
            razorpaySignature = paymentData?.signature
        )
    }

    override fun onPaymentError(errorCode: Int, errorDescription: String?, p2: PaymentData?) {


        viewModel.onPaymentError(
            errorCode = errorCode,
            errorDescription = errorDescription ?: "Unknown error"
        )
    }
}
