package com.example.mentora

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    // Theme Colors
    val bgColor = Color(0xFFF0FDF4)
    val mathGreenText = Color(0xFF16A34A)
    val darkText = Color(0xFF111827)

    val currentUser = FirebaseAuth.getInstance().currentUser


    var questionCount by remember { mutableStateOf("...") }
    var daysJoined by remember { mutableStateOf("...") }
    val userName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Learner"
    val userEmail = currentUser?.email ?: "No Email Linked"


    LaunchedEffect(Unit) {
        // 1. Calculate Days Joined (Account Age)
        val creationTime = currentUser?.metadata?.creationTimestamp ?: System.currentTimeMillis()
        val diffInMillies = abs(System.currentTimeMillis() - creationTime)
        val diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
        daysJoined = if (diffInDays == 0L) "1st Day" else "${diffInDays + 1} Days"

        // 2. Fetch Total Questions Asked
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .collection("history")
                .get()
                .addOnSuccessListener { result ->
                    questionCount = result.size().toString()
                }
                .addOnFailureListener {
                    questionCount = "0"
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Profile Image
        Image(
            painter = painterResource(id = R.drawable.panda_avatar),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))


        Text(text = userName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = darkText)
        Text(text = userEmail, fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(32.dp))

        // --- STATS ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Questions Asked",
                value = questionCount,
                color = mathGreenText,
                modifier = Modifier.weight(1f).height(100.dp)
            )
            StatCard(
                title = "Days Joined",
                value = daysJoined,
                color = mathGreenText,
                modifier = Modifier.weight(1f).height(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- LOGOUT BUTTON ---
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Log Out", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}