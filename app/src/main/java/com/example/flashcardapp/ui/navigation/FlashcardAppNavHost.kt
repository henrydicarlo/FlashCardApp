package com.example.flashcardapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.flashcardapp.ui.screens.DeckListScreen
import com.example.flashcardapp.ui.screens.DeckDetailScreen
import com.example.flashcardapp.ui.screens.StudySessionScreen
import com.example.flashcardapp.ui.screens.FlashcardCreateScreen
import com.example.flashcardapp.ui.screens.LocationScreen
import com.example.flashcardapp.ui.screens.StatsScreen
import com.example.flashcardapp.ui.viewmodel.FlashcardAppViewModel

@Composable
fun FlashcardAppNavHost(
    navController: NavHostController,
    viewModel: FlashcardAppViewModel
) {
    NavHost(navController = navController, startDestination = "decks") {
        composable("decks") {
            DeckListScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = "deck/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: -1L
            DeckDetailScreen(
                deckId = deckId,
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = "study/{deckId}?locationBased={locationBased}",
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("locationBased") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: -1L
            val locationBased = backStackEntry.arguments?.getBoolean("locationBased") ?: false

            StudySessionScreen(
                deckId = deckId,
                locationBased = locationBased,
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = "create_flashcard/{deckId}/{type}",
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: -1L
            val type = backStackEntry.arguments?.getString("type") ?: "BASIC"

            FlashcardCreateScreen(
                deckId = deckId,
                type = type,
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("locations") {
            LocationScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("stats") {
            StatsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}