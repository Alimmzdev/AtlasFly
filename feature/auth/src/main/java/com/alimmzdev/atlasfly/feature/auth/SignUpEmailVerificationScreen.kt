package com.alimmzdev.atlasfly.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.alimmzdev.atlasfly.feature.auth.components.AuthBrandRow
import com.alimmzdev.atlasfly.feature.auth.components.AuthMailMark
import com.alimmzdev.atlasfly.feature.auth.components.AuthScreenScaffold
import tech.nullexdev.atlasfly.core.designsystem.theme.AtlasFlyTheme

@Composable
fun SignUpEmailVerificationScreen(
    email: String,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpEmailVerificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SignUpEmailVerificationEvent.NavigateHome -> onNavigateToHome()
            }
        }
    }
    SignUpEmailVerificationContent(
        email = email,
        uiState = uiState,
        onCheck = { viewModel.onIntent(SignUpEmailVerificationUiIntent.CheckVerificationClicked) },
        onResend = { viewModel.onIntent(SignUpEmailVerificationUiIntent.ResendEmailClicked) },
        modifier = modifier,
    )
}

@Composable
private fun SignUpEmailVerificationContent(
    email: String,
    uiState: SignUpEmailVerificationUiState,
    onCheck: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthScreenScaffold(modifier = modifier) {
        AuthBrandRow()

        Spacer(modifier = Modifier.height(48.dp))

        AuthMailMark(modifier = Modifier.size(44.dp))

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Check your inbox",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "We sent a link to confirm it's you. Open it, then come back here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )

        uiState.message?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterHorizontally),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Button(
                onClick = onCheck,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Text(
                    text = "I confirmed it",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = onResend,
                modifier = Modifier.align(Alignment.Start),
            ) {
                Text(
                    text = "Didn't get it? Send again",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpEmailVerificationPreview() {
    AtlasFlyTheme {
        SignUpEmailVerificationContent(
            email = "maya@atlasfly.app",
            uiState = SignUpEmailVerificationUiState(),
            onCheck = {},
            onResend = {},
        )
    }
}
