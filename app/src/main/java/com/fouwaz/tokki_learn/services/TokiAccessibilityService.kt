package com.fouwaz.tokki_learn.services

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.fouwaz.tokki_learn.data.apps.AppInfoRepository
import com.fouwaz.tokki_learn.data.blocking.BlockedAppsRepository
import com.fouwaz.tokki_learn.gate.GateActivity
import com.fouwaz.tokki_learn.gate.GateContract
import com.fouwaz.tokki_learn.session.SessionPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TokiAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var blockedAppsRepository: BlockedAppsRepository
    private lateinit var appInfoRepository: AppInfoRepository
    private lateinit var sessionPolicy: SessionPolicy

    private var blockedPackages: Set<String> = emptySet()
    private var cooldownMinutes: Int = 15
    private var gateReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        blockedAppsRepository = BlockedAppsRepository(applicationContext)
        appInfoRepository = AppInfoRepository(applicationContext)
        sessionPolicy = SessionPolicy { cooldownMinutes }

        observeData()
        registerGateReceiver()
    }

    private fun observeData() {
        serviceScope.launch {
            blockedAppsRepository.blockedPackages.collectLatest { packages ->
                blockedPackages = packages
            }
        }

        serviceScope.launch {
            blockedAppsRepository.globalCooldownMinutes.collectLatest { minutes ->
                cooldownMinutes = minutes
            }
        }

        serviceScope.launch {
            blockedAppsRepository.lastGateTimestamps.collectLatest { timestamps ->
                sessionPolicy.refreshLastGateCache(timestamps)
            }
        }
    }

    private fun registerGateReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val packageName = intent?.getStringExtra(GateContract.EXTRA_PACKAGE_NAME) ?: return
                when (intent.action) {
                    GateContract.ACTION_GATE_COMPLETED,
                    GateContract.ACTION_GATE_DISMISSED -> {
                        sessionPolicy.markGateInactive(packageName)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(GateContract.ACTION_GATE_COMPLETED)
            addAction(GateContract.ACTION_GATE_DISMISSED)
        }
        ContextCompat.registerReceiver(
            this,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        gateReceiver = receiver
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }
        val packageName = event.packageName?.toString() ?: return

        // Ignore events from our own package.
        if (packageName == applicationContext.packageName) return

        if (!blockedPackages.contains(packageName)) return

        val now = System.currentTimeMillis()
        if (!sessionPolicy.shouldShowGate(packageName, now)) {
            return
        }

        sessionPolicy.markGateActive(packageName)
        serviceScope.launch {
            val label = appInfoRepository.getAppLabel(packageName)
            launchGate(packageName, label)
            blockedAppsRepository.recordGateShown(packageName, now)
            sessionPolicy.markGateShown(packageName, now)
        }
    }

    private fun launchGate(packageName: String, appLabel: String) {
        val intent = GateActivity.createIntent(
            context = this,
            packageName = packageName,
            appLabel = appLabel
        )
        startActivity(intent)
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        gateReceiver?.let { unregisterReceiver(it) }
    }
}
