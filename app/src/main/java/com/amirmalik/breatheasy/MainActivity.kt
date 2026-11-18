package com.amirmalik.breatheasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amirmalik.breatheasy.ui.BreatheEasyApp
import com.amirmalik.breatheasy.ui.BreatheEasyViewModel
import com.amirmalik.breatheasy.ui.theme.BreatheEasyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BreatheEasyTheme {
                val appViewModel: BreatheEasyViewModel = viewModel()
                BreatheEasyApp(viewModel = appViewModel)
            }
        }
    }
}
