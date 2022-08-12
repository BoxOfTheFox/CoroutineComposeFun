package com.example.mycomposeapplication

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CoroutineViewModel : ViewModel() {
    private val _strList = listOf("").toMutableStateList()
    val strList: List<String> = _strList

    private val _enableButton = listOf(true).toMutableStateList()
    val enableButton: List<Boolean> = _enableButton

    fun exception3() {
        _enableButton[0] = false
        _strList.clear()
        _strList.add("Using CoroutineExceptionHandler - can't log in real time, will show logs when ended")
        val dangerousWorkaround = mutableListOf<String>()
        runBlocking {
            val ceh = CoroutineExceptionHandler { _, throwable ->
                Log.e("exception3", "CEH handle $throwable")
                dangerousWorkaround.add("CEH handle $throwable")
            }

            val scope = CoroutineScope(Job())

            val job = scope.launch {
                supervisorScope {
                    val task1 = launch {
                        delay(1000)
                        Log.e("exception3", "Done background task")
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
            Log.e("exception3", "Program ends")
            dangerousWorkaround.add("Program ends")
        }
        _strList.addAll(dangerousWorkaround)
        _enableButton[0] = true
    }

    fun cancellation() {
        _enableButton[0] = false
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
        _enableButton[0] = true
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
}