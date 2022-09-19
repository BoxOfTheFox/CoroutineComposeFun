package com.example.mycomposeapplication.data

import kotlinx.coroutines.*

object StructuredConcurrencyNode : Card, Node {
    override val shortDescription = "Designed cancellation and failure cancellation"
    override val cards: List<Card> = listOf(
        BasicCancellationExample,
        CoroutineExceptionHandlerExample,
        SupervisorJobExample,
        SupervisorScopeExample,
        ExposedExample,
        UnhandledExample
    )
    override val title = "Structured Concurrency"
    override val description =
        "- Designed cancellation - For example, a task that's cancelled after" +
                " a user taps a \"Cancel\" button in a custom or arbitrary UI.\n" +
                "- Failure cancellation - For example, a cancellation that's caused by exceptions, " +
                "either intentionally (thrown) or unexpectedly (unhandled)."
}

object BasicCancellationExample : Card, Example {
    override val title = "Basic cancellation"
    override val description = "\tA coroutine can be deliberately cancelled using Job.cancel() " +
            "for launch, or Deferred.cancel() for async. If you need to call some suspending " +
            "functions inside your cleanup code, make sure you wrap your cleanup logic inside " +
            "withContext(Non Cancellable) { ... } block. The cancelled coroutine will remain in " +
            "the cancelling state until the cleanup exits. After the cleanup is done, the " +
            "aforementioned coroutine goes to the cancelled state.\n" +
            "\tA coroutine always waits for its children to complete before completing itself. " +
            "So cancelling a coroutine also cancels all of its children."
    override val shortDescription = "A coroutine can be deliberately cancelled using cancel()"

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val job = launch {
                try {
                    wasteCpu(log)
                } catch (e: CancellationException) {
                    log("caught CancellationException")
                }
            }
            delay(1200)
            log("I'm going to cancel this job")
            job.cancel()
            log("Done")
        }
    }

    private suspend fun wasteCpu(log: (String) -> Unit) = withContext(Dispatchers.Default) {
        var nextPrintTime = System.currentTimeMillis()
        while (isActive) {
            if (System.currentTimeMillis() >= nextPrintTime) {
                withContext(Dispatchers.Main) {
                    log("job: I'm working…")
                }
                nextPrintTime += 500
            }
        }
    }
}

object CoroutineExceptionHandlerExample: Card, Example {
    override val title = "Coroutine Exception Handler"
    override val description = "A CoroutineExceptionHandler is conceptually very similar to " +
            "Thread.UncaughtExceptionHandler - except it's intended for coroutines. It's Context " +
            "element, which should be added to the context of a scope or a coroutine. The scope " +
            "should create its own Job instance, as a CEH only takes effect when installed at " +
            "the top of a coroutine hierarchy."
    override val shortDescription = "Prevents the failure of scope to propagate to the main coroutine"

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val ceh = CoroutineExceptionHandler { _, e -> log("Caught original $e") }
            val scope = CoroutineScope(coroutineContext + ceh + Job())

            log("Start")
            val job = scope.launch {
                launch {
                    try {
                        delay(Long.MAX_VALUE)
                    } catch (e: CancellationException) {
                        log("Child 1 was cancelled")
                    }
                }

                launch {
                    delay(1000)
                    throw Exception()
                }
            }

            job.join()
        }
    }
}

object SupervisorJobExample: Card, Example {
    override val shortDescription = "Failure or cancellation of a child doesn't affect other children"
    override val title = "Supervisor Job"
    override val description = "SupervisorJob is a Job for which the failure or cancellation of " +
            "a child doesn't affect other children; nor does it affect the scope itself. A " +
            "SupervisorJob is typically used as a drop-in replacement for Job when building a " +
            "CoroutineScope. The resulting scope is then called a \"supervisor scope\". Such a " +
            "scope propagates cancellation downward only. "

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val ceh = CoroutineExceptionHandler { _, e -> log("CEH handle $e") }
            val supervisor = SupervisorJob()
            val scope = CoroutineScope(coroutineContext + ceh + supervisor)
            with(scope) {
                val firstChild = launch {
                    log("First child is failing")
                    throw AssertionError("First child is cancelled")
                }

                val secondChild = launch {
                    firstChild.join()

                    delay(10)
                    log("First child is cancelled: ${firstChild.isCancelled}, but second one is still active")
                }

                secondChild.join()
            }
        }
    }
}

