package com.example.mentora

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

// Transparent Scrollbar Modifier
fun Modifier.simpleVerticalScrollbar(state: LazyListState): Modifier = drawWithContent {
    drawContent()
    val totalItems = state.layoutInfo.totalItemsCount
    if (totalItems > 0) {
        val visibleItems = state.layoutInfo.visibleItemsInfo.size
        val firstVisible = state.firstVisibleItemIndex

        if (visibleItems < totalItems) {
            val scrollbarHeight = size.height * (visibleItems.toFloat() / totalItems)
            val scrollbarY = size.height * (firstVisible.toFloat() / totalItems)
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.2f),
                topLeft = Offset(size.width - 12f, scrollbarY),
                size = Size(8f, scrollbarHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}

data class ChatMessage(val isUser: Boolean, val text: String)

@Composable
fun InteractionScreen(
    initialMessage: String? = null,
    onQuestionAsked: () -> Unit,
    onHistoryAdded: (String, String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(listOf(ChatMessage(false, "Hello! I'm MathPanda, your AI Tutor, eager to help you with your questions!")))
    }
    var isProcessing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = results?.get(0) ?: ""
            if (recognizedText.isNotEmpty()) {
                input = if (input.isEmpty()) recognizedText else "$input $recognizedText"
            }
        }
    }

    val bgColor = Color(0xFFF0FDF4)
    val mathGreenText = Color(0xFF16A34A)
    val darkText = Color(0xFF111827)


    fun sendMessageToBackend(userText: String) {
        if (userText.isBlank() || isProcessing) return

        messages = messages + ChatMessage(true, userText)
        input = ""
        isProcessing = true
        onQuestionAsked()

        coroutineScope.launch {
            try {

                messages = messages + ChatMessage(false, "Thinking...")

                val steps = RetrofitClient.api.getLesson(TutorRequest(userText))


                messages = messages.dropLast(1) + ChatMessage(false, "")

                var fullText = ""
                for (step in steps) {

                    fullText = if (fullText.isEmpty()) step.speech else "$fullText ${step.speech}"


                    messages = messages.dropLast(1) + ChatMessage(false, fullText)

                    // Audio Playback call
                    AudioPlayer.playBase64Audio(context.cacheDir, step.audio)
                    kotlinx.coroutines.delay(800)
                }

                // 1. Local history
                onHistoryAdded(userText, fullText)

                // 2. Firebase Database history
                try {
                    FirebaseManager.saveHistory(userText, fullText)
                    android.util.Log.d("FirebaseTest", "History has been sent to Firebase!")
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseTest", "Firebase Save Error: ", e)
                }

            } catch (e: Exception) {
                android.util.Log.e("MentoraError", "Error: ", e)
                val errorMessage = when (e) {
                    is java.net.SocketTimeoutException -> "Connection Timeout!"
                    is java.net.ConnectException -> "Connection Refused!"
                    else -> "Oops! Network Error."
                }
                messages = messages.dropLast(1) + ChatMessage(false, errorMessage)
            } finally {
                isProcessing = false
            }
        }
    }


    LaunchedEffect(initialMessage) {
        if (!initialMessage.isNullOrBlank()) {
            sendMessageToBackend(initialMessage)
        }
    }


    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // --- TOP HEADER ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.panda_avatar),
                contentDescription = "Panda Profile",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = mathGreenText)) { append("Math") }
                        withStyle(style = SpanStyle(color = darkText)) { append("Panda") }
                    },
                    fontSize = 20.sp, fontWeight = FontWeight.ExtraBold
                )
                Text("AI Tutor", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }

        // --- CHAT HISTORY ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .simpleVerticalScrollbar(listState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(messages) { msg ->
                if (msg.isUser) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Card(
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = mathGreenText)
                        ) {
                            Text(text = msg.text, color = Color.White, modifier = Modifier.padding(12.dp))
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Top) {
                        Image(
                            painter = painterResource(id = R.drawable.panda_avatar),
                            contentDescription = "AI Avatar",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = msg.text, color = darkText)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Write your question here...") },
                enabled = !isProcessing,
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = mathGreenText,
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = { sendMessageToBackend(input) },
                modifier = Modifier
                    .size(52.dp)
                    .background(mathGreenText, CircleShape),
                enabled = !isProcessing
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier
                        .size(22.dp)
                        .offset(x = 2.dp)
                )
            }
        }
    }
}