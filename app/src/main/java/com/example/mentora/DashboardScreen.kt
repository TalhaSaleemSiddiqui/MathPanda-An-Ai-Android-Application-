package com.example.mentora

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(userName: String, onNavigateToChat: (String?) -> Unit) {
    val bgColor = Color(0xFFF0FDF4)
    val darkText = Color(0xFF111827)
    val mathGreenText = Color(0xFF16A34A)
    val grayText = Color(0xFF4B5563)
    val lightGreenCard = Color(0xFFDCFCE7)

    var questionInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // --- TOP APP BAR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Math (Green) Panda (Black)
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = mathGreenText)) {
                        append("Math")
                    }
                    withStyle(style = SpanStyle(color = darkText)) {
                        append("Panda")
                    }
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Hello, UserName aur Subtitle
            Text(text = "Hello, $userName", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = darkText)
            Text(text = "Let's make math fun today!", fontSize = 16.sp, color = grayText)
        }


            Image(
                painter = painterResource(id = R.drawable.intro),
                contentDescription = "Panda Intro Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .offset(x = 4.dp)
            )


        Spacer(modifier = Modifier.height(16.dp)) // Spacing thori behtar ki

        // --- INPUT BOX SECTION ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = questionInput,
                    onValueChange = { questionInput = it },
                    placeholder = { Text("Type your math question...", color = Color.LightGray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (questionInput.isNotBlank()) {
                                onNavigateToChat(questionInput)
                            } else {
                                onNavigateToChat(null)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(mathGreenText, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp).offset(x = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- EXAMPLES / TOPICS SECTION ---
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Try these examples", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = grayText)

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExampleCard(
                    iconTxt = "x²",
                    title = "Solve equation",
                    subtitle = "2x + 5 = 15",
                    iconBgColor = Color(0xFFE0E7FF),
                    onClick = { onNavigateToChat("Solve equation 2x + 5 = 15") }
                )
                ExampleCard(
                    iconTxt = "d/dx",
                    title = "Derivative",
                    subtitle = "d/dx (x² + 3x)",
                    iconBgColor = Color(0xFFDBEAFE),
                    onClick = { onNavigateToChat("Find the derivative of x² + 3x") }
                )
            }
        }
    }
}

@Composable
fun ExampleCard(iconTxt: String, title: String, subtitle: String, iconBgColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconTxt, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Texts
                Column {
                    Text(title, fontSize = 14.sp, color = Color.Gray)
                    Text(subtitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = Color.LightGray)
        }
    }
}