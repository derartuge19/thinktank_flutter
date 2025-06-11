package com.example.thinktank.Pages.IdeaSubmition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thinktank.R

@Composable
fun IdeaSubmissionContent(
    uiState: IdeaSubmissionUiState,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val fontColor = Color.White
    val buttonColor = Color(0xFFFFA500)
    val backgroundColor = Color(0xFF1A1A1A)
    val errorColor = Color(0xFFFF5252)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Transparent, shape = RoundedCornerShape(50))
                    .border(2.dp, Color.White, shape = RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Submit Your Idea",
                color = fontColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Light Bulb Image
        Image(
            painter = painterResource(id = R.drawable.bulb_image),
            contentDescription = "Idea Image",
            modifier = Modifier
                .size(200.dp)
                .padding(vertical = 24.dp)
        )

        Spacer(modifier = Modifier.weight(0.2f))

        // Form Fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Field
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Title", color = Color.White) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2A2A2A),
                    unfocusedContainerColor = Color(0xFF2A2A2A),
                    focusedBorderColor = buttonColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        text = "Minimum 3 characters",
                        color = if (uiState.title.length < 3 && uiState.title.isNotEmpty()) errorColor else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            )

            // Description Field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description", color = Color.White) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2A2A2A),
                    unfocusedContainerColor = Color(0xFF2A2A2A),
                    focusedBorderColor = buttonColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                supportingText = {
                    Text(
                        text = "Minimum 10 characters",
                        color = if (uiState.description.length < 10 && uiState.description.isNotEmpty()) errorColor else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            )

            // Tags Field
            OutlinedTextField(
                value = uiState.tags ?: "",
                onValueChange = onTagsChange,
                label = { Text("Tags (optional)", color = Color.White) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2A2A2A),
                    unfocusedContainerColor = Color(0xFF2A2A2A),
                    focusedBorderColor = buttonColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        text = "Separate tags with commas",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // Error Message
        if (uiState.error != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = errorColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = uiState.error,
                    color = errorColor,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Submit Button
        Button(
            onClick = onSubmit,
            enabled = !uiState.isSubmitting,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    "Submit Idea",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (uiState.submissionSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Idea Submitted Successfully!",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Redirecting to ideas page...",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
