package com.example.mycomposeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mycomposeapplication.ui.theme.MyComposeApplicationTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {
    private val viewModel: CoroutineViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyComposeApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // todo disable buttons on run
                            items(
                                listOf(
                                    "cancellation" to { viewModel.cancellation() },
                                    "exception3" to { viewModel.exception3() }
                                )
                            ) { item ->
                                Button(
                                    onClick = item.second,
                                    enabled = viewModel.enableButton.first()
                                ) {
                                    Text(text = item.first)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        CoroutineTests(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun CoroutineTests(viewModel: CoroutineViewModel) {
    Surface(
        color = Color.Black,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = 1.dp
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

