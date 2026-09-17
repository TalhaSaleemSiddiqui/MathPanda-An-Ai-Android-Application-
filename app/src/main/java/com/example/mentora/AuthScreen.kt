package com.example.mentora

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

@Composable
fun AuthScreen(onAuthSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }


    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    // Vibrant & Better Contrast Colors
    val greenColor = Color(0xFF16A34A)
    val bgColor = Color(0xFFF0FDF4)
    val darkText = Color(0xFF111827)
    val grayText = Color(0xFF4B5563)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.panda_avatar),
            contentDescription = "Panda Avatar",
            modifier = Modifier.size(110.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isLogin) {
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = greenColor)) {
                        append("Math")
                    }
                    append("Panda")
                }
            } else {
                buildAnnotatedString {
                    append("Create Account")
                }
            },
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = darkText
        )

        Text(
            text = if (isLogin) "Please log in to continue your learning journey." else "Join MathPanda and start learning!",
            fontSize = 14.sp,
            color = grayText,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        // Form Fields
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!isLogin) {
                Text("Full Name", fontSize = 13.sp, color = darkText, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Enter your full name", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Text(if (isLogin) "Email" else "Email", fontSize = 13.sp, color = darkText, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                placeholder = { Text(if (isLogin) "Enter email" else "Enter your email", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = greenColor,
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Text("Password", fontSize = 13.sp, color = darkText, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                placeholder = { Text(if (isLogin) "Enter password" else "Create a password", color = Color.Gray) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle Password", tint = Color.Gray)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = greenColor,
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )


            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }


            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Fields cannot be empty"
                        return@Button
                    }
                    if (!isLogin && name.isBlank()) {
                        errorMessage = "Please enter your name"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    if (isLogin) {
                        // --- LOGIN LOGIC ---
                        FirebaseManager.logIn(email, password) { success, errorMsg ->
                            isLoading = false
                            if (success) {

                                val savedName = FirebaseManager.auth.currentUser?.displayName
                                val finalName = if (!savedName.isNullOrBlank()) savedName else "Learner"
                                onAuthSuccess(finalName)
                            } else {
                                errorMessage = errorMsg ?: "Login failed"
                            }
                        }
                    } else {
                        // --- SIGNUP LOGIC ---
                        val userNameToSave = if (name.isNotBlank()) name else "Learner"
                        FirebaseManager.signUp(userNameToSave, email, password) { success, errorMsg ->
                            isLoading = false
                            if (success) {
                                onAuthSuccess(userNameToSave)
                            } else {
                                errorMessage = errorMsg ?: "Signup failed"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isLogin) "Login" else "Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))


        Row(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(if (isLogin) "Don't have an account? " else "Already have an account? ", color = grayText, fontSize = 15.sp)
            Text(
                text = if (isLogin) "Sign up" else "Login",
                color = greenColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable {
                    if (!isLoading) {
                        isLogin = !isLogin
                        password = ""
                        email = ""
                        errorMessage = null
                    }
                }
            )
        }
    }
}