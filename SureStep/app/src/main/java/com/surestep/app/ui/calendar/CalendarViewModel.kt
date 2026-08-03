package com.surestep.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surestep.app.data.repository.LogRepository
import com.surestep.app.domain.model.DaySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    logRepository: LogRepository,
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    val summaries: StateFlow<Map<LocalDate, DaySummary>> = _month
        .flatMapLatest { yearMonth ->
            logRepository.observeDaySummaries(
                start = yearMonth.atDay(1),
                end = yearMonth.atEndOfMonth(),
                today = LocalDate.now(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun previousMonth() {
        _month.value = _month.value.minusMonths(1)
    }

    fun nextMonth() {
        // There is nothing to show in the future, so forward navigation stops at
        // the current month.
        val next = _month.value.plusMonths(1)
        if (!next.isAfter(YearMonth.now())) _month.value = next
    }

    fun canGoForward(): Boolean = _month.value.isBefore(YearMonth.now())
}
