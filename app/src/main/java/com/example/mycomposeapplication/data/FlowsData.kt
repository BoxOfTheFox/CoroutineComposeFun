package com.example.mycomposeapplication.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mycomposeapplication.R
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.whileSelect
import java.io.IOException
import java.time.LocalDateTime

object FlowsNode: Card, Node {
    override val cards: List<Card> = listOf(
        IntroductionToFlowsExample,
        MoreRealisticExample,
        OperatorsExample,
        UpstreamDownstreamExample,
        CallbackBasedAPIExample,
        ConcurrentlyTransformStreamOfValuesExample,
        CreateCustomOperatorExample,
        TryCatchBlock1Example,
        TryCatchBlock2Example,
        SwallowExceptionExample,
        ExceptionTransparencyViolationExample,
        CatchOperatorExample,
        EmitFromCatchExample,
        MaterializeYourExceptionsExample,
        CustomFlowOperatorExample,
        SharedFlowsExample,
        UsingSharedFlowToStreamDataExample,
        EventBusExample,
        StateFlowExample
    )
    override val title = "Flows"
    override val shortDescription = "Similar to Sequences, except that each step can be asynchronous"
    override val description = "Flows, like Channels, are meant to handle asynchronous " +
            "streams of data, but at a higher level of abstraction and with better library " +
            "tooling. Conceptually, Flows are similar to Sequences, except that each step of a " +
            "Flow can by asynchronous. It is also easy to integrate flows in structured " +
            "concurrency, to avoid leaking resources."
}

object IntroductionToFlowsExample: Card, Example {
    override val code = R.raw.introduction_to_flows

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val flow = numbers()
            flow.collect {
                log("$it")
            }
        }
    }

    private fun numbers(): Flow<Int> = flow {
        emit(1)
        emit(2)
    }

    override val title = "An Introduction to Flows"
    override val shortDescription = "Example to define and collect Flow"
    override val description = "\t- Instead of returning a Channel instance, we're returning a " +
            "Flow instance\n" +
            "\t- Inside the Flow - we use the emit suspending function instead of send\n" +
            "\t- The numbers function, which returns a Flow instance, isn't a suspending function. " +
            "Invoking the numbers function doesn't start anything by itself - it just immediately " +
            "returns a Flow instance\n" +
            "\t- Once we get a flow, instead of looping over it, we use collect function which, " +
            "in Flows parlance, is called a terminal operator"
}

object MoreRealisticExample: Card, Example {
    override val code = R.raw.more_realistic_example

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val flow = getDataFlow(3, log)
            launch {
                flow.collect{
                    log("$it")
                }
            }
        }
    }

    private fun getDataFlow(n: Int, log: (String) -> Unit): Flow<TokenData>{
        return flow {
            connect(log)
            repeat(n) {
                val token = getToken(log)
                val data = getData(token, log)
                emit(TokenData(token, data))
            }
        }.onCompletion {
            disconnect(log)
        }
    }


    private suspend fun connect(log: (String) -> Unit) {
        log("Connecting..")
        delay(10)
    }

    private suspend fun getToken(log: (String) -> Unit): String {
        log("Getting token..")
        delay(15)
        return "token"
    }

    private suspend fun getData(token: String, log: (String) -> Unit): String {
        log("Getting data for $token")
        delay(5)
        return "data"
    }

    private fun disconnect(log: (String) -> Unit) {
        log("Disconnect")
    }

    data class TokenData(val token: String, val data: String? = null)

    override val title = "A More Realistic Example"
    override val shortDescription = "Example with mocked database connection"
    override val description = "\t- Creating a connection to the database and closing it on " +
            "completion is completely transparent to the client code that consumes the flow. " +
            "Client code only sees a flow of TokenData.\n" +
            "\t- All operations inside the flow are sequential. For example, once we get the " +
            "first token (say, \"token1\"), the flow invokes getData(\"token1\") and suspends " +
            "until it gets the result (say, \"data1\"). Then the flow emits the first " +
            "TokenData(\"token1\", \"data1\"). Only after that does the execution proceed with " +
            "\"token2\", etc.\n" +
            "\t- Invoking the getDataFlow function does nothing on its own. It simply returns a " +
            "flow. The code inside the flow executes only when a coroutine collects the flow.\n" +
            "\t- If the coroutine that collects the flow gets cancelled or reaches the end of the " +
            "flow, the code inside the onCompletion block executes. This guaranties that we " +
            "properly release the connection to the database."
}

