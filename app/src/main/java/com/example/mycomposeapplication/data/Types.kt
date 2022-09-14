package com.example.mycomposeapplication.data

import kotlinx.coroutines.CoroutineScope

interface Navigation {
    val route: String
}

interface Parent: Navigation {
    val cards: List<Card>
}

interface Screen {
    val title: String
    val description: String
}

interface Card: Screen, Navigation {
    val shortDescription: String
}

interface Example {
    fun execute(scope: CoroutineScope, log: (String) -> Unit)
}

data class CardMetadata(
    val title: String,
    val subtitle: String,
    val appDestination: String
)