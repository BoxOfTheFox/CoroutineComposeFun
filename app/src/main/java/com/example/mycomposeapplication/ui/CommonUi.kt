package com.example.mycomposeapplication.ui

import android.os.Build
import android.webkit.WebSettings
import androidx.annotation.RawRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.launch
import java.io.BufferedReader

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
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
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
            // todo scroll to bottom
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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ExampleViewPager(description: String, @RawRes code: Int) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val pages = listOf("Description", "Code")
    TabRow(
        modifier = Modifier.padding(top = 4.dp),
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }
    ) {
        pages.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(index)
                    }
                },
            )
        }
    }

    HorizontalPager(
        modifier = Modifier.fillMaxHeight(0.4f),
        count = pages.size,
        state = pagerState,
    ) { page ->
        if (page == 0) {
            Text(
                text = description,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp)
                    .padding(horizontal = 16.dp)
            )
        } else {
            val isDarkMode = isSystemInDarkTheme()
            WebView(
                state = rememberWebViewState(url = ""),
                onCreated = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it.isForceDarkAllowed && isDarkMode) {
                        it.settings.forceDark = WebSettings.FORCE_DARK_ON
                    }
                    it.loadData(
                        it.resources
                            .openRawResource(code)
                            .bufferedReader()
                            .use(BufferedReader::readText),
                        "text/html; charset=utf-8",
                        "UTF-8"
                    )
                },
                captureBackPresses = false
            )
        }
    }
}