object OperatorsExample: Card, Example {
    override val code = R.raw.operators

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val flow = numbers()
            val newFlow = flow.map(::transform)

            newFlow.collect {
                log(it)
            }
        }
    }

    private suspend fun transform(i: Int): String = withContext(Dispatchers.Default) {
        delay(10) // simulate real work
        "${i + 1}"
    }

    private fun numbers(): Flow<Int> = flow {
        emit(1)
        emit(2)
    }

    override val title = "Operators and Terminal Operators"
    override val shortDescription = "Ways to transform and collect flows"
    override val description = "\tCoroutine library provides functions such as map, filter, " +
            "debounce, buffer, onCompletion, etc. Those functions are called flow operators or " +
            "intermediate operators, because they operate on a flow and return another flow.\n" +
            "\tTerminal operators can be easily distinguished from other regular operators since " +
            "it's a suspending function that starts the collection of the flow. Some examples are:\n" +
            "\t- toList collects the given flow and returns a List containing all collected " +
            "elements\n" +
            "\t- collectLatest collects the given flow with a provided action. The difference from " +
            "collect is that when the original flow emits a new value, the action block for the " +
            "previous value is cancelled\n" +
            "\t- first returns the first element emitted by the flow and then cancels the flow's " +
            "collection. It throws a NuSuchElementException if the flow was empty. There's also " +
            "a variant, firstOrNull, which returns null if the flow was empty."
}

object UpstreamDownstreamExample: Card, Example { // fixme LocalDateTime
    override val code = R.raw.upstream_downstream

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            getMessagesFromUser("Amanda", "en-us").collect {
                log("Received message from ${it.user}: ${it.content}")
            }
        }
    }

    private fun getMessagesFromUser(user: String, language: String): Flow<Message> {
        return getMessageFlow()
            .filter { it.user == user }
            .map { it.translate(language) }
            .flowOn(Dispatchers.Default)
    }

    data class Message(
        val user: String,
        val date: LocalDateTime,
        val content: String
    )

    private fun getMessageFlow(): Flow<Message> = flow {
        emit(Message("Amanda", LocalDateTime.now(), "First msg"))
        emit(Message("Amanda", LocalDateTime.now(), "Second msg"))
        emit(Message("Pierre", LocalDateTime.now(), "First msg"))
        emit(Message("Amanda", LocalDateTime.now(), "Third msg"))
    }

    private suspend fun Message.translate(language: String): Message =
        withContext(Dispatchers.Default) {
            copy(content = "translated content")
        }

    override val title = "Upstream and Downstream example"
    override val shortDescription = "Example showing difference between Upstream and Downstream"
    override val description = "\t- The first operator, filter operates on the original flow and " +
            "returns another flow of messages which all originate from the same user passed as a " +
            "parameter\n" +
            "\t- The second operator, map, operates on the flow returned by filter and returns a " +
            "flow of translated messages. From the filter operator standpoint, the original flow " +
            "is the upstream flow, while the downstream flow is represented by all operators " +
            "happening after filter. The same reasoning applies for all intermediate operators - " +
            "they have their own relative upstreams and downstream flow\n" +
            "\t- The flowOn operator changes the context of the flow it is operating on. It " +
            "changes the coroutine context of the upstream flow, while not affecting the " +
            "downstream flow. Consequently, steps 1 and 2 are done using the dispatcher " +
            "Dispatcher.Default"
}

object CallbackBasedAPIExample: Card, Example {
    override val code = R.raw.callback_based_api

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        log("I need to figure out executing this example ^^'")
    }

    override val title = "Callback-based API"
    override val shortDescription = "API example for Upstream and Downstream example"
    override val description = "\tThe message factory has a publish/subscribe mechanism - we can " +
            "register or unregister observers for new incoming messages. This implementation polls " +
            "for new messages every second and notifies observers.\n" +
            "\tMessageFactory is said to be callback-based, because it holds references to " +
            "MessageObserver instances and calls methods on those instances when new messages are " +
            "retrieved. To bridge the flow world with the \"callback\" world, you can use the " +
            "callbackFlow builder.\n" +
            "\tThe callbackFlow builder creates a cold flow which doesn't perform anything until " +
            "you invoke a terminal operator. Let's break it down. First off, it's a parameterized " +
            "function which returns a flow of the given type. It's always done in three steps:\n" +
            "\t1. Instantiate the \"callback\". In this case, it's an observer.\n" +
            "\t2. Register that callback using the available api.\n" +
            "\t3. Listen for close event using awaitClose, and provide a relevant action to take " +
            "in this case. Most probably, you'll have to unregister the callback."
}

