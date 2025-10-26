package com.fouwaz.tokki_learn.session

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class SessionPolicy(
    private val cooldownMinutesProvider: () -> Int
) {

    private val activeGates = Collections.synchronizedSet(mutableSetOf<String>())
    private val lastGateTimestamps: MutableMap<String, Long> = ConcurrentHashMap()

    fun refreshLastGateCache(timestamps: Map<String, Long>) {
        lastGateTimestamps.clear()
        lastGateTimestamps.putAll(timestamps)
    }

    fun shouldShowGate(packageName: String, nowMillis: Long): Boolean {
        if (activeGates.contains(packageName)) {
            return false
        }
        val cooldownMillis = TimeUnit.MINUTES.toMillis(cooldownMinutesProvider().toLong())
        val lastShown = lastGateTimestamps[packageName] ?: return true
        return nowMillis - lastShown >= cooldownMillis
    }

    fun markGateActive(packageName: String) {
        activeGates.add(packageName)
    }

    fun markGateInactive(packageName: String) {
        activeGates.remove(packageName)
    }

    fun markGateShown(packageName: String, timestampMillis: Long) {
        lastGateTimestamps[packageName] = timestampMillis
    }
}
