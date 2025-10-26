package com.fouwaz.tokki_learn.data.blocking

import android.content.Context
import com.fouwaz.tokki_learn.data.datastore.UserPreferencesDataSource
import com.fouwaz.tokki_learn.data.datastore.userPreferencesDataStore
import kotlinx.coroutines.flow.Flow

class BlockedAppsRepository(
    context: Context
) {

    private val dataSource = UserPreferencesDataSource(context.userPreferencesDataStore)

    val blockedPackages: Flow<Set<String>> = dataSource.blockedPackages

    val globalCooldownMinutes: Flow<Int> = dataSource.globalCooldownMinutes

    val lastGateTimestamps: Flow<Map<String, Long>> = dataSource.lastGateTimestamps

    suspend fun setPackageBlocked(packageName: String, blocked: Boolean) {
        dataSource.setPackageBlocked(packageName, blocked)
    }

    suspend fun setGlobalCooldownMinutes(minutes: Int) {
        dataSource.setGlobalCooldownMinutes(minutes)
    }

    suspend fun recordGateShown(packageName: String, timestampMillis: Long) {
        dataSource.recordGateShown(packageName, timestampMillis)
    }

    suspend fun clearGateTimestamp(packageName: String) {
        dataSource.clearGateTimestamp(packageName)
    }

    suspend fun clearAllGateTimestamps() {
        dataSource.clearAllGateTimestamps()
    }
}
