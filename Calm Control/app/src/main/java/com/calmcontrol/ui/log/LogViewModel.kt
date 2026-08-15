package com.calmcontrol.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.calmcontrol.CalmControlApp
import com.calmcontrol.data.QuoteRotation
import com.calmcontrol.data.local.Outcome
import com.calmcontrol.data.local.TriggerCategory
import com.calmcontrol.data.repository.ReportsRepository
import com.calmcontrol.domain.DailySummary
import com.calmcontrol.domain.ReportsCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogViewModel(
    private val repository: ReportsRepository,
    private val quoteRotation: QuoteRotation,
) : ViewModel() {

    private val today = MutableStateFlow(repository.today())

    /**
     * Today's tally, shown under the buttons. Logging a moment and immediately seeing the count
     * move is the smallest possible proof that the act was worth doing.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val todaySummary: StateFlow<DailySummary?> =
        combine(
            today,
            today.flatMapLatest(repository::observeDay),
        ) { day, events ->
            ReportsCalculator.dailySummary(events, day, repository.zone)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refreshToday() {
        val current = repository.today()
        if (current != today.value) today.value = current
    }

    /**
     * Logs the moment and returns the quote to show for it. The write is fire-and-forget; the
     * dialog must not wait on a database round trip to appear.
     */
    fun log(outcome: Outcome, category: TriggerCategory): QuoteMoment {
        viewModelScope.launch {
            repository.logEvent(outcome = outcome, category = category)
        }
        return QuoteMoment(outcome, quoteRotation.next())
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as CalmControlApp
                LogViewModel(app.reportsRepository, app.quoteRotation)
            }
        }
    }
}
