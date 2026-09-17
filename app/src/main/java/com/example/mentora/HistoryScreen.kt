package com.example.mentora

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class HistorySession(val id: String, val query: String, val summary: String)

@Composable
fun HistoryScreen(
    onHistoryItemClick: (HistorySession) -> Unit = {}
) {
    val bgColor = Color(0xFFF0FDF4)
    val mathGreenText = Color(0xFF16A34A)
    val darkText = Color(0xFF111827)

    // States for Data and UI
    var historyList by remember { mutableStateOf<List<HistorySession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedSession by remember { mutableStateOf<HistorySession?>(null) }
    val listState = rememberLazyListState()


    LaunchedEffect(Unit) {
        FirebaseManager.getHistory { fetchedData ->

            val mappedList = fetchedData.mapIndexed { index, dataMap ->
                HistorySession(
                    id = index.toString(),
                    query = dataMap["query"] as? String ?: "No Question",
                    summary = dataMap["summary"] as? String ?: "No Answer"
                )
            }
            historyList = mappedList
            isLoading = false
        }
    }

    BackHandler(enabled = selectedSession != null) {
        selectedSession = null
    }

    if (selectedSession != null) {
        HistoryDetailScreen(
            session = selectedSession!!,
            onBackClick = { selectedSession = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = mathGreenText,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("History", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = darkText)
            }


            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = mathGreenText)
                }
            } else if (historyList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No history yet. Ask MathPanda a question!", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .simpleVerticalScrollbar(listState, color = mathGreenText)
                ) {
                    items(historyList) { session ->
                        HistoryCard(
                            session = session,
                            onClick = {
                                selectedSession = session
                                onHistoryItemClick(session)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(session: HistorySession, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.query,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = session.summary,
                fontSize = 14.sp,
                color = Color.DarkGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HistoryDetailScreen(session: HistorySession, onBackClick: () -> Unit) {
    val bgColor = Color(0xFFF0FDF4)
    val mathGreenText = Color(0xFF16A34A)
    val darkText = Color(0xFF111827)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = darkText)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Session Details", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = darkText)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = session.query,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = darkText
                )
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFE5E7EB))

                Text("Conversation Summary:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = darkText)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = session.summary,
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    color: Color = Color.Gray
): Modifier = drawWithContent {
    drawContent()
    val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: return@drawWithContent
    val needDrawScrollbar = state.layoutInfo.totalItemsCount > state.layoutInfo.visibleItemsInfo.size

    if (needDrawScrollbar) {
        val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
        val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
        val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

        drawRect(
            color = color,
            topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
            size = Size(width.toPx(), scrollbarHeight),
            alpha = 0.5f
        )
    }
}