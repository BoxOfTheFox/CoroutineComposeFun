package com.example.mycomposeapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.mycomposeapplication.data.Card
import com.example.mycomposeapplication.data.MainNode
import com.example.mycomposeapplication.data.Node


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MainNode::class.java.name,
        modifier = modifier.fillMaxSize()
    ) {
        mainGraph(navController)
    }
}

private fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(startDestination = "home", route = MainNode::class.java.name) {
        composable(route = "home") {
            MainScreen(MainNode) {
                navController.navigate(it)
            }
        }
        composable(route = "${MainNode::class.java.name}/{card}") {
            it.arguments?.getString("card")?.let(MainNode::getCard)?.let {
                MainScreen(it) {
                    navController.navigate(it)
                }
            }
        }
    }
}

private fun Card.getCard(name: String): Card? = when {
    this::class.java.name == name -> this
    this is Node -> cards.firstNotNullOfOrNull { it.getCard(name) }
    else -> null
}
