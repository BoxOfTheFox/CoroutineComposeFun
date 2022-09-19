package com.example.mycomposeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.mycomposeapplication.data.*
import com.example.mycomposeapplication.ui.CardGrid
import com.example.mycomposeapplication.ui.ExampleCard
import com.example.mycomposeapplication.ui.theme.MyComposeApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyComposeApplicationTheme {
                val navController = rememberNavController()
                AppNavHost(navController)
            }
        }
    }
}

@Composable
fun MainScreen(
    card: Card,
    onCardSelected: (String) -> Unit
) {
    Scaffold { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.h4
            )
            Text(
                text = card.description,
                modifier = Modifier.paddingFromBaseline(bottom = 12.dp)
            )
            if (card is Node)
                CardGrid(
                    modifier = Modifier.fillMaxSize(),
                    cards = card.cards,
                    onCardSelected = onCardSelected
                )
            else if (card is Example)
                ExampleCard(example = card)
        }
    }
}
