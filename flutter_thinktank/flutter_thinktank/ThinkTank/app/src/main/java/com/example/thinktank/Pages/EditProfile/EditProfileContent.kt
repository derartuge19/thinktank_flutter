package com.example.thinktank.Pages.EditProfile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.thinktank.R

@Composable
fun EditProfileContent(
    uiState: EditProfileUiState,
    onBackClick: () -> Unit,
    onSaveChangesClick: () -> Unit,
    onFieldChange: (EditProfileUiState) -> Unit,
    onImageSelect: (Uri) -> Unit
) {
    val orange = Color(0xFFFFA500)
    val white = Color.White
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelect(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = white
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Edit Profile",
                fontSize = 24.sp,
                color = orange
            )
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        // Profile Picture
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, orange, CircleShape)
                .clickable { imagePicker.launch("image/*") }
        ) {
            if (uiState.profilePicture != null) {
                AsyncImage(
                    model = uiState.profilePicture,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.user_profile_icon),
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Change Photo",
                tint = orange,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color.Black, CircleShape)
                    .padding(4.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Form Fields
        OutlinedTextField(
            value = uiState.firstName,
            onValueChange = { onFieldChange(uiState.copy(firstName = it)) },
            label = { Text("First Name", color = white) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = orange,
                unfocusedBorderColor = orange,
                focusedLabelColor = orange,
                unfocusedLabelColor = white,
                cursorColor = orange,
                focusedTextColor = white,
                unfocusedTextColor = white
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = { onFieldChange(uiState.copy(lastName = it)) },
            label = { Text("Last Name", color = white) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = orange,
                unfocusedBorderColor = orange,
                focusedLabelColor = orange,
                unfocusedLabelColor = white,
                cursorColor = orange,
                focusedTextColor = white,
                unfocusedTextColor = white
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onFieldChange(uiState.copy(email = it)) },
            label = { Text("Email", color = white) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = orange,
                unfocusedBorderColor = orange,
                focusedLabelColor = orange,
                unfocusedLabelColor = white,
                cursorColor = orange,
                focusedTextColor = white,
                unfocusedTextColor = white
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Error Message
        uiState.error?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Save Button
        Button(
            onClick = onSaveChangesClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = orange
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Save Changes", color = Color.White)
            }
        }
    }
}
