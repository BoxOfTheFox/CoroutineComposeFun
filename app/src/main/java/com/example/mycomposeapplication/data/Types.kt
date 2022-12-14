package com.example.mycomposeapplication.data

import kotlinx.coroutines.CoroutineScope

interface Node {
    val cards: List<Card>
}

interface Example {
    val code: Int
    operator fun invoke(scope: CoroutineScope, log: (String) -> Unit)
}

interface Card {
    val title: String
    val shortDescription: String
    val description: String
}