object ConcurrentlyTransformStreamOfValuesExample: Card, Example {
    override val code = R.raw.concurrently_transform_stream_of_values

    data class Location(val i: Int)
    data class Content(val i: Int)

    private val locationsFlow = flowOf(*Array(10, ::Location))

    @OptIn(FlowPreview::class)
    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            // Defining the Flow of Content - nothing is executing yet
            val contentFlow = locationsFlow.map { loc ->
                flow {
                    emit(transform(loc, log))
                }
            }.flattenMerge(4)

            // We now collect the entire flow using the toList terminal operator
            val contents = contentFlow.toList()

            contents.forEach { log("$it") }
        }
    }

    private suspend fun transform(loc: Location, log: (String) -> Unit): Content = withContext(Dispatchers.IO) {
        log("transform $loc")
        Content(loc.i)
    }

    override val title = "Concurrently Transform a Stream Of Values"
    override val shortDescription = "Transformation done in parallel"
    override val description = "To understand what's going on here, you should realize that " +
            "locations.map{…} returns a flow of flow (e.g., the type is Flow<Flow<Content>>). " +
            "Indeed, inside the map{…} operator, a new flow is created upon emission of a location " +
            "by the the upstream flow (which is locationsFlow). Each of those created flows is of " +
            "type Flow<Content> and individually performs location transformation.\n" +
            "\tThe last statement, flattenMerge, merges all those created flows inside a new " +
            "resulting Flow<Content> (which we assign to contentFlow). Also, flattenMerge has a " +
            "\"concurrency\" parameter. Indeed, it would probably be inappropriate to concurrently " +
            "create and collect a flow every time we receive a location. With concurrency level " +
            "of 4, we ensure that no more than four flows will be collected at a given point in " +
            "time.\n" +
            "\tThanks to the suspending nature of flows, you get back pressure for free. New " +
            "locations are collected from locationFlow only when the machinery is available to " +
            "produce them."
}

