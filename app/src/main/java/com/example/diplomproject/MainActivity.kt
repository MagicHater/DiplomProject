package com.example.diplomproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.diplomproject.navigation.AppNavHost
import com.example.diplomproject.ui.theme.DiplomProjectTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiplomProjectTheme {
                AppNavHost()
            }
        }
    }
}
