package com.fouwaz.tokki_learn.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val LAST_GATE_PREFIX = "last_gate_"
private const val DEFAULT_COOLDOWN_MINUTES = 15

class UserPreferencesDataSource(
    private val dataStore: DataStore<Preferences>
) {

    private val blockedPackagesKey = stringSetPreferencesKey("blocked_packages")
    private val cooldownMinutesKey = intPreferencesKey("global_cooldown_minutes")
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    val blockedPackages: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[blockedPackagesKey] ?: emptySet()
    }

    val globalCooldownMinutes: Flow<Int> = dataStore.data.map { preferences ->
        preferences[cooldownMinutesKey] ?: DEFAULT_COOLDOWN_MINUTES
    }

    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[onboardingCompletedKey] ?: false
    }

    val lastGateTimestamps: Flow<Map<String, Long>> = dataStore.data.map { preferences ->
        preferences.asMap().mapNotNull { (key, value) ->
            val name = key.name
            if (name.startsWith(LAST_GATE_PREFIX)) {
                val packageName = name.removePrefix(LAST_GATE_PREFIX)
                (value as? Long)?.let { timestamp ->
                    packageName to timestamp
                }
            } else {
                null
            }
        }.toMap()
    }

    suspend fun setPackageBlocked(packageName: String, blocked: Boolean) {
        dataStore.edit { preferences ->
            val current = preferences[blockedPackagesKey] ?: emptySet()
            preferences[blockedPackagesKey] = if (blocked) {
                current + packageName
            } else {
                current - packageName
            }
        }
    }

    suspend fun setBlockedPackages(packageNames: Set<String>) {
        dataStore.edit { preferences ->
            preferences[blockedPackagesKey] = packageNames
        }
    }

    suspend fun setGlobalCooldownMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[cooldownMinutesKey] = minutes
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = completed
        }
    }

    suspend fun recordGateShown(packageName: String, timestampMillis: Long) {
        dataStore.edit { preferences ->
            preferences[lastGateKey(packageName)] = timestampMillis
        }
    }

    suspend fun clearGateTimestamp(packageName: String) {
        dataStore.edit { preferences ->
            preferences.remove(lastGateKey(packageName))
        }
    }

    suspend fun clearAllGateTimestamps() {
        dataStore.edit { preferences ->
            val keysToRemove = preferences.asMap().keys.filter { key ->
                key.name.startsWith(LAST_GATE_PREFIX)
            }
            keysToRemove.forEach { key -> preferences.remove(key) }
        }
    }

    private fun lastGateKey(packageName: String) =
        longPreferencesKey("$LAST_GATE_PREFIX$packageName")
}
