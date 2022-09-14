package com.example.mycomposeapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycomposeapplication.MainViewModel
import com.example.mycomposeapplication.data.CardMetadata
import com.example.mycomposeapplication.data.Example

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
            onCardSelected(meta.appDestination)
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

@Composable
fun ExampleExecutor(example: Example, viewModel: MainViewModel = viewModel()) {
    var showFab by remember { mutableStateOf(true) }
    Scaffold(
        floatingActionButton = {
            if (showFab)
                FloatingActionButton(onClick = {
                    showFab = false
                    viewModel.execute(example)
                }) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Start example")
                }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { padding ->
        Surface(
            color = Color.Black,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            shape = MaterialTheme.shapes.medium,
        ) {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(items = viewModel.strList) { str ->
                    Text(
                        fontFamily = FontFamily.Monospace,
                        color = Color.Green,
                        text = " > $str"
                    )
                }
            }
        }
    }
}
