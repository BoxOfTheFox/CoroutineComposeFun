package com.example.mycomposeapplication

/**
 * Contract for information needed on every navigation destination
 */
interface AppDestination {
    operator fun div(appDestination: AppDestination) = object : AppDestination {
        override val route = "${this@AppDestination.route}/${appDestination.route}"
    }

    operator fun div(argument: String) = object : AppDestination {
        override val route = "${this@AppDestination.route}/$argument"
    }

    operator fun div(argument: AppDestinationArgument) = object : AppDestination {
        override val route = "${this@AppDestination.route}/{${argument.argument}}"
    }

    val route: String
}

interface AppDestinationArgument {
    val argument: String
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

object DetailArgument: AppDestinationArgument {
    override val argument = "detailArgument"
}