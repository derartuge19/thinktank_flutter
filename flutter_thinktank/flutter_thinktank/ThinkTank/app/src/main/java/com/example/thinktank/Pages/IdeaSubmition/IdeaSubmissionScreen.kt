package com.example.thinktank.Pages.IdeaSubmition

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import retrofit2.Call
import retrofit2.Response

@Composable
fun IdeaSubmissionScreen(
    onNavigateToIdeas: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: IdeaSubmissionViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.onSubmissionSuccess = onNavigateToIdeas
    }

    IdeaSubmissionContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onTagsChange = viewModel::onTagsChange,
        onSubmit = viewModel::onSubmitIdea
    )
}

@Composable
fun IdeaSubmissionScreenPreview() {
    val context = LocalContext.current
    val viewModel = remember { IdeaSubmissionViewModel(context.applicationContext as Application) }
    val uiState by viewModel.uiState.collectAsState()
    
    IdeaSubmissionContent(
        uiState = uiState,
        onBackClick = { },
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onTagsChange = viewModel::onTagsChange,
        onSubmit = viewModel::onSubmitIdea
    )
}
