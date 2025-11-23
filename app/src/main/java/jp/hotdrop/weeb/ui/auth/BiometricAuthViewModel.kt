package jp.hotdrop.weeb.ui.auth

import android.app.Application
import androidx.biometric.BiometricManager
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.weeb.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BiometricAuthViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(BiometricAuthUiState())
    val uiState: StateFlow<BiometricAuthUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<BiometricAuthEffect>(replay = 1)
    val effects: SharedFlow<BiometricAuthEffect> = _effects.asSharedFlow()

    init {
        val alreadyAuthenticated = savedStateHandle.get<Boolean>(STATE_KEY_AUTHENTICATED) ?: false
        if (alreadyAuthenticated) {
            launch { _effects.emit(BiometricAuthEffect.NavigateToMain) }
        } else if (!canAuthenticate()) {
            _uiState.value = _uiState.value.copy(
                message = "端末で生体認証を利用できません。アプリを終了します。",
                canRetry = false,
                isAuthenticating = false
            )
            launch { _effects.emit(BiometricAuthEffect.CloseApp) }
        } else {
            requestAuthentication()
        }
    }

    fun requestAuthentication() {
        if (_uiState.value.isAuthenticating) return
        _uiState.value = _uiState.value.copy(
            isAuthenticating = true,
            message = null,
            canRetry = false
        )
        launch { _effects.emit(BiometricAuthEffect.LaunchPrompt) }
    }

    fun onAuthenticationSucceeded() {
        savedStateHandle[STATE_KEY_AUTHENTICATED] = true
        _uiState.value = _uiState.value.copy(
            isAuthenticated = true,
            isAuthenticating = false,
            message = null,
            canRetry = false
        )
        launch { _effects.emit(BiometricAuthEffect.NavigateToMain) }
    }

    fun onAuthenticationFailed() {
        _uiState.value = _uiState.value.copy(
            isAuthenticating = false,
            message = "認証に失敗しました。再試行してください。",
            canRetry = true
        )
    }

    fun onAuthenticationError(message: String) {
        _uiState.value = _uiState.value.copy(
            isAuthenticating = false,
            message = message.ifBlank { "認証エラーが発生しました。再試行してください。" },
            canRetry = true
        )
    }

    private fun canAuthenticate(): Boolean {
        val manager = BiometricManager.from(application)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        return manager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    companion object {
        private const val STATE_KEY_AUTHENTICATED = "state_key_authenticated"
    }
}

data class BiometricAuthUiState(
    val isAuthenticated: Boolean = false,
    val isAuthenticating: Boolean = false,
    val message: String? = null,
    val canRetry: Boolean = false
)

sealed class BiometricAuthEffect {
    data object LaunchPrompt : BiometricAuthEffect()
    data object NavigateToMain : BiometricAuthEffect()
    data object CloseApp : BiometricAuthEffect()
}
