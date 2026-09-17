package com.example.mentora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MentoraTheme {
                MentoraApp()
            }
        }
    }
}

@Composable
fun MentoraTheme(content: @Composable () -> Unit) {
    val MentoraGreen = Color(0xFF16A34A)
    val MentoraLightGreen = Color(0xFFDCFCE7)
    val MentoraBackground = Color(0xFFF0FDF4)

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = MentoraGreen,
            secondary = MentoraLightGreen,
            background = MentoraBackground,
            surface = Color.White
        ),
        content = content
    )
}

@Composable
fun MentoraApp() {
    val navController = rememberNavController()
    var currentUserName by remember { mutableStateOf("Student") }


    val sharedHistory = remember { mutableStateListOf<HistorySession>() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf("home", "history", "profile")) {
                NavigationBar(
                    modifier = Modifier
                        .padding(horizontal = 0.dp, vertical = 0.dp)
                        .clip(RectangleShape)
                        .border(0.dp, Color.Green, RectangleShape),
                    containerColor = Color(0xFF16A34A),
                )  {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontWeight = FontWeight.Bold) },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            unselectedIconColor = Color.White,
                            selectedTextColor = Color.Black,
                            unselectedTextColor = Color.White,
                            indicatorColor = Color(0xFFDCFCE7)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "History") },
                        label = { Text("History", fontWeight = FontWeight.Bold) },
                        selected = currentRoute == "history",
                        onClick = {
                            navController.navigate("history") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            unselectedIconColor = Color.White,
                            selectedTextColor = Color.Black,
                            unselectedTextColor = Color.White,
                            indicatorColor = Color(0xFFDCFCE7)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontWeight = FontWeight.Bold) },
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            unselectedIconColor = Color.White,
                            selectedTextColor = Color.Black,
                            unselectedTextColor = Color.White,
                            indicatorColor = Color(0xFFDCFCE7)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "auth",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(onAuthSuccess = { name ->
                    currentUserName = name
                    navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                })
            }
            composable("home") {
                DashboardScreen(
                    userName = currentUserName,
                    onNavigateToChat = { query ->
                        if (query != null) {
                            navController.currentBackStackEntry?.savedStateHandle?.set("initialQuery", query)
                        }
                        navController.navigate("ai_tutor")
                    }
                )
            }
            composable("ai_tutor") {
                val initialQuery = remember {
                    val query = navController.previousBackStackEntry?.savedStateHandle?.get<String>("initialQuery")
                    navController.previousBackStackEntry?.savedStateHandle?.remove<String>("initialQuery")
                    query
                }

                InteractionScreen(
                    initialMessage = initialQuery,
                    onQuestionAsked = { },
                    onHistoryAdded = { question, answer ->
                        sharedHistory.add(
                            0,
                            HistorySession(
                                id = System.currentTimeMillis().toString(),
                                query = question,
                                summary = answer
                            )
                        )
                    }
                )
            }

            composable("history") {
                HistoryScreen()
            }

            composable("profile") {
                ProfileScreen(
                    onLogout = {

                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}