object CreateCustomOperatorExample: Card, Example {
    override val code = R.raw.create_custom_operator

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val flow = (1..100).asFlow().onEach { delay(10) }
            val startTime = System.currentTimeMillis()
            flow.bufferTimeout(10, 50).collect {
                val time = System.currentTimeMillis() - startTime
                log("$time ms: $it")
            }
        }
    }

    /**
     * Buffers the upstream flow producing lists of elements when:
     * * A number of [maxSize] elements have been emitted
     * * A timeout of [maxDelayMillis] has expired
     *
     * Consequently, the produced lists of elements have a maximum size of [maxSize].
     */
    @OptIn(ObsoleteCoroutinesApi::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
    // 17
    private fun <T> Flow<T>.bufferTimeout(maxSize: Int, maxDelayMillis: Long): Flow<List<T>> = flow {
        require(maxSize > 0) { "maxSize should be greater than 0" }
        require(maxDelayMillis > 0) { "maxDelayMillis should be greater than 0" }

        // 21
        coroutineScope {
            // 22
            val channel = produceIn(this)
            // 23
            val ticker = ticker(maxDelayMillis)
            val buffer = mutableListOf<T>()

            suspend fun emitBuffer() {
                if (buffer.isNotEmpty()) {
                    emit(buffer.toList())
                    buffer.clear()
                }
            }

            try {
                // 34
                whileSelect {
                    channel.onReceive { value ->
                        buffer.add(value)
                        if (buffer.size >= maxSize) emitBuffer()
                        true
                    }
                    ticker.onReceive {
                        emitBuffer()
                        true
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                // 46
                emitBuffer()
            } finally {
                // 48
                channel.cancel()
                // 49
                ticker.cancel()
            }
        }
    }

    override val title = "Create a Custom Operator"
    override val shortDescription = "Implementation of bufferTimeout"
    override val description = "The flow returned by bufferTimeout should buffer elements and " +
            "emit a list (batch) of elements when either:\n" +
            "\t- The buffer is full\n" +
            "\t- A predefined maximum amount of time has elapsed (timeout)\n" +
            "Here's the explanation:\n" +
            "\t- First of all, the signature of the operator tells us a lot. It's declared as an " +
            "extension function of FLow<T>, so you can use it like this: " +
            "upstreamFlow.bufferTimeout(10, 100). As for the return type, it's Flow<List<T>>. " +
            "Remember that you want to process elements by batches, so the flow returned by " +
            "bufferTimeout should return elements as List<T>.\n" +
            "\t- Line 17: we're using a flow() builder. As a reminder, the builder provides you an " +
            "instance of FlowCollector, and the block of code is an extension function with " +
            "FlowCollector, as the receiver type. In other words, you can invoke emit from inside " +
            "the block of code.\n" +
            "\t- Line 21: we're using coroutineScope{} because we'll start new coroutines, which " +
            "is only possible within CoroutineScope.\n" +
            "\t- Line 22: from out coroutine standpoint, received elements should come from a " +
            "ReceiveChannel. So another inner coroutine should be started to consume the upstream " +
            "flow and send them over channel. This is exactly the purpose of the produceIn flow " +
            "operator.\n" +
            "\t- Line 23: we need to generate \"timeout\" events. A library function already " +
            "exists exactly for that purpose: ticker (obsolete). It creates a channel that " +
            "produces the first item after the given initial delay, and subsequently items with " +
            "the given delay between them. As specified in the documentation, ticker starts a new " +
            "coroutine eagerly, and we're fully responsible for cancelling it.\n" +
            "\t- Line 34: we're using whileSelect, which really is just syntax sugar for looping " +
            "in a select expression while clauses return true. Inside the whileSelect{} block you " +
            "can see the logic of adding an element to the buffer only if it's not full, and " +
            "emitting the whole buffer otherwise.\n" +
            "\t- Line 46: when the upstream flow collection completes, the coroutine started " +
            "with produceIn will still attempt to read from that flow, and a " +
            "ClosedReceiveChannelException will be raised. So we catch that exception, and we know " +
            "that we should emit content of the buffer.\n" +
            "\t- Lines 48 and 49: channels are hot entities - they should be cancelled when " +
            "they're not supposed to be used anymore. As for the ticker, it should be cancelled too."
}

object TryCatchBlock1Example: Card, Example {
    override val code = R.raw.try_catch_block1

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            try {
                upstream.collect {
                    if (it > 2)
                        throw RuntimeException()
                    log("Received $it")
                }
            } catch (e: Throwable) {
                log("Caught $e")
            }
        }
    }

    private val upstream = flowOf(1,2,3)

    override val title = "The try/catch Block"
    override val shortDescription = "First example"
    override val description = "If we purposely throw an exception inside the collect{} block, " +
            "we can catch the exception by wrapping the whole chain in a try/catch block."
}

object TryCatchBlock2Example: Card, Example {
    override val code = R.raw.try_catch_block2

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            try {
                upstream.collect {
                    log("Received $it")
                }
            } catch (e: Throwable) {
                log("Caught $e")
            }
        }
    }

    private val upstream = flowOf(1,2,3).onEach {
        if (it > 2) throw RuntimeException()
    }

    override val title = "The try/catch Block"
    override val shortDescription = "Second example"
    override val description = "Exception also works when raised from inside the upstream flow."
}

object SwallowExceptionExample: Card, Example {
    override val code = R.raw.swallow_exception

    private var danger: ((String) -> Unit)? = null
    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        danger = log
        scope.launch {
            try {
                upstream.collect {
                    log("Received $it")
                    check(it <= 2) {
                        "Collected $it while we expect values below 2"
                    }
                }
            } catch (e: Throwable) {
                log("Caught $e")
            }
        }
    }

    private val upstream: Flow<Int> = flow {
        for(i in 1..3) {
            try {
                emit(i)
            }catch (e: Throwable) {
                danger?.invoke("Intercept downstream exception $e")
            }
        }
    }

    override val title = "Swallowing exceptions"
    override val shortDescription = "Flows swallow downstream exceptions"
    override val description = "If you try to intercept exception in the flow itself, it will " +
            "swallow exception."
}

