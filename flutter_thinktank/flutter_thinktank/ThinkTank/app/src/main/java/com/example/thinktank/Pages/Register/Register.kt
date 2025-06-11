package com.example.thinktank.Pages.Register

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import com.example.thinktank.R
import com.example.thinktank.viewmodels.RegisterViewModel
import kotlinx.coroutines.launch

@Composable
fun Register(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RegisterViewModel? = null
) {
    val context = LocalContext.current
    val registerViewModel = viewModel ?: remember { RegisterViewModel(context.applicationContext as Application) }
    val scope = rememberCoroutineScope()

    val firstName by registerViewModel.firstName.collectAsState()
    val lastName by registerViewModel.lastName.collectAsState()
    val email by registerViewModel.email.collectAsState()
    val password by registerViewModel.password.collectAsState()
    val registrationError by registerViewModel.registrationError.collectAsState()
    val isRegistering by registerViewModel.isRegistering.collectAsState()
    val registrationSuccess by registerViewModel.registrationSuccess.collectAsState()

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            onNavigateToHome()
        }
    }

    RegisterContent(
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = password,
        registrationError = registrationError,
        isRegistering = isRegistering,
        onFirstNameChange = { registerViewModel.updateFirstName(it) },
        onLastNameChange = { registerViewModel.updateLastName(it) },
        onEmailChange = { registerViewModel.updateEmail(it) },
        onPasswordChange = { registerViewModel.updatePassword(it) },
        onRegisterClick = {
            scope.launch {
                registerViewModel.register()
            }
        },
        onLoginClick = onNavigateToLogin
    )
}

@Composable
fun RegisterContent(
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    registrationError: String?,
    isRegistering: Boolean,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
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
            modifier = Modifier.padding(top = 85.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.register_image),
                contentDescription = "Walking Girl",
                modifier = Modifier.width(550.dp).height(250.dp)
            )
            Spacer(modifier = Modifier.height(23.dp))
            Text(
                "Register and Unleash Your Creativity!",
                style = TextStyle(fontSize = 22.sp, color = Color(0xFFFFA60C))
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                label = { Text("Firstname:", color = Color.White) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                textStyle = TextStyle(color = Color(0xFFFAA60C), fontSize = 22.sp)
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = onLastNameChange,
                label = { Text("Lastname:", color = Color.White) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                textStyle = TextStyle(color = Color(0xFFFAA60C), fontSize = 22.sp)
            )
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

            if (registrationError != null) {
                Text(
                    text = registrationError,
                    style = TextStyle(fontSize = 16.sp, color = Color.Red),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .width(250.dp)
                    .height(50.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFAA60C)
                ),
                enabled = !isRegistering
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Register", fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Already have an account? Login",
                style = TextStyle(fontSize = 16.sp, color = Color(0xFFFAA60C)),
                modifier = Modifier.clickable {
                    onLoginClick()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    Register(onNavigateToLogin = {}, onNavigateToHome = {})
}
