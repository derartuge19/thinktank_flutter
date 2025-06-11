package com.example.thinktank.Pages.EditProfile

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    EditProfileContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSaveChangesClick = { viewModel.onSaveChanges() },
        onFieldChange = { updatedState -> viewModel.onFieldChange { updatedState } },
        onImageSelect = { viewModel.uploadProfilePicture(it) }
    )
}
