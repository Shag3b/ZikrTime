package com.shag3b.zikrtime.ui.permission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shag3b.zikrtime.R

/**
 * Dialog shown when notification permission is required
 */
@Composable
fun NotificationPermissionDialog(
    onEnableClick: () -> Unit,
    onExitClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Cannot dismiss - notification is required */ },
        title = {
            Text(
                text = stringResource(R.string.permission_required_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.permission_rationale_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onEnableClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.enable_notifications))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onExitClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.exit_app))
            }
        }
    )
}

/**
 * Dialog shown when permission is permanently denied
 */
@Composable
fun PermissionPermanentlyDeniedDialog(
    onOpenSettingsClick: () -> Unit,
    onExitClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Cannot dismiss - notification is required */ },
        title = {
            Text(
                text = stringResource(R.string.permission_required_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.permission_permanently_denied_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            Button(
                onClick = onOpenSettingsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onExitClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.exit_app))
            }
        }
    )
}

/**
 * Blocking screen shown when notifications are disabled
 * Prevents access to main app features
 */
@Composable
fun NotificationsDisabledScreen(
    onOpenSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon or illustration
            Text(
                text = "🔔",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Title
            Text(
                text = stringResource(R.string.notifications_disabled_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Message
            Text(
                text = stringResource(R.string.notifications_disabled_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Enable button
            Button(
                onClick = onOpenSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.open_settings),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

