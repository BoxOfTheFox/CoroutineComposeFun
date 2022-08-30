package com.example.mycomposeapplication.ui.detail

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

abstract class AbstractDetailViewModel: ViewModel() {
    protected val _strList = listOf("").toMutableStateList()
    val strList: List<String> = _strList

    abstract fun execute()
}

class CancellationViewModel : AbstractDetailViewModel(){
    override fun execute() {
        _strList.clear()
        viewModelScope.launch {
            val job = launch {
                try {
                    wasteCpu()
                } catch (e: CancellationException) {
                    withContext(Dispatchers.Main) {
                        _strList.add("yems")
                        Log.e("cancellation", "yems")
                    }
                }
            }
            delay(1200)
            withContext(Dispatchers.Main) {
                _strList.add("main: I'm going to cancel this job")
                Log.e("cancellation", "main: I'm going to cancel this job")
            }
            job.cancel()
            withContext(Dispatchers.Main) {
                _strList.add("main: Done")
                Log.e("cancellation", "main: Done")
            }
        }
    }

    private suspend fun wasteCpu() = withContext(Dispatchers.Default) {
        var nextPrintTime = System.currentTimeMillis()
        while (isActive) {
            if (System.currentTimeMillis() >= nextPrintTime) {
                withContext(Dispatchers.Main) {
                    _strList.add("job: I'm working…")
                    Log.e("cancellation", "job: I'm working…")
                }
                nextPrintTime += 500
            }
        }
    }

    companion object {
        const val name = "Basic cancellation"
    }
}

class HandledLaunchCEHViewModel: AbstractDetailViewModel() {
    /**
     * Handles launch exception with CEH in direct child of supervisorScope
     */
    override fun execute() {
        viewModelScope.launch{
            _strList.clear()
            _strList.add("Can't log in real time, will show logs when finished")
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
            _strList.addAll(dangerousWorkaround)
        }
    }

    companion object {
        const val name = "Handled"
    }
}

class UnhandledLaunchCEHViewModel: AbstractDetailViewModel(){
    /**
     * Handles launch exception with CEH but fail stops all children
     */
    override fun execute() {
        viewModelScope.launch{
            _strList.clear()
            _strList.add("Can't log in real time, will show logs when finished")
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
            _strList.addAll(dangerousWorkaround)
        }
    }

    companion object {
        const val name = "Unhandled launch"
    }
}

class UnhandledAsyncCEHViewModel: AbstractDetailViewModel(){
    /**
     * Handles async silent exception but fail stops all children
     */
    override fun execute() {
        viewModelScope.launch {
            _strList.clear()
            _strList.add("Can't log in real time, will show logs when finished")
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
            _strList.addAll(dangerousWorkaround)
        }
    }

    companion object {
        const val name = "Unhandled async"
    }
}

class ExposedAsyncTryCatchViewModel: AbstractDetailViewModel() {
    /**
     * Handles exposed exception with supervisorScope and try-catch
     */
    override fun execute() {
        viewModelScope.launch {
            _strList.clear()
            _strList.add("Can't log in real time, will show logs when finished")
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
            _strList.addAll(dangerousWorkaround)
        }
    }

    companion object {
        const val name = "Exposed"
    }
}