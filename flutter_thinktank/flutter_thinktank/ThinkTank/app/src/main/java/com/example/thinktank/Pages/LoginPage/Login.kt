package com.example.thinktank.Pages.LoginPage

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.thinktank.R
import android.app.Application
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { LoginViewModel(context.applicationContext as Application) }
    
    Login(
        onNavigateToSignup = { navController.navigate("signup") },
        onNavigateBack = { navController.popBackStack() },
        onLoginSuccess = onLoginSuccess
    )
}

@Composable
fun Login(
    onNavigateToSignup: () -> Unit,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LoginContent(
        email = email,
        password = password,
        loginError = loginError,
        isLoading = isLoading,
        onEmailChange = { viewModel.updateEmail(it) },
        onPasswordChange = { viewModel.updatePassword(it) },
        onLoginClick = {
            scope.launch {
                viewModel.login(onSuccess = onLoginSuccess)
            }
        },
        onSignupClick = onNavigateToSignup,
        onBackClick = onNavigateBack
    )
}

@Composable
fun LoginContent(
    email: String,
    password: String,
    loginError: String?,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_page_image),
                contentDescription = "Walking Girl",
                modifier = Modifier.width(550.dp).height(250.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                "Welcome Back To ThinkTank!",
                style = TextStyle(fontSize = 22.sp, color = Color(0xFFFFA60C))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email:", color = Color.White) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                textStyle = TextStyle(color = Color(0xFFFAA60C), fontSize = 22.sp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password:", color = Color.White) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                textStyle = TextStyle(color = Color(0xFFFAA60C), fontSize = 22.sp),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (loginError != null) {
                Text(
                    text = loginError,
                    style = TextStyle(fontSize = 16.sp, color = Color.Red),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .width(250.dp)
                    .height(50.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFAA60C)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Login", fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Don't have an account? Sign up",
                style = TextStyle(fontSize = 16.sp, color = Color(0xFFFAA60C)),
                modifier = Modifier.clickable {
                    onSignupClick()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    Login(onNavigateToSignup = {}, onNavigateBack = {}, onLoginSuccess = {})
} 
