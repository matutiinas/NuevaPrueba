package com.sportswipe.shared.presentation.profile

import com.sportswipe.shared.domain.model.MyProfile
import com.sportswipe.shared.domain.model.Settings
import com.sportswipe.shared.domain.repository.MyProfileRepository
import com.sportswipe.shared.domain.repository.SettingsRepository
import com.sportswipe.shared.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileUiState(val profile: MyProfile? = null, val settings: Settings = Settings())

class ProfileViewModel(
    private val myProfileRepository: MyProfileRepository,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        scope.launch {
            _state.value = ProfileUiState(
                profile = myProfileRepository.myProfile().first(),
                settings = settingsRepository.settings().first(),
            )
        }
    }

    fun togglePremium(enabled: Boolean) {
        scope.launch {
            settingsRepository.setPremium(enabled)
            _state.value = _state.value.copy(settings = settingsRepository.settings().first())
        }
    }

    fun deactivateAccount() { scope.launch { myProfileRepository.deactivate() } }
    fun deleteAccount() { scope.launch { myProfileRepository.deleteAllData() } }
}