object ExceptionTransparencyViolationExample: Card, Example {
    override val code = R.raw.exception_transparency_violation

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            try {
                violatesExceptionTransparency.collect {
                    log("Received $it")
                    check(it <= 2) {
                        "Collected $it while we expect values below 2"
                    }
                }
            } catch (e: Throwable) {
                log("Caught $e")
            }
        }
    }

    private val violatesExceptionTransparency: Flow<Int> = flow {
        for(i in 1..3) {
            try {
                emit(i)
            }catch (e: Throwable) {
                emit(-1)
            }
        }
    }

    override val title = "Exception Transparency Violation"
    override val shortDescription = "Examples of what to avoid"
    override val description = "\nA flow implementation shouldn't have a side effect on the code " +
            "that collects that flow. Likewise, the code that collects a flow shouldn't be aware " +
            "of the implementation details of the upstream flow. A flow should always be " +
            "transparent to exceptions: it should propagate exceptions coming from a collector. " +
            "In other words, a flow should never swallow downstream exceptions.\n" +
            "\tTrying to emit values inside flow's try/catch block is also " +
            "exception transparency violation. The try/catch block should only be used to " +
            "surround the collector, to handle exceptions raised from collector itself, or " +
            "(possibly, although it's not ideal) to handle exceptions raised from the flow."
}

object CatchOperatorExample: Card, Example {
    override val code = R.raw.catch_operator

    private var danger: ((String) -> Unit)? = null

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        danger = log
        scope.launch {
            try {
                encapsulateError.collect {
                    if (it > 2) throw RuntimeException()
                    log("Received $it")
                }
            } catch (e: RuntimeException){
                log("Collector stopped collecting the flow")
            }
        }
    }

    private val upstream = flowOf(1,3,-1)

    private val encapsulateError = upstream.onEach {
        if (it < 0) throw java.lang.NumberFormatException("Values should be greater than 0")
    }.catch {
        danger?.invoke("Caught $this")
    }

    override val title = "The Catch Operator"
    override val shortDescription = "The catch operator allows for a declarative style of " +
            "catching exceptions."
    override val description = "The catch operator allows for a declarative style of catching " +
            "exceptions. It catches all upstream exceptions. By all exceptions, we mean that it " +
            "even catches Throwables. Since it only catches upstream exceptions, the catch " +
            "operator doesn't have the exception issue of the try/catch block."
}

object EmitFromCatchExample: Card, Example {
    override val code = R.raw.emit_from_catch

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            encapsulateError.collect {
                log("Received $it")
            }
        }
    }

    private val upstream = flowOf(1,3,-1)

    private val encapsulateError = upstream.onEach {
        if (it < 0) throw NumberFormatException("Values should be greater than 0")
    }.catch {
        emit(0)
    }

    override val title = "Emitting from inside catch"
    override val shortDescription = "Example on o emitting form inside a catch"
    override val description = "Sometimes it will make sense to emit a particular value when you " +
            "catch an exception from inside the flow. Emitting values from inside catch is " +
            "especially useful to materialize exceptions."
}

object MaterializeYourExceptionsExample: Card, Example {
    override val code = R.raw.materialize_your_exceptions

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val resultFlow = urlFlow.map {
                fetchResult(it, log)
            }

            val results = resultFlow.toList()
            log("Results: $results")
        }
    }

    private val urlFlow = flowOf("url-1", "url-2", "url-retry")
    data class Image(val url: String)

    private suspend fun fetchImage(url: String): Image {
        delay(10)

        if (url.contains("retry"))
            throw IOException("Server returned HTTP code 503")

        return Image(url)
    }

    sealed class Result
    data class Success(val image: Image): Result()
    data class Error(val url: String): Result()

    private suspend fun fetchResult(url: String, log: (String) -> Unit): Result {
        log("Fetching $url…")
        return try {
            val image = fetchImage(url)
            Success(image)
        } catch (e: IOException) {
            Error(url)
        }
    }

    override val title = "Materialize Your Exceptions"
    override val shortDescription = "Process of catching exceptions and emitting special values " +
            "or objects that represent those exceptions."
    override val description = "Process of catching exceptions and emitting special values or " +
            "objects that represent those exceptions. The goal is to avoid throwing exceptions " +
            "from inside the flow, because code execution then goes to whatever place that " +
            "collects that flow."
}

object CustomFlowOperatorExample: Card, Example {
    override val code = R.raw.custom_flow_operator

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val urlFlow = flowOf("url-1", "url-2", "url-retry")

            val resultFlowWithRetry = urlFlow.mapWithRetry(
                { url -> fetchResult(url, log) },
                {value, attempt -> value is Error && attempt < 3L}
            )

