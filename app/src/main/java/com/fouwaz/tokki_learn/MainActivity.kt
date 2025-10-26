package com.fouwaz.tokki_learn

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fouwaz.tokki_learn.home.MainViewModel
import com.fouwaz.tokki_learn.home.ui.MainScreen
import com.fouwaz.tokki_learn.permissions.PermissionType
import com.fouwaz.tokki_learn.ui.theme.Tokki_learnTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Tokki_learnTheme {
                val state = mainViewModel.uiState.collectAsStateWithLifecycle()
                MainScreen(
                    state = state.value,
                    onToggleApp = { packageName, blocked ->
                        mainViewModel.toggleApp(packageName, blocked)
                    },
                    onRequestPermission = { permissionType ->
                        openSystemPermission(permissionType)
                    },
                    onSetCooldown = { minutes ->
                        mainViewModel.setGlobalCooldown(minutes)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshPermissions()
    }

    private fun openSystemPermission(permissionType: PermissionType) {
        val intent = when (permissionType) {
            PermissionType.OVERLAY -> Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )

            PermissionType.ACCESSIBILITY -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

            PermissionType.USAGE_ACCESS -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val success = runCatching { startActivity(intent) }.isSuccess
        if (!success) {
            Toast.makeText(
                this,
                "Unable to open system settings. Please grant the permission manually.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
