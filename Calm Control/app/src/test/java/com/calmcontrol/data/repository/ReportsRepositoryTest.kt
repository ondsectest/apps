package com.calmcontrol.data.repository

import com.calmcontrol.data.local.TriggerEvent
import com.calmcontrol.data.local.TriggerEventDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Targets the query boundaries themselves: [ReportsRepository.observeDay] was added so the Log
 * screen queries one calendar day instead of the full 90-day Reports window. A fake DAO that
 * records the millisecond range it was asked for is what actually proves that — a passing test
 * suite that never inspects the range would not catch an off-by-one or a window that quietly
 * stayed too wide.
 */
class ReportsRepositoryTest {

    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 8, 15)
    private val clock = Clock.fixed(today.atStartOfDay(zone).toInstant().plusSeconds(3600), zone)

    private class RecordingDao : TriggerEventDao {
        var lastFrom: Long? = null
        var lastTo: Long? = null

        override fun observeBetween(fromInclusive: Long, toExclusive: Long): Flow<List<TriggerEvent>> {
            lastFrom = fromInclusive
            lastTo = toExclusive
            return flowOf(emptyList())
        }

        override suspend fun insert(event: TriggerEvent): Long = 0L
        override suspend fun insertAll(events: List<TriggerEvent>) = Unit
        override suspend fun count(): Int = 0
    }

    @Test
    fun `observeDay queries exactly one calendar day, not the wider report window`() = runBlocking {
        val dao = RecordingDao()
        val repository = ReportsRepository(dao, clock)

        repository.observeDay(today).collect { }

        val expectedFrom = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val expectedTo = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        assertEquals(expectedFrom, dao.lastFrom)
        assertEquals(expectedTo, dao.lastTo)
        assertEquals(24 * 60 * 60 * 1000L, dao.lastTo!! - dao.lastFrom!!)
    }

    @Test
    fun `observeRecentWindow still spans the full window plus smoothing warm-up`() = runBlocking {
        val dao = RecordingDao()
        val repository = ReportsRepository(dao, clock)

        repository.observeRecentWindow(today).collect { }

        val expectedFrom = today
            .minusDays((ReportsRepository.WINDOW_DAYS - 1).toLong())
            .minusDays(ReportsRepository.SMOOTHING_WARMUP_DAYS.toLong())
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val expectedTo = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        assertEquals(expectedFrom, dao.lastFrom)
        assertEquals(expectedTo, dao.lastTo)
    }
}
