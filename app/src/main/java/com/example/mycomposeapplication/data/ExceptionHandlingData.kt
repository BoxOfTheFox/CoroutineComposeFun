package com.example.mycomposeapplication.data

import android.util.Log
import kotlinx.coroutines.*

object ExceptionHandling: Navigation, Screen, Card, Parent {
    override val shortDescription = "wip description"
    override val route: String = this::class.java.simpleName
    override val cards: List<Card> = listOf(HandledLaunchCEH, UnhandledLaunchCEH, UnhandledAsyncCEH, ExposedAsyncTryCatch)
    override val title = route
    override val description = ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(4)
}

/**
 * Handles launch exception with CEH in direct child of supervisorScope
 */
object HandledLaunchCEH: Navigation, Screen, Card, Example {
    override val shortDescription = "wip description"
    override val title = "Handled"
    override val description = ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(4)
    override val route: String = this::class.java.simpleName

    override fun execute(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch{
            log("Can't log in real time, will show logs when finished")
            val dangerousWorkaround = mutableListOf<String>()
            delay(100)
            runBlocking {
                val ceh = CoroutineExceptionHandler { _, throwable ->
                    Log.e("handledLaunchCEH", "CEH handle $throwable")
                    dangerousWorkaround.add("CEH handle $throwable")
                }

                val scope = CoroutineScope(Job())

                val job = scope.launch {
                    supervisorScope {
                        val task1 = launch {
                            delay(1000)
                            Log.e("handledLaunchCEH", "Done background task")
                            dangerousWorkaround.add("Done background task")
                        }

                        val task2 = launch(ceh) {
                            throw Exception()
                        }

                        task1.join()
                        task2.join()
                    }
                }

                job.join()
                Log.e("handledLaunchCEH", "Program ends")
                dangerousWorkaround.add("Program ends")
            }
            dangerousWorkaround.forEach { log(it) }
        }
    }
}

/**
 * Handles launch exception with CEH but fail stops all children
 */
object UnhandledLaunchCEH: Navigation, Screen, Card, Example {
    override val shortDescription = "wip description"
    override val title = "Unhandled launch"
    override val description = ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(4)
    override val route: String = this::class.java.simpleName

    override fun execute(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch{
            log("Can't log in real time, will show logs when finished")
            val dangerousWorkaround = mutableListOf<String>()
            delay(100)
            runBlocking {
                val ceh = CoroutineExceptionHandler { _, throwable ->
                    Log.e("unhandledLaunchCEH", "CEH handle $throwable")
                    dangerousWorkaround.add("CEH handle $throwable")
                }

                val scope = CoroutineScope(Job() + ceh)

                val job = scope.launch {
                    coroutineScope {
                        val task1 = launch {
                            delay(1000)
                            Log.e("unhandledLaunchCEH", "Done background task")
                            dangerousWorkaround.add("Done background task")
                        }

                        val task2 = launch {
                            throw Exception()
                        }

                        task1.join()
                        task2.join()
                    }
                }

                job.join()
                Log.e("unhandledLaunchCEH", "Program ends")
                dangerousWorkaround.add("Program ends")
            }
            dangerousWorkaround.forEach { log(it) }
        }
    }
}

/**
 * Handles async silent exception but fail stops all children
 */
object UnhandledAsyncCEH: Navigation, Screen, Card, Example {
    override val shortDescription = "wip description"
    override val title = "Unhandled async"
    override val description =
        ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(4)
    override val route: String = this::class.java.simpleName

    override fun execute(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            log("Can't log in real time, will show logs when finished")
            val dangerousWorkaround = mutableListOf<String>()
            delay(100)
            runBlocking {
                val ceh = CoroutineExceptionHandler { _, throwable ->
                    Log.e("unhandledAsyncCEH", "CEH handle $throwable")
                    dangerousWorkaround.add("CEH handle $throwable")
                }

                // ceh could be passed here
                val scope = CoroutineScope(Job())

                val job = scope.launch(ceh) {
                    coroutineScope {
                        val task1 = launch {
                            delay(1000)
                            Log.e("unhandledAsyncCEH", "Done background task")
                            dangerousWorkaround.add("Done background task")
                        }

                        val task2 = async {
                            throw Exception()
                            1
                        }

                        task1.join()
                    }
                }

                job.join()
                Log.e("unhandledAsyncCEH", "Program ends")
                dangerousWorkaround.add("Program ends")
            }
            dangerousWorkaround.forEach { log(it) }
        }
    }
}

/**
 * Handles exposed exception with supervisorScope and try-catch
 */
object ExposedAsyncTryCatch: Navigation, Screen, Card, Example {
    override val shortDescription = "wip description"
    override val title = "Exposed"
    override val description =
        ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(4)
    override val route: String = this::class.java.simpleName

    override fun execute(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            log("Can't log in real time, will show logs when finished")
            val dangerousWorkaround = mutableListOf<String>()
            delay(100)
            runBlocking {
                // ceh could be passed here
                val scope = CoroutineScope(Job())

                val job = scope.launch {
                    supervisorScope {
                        val task1 = launch {
                            delay(1000)
                            Log.e("exposedAsyncTryCatch", "Done background task")
                            dangerousWorkaround.add("Done background task")
                        }

                        val task2 = async {
                            throw Exception()
                            1
                        }

                        try {
                            task2.await()
                        }catch (e: Exception){
                            Log.e("exposedAsyncTryCatch", "Caught exception &$e")
                            dangerousWorkaround.add("Caught exception &$e")
                        }

                        task1.join()
                    }
                }

                job.join()
                Log.e("exposedAsyncTryCatch", "Program ends")
                dangerousWorkaround.add("Program ends")
            }
            dangerousWorkaround.forEach { log(it) }
        }
    }
}
