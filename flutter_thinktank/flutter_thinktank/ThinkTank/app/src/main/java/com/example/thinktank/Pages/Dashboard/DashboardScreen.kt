package com.example.thinktank.Pages.Dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.thinktank.data.TokenManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenManager = TokenManager(context.applicationContext as android.app.Application)
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        val token = tokenManager.getToken()
        println("\n=== Dashboard Screen Launched ===")
        println("Token retrieved: ${token != null}")
        if (token != null) {
            viewModel.loadApprovedIdeas(token)
        } else {
            println("No token found, redirecting to login")
            navController.navigate("login") {
                popUpTo("dashboard") { inclusive = true }
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFFFA500)
                )
            }
            uiState.error?.isNotEmpty() == true -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.error ?: "An error occurred",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val token = tokenManager.getToken()
                            if (token != null) {
                                viewModel.loadApprovedIdeas(token)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA500)
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }
            else -> {
                DashboardContent(
                    uiState = uiState,
                    onMenuClick = { viewModel.onDrawerOpen() },
                    onDrawerClose = { viewModel.onDrawerClose() },
                    onNavigate = { route -> navController.navigate(route) },
                    navController = navController
                )
            }
        }
    }
}
