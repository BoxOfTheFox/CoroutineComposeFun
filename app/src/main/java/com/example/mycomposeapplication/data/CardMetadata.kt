package com.example.mycomposeapplication.data

import com.example.mycomposeapplication.AppDestination
import com.example.mycomposeapplication.Cancellation
import com.example.mycomposeapplication.Detail
import com.example.mycomposeapplication.ExceptionHandling
import com.example.mycomposeapplication.ui.detail.*

data class CardMetadata(
    val title: String,
    val subtitle: String,
    val appDestination: AppDestination
) {
    companion object{
        val chapterCards = listOf(
            CardMetadata(
                "Cancellation",
                "hmm",
                Cancellation
            ),
            CardMetadata(
                "Exception handling",
                "hmm",
                ExceptionHandling
            )
        )

        val cancellationCards = listOf(
            CardMetadata(
                CancellationViewModel.name,
                "hmm",
                Cancellation/Detail/CancellationViewModel.name
            )
        )

        val exceptionHandlingCards = listOf(
            CardMetadata(
                ExposedAsyncTryCatchViewModel.name,
                "Handles async exception with supervisorScope and try-catch",
                ExceptionHandling/Detail/ExposedAsyncTryCatchViewModel.name
            ),
            CardMetadata(
                HandledLaunchCEHViewModel.name,
                "Handles launch exception with CEH in direct child of supervisorScope",
                ExceptionHandling/Detail/HandledLaunchCEHViewModel.name
            ),
            CardMetadata(
                UnhandledAsyncCEHViewModel.name,
                "Handles async silent exception but exception stops all children",
                ExceptionHandling/Detail/UnhandledAsyncCEHViewModel.name
            ),
            CardMetadata(
                UnhandledLaunchCEHViewModel.name,
                "Handles launch exception with CEH but exception stops all children",
                ExceptionHandling/Detail/UnhandledLaunchCEHViewModel.name
            )
        )
    }
}