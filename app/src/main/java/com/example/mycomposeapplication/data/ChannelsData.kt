package com.example.mycomposeapplication.data

import com.example.mycomposeapplication.R
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import kotlin.random.Random

// todo describe CSP
object ChannelsNode : Card, Node {
    override val cards: List<Card> = listOf(
        RendezvousChannelExample,
        IteratingOverChannelExample,
        UnlimitedChannelExample,
        ConflatedChannelExample,
        BufferedChannelExample,
        CommunicatingSequentialProcessesExample
    )
    override val title = "Channels"
    override val description = "A channel is a queue with suspending functions send and receive. " +
            "It also has nonsuspending counterparts: trySend and tryReceive. These two methods " +
            "are also nonblocking. trySend tries to immediately add an element to the channel, " +
            "and returns a wrapper class around the result. That wrapper class, ChannelResult<T>, " +
            "also indicates the success or the failure of the operation. tryReceive tries to " +
            "immediately retrieve an element from the channel, and returns a ChannelResult<T> instance."
    override val shortDescription = "A queue with suspending functions send and receive"
}

object RendezvousChannelExample : Card, Example {
    override val title = "Rendezvous Channel"
    override val description = "A rendezvous channel does not have any buffer at all. An element " +
            "is transferred from sender to receiver only when send and receive invocations meet " +
            "in time (rendezvous), so send suspends until another coroutine invokes receive, and " +
            "receive suspends until another coroutine invokes send."
    override val shortDescription = "A rendezvous channel does not have any buffer at all"
    override val code = R.raw.rendezvous_channel

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val channel = Channel<Item>()
            launch {
                channel.send(Item(1))
                channel.send(Item(2))
                log("Done sending")
            }

            log(channel.receive().toString())
            log(channel.receive().toString())

            log("Done!")
        }
    }
}

object IteratingOverChannelExample : Card, Example {
    override val title = "Iterating over Channel"
    override val description = "\tA Channel can be iterated over, using a regular for loop. Note " +
            "that since channels don't implement Iterable, you can't use forEach or other similar " +
            "functions from the Kotlin Standard Library.\n" +
            "\tImplicitly, x is equal to channel.receive() at each iteration. Consequently, a " +
            "coroutine iterating over a channel could do so indefinitely, unless it contains " +
            "conditional logic to break the loop. Fortunately, there's a standard mechanism to " +
            "break the loop - closing the channel.\n" +
            "\t\"Done sending\" appears before \"Done!\". This happens because the main coroutine " +
            "only leaves the channel iteration when channel is closed."
    override val shortDescription = "A Channel can be iterated over, using a regular for loop"
    override val code = R.raw.iterating_over_channel

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val channel = Channel<Item>()
            launch {
                channel.send(Item(1))
                channel.send(Item(2))
                log("Done sending")
                channel.close()
            }

            for (x in channel)
                log(x.toString())

            log("Done!")
        }
    }
}

data class Item(val number: Int)

object UnlimitedChannelExample : Card, Example {
    override val title = "Unlimited Channel"
    override val description = "An unlimited channel has a buffer that is only limited by the " +
            "amount of available memory. Senders to his channel never suspend, while receivers " +
            "only suspend when the channel is empty. Coroutines exchanging data via an unlimited " +
            "channel don't need to meet in time."
    override val shortDescription = "An unlimited channel has a buffer that is only limited by the amount of available memory"
    override val code = R.raw.unlimited_channel

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val channel = Channel<Int>(Channel.UNLIMITED)
            val childJob = launch(Dispatchers.IO) {
                log("Child executing from ${Thread.currentThread().name}")
                var i = 0
                while(isActive)
                    channel.send(i++)
                log("Child is done sending")
            }

            log("Parent executing from ${Thread.currentThread().name}")
            for (x in channel) {
                log(x.toString())

                // todo 1_000_000
                if (x == 1_000){
                    childJob.cancel()
                    break
                }
            }

            log("Done!")
        }
    }
}

object ConflatedChannelExample : Card, Example {
    override val title = "Conflated Channel"
    override val description = "\tThis channel has a buffer of size 1, and only keeps the last sent " +
            "element.\n" +
            "\tThe first sent element is \"one\". When \"two\" is sent, it replaces \"one\" in the " +
            "channel."
    override val shortDescription = "This channel has a buffer of size 1, and only keeps the last sent element"
    override val code = R.raw.conflated_channel

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val channel = Channel<String>(Channel.CONFLATED)
            val job = launch {
                channel.send("one")
                channel.send("two")
            }

            job.join()
            val elem = channel.receive()
            log("Last value was: $elem")
        }
    }
}

object BufferedChannelExample : Card, Example {
    override val title = "Buffered Channel"
    override val description = "A buffered channel is a Channel with a fixed capacity - an integer " +
            "greater than 0. Senders to this channel don't suspend the buffer is full, and receivers " +
            "from this channel don't suspend unless the buffer is empty."
    override val shortDescription = "A buffered channel is a Channel with a fixed capacity"
    override val code = R.raw.buffered_channel

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val channel = Channel<Int>(2)
            launch {
                for (i in 0..4) {
                    log("Send $i")
                    channel.send(i)
                }
            }

            launch {
                for (i in channel)
                    log("Received $i")
            }
        }
    }
}

object CommunicatingSequentialProcessesExample: Card, Example {
    override val code = R.raw.csp

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val shapes = Channel<Shape>()
            val locations = Channel<Location>()

            with(ShapeCollector(4)) {
                start(locations, shapes)
                consumeShapes(shapes)
            }

