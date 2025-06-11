package com.example.thinktank.Pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thinktank.R

@Composable
fun LandingPage(onNavigateToSignup: () -> Unit, onNavigateToLogin: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = R.drawable.landing_page_lightbulb),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = { onNavigateToLogin() },
                    modifier = Modifier
                        .width(270.dp)
                        .height(70.dp)
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFAA60C))
                ) {
                    Text("Login",
                        fontSize = 26.sp,
                        color = Color.Black)
                }

                Button(
                    onClick = onNavigateToSignup,
                    modifier = Modifier
                        .width(270.dp)
                        .height(70.dp)
                        .shadow(elevation = 15.dp)
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFAA60C))
                ) {
                    Text("Register",
                        fontSize = 26.sp,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "ThinkTank Inspire the world!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFAA60C),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingPagePreview() {
    LandingPage(onNavigateToSignup = {}, onNavigateToLogin = {})
}
