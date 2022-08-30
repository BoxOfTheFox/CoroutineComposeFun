package com.example.mycomposeapplication.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
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

@Composable
fun DetailScreen(title: String, viewModel: AbstractDetailViewModel = viewModel()) {
    Scaffold { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.h4
            )
            Text(
                text = ("Composem ipsum color sit lazy, padding theme elit, sed do bouncy.").repeat(
                    4
                ),
                modifier = Modifier.paddingFromBaseline(bottom = 12.dp)
            )
            CoroutineTests(viewModel)
        }
    }
}

@Composable
fun CoroutineTests(viewModel: AbstractDetailViewModel) {
    var showFab by remember { mutableStateOf(true) }
    Scaffold(
        floatingActionButton = {
            if (showFab)
                FloatingActionButton(onClick = {
                    showFab = false
                    viewModel.execute()
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