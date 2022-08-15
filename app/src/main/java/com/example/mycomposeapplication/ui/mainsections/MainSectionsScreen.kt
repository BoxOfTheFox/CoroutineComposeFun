package com.example.mycomposeapplication.ui.mainsections

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mycomposeapplication.data.CardMetadata
import com.example.mycomposeapplication.ui.components.CategoryGrid

@Composable
fun MainSectionsScreen(
    cards: List<CardMetadata>,
    onCardSelected: (String) -> Unit
) {
    CategoryGrid(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        cards = cards,
        onCardSelected = onCardSelected
    )
}