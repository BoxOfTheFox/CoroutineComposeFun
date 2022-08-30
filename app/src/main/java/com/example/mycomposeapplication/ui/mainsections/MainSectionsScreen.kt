package com.example.mycomposeapplication.ui.mainsections

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mycomposeapplication.data.CardMetadata
import com.example.mycomposeapplication.ui.components.CategoryGrid

@Composable
fun MainSectionsScreen(
    viewModel: AbstractSectionViewModel,
    onCardSelected: (String) -> Unit
) {
    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                text = viewModel.title,
                style = MaterialTheme.typography.h4
            )
            Text(
                text = viewModel.description,
                modifier = Modifier.paddingFromBaseline(bottom = 12.dp)
            )
            CategoryGrid(
                modifier = Modifier.fillMaxSize(),
                cards = viewModel.cards,
                onCardSelected = onCardSelected
            )
        }
    }
}