            sendLocations(locations, log)
        }
    }

    var count = 0

    private fun CoroutineScope.consumeShapes(
        shapesInput: ReceiveChannel<Shape>
    ) = launch {
        for (shape in shapesInput) {
            count++
        }
    }

    private fun CoroutineScope.sendLocations(
        locationsOutput: SendChannel<Location>,
        log: (String) -> Unit
    ) = launch {
        withTimeoutOrNull(3000) {
            while (true) {
                val locations = Location(Random.nextInt(), Random.nextInt())
                locationsOutput.send(locations)
            }
        }
        log("Received $count shapes")
        count = 0
    }

    override val title = "Communicating Sequential Processes"
    override val shortDescription = "Practical example with channels"
    override val description = "\tDepending on the inputs of the user, application has to display " +
            "an arbitrary number of shapes. The main thread, which already handles user input, " +
            "will simulate requests for new shapes. It's a producer-consumer problem: the main " +
            "thread makes requests, while some background task handles them and returns the " +
            "results to the main thread. Implementation should:\n" +
            "\t- Be thread-safe\n" +
            "\t- Reduce the risk of overwhelming the device memory\n" +
            "\t- Have no thread contention (we won't use locks)\n" +
            "\tUsing coroutines and channels, we can share by commuting instead of commuting by " +
            "sharing. The key idea is to encapsulate mutable states inside coroutines." +
            "collectShapes is a consumer, while worker is a producer. start creates multiple " +
            "instances of worker (producer) and one instance of collectShapes (consumer). " +
            "locationsProcessed is created with a capacity of 1 which is an important detail " +
            "explained later.\n" +
            "\tcollectShapes has mutable state (locationsBeingProcessed) that holds references " +
            "to processed states. Locations are provided with a ReceiveChannel. If new coroutine " +
            "was started for each location, we could achieve unlimited concurrency. To avoid " +
            "this pool of workers is created.\n" +
            "\tWorkers fetch ShapeData for given location. collectShapes has SendChannel " +
            "(locationsToProcess) that sends locations to workers' ReceiveChannel only if " +
            "location isn't currently processed. Workers iterate on same Channel which means " +
            "that each worker coroutine will receive location without interfering with each " +
            "other. Worker returns fetched ShapeData with SendChannel (shapesOutput) and sends " +
            "to collectShapes processed location (locationsProcessed). To process multiple " +
            "ReceiveChannels in collectShapes select expression is used.\n" +
            "\tThe select expression waits for the result of multiple suspending functions " +
            "simultaneously, which are specified using clauses in the body of this select " +
            "invocation. The caller is suspended until one of the clauses is either selected or " +
            "fails. Since select doesn't iterate over channels it's necessary to wrap it in a loop.\n" +
            "\tInteraction between coroutines shown in this example is known as fan-out. It's " +
            "achieved by launching several coroutines which all iterate over the same instance " +
            "of ReceiveChannel. If one of the workers fails, the other ones will continue to " +
            "receive from the channel - making system resilient to some extent.\n" +
            "\tInversely, when several coroutines send elements to the same SendChannel instance," +
            "we're talking about fan-in. again, you've got a good example since all workers send " +
            "Shape instances to shapesOutput.\n" +
            "\tlocationsProcessed has capacity of 1 to avoid deadlock. If it was default it would " +
            "send 4 locations and suspend collectShapes on 5th. Worker after processing location " +
            "would try to send processed location with locationsProcessed to suspended " +
            "collectShapes - therefore suspend itself and deadlock. Buffer in locationsProcessed " +
            "allows worker to store location, release lock and take another location from " +
            "locationsToProcess. Additionally order of select expressions is important as it " +
            "makes sure that eventually when collectShapes suspends buffer is always empty - so " +
            "a worker can send location without being suspended."
}

data class Shape(val location: Location, val data: ShapeData)
data class Location(val x: Int, val y: Int)
class ShapeData

class ShapeCollector(private val workerCount: Int) {
    fun CoroutineScope.start(
        locations: ReceiveChannel<Location>,
        shapesOutput: SendChannel<Shape>
    ){
        val locationsToProcess = Channel<Location>()
        val locationsProcessed = Channel<Location>(capacity = 1)

        repeat(workerCount) {
            worker(locationsToProcess, locationsProcessed, shapesOutput)
        }
        collectShapes(locations, locationsToProcess, locationsProcessed)
    }

    private fun CoroutineScope.collectShapes(
        locations: ReceiveChannel<Location>,
        locationsToProcess: SendChannel<Location>,
        locationsProcessed: ReceiveChannel<Location>
    ) = launch(Dispatchers.Default) {
        val locationsBeingProcessed = mutableListOf<Location>()

        while (true) {
            select<Unit> {
                locationsProcessed.onReceive {
                    locationsBeingProcessed.remove(it)
                }
                locations.onReceive {
                    if (!locationsBeingProcessed.any { loc -> loc == it }) {
                        // Add it to locations being processed
                        locationsBeingProcessed.add(it)

                        // Now download the shape at location
                        locationsToProcess.send(it)
                    }
                }
            }
        }
    }

    private fun CoroutineScope.worker(
        locationsToProcess: ReceiveChannel<Location>,
        locationsProcessed: SendChannel<Location>,
        shapesOutput: SendChannel<Shape>
    ) = launch(Dispatchers.IO) {
        for (loc in locationsToProcess){
            try {
                val data = getShapeData(loc)
                val shape = Shape(loc, data)
                shapesOutput.send(shape)
            } finally {
                locationsProcessed.send(loc)
            }
        }
    }

    private suspend fun getShapeData(
        location: Location
    ): ShapeData = withContext(Dispatchers.IO) {
        // Simulate some remote API delay
        delay(10)
        ShapeData()
    }
}
