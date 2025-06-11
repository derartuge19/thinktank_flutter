@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.thinktank.Pages.Feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thinktank.data.models.Idea
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.thinktank.data.TokenManager
import android.app.Application
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

sealed class AdminFeedbackRoute(val route: String) {
    object IdeaPool : AdminFeedbackRoute("idea_pool")
    object FeedbackDetail : AdminFeedbackRoute("feedback_detail")
    object ReviewedIdeas : AdminFeedbackRoute("reviewed_ideas")
    object EditFeedback : AdminFeedbackRoute("edit_feedback")
}

@Composable
fun AdminFeedbackNav(
    token: String,
    navController: NavHostController,
    feedbackViewModel: FeedbackViewModel,
    onEditFeedback: (Idea) -> Unit,
    onDeleteFeedback: (Idea) -> Unit
) {
    val localNavController = rememberNavController()
    val uiState by feedbackViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext as Application) }
    val userRole = tokenManager.getUserRole()
    val isAdmin = userRole == "admin"

    if (!isAdmin) {
        LaunchedEffect(Unit) {
            navController.navigate("dashboard") {
                popUpTo("feedback_pool") { inclusive = true }
            }
        }
        return
    }

    NavHost(navController = localNavController, startDestination = AdminFeedbackRoute.IdeaPool.route) {
        composable(AdminFeedbackRoute.IdeaPool.route) {
            feedbackViewModel.loadIdeas(token)
            IdeaPoolListScreen(
                ideas = uiState.ideas,
                onReviewIdea = { idea ->
                    feedbackViewModel.selectIdea(idea)
                    localNavController.navigate(AdminFeedbackRoute.FeedbackDetail.route)
                },
                onViewReviewedIdeas = {
                    localNavController.navigate(AdminFeedbackRoute.ReviewedIdeas.route)
                },
                onBack = { 
                    navController.navigate("dashboard") {
                        popUpTo("feedback_pool") { inclusive = true }
                    }
                }
            )
        }
        composable(AdminFeedbackRoute.FeedbackDetail.route) {
            val selectedIdea = uiState.selectedIdea
            if (selectedIdea != null) {
                FeedbackScreen(
                    navController = localNavController,
                    viewModel = feedbackViewModel
                )
            }
        }
        composable(AdminFeedbackRoute.ReviewedIdeas.route) {
            LaunchedEffect(Unit) {
                feedbackViewModel.loadApprovedIdeas(token)
            }
            ReviewedIdeasScreen(
                ideas = uiState.ideas,
                onBack = { 
                    navController.navigate("dashboard") {
                        popUpTo("feedback_pool") { inclusive = true }
                    }
                },
                onEditFeedback = { idea ->
                    feedbackViewModel.selectIdea(idea)
                    localNavController.navigate(AdminFeedbackRoute.EditFeedback.route)
                },
                onDeleteFeedback = { idea ->
                    feedbackViewModel.deleteFeedback(token, idea.id.toInt())
                },
                isAdmin = true,
                isLoading = uiState.isLoading,
                error = uiState.error
            )
        }
        composable(AdminFeedbackRoute.EditFeedback.route) {
            val selectedIdea = uiState.selectedIdea
            if (selectedIdea != null) {
                EditFeedbackScreen(
                    idea = selectedIdea,
                    currentFeedback = selectedIdea.feedback?.firstOrNull()?.comment ?: "",
                    onSave = { newFeedback ->
                        feedbackViewModel.updateFeedback(
                            token = token,
                            ideaId = selectedIdea.id.toInt(),
                            feedback = newFeedback
                        )
                        navController.navigate("dashboard") {
                            popUpTo("feedback_pool") { inclusive = true }
                        }
                    },
                    onBack = { 
                        navController.navigate("dashboard") {
                            popUpTo("feedback_pool") { inclusive = true }
                        }
                    },
                    isLoading = uiState.isLoading,
                    error = uiState.error
                )
            }
        }
    }
} 