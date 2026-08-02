package com.calmcontrol

import android.app.Application
import com.calmcontrol.data.DemoDataSeeder
import com.calmcontrol.data.QuoteRotation
import com.calmcontrol.data.local.CalmControlDatabase
import com.calmcontrol.data.repository.ReportsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manual dependency wiring. The graph is one database and one repository, which is not enough
 * to justify a DI framework and its build-time cost.
 */
class CalmControlApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val reportsRepository: ReportsRepository by lazy {
        ReportsRepository(CalmControlDatabase.get(this).triggerEventDao())
    }

    val quoteRotation: QuoteRotation by lazy { QuoteRotation(this) }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) seedDemoDataIfEmpty()
    }

    /**
     * Debug-only, and only when there is nothing there — it must never touch a real log. Delete
     * this along with [DemoDataSeeder] once the logging UI can produce data of its own.
     */
    private fun seedDemoDataIfEmpty() {
        applicationScope.launch {
            if (reportsRepository.isEmpty()) {
                reportsRepository.insertAll(
                    DemoDataSeeder.generate(
                        today = reportsRepository.today(),
                        zone = reportsRepository.zone,
                    ),
                )
            }
        }
    }
}
