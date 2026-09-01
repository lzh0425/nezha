package com.example.nezhadashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.nezhadashboard.ui.DashboardScreen
import com.example.nezhadashboard.ui.NezhaViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NezhaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen(viewModel = viewModel)
        }
    }
}
