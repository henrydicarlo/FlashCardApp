package com.example.flashcardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.flashcardapp.ui.navigation.FlashcardAppNavHost
import com.example.flashcardapp.ui.theme.FlashCardAppTheme
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FlashcardAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FlashCardAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    FlashcardAppNavHost(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}