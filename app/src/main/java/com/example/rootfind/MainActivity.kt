package com.example.rootfind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.rootfind.ui.home.RootFindViewModel
import com.example.rootfind.ui.navigation.RootFindNavHost
import com.example.rootfind.ui.theme.RootFindTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RootFindViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            RootFindTheme(
                appTheme = uiState.settings.theme,
                dynamicColor = uiState.settings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootFindNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
