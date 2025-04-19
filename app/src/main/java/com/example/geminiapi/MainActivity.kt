package com.example.geminiapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.geminiapi.ui.theme.GeminiAPITheme

enum class Screen {
    MAIN_MENU, BAKING, AI_AGENT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiAPITheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.MAIN_MENU) }
                    
                    when (currentScreen) {
                        Screen.MAIN_MENU -> MainMenuScreen(
                            onNavigateToBaking = { currentScreen = Screen.BAKING },
                            onNavigateToAIAgent = { currentScreen = Screen.AI_AGENT }
                        )
                        Screen.BAKING -> BakingScreen(
                            onNavigateToAIAgent = { currentScreen = Screen.AI_AGENT }
                        )
                        Screen.AI_AGENT -> AIAgentScreen(
                            onNavigateToBaking = { currentScreen = Screen.BAKING }
                        )
                    }
                }
            }
        }
    }
}