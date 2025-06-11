package com.example.thinktank.Pages.Profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thinktank.R
import com.example.thinktank.data.models.Idea
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    onDrawerClose: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val backgroundColor = Color(0xFF1A1A1A)
    val accentColor = Color(0xFFFFA500)
    val fontFamily = FontFamily(
        Font(R.font.kellyslab_regular, FontWeight.Normal)
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
                text = "Account Options",
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
            DrawerMenuItem(
                title = "Logout",
                icon = Icons.Default.ExitToApp,
                onClick = {
                    onLogout()
                    onDrawerClose()
                }
            )
            DrawerMenuItem(
                title = "Delete Account",
                icon = Icons.Default.Delete,
                onClick = {
                    onDeleteAccount()
                    onDrawerClose()
                }
            )
        }
    }
}

@Composable
fun DrawerMenuItem(
    title: String,
    icon: ImageVector,
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
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onEditProfile: () -> Unit,
    onEditProfileNavigate: () -> Unit,
    onToggleFeedback: () -> Unit,
    onLogout: () -> Unit,
    onEditIdea: (Idea) -> Unit,
    onDeleteIdea: (String) -> Unit,
    onViewMyIdeas: () -> Unit,
    onToggleDrawer: () -> Unit,
    onRefreshProfile: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val fontFamily = FontFamily(
        Font(R.font.kellyslab_regular, FontWeight.Normal)
    )
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backgroundColor = Color(0xFF1A1A1A)
    val accentColor = Color(0xFFFFA500)
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Show error dialog if there's an error
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { /* Handle dismiss */ },
            title = { Text("Error") },
            text = { Text(uiState.error) },
            confirmButton = {
                TextButton(onClick = { /* Handle confirm */ }) {
                    Text("OK")
                }
            }
        )
    }

    // Show delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteAccount()
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Refresh profile data when returning from edit screen
    LaunchedEffect(key1 = 1) {
        onRefreshProfile()
    }

    LaunchedEffect(uiState.isDrawerOpen) {
        if (uiState.isDrawerOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (drawerState.currentValue != DrawerValue.Closed) {
                DrawerContent(
                    onDrawerClose = {
                        scope.launch {
                            drawerState.close()
                            onToggleDrawer()
                        }
                    },
                    onLogout = onLogout,
                    onDeleteAccount = {
                        scope.launch {
                            drawerState.close()
                            onToggleDrawer()
                            showDeleteConfirmation = true
                        }
                    }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Top Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Profile",
                            fontSize = 28.sp,
                            fontFamily = fontFamily,
                            color = Color(0xFFFFA500)
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onToggleDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    }
                }

                item {
                    // Profile Header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(80.dp)) {
                                    Image(
                                        painter = painterResource(R.drawable.user_profile_icon),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color(0xFFFFA500), CircleShape)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Photo",
                                        tint = Color(0xFFFFA500),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.BottomEnd)
                                            .background(Color.Black, CircleShape)
                                            .padding(4.dp)
                                    )
                                }

                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "Full Name",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = uiState.userName,
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Email Address",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = uiState.email,
                                        color = Color(0xFFFFA500),
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            onEditProfile()
                                            onEditProfileNavigate()
                                        },
                                        border = BorderStroke(1.dp, Color(0xFFFFA500)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFA500))
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Edit Profile")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    // Stats Section
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard(
                            icon = Icons.Default.ThumbUp,
                            value = "0",
                            label = "Likes"
                        )
                        StatCard(
                            icon = Icons.Default.Star,
                            value = uiState.submittedIdeas.size.toString(),
                            label = "Ideas"
                        )
                        StatCard(
                            icon = Icons.Default.Star,
                            value = "0",
                            label = "Contributions"
                        )
                    }
                }

                item {
                    // Bio Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "About Me",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (uiState.bio.isNotEmpty()) uiState.bio else "No bio added yet. Click 'Edit Profile' to add your bio.",
                                color = if (uiState.bio.isNotEmpty()) Color.White else Color.Gray,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                item {
                    // Status Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Status",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Status.values().forEach { status ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = (status == uiState.status),
                                        onClick = { /* viewModel.updateStatus(status) */ },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFFFFA500),
                                            unselectedColor = Color.White
                                        )
                                    )
                                    Text(
                                        status.displayName,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Recent Ideas Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Recent Ideas",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = onViewMyIdeas,
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color(0xFFFFA500)
                                    )
                                ) {
                                    Text("View All")
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            if (uiState.submittedIdeas.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No ideas submitted yet",
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                }
                            } else {
                                uiState.submittedIdeas.take(3).forEach { idea ->
                                    IdeaCard(
                                        idea = idea,
                                        onEditClick = { onEditIdea(idea) },
                                        onDeleteClick = { onDeleteIdea(idea.id) }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    // Admin Feedback Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Admin Feedback",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = onToggleFeedback) {
                                    Icon(
                                        if (uiState.isFeedbackExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFFFFA500)
                                    )
                                }
                            }
                            if (uiState.isFeedbackExpanded) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    uiState.feedback,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                item {
                    // Logout Button
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.Red),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Log Out")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFA500),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeaCard(
    idea: Idea,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = idea.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFFFFA500)
                        )
                    }

                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = idea.description,
                fontSize = 14.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (idea.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    idea.tags.forEach { tag ->
                        Surface(
                            color = Color(0xFFFFA500).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color(0xFFFFA500),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Created: ${formatDate(idea.createdAt)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Idea") },
            text = { Text("Are you sure you want to delete this idea?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDate(date: Date): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
}