            val results = resultFlowWithRetry.toList()
            log("Results: $results")
        }
    }

    private fun <T, R : Any> Flow<T>.mapWithRetry(
        action: suspend (T) -> R,
        predicate: suspend (R, attempt: Int) -> Boolean
    ) = map { data ->
        var attempt = 0
        var shallRetry: Boolean
        var lastValue: R? = null
        do {
            val tr = action(data)
            shallRetry = predicate(tr, ++attempt)
            if(!shallRetry) lastValue = tr
        } while (shallRetry)
        return@map lastValue
    }

    data class Image(val url: String)

    private suspend fun fetchImage(url: String): Image {
        delay(10)

        if (url.contains("retry"))
            throw IOException("Server returned HTTP code 503")

        return Image(url)
    }

    sealed class Result
    data class Success(val image: Image): Result()
    data class Error(val url: String): Result()

    private suspend fun fetchResult(url: String, log: (String) -> Unit): Result {
        log("Fetching $url…")
        return try {
            val image = fetchImage(url)
            Success(image)
        } catch (e: IOException) {
            Error(url)
        }
    }

    override val title = "Custom Flow Operator"
    override val shortDescription = "Flow operator that retries while predicate returns true"
    override val description = "Flow operator that retries while predicate returns true."
}

object SharedFlowsExample: Card, Example {
    override val code = R.raw.shared_flows

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val sharedFlow = MutableSharedFlow<String>()

            launch { // First subscriber
                sharedFlow.collect {
                    log("Subscriber 1 receives $it")
                }
            }

            launch { // Second subscriber - slow
                sharedFlow.collect {
                    log("Subscriber 2 receives $it")
                    delay(3000)
                }
            }

            launch { // Start emitting values
                sharedFlow.emit("one")
                sharedFlow.emit("two")
                sharedFlow.emit("three")
            }

            Unit
        }
    }

    override val title = "Shared Flows"
    override val shortDescription = "A SharedFlow broadcasts events to all its subscribers"
    override val description = "\tA SharedFlow broadcasts events to all its subscribers. To invoke " +
            "you use MutableSharedFlow(), to make it immutable you can use .asSharedFlow(). " +
            "A subscriber registers when it starts collecting SharedFlow. If scope is cancelled " +
            "so is the subscriber. A MutableSharedFlow exposes two methods to emit values:\n" +
            "\t- emit This suspends under some conditions\n" +
            "\t- tryEmit This never suspends. It tries to emit the value immediately"
}

object UsingSharedFlowToStreamDataExample: Card, Example {
    override val code = R.raw.using_shared_flow_to_stream_data

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val repo = NewsRepository(dao)
            NewsViewModel(repo, scope, log)
            delay(150)
            AnotherViewModel(repo, scope, log)

            delay(30_000)
            repo.stop()
        }
    }

    data class News(val content: String)

    interface NewsDao {
        suspend fun fetchNewsFromApi(): List<News>
    }

    class NewsRepository(private val dao: NewsDao) {
        private val _newsFeed = MutableSharedFlow<News>()
        val newsFeed = _newsFeed.asSharedFlow()

        private val scope = CoroutineScope(Job() + Dispatchers.Default)

        init {
            scope.launch {
                while (true) {
                    val news = dao.fetchNewsFromApi()
                    news.forEach { _newsFeed.emit(it) }

                    delay(3000)
                }
            }
        }

        fun stop() = scope.cancel()
    }

    class NewsViewModel(
        private val repository: NewsRepository,
        scope: CoroutineScope,
        log: (String) -> Unit
    ) {
        private val newsList = mutableListOf<News>()

        private val _newsLiveData = MutableLiveData<List<News>>(newsList)
        val newsLiveData: LiveData<List<News>> = _newsLiveData

        init {
            scope.launch {
                repository.newsFeed.collect {
                    log("NewsViewModel receives $it")
                    newsList.add(it)
                    _newsLiveData.value = newsList
                }
            }
        }
    }

    class AnotherViewModel(
        private val repository: NewsRepository,
        scope: CoroutineScope,
        log: (String) -> Unit
        ) {
        init {
            scope.launch {
                repository.newsFeed.collect {
                    log("AnotherViewModel receives $it")
                }
            }
        }
    }

    private val dao = object: NewsDao {
        override suspend fun fetchNewsFromApi(): List<News> {
            delay(100)
            return List(10) { News("news content $it") }
        }
    }

    override val title = "Using SharedFlow to Stream Data"
    override val shortDescription = "Example app fetching news from API or local DB and displaying it"
    override val description = "\tThe repository is responsible for querying the remote API at " +
            "regular intervals, and provides a means for view-models to et the newsfeed. As soon " +
            "as the repository instance instance is created, we start fetching news from the API. " +
            "Every time we get a list of News instances, we emit those values using our " +
            "MutableSharedFlow.\n" +
            "\tView-model subscribes to shared flow with collect. Every time repository emits " +
            "News, it's added to LiveData and updated to a view.\n" +
            "\tThe other view-model was created 250 ms after the launch of the program. Because " +
            "of that it missed two news entries. This is because, at the time the shared flow " +
            "emits the first two news entries, the first view-model is the only subscriber. The " +
            "second view-model comes after and only receives subsequent news."
}

