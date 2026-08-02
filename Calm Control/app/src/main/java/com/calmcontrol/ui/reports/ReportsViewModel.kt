package com.calmcontrol.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.calmcontrol.CalmControlApp
import com.calmcontrol.data.repository.ReportsRepository
import com.calmcontrol.domain.DailySummary
import com.calmcontrol.domain.DayBucket
import com.calmcontrol.domain.ReportsCalculator
import com.calmcontrol.domain.StrongestArea
import com.calmcontrol.domain.TrendPoint
import com.calmcontrol.domain.TrendRange
import com.calmcontrol.domain.TriggerShare
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class ReportsUiState(
    val isLoading: Boolean = true,
    val daily: DailySummary? = null,
    val week: List<DayBucket> = emptyList(),
    val trend: List<TrendPoint> = emptyList(),
    val trendRange: TrendRange = TrendRange.DAYS_30,
    val triggers: List<TriggerShare> = emptyList(),
    val strongestArea: StrongestArea? = null,
    val reflection: List<String> = emptyList(),
    val hasAnyData: Boolean = false,
)

class ReportsViewModel(private val repository: ReportsRepository) : ViewModel() {

    /**
     * Held in state rather than read at each use, because "today" can change under a session
     * that is left open past midnight. [refreshToday] is called from the screen on resume.
     */
    private val today = MutableStateFlow(repository.today())
    private val trendRange = MutableStateFlow(TrendRange.DAYS_30)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val events = today.flatMapLatest(repository::observeRecentWindow)

    /**
     * Every chart is derived from one window of events, so a single Room emission after a logged
     * RED or GREEN moment refreshes the whole screen with no extra plumbing.
     */
    val uiState: StateFlow<ReportsUiState> =
        combine(today, trendRange, events) { day, range, windowEvents ->
            val zone = repository.zone
            val breakdown = ReportsCalculator.triggerBreakdown(windowEvents, day, zone)
            ReportsUiState(
                isLoading = false,
                daily = ReportsCalculator.dailySummary(windowEvents, day, zone),
                week = ReportsCalculator.weekBuckets(windowEvents, day, zone),
                trend = ReportsCalculator.trend(windowEvents, day, range, zone),
                trendRange = range,
                triggers = breakdown,
                strongestArea = ReportsCalculator.strongestArea(breakdown),
                reflection = ReportsCalculator.monthlyReflection(windowEvents, day, zone),
                hasAnyData = windowEvents.isNotEmpty(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReportsUiState(),
        )

    fun selectRange(range: TrendRange) {
        trendRange.value = range
    }

    /** Rolls the reports over to a new day if the app was left open overnight. */
    fun refreshToday() {
        val current: LocalDate = repository.today()
        if (current != today.value) today.value = current
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as CalmControlApp
                ReportsViewModel(app.reportsRepository)
            }
        }
    }
}
