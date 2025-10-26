package com.fouwaz.tokki_learn.home

import com.fouwaz.tokki_learn.permissions.PermissionType

data class PermissionItem(
    val type: PermissionType,
    val title: String,
    val description: String,
    val granted: Boolean
)

data class AppListItem(
    val packageName: String,
    val label: String,
    val isBlocked: Boolean
)

data class MainUiState(
    val permissions: List<PermissionItem> = emptyList(),
    val apps: List<AppListItem> = emptyList(),
    val isLoadingApps: Boolean = true,
    val selectedAppCount: Int = 0,
    val cooldownMinutes: Int = 15
)
