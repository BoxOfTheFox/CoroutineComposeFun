package com.example.mycomposeapplication.data

object CoroutineConcepts: Navigation, Screen, Card, Parent {
    override val shortDescription = "wip description"
    override val route: String = this::class.java.simpleName
    override val cards: List<Card> = emptyList()
    override val title = route
    override val description = ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(4)
}