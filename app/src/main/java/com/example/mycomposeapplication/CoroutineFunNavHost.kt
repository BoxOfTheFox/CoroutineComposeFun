package com.example.mycomposeapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.mycomposeapplication.ui.detail.*
import com.example.mycomposeapplication.ui.mainsections.CancellationSectionViewModel
import com.example.mycomposeapplication.ui.mainsections.ExceptionHandlingSectionViewModel
import com.example.mycomposeapplication.ui.mainsections.MainSectionViewModel
import com.example.mycomposeapplication.ui.mainsections.MainSectionsScreen

@Composable
fun CoroutineFunNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Main.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(route = Main.route) {
            val viewModel: MainSectionViewModel = viewModel()
            MainSectionsScreen(viewModel) {
                navController.navigate(it)
            }
        }
        cancellationGraph(navController)
        exceptionHandlingGraph(navController)
    }
}

@Composable
private fun String.createViewModel() = when(this){
    CancellationViewModel.name -> {
        val tmp: CancellationViewModel = viewModel()
        tmp
    }
    HandledLaunchCEHViewModel.name -> {
        val tmp: HandledLaunchCEHViewModel = viewModel()
        tmp
    }
    UnhandledLaunchCEHViewModel.name -> {
        val tmp: UnhandledLaunchCEHViewModel = viewModel()
        tmp
    }
    UnhandledAsyncCEHViewModel.name -> {
        val tmp: UnhandledAsyncCEHViewModel = viewModel()
        tmp
    }
    ExposedAsyncTryCatchViewModel.name -> {
        val tmp: ExposedAsyncTryCatchViewModel = viewModel()
        tmp
    }
    else -> throw Exception()
}

private fun NavGraphBuilder.exceptionHandlingGraph(navController: NavHostController) {
    navigation(startDestination = "home", route = ExceptionHandling.route) {
        composable(route = "home") {
            val viewModel: ExceptionHandlingSectionViewModel = viewModel()
            MainSectionsScreen(viewModel) {
                navController.navigate(it)
            }
        }
        composable(route = (ExceptionHandling / Detail / DetailArgument).route) {
            it.arguments?.getString(DetailArgument.argument)?.let {
                DetailScreen(it, it.createViewModel())
            }
        }
    }
}

private fun NavGraphBuilder.cancellationGraph(navController: NavHostController) {
    navigation(startDestination = "home", route = Cancellation.route) {
        composable(route = "home") {
            val viewModel: CancellationSectionViewModel = viewModel()
            MainSectionsScreen(viewModel) {
                navController.navigate(it)
            }
        }
        composable(route = (Cancellation / Detail / DetailArgument).route) {
            it.arguments?.getString(DetailArgument.argument)?.let {
                DetailScreen(it, it.createViewModel())
            }
        }
    }
}