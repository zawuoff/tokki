package com.fouwaz.tokki_learn.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fouwaz.tokki_learn.home.AppListItem
import com.fouwaz.tokki_learn.home.MainUiState
import com.fouwaz.tokki_learn.home.PermissionItem
import com.fouwaz.tokki_learn.permissions.PermissionType
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    state: MainUiState,
    onToggleApp: (String, Boolean) -> Unit,
    onRequestPermission: (PermissionType) -> Unit,
    onSetCooldown: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Toki checkpoints",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pick the apps you want to gate and grant the required permissions so we can step in at the right moment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            PermissionSection(
                permissions = state.permissions,
                onRequestPermission = onRequestPermission
            )
        }

        item {
            CooldownSection(
                cooldownMinutes = state.cooldownMinutes,
                onSetCooldown = onSetCooldown
            )
        }

        item {
            AppListHeader(selectedCount = state.selectedAppCount)
        }

        if (state.isLoadingApps) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(state.apps, key = { it.packageName }) { app ->
                AppRow(
                    app = app,
                    onToggleApp = onToggleApp
                )
                Divider()
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionSection(
    permissions: List<PermissionItem>,
    onRequestPermission: (PermissionType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Required permissions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        permissions.forEach { permission ->
            PermissionCard(permission = permission, onRequestPermission = onRequestPermission)
        }
    }
}

@Composable
private fun PermissionCard(
    permission: PermissionItem,
    onRequestPermission: (PermissionType) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Text(
                    text = permission.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = permission.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!permission.granted) {
                Button(onClick = { onRequestPermission(permission.type) }) {
                    Text(text = "Grant permission")
                }
            } else {
                Text(
                    text = "Granted",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CooldownSection(
    cooldownMinutes: Int,
    onSetCooldown: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Cooldown window",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = "How long should Toki wait before showing the checkpoint again for the same app?",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        var sliderValue by rememberSaveable { mutableStateOf(cooldownMinutes.toFloat()) }
        LaunchedEffect(cooldownMinutes) {
            sliderValue = cooldownMinutes.toFloat()
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {
                val quantized = (sliderValue / 5f).roundToInt() * 5
                sliderValue = quantized.toFloat()
                onSetCooldown(quantized)
            },
            valueRange = 5f..60f,
            steps = ((60 - 5) / 5) - 1
        )
        Text(
            text = "Checkpoint repeats every ${sliderValue.toInt()} minute(s).",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun AppListHeader(selectedCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Blocked apps",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = if (selectedCount == 0) {
                "Pick at least one app to start using checkpoints."
            } else {
                "$selectedCount app(s) gated right now."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppRow(
    app: AppListItem,
    onToggleApp: (String, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = app.isBlocked,
            onCheckedChange = { checked -> onToggleApp(app.packageName, checked) }
        )
    }
}
