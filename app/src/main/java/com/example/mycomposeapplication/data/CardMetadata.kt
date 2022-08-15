package com.example.mycomposeapplication.data

import com.example.mycomposeapplication.AppDestination
import com.example.mycomposeapplication.Cancellation
import com.example.mycomposeapplication.Detail
import com.example.mycomposeapplication.ExceptionHandling

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
                "Basic cancellation",
                "hmm",
                Cancellation/Detail
            )
        )

        val exceptionHandlingCards = listOf(
            CardMetadata(
                "Exposed",
                "Handles async exception with supervisorScope and try-catch",
                ExceptionHandling/Detail
            ),
            CardMetadata(
                "Unhandled",
                "Handles launch exception with CEH in direct child of supervisorScope",
                ExceptionHandling/Detail
            ),
            CardMetadata(
                "Unhandled async",
                "Handles async silent exception but exception stops all children",
                ExceptionHandling/Detail
            ),
            CardMetadata(
                "Unhandled launch",
                "Handles launch exception with CEH but exception stops all children",
                ExceptionHandling/Detail
            )
        )
    }
}