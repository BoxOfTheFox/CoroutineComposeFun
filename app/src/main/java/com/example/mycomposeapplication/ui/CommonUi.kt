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
import com.example.mycomposeapplication.data.Card
import com.example.mycomposeapplication.data.Example
import com.example.mycomposeapplication.data.MainNode

@Composable
fun CardGrid(
    modifier: Modifier = Modifier,
    cards: List<Card>,
    onCardSelected: (String) -> Unit
){
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(cards) { item ->
            MainCard(card = item, onClick = onCardSelected)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainCard(
    modifier: Modifier = Modifier,
    card: Card,
    onClick: (String) -> Unit
) {
    Card(
        elevation = 1.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 80.dp)
            .fillMaxWidth(),
        onClick = {
            onClick("${MainNode::class.java.name}/${card::class.java.name}")
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.h6,
                maxLines = 1
            )
            Text(
                text = card.shortDescription,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.paddingFromBaseline(28.dp),
                maxLines = 2
            )
        }
    }
}

@Composable
fun ExampleCard(
    modifier: Modifier = Modifier,
    example: Example,
    viewModel: MainViewModel = viewModel()
) {
    var showFab by remember { mutableStateOf(true) }
    Scaffold(
        modifier = modifier,
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
