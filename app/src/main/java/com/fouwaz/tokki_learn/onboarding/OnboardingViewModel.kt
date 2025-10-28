package com.fouwaz.tokki_learn.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fouwaz.tokki_learn.data.datastore.UserPreferencesDataSource
import com.fouwaz.tokki_learn.data.datastore.userPreferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class OnboardingStep {
    data object Welcome : OnboardingStep()
    data object Unlocks : OnboardingStep()
    data object Distraction : OnboardingStep()
    data object Outro : OnboardingStep()
    data object InstagramIntro : OnboardingStep()
    data object InstagramNotification : OnboardingStep()
    data object Exercise : OnboardingStep()
}

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val dataSource = UserPreferencesDataSource(application.userPreferencesDataStore)

    private val _currentStep = MutableStateFlow<OnboardingStep>(OnboardingStep.Welcome)
    val currentStep = _currentStep.asStateFlow()

    val completed = dataSource.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun onWelcomeContinue() {
        _currentStep.value = OnboardingStep.Unlocks
    }

    fun onUnlocksContinue() {
        _currentStep.value = OnboardingStep.Distraction
    }

    fun onDistractionContinue() {
        _currentStep.value = OnboardingStep.Outro
    }

    fun onOutroContinue() {
        _currentStep.value = OnboardingStep.InstagramIntro
    }

    fun onInstagramIntroContinue() {
        _currentStep.value = OnboardingStep.InstagramNotification
    }

    fun onInstagramNotificationContinue() {
        _currentStep.value = OnboardingStep.Exercise
    }

    fun onExerciseContinue() {
        setCompleted()
    }

    fun goBack() {
        when (_currentStep.value) {
            OnboardingStep.Unlocks -> _currentStep.value = OnboardingStep.Welcome
            OnboardingStep.Distraction -> _currentStep.value = OnboardingStep.Unlocks
            OnboardingStep.Outro -> _currentStep.value = OnboardingStep.Distraction
            OnboardingStep.InstagramIntro -> _currentStep.value = OnboardingStep.Outro
            OnboardingStep.InstagramNotification -> _currentStep.value = OnboardingStep.InstagramIntro
            OnboardingStep.Exercise -> _currentStep.value = OnboardingStep.InstagramNotification
            else -> {}
        }
    }

    fun setCompleted() {
        viewModelScope.launch {
            dataSource.setOnboardingCompleted(true)
        }
    }
}
