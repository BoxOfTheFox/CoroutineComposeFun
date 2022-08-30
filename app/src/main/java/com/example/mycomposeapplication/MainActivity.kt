package com.example.mycomposeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.mycomposeapplication.ui.theme.MyComposeApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CoroutineFunApp() }
    }
}

@Composable
fun CoroutineFunApp() {
    MyComposeApplicationTheme {
        val navController = rememberNavController()
        CoroutineFunNavHost(navController)
    }
}