object EventBusExample: Card, Example {
    override val code = R.raw.event_bus

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        scope.launch {
            val eventBus = EventBus()

            delay(100)
            log("start download")
            Downloader(eventBus, this, log)
            eventBus.startDownload("http://somewebsite_link")
            Unit
        }
    }

    class EventBus {
        private val _startDownloadEvent = MutableSharedFlow<DownloadEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        val startDownloadEvent = _startDownloadEvent.asSharedFlow()

        fun startDownload(url: String) = _startDownloadEvent.tryEmit(DownloadEvent(url))
    }

    data class DownloadEvent(val url: String)

    class Downloader(private val eventBus: EventBus, scope: CoroutineScope, log: (String) -> Unit) {
        init {
            scope.launch {
                log("subscribe")
                eventBus.startDownloadEvent.collect {
                    download(it.url, log)
                }
            }
        }

        private fun download(url: String, log: (String) -> Unit) {
            log("Downloading $url..")
        }
    }

    override val title = "Event Bus"
    override val shortDescription = "Using SharedFlow as an EventBus"
    override val description = "\tYou need an event bus when all the following conditions are met:\n" +
            "\t- You need to broadcast an event across one or several subscribers\n" +
            "\t- The event should be processed only once\n" +
            "\t- If a component isn't registered as a subscriber at the time you emit the event, " +
            "the event is lost for that component\n" +
            "\tA SharedFlow can optionally replay values for new subscribers. The number of of " +
            "values to replay is configurable, using the replay parameter of the " +
            "MutableSharedFlow function. A shared flow with replay > 0 internally uses cache that " +
            "works similarly to a Channel. By default, when the replay cache is full, emit " +
            "suspends until all subscribers start processing the oldest value in the cache. As " +
            "for tryEmit, it returns false since it can't add the value to the cache. " +
            "BufferOverflow is an enum responsible for defining behavior when cache is full. Three " +
            "values are possible: SUSPEND, DROP_OLDEST and DROP_LATEST. SUSPEND is default.\n" +
            "\textraBufferCapacity allows shared flow to buffer values without replying them, " +
            "allowing slow subscribers to lag behind other, faster subscribers. One immediate " +
            "consequence of creating a shared flow with, for example, extraBufferCapacity = 1 and " +
            "onBufferOverflow - BufferOverflow.DROP_OLDEST, is that you're guaranteed that tryEmit " +
            "will always successfully insert a value into the shared flow."
}

object StateFlowExample: Card, Example {
    override val code = R.raw.state_flow

    override fun invoke(scope: CoroutineScope, log: (String) -> Unit) {
        log("I need to figure out executing this example ^^'")
    }

    override val title = "StateFlow"
    override val shortDescription = "A Specialized SharedFlow"
    override val description = "\tIf the service fires a \"download-finished\" event, you don't " +
            "want your UI to miss that. When the user navigates to the view displaying the status " +
            "of the download, the view should render the updated state of the download.\n" +
            "\tYou will face situations where sharing a state is required. This situation is so " +
            "common that a type of shared flow was specifically created for it: StateFlow. When " +
            "sharing a state, a state flow:\n" +
            "\t- Shares only one value: the current state\n" +
            "\t- Replays the state. Indeed, subscribers should get the last state even if they " +
            "subscribe afterward\n" +
            "\t- Emits an initial value - much like LiveData has an initial value\n" +
            "\t- Emits new values only when the state changes"
}