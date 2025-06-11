import com.example.thinktank.data.models.Idea

data class FeedbackUiState(
    val ideas: List<Idea> = emptyList(),
    val selectedIdea: Idea? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) 