package com.example.mycomposeapplication.data

object MainNode: Card, Node {
    override val title = "Compose and Coroutines Fun"
    override val description = """
        This application was created to learn basics of Compose and Coroutines.
        Compose code doesn't follow good programming practices and was made more as an experiment to
        see Compose possibilities. Coroutines examples and explanations are based on Programming Android with Kotlin
        - Achieving Structured Concurrency with Coroutines by Pierre-Olivier Laurence et al.
    """.trimIndent().replace('\n', ' ')
    override val shortDescription = ""
    override val cards: List<Card> = listOf(
        CoroutineConcepts,
        StructuredConcurrencyNode,
        ChannelsNode,
        FlowsNode
    )
}