object SupervisorScopeExample: Card, Example {
    override val shortDescription = "Supervisor Scope creates a Supervisor Job"
    override val title = "Supervisor Scope"
    override val description = "Similarly to coroutineScope builder - which inherits the current " +
            "context and creates a new Job - supervisorScope creates a SupervisorJob. Just like " +
            "coroutineScope, it waits for all children to complete. One crucial difference with " +
            "coroutineScope is that it only propagates cancellation downward, and cancels all " +
            "children only if it has failed itself."

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val ceh = CoroutineExceptionHandler { _, e -> log("CEH handle $e") }

            supervisorScope {
                val firstChild = launch(ceh) {
                    log("First child is failing")
                    throw AssertionError("First child is cancelled")
                }

                val secondChild = launch {
                    firstChild.join()

                    delay(10)
                    log("First child is cancelled: ${firstChild.isCancelled}, but second one is still active")
                }

                secondChild.join()
            }
        }
    }
}

object ExposedExample: Card, Example {
    override val shortDescription = "Handles exposed exception with supervisorScope and try-catch"
    override val title = "Exposed"
    override val description = "\tInside a supervisorScope, async exposes uncaught exceptions in " +
            "the await call. If you don't surround the await call with a try/catch block, then " +
            "the scope of supervisorScope fails and cancels task1, then exposes to its parent " +
            "the exception that caused failure. So this means that even when using a " +
            "supervisorScope, unhandled exceptions in a scope lead to the cancellation of the " +
            "entire coroutine hierarchy beneath that scope - and the exception is propagated up." +
            "\n\tWithout supervisorScope even if you don't call task2.await(), the program still " +
            "crashes because coroutineScope fails and exposes to its parent the exception that " +
            "caused failure. Then scope.launch treats this exception as unhandled."

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val job = CoroutineScope(Job()).launch {
                supervisorScope {
                    val task1 = launch {
                        delay(1000)
                        log("Done background task")
                    }

                    val task2 = async {
                        throw Exception()
                        1
                    }

                    try {
                        task2.await()
                    } catch (e: Exception) {
                        log("Caught exception $e")
                    }

                    task1.join()
                }
            }

            job.join()
            log("Program ends")
        }
    }
}

object UnhandledExample: Card, Example {
    override val shortDescription = "Handles launch exception with CEH but fail stops all children"
    override val title = "Unhandled"
    override val description = "\tThe coroutine framework treats unhandled exceptions in a " +
            "specific way: it tries to use a CEH if the coroutine context has one. If not, it " +
            "delegates to the global handler. This handler calls a customizable set of CEH and " +
            "calls the standard mechanism of unhandled exceptions: Thread.uncaughtExceptionHandler." +
            "\n\tHow you could handle this exception? Since coroutineScope exposes exceptions, " +
            "you could wrap coroutineScope inside a try/catch statement. Alternatively, if you " +
            "don't handle it correctly, the preceding coroutineSCope, scope.launch treats this " +
            "exception as unhandled. Then your last chance to handle this exception is to " +
            "register a CEH. There are at least two reasons you would do that: first, to stop " +
            "the exception's propagation and avoid a program crash; and second, to notify your " +
            "crash analytics and rethrow the exception - potentially making the application " +
            "crash. In any case, we're not advocating for silently catching exceptions. If you " +
            "do want to use CEH, there are a couple of things you should know. A CEH only works " +
            "when registered to:\n\t- launch (not async) when launch is root coroutine builder" +
            "\n\t- A scope\n\t- supervisorScope direct child"

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val ceh = CoroutineExceptionHandler { _, e -> log("CEH handle $e") }

            val job = CoroutineScope(Job() + ceh).launch {
                coroutineScope {
                    val task1 = launch {
                        delay(1000)
                        log("Done background task")
                    }

                    val task2 = async {
                        throw Exception()
                        1
                    }

                    task1.join()
                }
            }

            job.join()
            log("Program ends")
        }
    }
}