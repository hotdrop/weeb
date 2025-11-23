package jp.hotdrop.weeb.ui.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BiometricAuthScreen(
    state: BiometricAuthUiState,
    effects: SharedFlow<BiometricAuthEffect>,
    onRetryAuthentication: () -> Unit,
    onAuthenticationSucceeded: () -> Unit,
    onAuthenticationFailed: () -> Unit,
    onAuthenticationError: (String) -> Unit,
    onNavigateToMain: () -> Unit,
    onExitApp: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val executor = remember { ContextCompat.getMainExecutor(context) }
    var latestPrompt by remember { mutableStateOf<BiometricPrompt?>(null) }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("生体認証")
            .setSubtitle("続行するには生体認証を実行してください")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("キャンセル")
            .build()
    }

    LaunchedEffect(activity) {
        if (activity == null) {
            onAuthenticationError("生体認証を開始できません。端末を確認してください。")
            onExitApp()
            return@LaunchedEffect
        }
        latestPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onAuthenticationSucceeded()
                }

                override fun onAuthenticationFailed() {
                    onAuthenticationFailed()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onAuthenticationError(errString.toString())
                }
            }
        )
    }

    LaunchedEffect(latestPrompt) {
        val prompt = latestPrompt ?: return@LaunchedEffect
        effects.collectLatest { effect ->
            when (effect) {
                BiometricAuthEffect.LaunchPrompt -> {
                    prompt.authenticate(promptInfo)
                }
                BiometricAuthEffect.NavigateToMain -> onNavigateToMain()
                BiometricAuthEffect.CloseApp -> onExitApp()
            }
        }
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "生体認証を行ってください",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "認証が成功するとアプリが起動します。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (state.message != null) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (state.isAuthenticating) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
            }
            Button(
                onClick = onRetryAuthentication,
                enabled = !state.isAuthenticating && (state.canRetry || !state.isAuthenticated)
            ) {
                Text(text = "再試行")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onExitApp,
                enabled = !state.isAuthenticating
            ) {
                Text(text = "終了する")
            }
        }
    }
}
