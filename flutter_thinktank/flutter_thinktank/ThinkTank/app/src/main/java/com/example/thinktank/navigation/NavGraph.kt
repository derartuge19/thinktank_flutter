import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.thinktank.Pages.IdeaSubmition.IdeaSubmissionScreen
import com.example.thinktank.Pages.Ideas.IdeasScreen
import com.example.thinktank.Pages.EditIdea.EditIdeaScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object IdeaSubmission : Screen("idea_submission")
    object Ideas : Screen("ideas")
    object EditIdea : Screen("edit_idea/{ideaId}") {
        fun createRoute(ideaId: String) = "edit_idea/$ideaId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ... existing composables ...
        
        composable(Screen.IdeaSubmission.route) {
            IdeaSubmissionScreen(
                onNavigateToIdeas = {
                    navController.navigate(Screen.Ideas.route) {
                        popUpTo(Screen.IdeaSubmission.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Ideas.route) {
            IdeasScreen(
                onNavigateToEdit = { idea ->
                    navController.navigate(Screen.EditIdea.createRoute(idea.id))
                }
            )
        }
        
        composable(
            route = Screen.EditIdea.route,
            arguments = listOf(
                navArgument("ideaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val ideaId = backStackEntry.arguments?.getString("ideaId") ?: return@composable
            EditIdeaScreen(
                ideaId = ideaId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
} 