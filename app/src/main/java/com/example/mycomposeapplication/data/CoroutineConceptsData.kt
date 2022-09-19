package com.example.mycomposeapplication.data

import kotlinx.coroutines.*

// todo figure out how to show Continuation
object CoroutineConcepts: Card, Node {
    override val shortDescription = "Blocks of code that can be dispatched to threads that are nonblocking"
    override val cards: List<Card> = listOf(YourFirstCoroutine, AsyncCoroutineBuilder, CoroutineScopeAndContext, SuspendingFunction)
    override val title = "Coroutine Concepts"
    override val description = """
        Coroutine-enabled primitives allow developers to write sequential, asynchronous code at low
        cost. The design of coroutines comprises suspending functions, structured concurrency, and
        other specific considerations like coroutine context and coroutine scope.
    """.trimIndent().replace('\n',' ')
}

object YourFirstCoroutine : Card, Example {
    override val title = "Your First Coroutine"
    override val description = """
        - The launch coroutine builder is "fire-and-forget" work - in other words, there is no result to return
        - Once called, it immediately returns a Job instance, and starts a new coroutine. A Job represents the coroutine itself, like a handle on its lifecycle. The coroutine can be cancelled by calling the cancel method on its Job instance.
        - A coroutine that is started with launch will not return result, but rather, a reference to the background job
    """.trimIndent()
    override val shortDescription = "Simple coroutine example"

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val job: Job = launch {
                var i = 0
                while (true) {
                    log("$i I'm working")
                    i++
                    delay(10)
                }
            }

            delay(30)
            job.cancel()
            log("job cancelled")
        }
    }
}

object AsyncCoroutineBuilder : Card, Example {
    override val title = "The async Coroutine Builder"
    override val description = """
        - The async coroutine builder is intended for parallel decomposition of work - that is, you explicitly specify that some tasks run concurrently
        - Once called, an async immediately returns a Deferred instance. Deferred is a specialized Job, with a few extra methods like await. It's a Job with a return value
        - Very similarly to Futures and Promises, you invoke the await method on the Deferred instance to get the return value
    """.trimIndent()
    override val shortDescription = "The async coroutine builder can be compared to Java's Future/Promise model"

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val slow: Deferred<Int> = async {
                log("Call for slow")
                var result = 0
                delay(1000)
                for (i in 1..10)
                    result += 1
                log("Call complete for slow: $result")
                result
            }

            val quick: Deferred<Int> = async {
                log("Call for quick")
                delay(1000)
                log("Call complete for quick: 5")
                5
            }

            val result: Int = quick.await() + slow.await()
            log("$result")
        }
    }
}

object CoroutineScopeAndContext : Card, Example {
    override val title = "Scope, Context and Dispatcher"
    override val description = "\tThe context of a newly created coroutine started with launch or async, " +
            "the coroutine context, inherits from the scope context and from the context passed in as " +
            "a parameter (the supplied context) - the latter taking precedence over the former.\n" +
            "\tDispatcher dispatches coroutines on a specific thread or thread pool. By providing a " +
            "dispatcher context, you can easily designate where logic flow executes. By default " +
            "there are four Dispatchers available out of the box:\n" +
            "- Dispatchers.Main - This uses the main thread, or the UI thread, of the platform you're using\n" +
            "- Dispatchers.Default - This is meant for CPU-bound tasks, and is backed by a thread pool of four threads by default\n" +
            "- Dispatchers.IO - This is meant for IO-bound tasks, and is backed by a thread pool of 64 threads by default" +
            "- Dispatchers.Unconfined - This isn't something you should use or even need as you're learning coroutines. " +
            "It's primarily used on the internals of the coroutines library"
    override val shortDescription = "Scope is a container for a Context, Dispatcher dispatches coroutines"

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            launch(Dispatchers.Main) {
                log("I'm executing in ${Thread.currentThread().name}")
            }
            launch(Dispatchers.Default) {
                log("I'm executing in ${Thread.currentThread().name}")
            }
        }
    }
}

object SuspendingFunction : Card, Example {
    override val title = "Suspending Function"
    override val description = "A suspending function denotes a function which might not return " +
            "immediately. Using withContext and the appropriate Dispatcher, any blocking function " +
            "can be turned into nonblocking suspending function."
    override val shortDescription = "A suspending function denotes a function which might not return immediately"

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val profile = fetchProfile("profileId", log)
            loadProfile(profile, log)
        }
    }

    private suspend fun fetchProfile(id: String, log: (String) -> Unit) = withContext(Dispatchers.IO) {
        log("fetching profile with id: $id")
        Profile(id)
    }

    private fun loadProfile(profile: Profile, log: (String) -> Unit) {
        log("loading profile: $profile")
    }

    data class Profile(val id: String)
}