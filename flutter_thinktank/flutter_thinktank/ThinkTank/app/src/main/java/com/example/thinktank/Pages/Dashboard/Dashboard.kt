package com.example.thinktank.Pages.Dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.thinktank.R
import com.example.thinktank.data.models.Idea
import com.example.thinktank.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    println("\n=== Dashboard State ===")
    println("Loading: ${uiState.isLoading}")
    println("Error: ${uiState.error}")
    println("Approved Ideas Count: ${uiState.approvedIdeas.size}")
    uiState.approvedIdeas.forEach { idea ->
        println("Idea ${idea.id}: ${idea.title} by ${idea.user?.firstName} ${idea.user?.lastName}")
    }
    
    // Remember these values to prevent recomposition
    val fontFamily = remember {
        FontFamily(Font(R.font.kellyslab_regular, FontWeight.Normal))
    }
    val backgroundColor = remember { Color(0xFF1A1A1A) }
    val accentColor = remember { Color(0xFFFFA500) }

    // Load approved ideas when the screen is first displayed
    LaunchedEffect(Unit) {
        val token = context.getSharedPreferences("auth_prefs", 0)
            .getString("auth_token", null)
        println("\n=== Dashboard Token Check ===")
        println("Token found: ${token != null}")
        if (token != null) {
            println("Loading approved ideas with token...")
            viewModel.loadApprovedIdeas(token)
        } else {
            println("No token found in SharedPreferences")
            viewModel.setPreviewState(emptyList())
        }
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
            DrawerContent(
                onDrawerClose = {
                    scope.launch {
                        viewModel.onDrawerClose()
                    }
                },
                onNavigate = { route -> navController.navigate(route) },
                navController = navController
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ThinkTank",
                                color = accentColor,
                                fontSize = 28.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.onDrawerOpen() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    color = accentColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor
                    )
                )
            },
            containerColor = backgroundColor
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.ime)
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = accentColor,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    !uiState.error.isNullOrEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = uiState.error ?: "Unknown error",
                                    color = Color.Red,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        val token = context.getSharedPreferences("auth_prefs", 0)
                                            .getString("auth_token", null)
                                        if (token != null) {
                                            viewModel.loadApprovedIdeas(token)
                                        }
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            // Header Section
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(accentColor.copy(alpha = 0.1f))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.dashboard_bulb),
                                    contentDescription = "Light Bulb",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Title Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Approved Ideas",
                                    fontSize = 28.sp,
                                    fontFamily = fontFamily,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.approvedIdeas.size} Ideas",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Ideas Grid
                            if (uiState.approvedIdeas.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No approved ideas yet",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 16.sp
                                    )
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    items(uiState.approvedIdeas) { idea ->
                                        println("Rendering idea card for ${idea.id}: ${idea.title}")
                                        ProjectCard(
                                            idea = idea,
                                            onClick = { println("Clicked idea: ${idea.title}") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    val mockIdeas = listOf(
        Idea(
            id = "1",
            title = "Project One",
            description = "Description of project one",
            status = "Approved",
            user = User(
                id = 1,
                firstName = "John",
                lastName = "Doe",
                email = "john@example.com"
            )
        ),
        Idea(
            id = "2",
            title = "Project Two",
            description = "Description of project two",
            status = "Approved",
            user = User(
                id = 2,
                firstName = "Jane",
                lastName = "Smith",
                email = "jane@example.com"
            )
        ),
        Idea(
            id = "3",
            title = "Project Three",
            description = "Description of project three",
            status = "Approved",
            user = User(
                id = 3,
                firstName = "Bob",
                lastName = "Johnson",
                email = "bob@example.com"
            )
        ),
        Idea(
            id = "4",
            title = "Project Four",
            description = "Description of project four",
            status = "Approved",
            user = User(
                id = 4,
                firstName = "Alice",
                lastName = "Brown",
                email = "alice@example.com"
            )
        )
    )

    val mockViewModel = DashboardViewModel().apply {
        setPreviewState(mockIdeas)
    }

   
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ThinkTank",
                            color = Color(0xFFFFA500),
                            fontSize = 28.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Approved Ideas",
                fontSize = 28.sp,
                color = Color(0xFFFFA500),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(mockIdeas) { idea ->
                    ProjectCard(idea = idea, onClick = {})
                }
            }
        }
    }
} 
