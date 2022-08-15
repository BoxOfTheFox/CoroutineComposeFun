package com.example.mycomposeapplication

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.mycomposeapplication.data.CardMetadata
import com.example.mycomposeapplication.ui.detail.DetailScreen
import com.example.mycomposeapplication.ui.mainsections.MainSectionsScreen

@Composable
fun CoroutineFunNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Main.route,
        modifier = modifier
    ) {
        composable(route = Main.route) {
            MainSectionsScreen(CardMetadata.chapterCards) {
                navController.navigate(it)
            }
        }
        cancellationGraph(navController)
        exceptionHandlingGraph(navController)
    }
}

private fun NavGraphBuilder.exceptionHandlingGraph(navController: NavHostController) {
    navigation(startDestination = "home", route = ExceptionHandling.route) {
        composable(route = "home") {
            MainSectionsScreen(CardMetadata.exceptionHandlingCards) {
                navController.navigate(it)
            }
        }
        composable(route = (ExceptionHandling / Detail).route) {
            DetailScreen("ExceptionHandling details")
        }
    }
}

private fun NavGraphBuilder.cancellationGraph(navController: NavHostController) {
    navigation(startDestination = "home", route = Cancellation.route) {
        composable(route = "home") {
            MainSectionsScreen(CardMetadata.cancellationCards) {
                navController.navigate(it)
            }
        }
        composable(route = (Cancellation / Detail).route) {
            DetailScreen("Cancellation details")
        }
    }
}