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
import com.example.mycomposeapplication.ui.CategoryGrid
import com.example.mycomposeapplication.ui.ExampleExecutor
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
    screen: Screen,
    onCardSelected: (String) -> Unit
) {
    Scaffold { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            Text(
                text = screen.title,
                style = MaterialTheme.typography.h4
            )
            Text(
                text = screen.description,
                modifier = Modifier.paddingFromBaseline(bottom = 12.dp)
            )
            if (screen is Parent)
                CategoryGrid(
                    modifier = Modifier.fillMaxSize(),
                    cards = screen.mapToCardMetadataList(),
                    onCardSelected = onCardSelected
                )
            else if (screen is Example)
                ExampleExecutor(screen)
        }
    }
}

private fun Parent.mapToCardMetadataList() = cards.map {
    CardMetadata(
        it.title,
        it.shortDescription,
        if (it is Example)
            "${Main.route}/$route?example=${it.route}"
        else
            "${Main.route}/${it.route}"
    )
}
