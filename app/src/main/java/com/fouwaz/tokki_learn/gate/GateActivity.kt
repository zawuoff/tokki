package com.fouwaz.tokki_learn.gate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.fouwaz.tokki_learn.MainActivity
import com.fouwaz.tokki_learn.gate.ui.GateScreen
import com.fouwaz.tokki_learn.lesson.Language
import com.fouwaz.tokki_learn.ui.theme.Tokki_learnTheme
import kotlinx.coroutines.launch

class GateActivity : ComponentActivity() {

    private lateinit var targetPackageName: String
    private lateinit var appLabel: String
    private var completionBroadcastSent = false

    private val gateViewModel: GateViewModel by viewModels {
        GateViewModel.factory(
            targetPackageName = targetPackageName,
            appLabel = appLabel,
            language = Language.SPANISH
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra(GateContract.EXTRA_PACKAGE_NAME)
        val label = intent.getStringExtra(GateContract.EXTRA_APP_LABEL)

        if (packageName.isNullOrEmpty() || label.isNullOrEmpty()) {
            finish()
            return
        }

        targetPackageName = packageName
        appLabel = label

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // No-op to prevent bypassing the gate.
                }
            }
        )

        lifecycleScope.launch {
            gateViewModel.events.collect { event ->
                when (event) {
                    is GateEvent.Completed -> {
                        notifyGateCompleted()
                    }
                    is GateEvent.ContinueToApp -> {
                        notifyGateCompleted()
                        launchTargetApp(event.packageName)
                        finish()
                    }
                    GateEvent.ContinueLearning -> {
                        notifyGateCompleted()
                        launchMainApp()
                        finish()
                    }
                }
            }
        }

        setContent {
            Tokki_learnTheme {
                val state = gateViewModel.uiState.collectAsStateWithLifecycle()
                GateScreen(
                    state = state.value,
                    onMultipleChoiceSelected = gateViewModel::onMultipleChoiceSelected,
                    onInputChanged = gateViewModel::onInputChanged,
                    onInputSubmitted = gateViewModel::onInputSubmitted,
                    onContinueToApp = gateViewModel::onContinueToTargetApp,
                    onContinueLearning = gateViewModel::onContinueLearning
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!completionBroadcastSent) {
            sendBroadcast(
                Intent(GateContract.ACTION_GATE_DISMISSED).apply {
                    putExtra(GateContract.EXTRA_PACKAGE_NAME, targetPackageName)
                }
            )
        }
    }

    private fun notifyGateCompleted() {
        if (!completionBroadcastSent) {
            completionBroadcastSent = true
            sendBroadcast(
                Intent(GateContract.ACTION_GATE_COMPLETED).apply {
                    putExtra(GateContract.EXTRA_PACKAGE_NAME, targetPackageName)
                }
            )
        }
    }

    private fun launchTargetApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }

    private fun launchMainApp() {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(mainIntent)
    }

    companion object {
        fun createIntent(
            context: android.content.Context,
            packageName: String,
            appLabel: String
        ): Intent {
            return Intent(context, GateActivity::class.java).apply {
                putExtra(GateContract.EXTRA_PACKAGE_NAME, packageName)
                putExtra(GateContract.EXTRA_APP_LABEL, appLabel)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }
}
