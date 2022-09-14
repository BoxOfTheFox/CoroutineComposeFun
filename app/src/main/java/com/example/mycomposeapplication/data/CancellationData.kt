package com.example.mycomposeapplication.data

import android.util.Log
import kotlinx.coroutines.*

object Cancellation: Navigation, Screen, Card, Parent {
    override val shortDescription = "wip description"
    override val route: String = this::class.java.simpleName
    override val cards: List<Card> = listOf(BasicCancellation)
    override val title = route
    override val description = ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(4)
}

object BasicCancellation : Navigation, Screen, Card, Example {
    override val route: String = this::class.java.simpleName
    override val title = "Basic cancellation"
    override val description = "Composem ipsum color sit lazy, padding theme elit, sed do bouncy.".repeat(4)
    override val shortDescription = "wip description"

    override fun execute(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val job = launch {
                try {
                    wasteCpu(log)
                } catch (e: CancellationException) {
                    withContext(Dispatchers.Main) {
                        log("yems")
                        Log.e("cancellation", "yems")
                    }
                }
            }
            delay(1200)
            withContext(Dispatchers.Main) {
                log("main: I'm going to cancel this job")
                Log.e("cancellation", "main: I'm going to cancel this job")
            }
            job.cancel()
            withContext(Dispatchers.Main) {
                log("main: Done")
                Log.e("cancellation", "main: Done")
            }
        }
    }

    private suspend fun wasteCpu(log: (String) -> Unit) = withContext(Dispatchers.Default) {
        var nextPrintTime = System.currentTimeMillis()
        while (isActive) {
            if (System.currentTimeMillis() >= nextPrintTime) {
                withContext(Dispatchers.Main) {
                    log("job: I'm working…")
                    Log.e("cancellation", "job: I'm working…")
                }
                nextPrintTime += 500
            }
        }
    }
}