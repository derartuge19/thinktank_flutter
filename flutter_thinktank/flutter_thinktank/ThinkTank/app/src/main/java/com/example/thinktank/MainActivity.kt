package com.example.thinktank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.thinktank.Pages.Dashboard.DashboardScreen
import com.example.thinktank.Pages.EditProfile.EditProfileScreen
import com.example.thinktank.Pages.IdeaSubmition.IdeaSubmissionScreen
import com.example.thinktank.Pages.Ideas.IdeasScreen
import com.example.thinktank.Pages.LandingPage
import com.example.thinktank.Pages.LoginPage.Login
import com.example.thinktank.Pages.LoginPage.LoginViewModel
import com.example.thinktank.Pages.LogoutScreen
import com.example.thinktank.Pages.Register.Register
import com.example.thinktank.viewmodels.RegisterViewModel
import com.example.thinktank.Pages.Profile.ProfileScreen
import com.example.thinktank.Pages.Profile.ProfileViewModel
import com.example.thinktank.Pages.Profile.ProfileUiState
import com.example.thinktank.Pages.MyIdeas.MyIdeasScreen
import com.example.thinktank.Pages.MyIdeas.MyIdeasViewModel
import com.example.thinktank.Pages.MyIdeas.NavigationEvent
import com.example.thinktank.data.models.Idea
import com.example.thinktank.ui.theme.ThinkTankTheme
import com.example.thinktank.Pages.EditIdea.EditIdeaScreen
import com.example.thinktank.Pages.EditIdea.EditIdeaViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.thinktank.Pages.Feedback.AdminFeedbackNav
import com.example.thinktank.data.TokenManager
import com.example.thinktank.Pages.Feedback.FeedbackViewModel
import com.example.thinktank.Pages.Feedback.AdminFeedbackRoute
import com.example.thinktank.Pages.IdeaSubmition.IdeaSubmissionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThinkTankTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val profileViewModel: ProfileViewModel = viewModel()
                    val scope = rememberCoroutineScope()
                    val tokenManager = TokenManager(application)

                    NavHost(navController = navController, startDestination = "landing") {
                        composable("landing") {
                            LandingPage(
                                onNavigateToSignup = { navController.navigate("register") },
                                onNavigateToLogin = { navController.navigate("login") }
                            )
                        }

                        composable("login") {
                            val viewModel: LoginViewModel = viewModel()
                            Login(
                                onNavigateToSignup = { navController.navigate("register") },
                                onNavigateBack = { navController.popBackStack() },
                                onLoginSuccess = { 
                                    profileViewModel.loadProfile()
                                    kotlinx.coroutines.MainScope().launch {
                                        // Wait for profile to load
                                        kotlinx.coroutines.delay(1000)
                                        navController.navigate("dashboard") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("register") {
                            val viewModel: RegisterViewModel = viewModel()
                            Register(
                                onNavigateToLogin = { navController.navigate("login") },
                                onNavigateToHome = { 
                                    profileViewModel.loadProfile()
                                    kotlinx.coroutines.MainScope().launch {
                                        // Wait for profile to load
                                        kotlinx.coroutines.delay(1000)
                                        navController.navigate("dashboard") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("dashboard") {
                            DashboardScreen(navController = navController)
                        }

                        composable("profile") {
                            val uiState = profileViewModel.uiState.collectAsState().value
                            
                            // Load profile when screen is first shown
                            LaunchedEffect(Unit) {
                                profileViewModel.loadProfile()
                            }
                            
                            ProfileScreen(
                                uiState = uiState,
                                onBackClick = { navController.navigateUp() },
                                onMenuClick = { /* Handle menu click */ },
                                onEditProfile = { /* Handle edit profile click */ },
                                onEditProfileNavigate = { navController.navigate("edit_profile") },
                                onToggleFeedback = { profileViewModel.toggleFeedbackExpanded() },
                                onLogout = {
                                    profileViewModel.logout()
                                    navController.navigate("landing") {
                                        popUpTo("profile") { inclusive = true }
                                    }
                                },
                                onEditIdea = { idea: Idea ->
                                    navController.navigate("edit_idea/${idea.id}")
                                },
                                onDeleteIdea = { ideaId: String ->
                                    profileViewModel.deleteIdea(ideaId)
                                },
                                onViewMyIdeas = {
                                    navController.navigate("my_ideas")
                                },
                                onToggleDrawer = { profileViewModel.toggleDrawer() },
                                onRefreshProfile = { profileViewModel.loadProfile() },
                                onDeleteAccount = {
                                    profileViewModel.deleteUser()
                                    navController.navigate("landing") {
                                        popUpTo("profile") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("edit_profile") {
                            EditProfileScreen(onBackClick = { 
                                profileViewModel.loadProfile()
                                navController.popBackStack() 
                            })
                        }

                        composable("my_ideas") {
                            val viewModel: MyIdeasViewModel = viewModel()
                            val uiState = viewModel.uiState.collectAsState().value

                            LaunchedEffect(uiState.navigationEvent) {
                                when (val event = uiState.navigationEvent) {
                                    is NavigationEvent.EditIdea -> {
                                        navController.navigate("edit_idea/${event.idea.id}")
                                    }
                                    null -> {}
                                }
                            }

                            MyIdeasScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() },
                                onEditIdea = { idea: Idea ->
                                    navController.navigate("edit_idea/${idea.id}")
                                }
                            )
                        }

                        composable("feedback_pool") {
                            val token = "Bearer ${tokenManager.getToken()}"
                            val userRole = tokenManager.getUserRole()
                            
                            if (userRole == "admin") {
                                val feedbackViewModel = viewModel<FeedbackViewModel>()
                                AdminFeedbackNav(
                                    token = token,
                                    navController = navController,
                                    feedbackViewModel = feedbackViewModel,
                                    onEditFeedback = { idea: Idea ->
                                        feedbackViewModel.selectIdea(idea)
                                        navController.navigate(AdminFeedbackRoute.EditFeedback.route)
                                    },
                                    onDeleteFeedback = { idea: Idea ->
                                        feedbackViewModel.deleteFeedback(token, idea.id.toInt())
                                    }
                                )
                            } else {
                                // Redirect non-admin users to dashboard
                                LaunchedEffect(Unit) {
                                    navController.navigate("dashboard") {
                                        popUpTo("feedback_pool") { inclusive = true }
                                    }
                                }
                            }
                        }

                        composable("edit_idea/{ideaId}") { backStackEntry ->
                            val ideaId = backStackEntry.arguments?.getString("ideaId") ?: return@composable
                            val viewModel: EditIdeaViewModel = viewModel()
                            val myIdeasViewModel: MyIdeasViewModel = viewModel()
                            
                            LaunchedEffect(ideaId) {
                                viewModel.loadIdea(ideaId)
                            }

                            EditIdeaScreen(
                                viewModel = viewModel,
                                onBackClick = { 
                                    scope.launch {
                                        myIdeasViewModel.loadIdeas()
                                        navController.popBackStack()
                                    }
                                },
                                ideaId = ideaId
                            )
                        }

                        composable("logout") {
                            LogoutScreen(navController)
                        }

                        composable("idea_submission") {
                            val viewModel: IdeaSubmissionViewModel = viewModel()
                            val scope = rememberCoroutineScope()
                            
                            LaunchedEffect(Unit) {
                                viewModel.onSubmissionSuccess = {
                                    scope.launch {
                                        navController.navigate("my_ideas") {
                                            popUpTo("idea_submission") { inclusive = true }
                                        }
                                    }
                                }
                            }

                            IdeaSubmissionScreen(
                                onNavigateToIdeas = {
                                    scope.launch {
                                        navController.navigate("my_ideas") {
                                            popUpTo("idea_submission") { inclusive = true }
                                        }
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("ideas") {
                            IdeasScreen(
                                onNavigateToEdit = { idea: Idea ->
                                    navController.navigate("edit_idea/${idea.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
