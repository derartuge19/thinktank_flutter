package com.example.thinktank.Pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinktank.R

@Composable
fun LogoutScreen(
//    onLogoutConfirm: () -> Unit,
    navController: NavController,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bulb_image),
                contentDescription = "Logout Illustration",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Logged out",
                fontSize = 40.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Thank you for using Think Tank",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    navController.navigate("login")
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xCCD9D9D9)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Sign in again", fontSize = 20.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "See you later",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFFFA500)
            )
        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun LogoutScreenPreview() {
//    LogoutScreen(
//        onLogoutConfirm = {},
//        navController: NavController,
//    )
//}
