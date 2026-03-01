package com.sportswipe.shared.presentation.chats

import com.sportswipe.shared.domain.model.Match
import com.sportswipe.shared.domain.model.Message
import com.sportswipe.shared.domain.repository.MatchRepository
import com.sportswipe.shared.domain.repository.MessageRepository
import com.sportswipe.shared.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ChatsUiState(
    val matches: List<Match> = emptyList(),
    val messages: List<Message> = emptyList(),
    val selectedMatchId: Long? = null,
)

class ChatsViewModel(
    private val matchRepository: MatchRepository,
    private val messageRepository: MessageRepository,
) : BaseViewModel() {
    private val _state = MutableStateFlow(ChatsUiState())
    val state: StateFlow<ChatsUiState> = _state.asStateFlow()

    init {
        scope.launch { _state.value = _state.value.copy(matches = matchRepository.matches().first()) }
    }

    fun openChat(matchId: Long) {
        scope.launch {
            _state.value = _state.value.copy(
                selectedMatchId = matchId,
                messages = messageRepository.messages(matchId).first()
            )
        }
    }

    fun send(message: String) {
        val matchId = _state.value.selectedMatchId ?: return
        scope.launch {
            messageRepository.send(matchId, message)
            openChat(matchId)
        }
    }
}
