package com.example.flashcardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flashcardapp.screens.AddDeckScreen
import com.example.flashcardapp.screens.AddFlashCardScreen
import com.example.flashcardapp.screens.DashboardScreen
import com.example.flashcardapp.ui.theme.FlashCardAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlashCardAppTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { DashboardScreen(navController) }
                        composable("add_deck") { AddDeckScreen(navController) }
                        composable("add_flashcard") { AddFlashCardScreen(navController) }
                    }
                }
            }
        }
    }
}



//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MaterialTheme {
//                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
//                    DashboardScreen()
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    FlashCardAppTheme {
//        Greeting("Android")
//    }
//}