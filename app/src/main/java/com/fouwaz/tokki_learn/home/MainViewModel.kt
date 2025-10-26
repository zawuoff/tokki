package com.fouwaz.tokki_learn.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fouwaz.tokki_learn.data.apps.AppInfo
import com.fouwaz.tokki_learn.data.apps.AppInfoRepository
import com.fouwaz.tokki_learn.data.blocking.BlockedAppsRepository
import com.fouwaz.tokki_learn.permissions.PermissionStatus
import com.fouwaz.tokki_learn.permissions.PermissionType
import com.fouwaz.tokki_learn.permissions.PermissionsManager
import com.fouwaz.tokki_learn.services.TokiAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val blockedAppsRepository = BlockedAppsRepository(application)
    private val appInfoRepository = AppInfoRepository(application)
    private val permissionsManager = PermissionsManager(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var cachedApps: List<AppInfo> = emptyList()
    private var latestBlockedPackages: Set<String> = emptySet()

    init {
        observeBlockedPackages()
        observeCooldown()
        viewModelScope.launch {
            loadApps()
        }
        refreshPermissions()
    }

    fun refreshPermissions() {
        val statuses = permissionsManager.allPermissionStatuses(TokiAccessibilityService::class.java)
        _uiState.update { current ->
            current.copy(
                permissions = buildPermissionItems(statuses)
            )
        }
    }

    fun toggleApp(packageName: String, blocked: Boolean) {
        viewModelScope.launch {
            blockedAppsRepository.setPackageBlocked(packageName, blocked)
        }
    }

    fun setGlobalCooldown(minutes: Int) {
        viewModelScope.launch {
            blockedAppsRepository.setGlobalCooldownMinutes(minutes)
        }
    }

    private fun observeBlockedPackages() {
        viewModelScope.launch {
            blockedAppsRepository.blockedPackages.collect { packages ->
                latestBlockedPackages = packages
                updateAppList()
            }
        }
    }

    private fun observeCooldown() {
        viewModelScope.launch {
            blockedAppsRepository.globalCooldownMinutes.collect { minutes ->
                _uiState.update { current ->
                    current.copy(cooldownMinutes = minutes)
                }
            }
        }
    }

    private suspend fun loadApps() = withContext(Dispatchers.IO) {
        _uiState.update { it.copy(isLoadingApps = true) }
        cachedApps = appInfoRepository.getLaunchableApps()
        updateAppList()
    }

    private fun updateAppList() {
        val apps = cachedApps.map { appInfo ->
            AppListItem(
                packageName = appInfo.packageName,
                label = appInfo.label,
                isBlocked = latestBlockedPackages.contains(appInfo.packageName)
            )
        }
            .sortedWith(
                compareByDescending<AppListItem> { it.isBlocked }.thenBy { it.label.lowercase() }
            )

        _uiState.update { current ->
            current.copy(
                apps = apps,
                isLoadingApps = false,
                selectedAppCount = latestBlockedPackages.size
            )
        }
    }

    private fun buildPermissionItems(statuses: List<PermissionStatus>): List<PermissionItem> {
        return statuses.map { status ->
            when (status.type) {
                PermissionType.OVERLAY -> PermissionItem(
                    type = status.type,
                    title = "Draw over other apps",
                    description = "Needed to show the Toki checkpoint over blocked apps.",
                    granted = status.granted
                )

                PermissionType.ACCESSIBILITY -> PermissionItem(
                    type = status.type,
                    title = "Accessibility service",
                    description = "Lets Toki notice when you open a blocked app.",
                    granted = status.granted
                )

                PermissionType.USAGE_ACCESS -> PermissionItem(
                    type = status.type,
                    title = "Usage access",
                    description = "Helps Toki track last opened times for cooldowns.",
                    granted = status.granted
                )
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): androidx.lifecycle.ViewModelProvider.Factory {
            return object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(application) as T
                }
            }
        }
    }
}
