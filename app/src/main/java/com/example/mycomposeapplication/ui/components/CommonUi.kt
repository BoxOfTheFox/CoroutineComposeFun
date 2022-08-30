package com.example.mycomposeapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mycomposeapplication.data.CardMetadata

@Composable
fun CategoryGrid(
    modifier: Modifier = Modifier,
    cards: List<CardMetadata>,
    onCardSelected: (String) -> Unit
){
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(cards) { item ->
            CategoryCard(item, onCardSelected)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CategoryCard(
    meta: CardMetadata,
    onCardSelected: (String) -> Unit
) {
    Card(
        elevation = 1.dp,
        modifier = Modifier
            .defaultMinSize(minHeight = 80.dp)
            .fillMaxWidth(),
        onClick = {
            onCardSelected(meta.appDestination.route)
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = meta.title,
                style = MaterialTheme.typography.h6,
                maxLines = 1
            )
            Text(
                text = meta.subtitle,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.paddingFromBaseline(28.dp),
                maxLines = 2
            )
        }
    }
}