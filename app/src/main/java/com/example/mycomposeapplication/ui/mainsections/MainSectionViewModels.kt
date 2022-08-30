package com.example.mycomposeapplication.ui.mainsections

import androidx.lifecycle.ViewModel
import com.example.mycomposeapplication.data.CardMetadata

abstract class AbstractSectionViewModel : ViewModel() {
    abstract val cards: List<CardMetadata>
    abstract val description: String
    abstract val title: String
}

class MainSectionViewModel : AbstractSectionViewModel() {
    override val cards = CardMetadata.chapterCards
    override val description = ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(4)
    override val title = "Main"
}

class CancellationSectionViewModel: AbstractSectionViewModel() {
    override val cards = CardMetadata.cancellationCards
    override val description = ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(4)
    override val title = "Cancellation"
}

class ExceptionHandlingSectionViewModel: AbstractSectionViewModel() {
    override val cards = CardMetadata.exceptionHandlingCards
    override val description = ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(4)
    override val title = "Exception Handling"
}