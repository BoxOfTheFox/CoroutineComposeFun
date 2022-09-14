package com.example.mycomposeapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mycomposeapplication.data.Main
import com.example.mycomposeapplication.data.Parent
import com.example.mycomposeapplication.data.Screen


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Main.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(route = Main.route) {
            MainScreen(Main) {
                navController.navigate(it)
            }
        }
        composable(
            route = "${Main.route}/{chapter}?example={example}",
            arguments = listOf(navArgument("example") { nullable = true })
        ) {
            val chapter = it.arguments?.getString("chapter")
            val example = it.arguments?.getString("example")
            chapter?.getScreen(example)?.let {
                MainScreen(it) {
                    navController.navigate(it)
                }
            }
        }
    }
}

private fun String.getScreen(example: String?): Screen? {
    val card = Main.cards.find { this == it.route }
    return if (example != null && card is Parent) {
        card.cards.find { it.route == example }
    } else {
        card
    }
}