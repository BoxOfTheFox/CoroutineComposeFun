package com.example.mycomposeapplication

/**
 * Contract for information needed on every navigation destination
 */
interface AppDestination {
    operator fun div(appDestination: AppDestination) = object : AppDestination {
        override val route = "${this@AppDestination.route}/${appDestination.route}"
    }

    val route: String
}

/**
 * App navigation destinations
 */
object Main : AppDestination {
    override val route = "main"
}

object Cancellation : AppDestination {
    override val route = "cancellation"
}

object ExceptionHandling : AppDestination {
    override val route = "exception_handling"
}

object Detail : AppDestination {
    override val route = "detail"

}