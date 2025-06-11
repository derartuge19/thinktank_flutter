package com.example.thinktank.Pages.Dashboard

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinktank.R

@Composable
fun DrawerContent(
    onNavigate: (String) -> Unit,
    onDrawerClose: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val backgroundColor = Color(0xFF1A1A1A)
    val accentColor = Color(0xFFFFA500)
    val fontFamily = FontFamily(
        Font(R.font.kellyslab_regular, FontWeight.Normal)
    )

    val menuItems = listOf(
        MenuItem("Dashboard", Icons.Default.Home, "dashboard"),
        MenuItem("User Profile", Icons.Default.Person, "profile"),
        MenuItem("Idea Submission", Icons.Default.Add, "idea_submission"),
        MenuItem("Feedback Pool", Icons.Default.RateReview, "feedback_pool"),
        MenuItem("Logout", Icons.Default.ExitToApp, "logout"),
        MenuItem("Exit", Icons.Default.Close, "landing")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Drawer Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Text(
                text = "Menu",
                color = accentColor,
                fontSize = 24.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        // Menu Items
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            menuItems.forEach { item ->
                DrawerMenuItem(
                    item = item,
                    onClick = {
                        if (item.route == "idea_submission") {
                            navController.navigate(item.route)
                        } else if (item.route == "landing" && activity != null) {
                            activity.finish()
                        } else {
                            navController.navigate(item.route)
                        }
                        onDrawerClose()
                    }
                )
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    item: MenuItem,
    onClick: () -> Unit
) {
    val backgroundColor = Color(0xFF1A1A1A)
    val accentColor = Color(0xFFFFA500)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)
