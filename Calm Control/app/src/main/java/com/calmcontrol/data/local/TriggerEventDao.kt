package com.calmcontrol.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerEventDao {

    /**
     * The single source for every chart on the Reports screen. Room re-emits on any write to the
     * table, which is what makes the charts update the instant a RED or GREEN event is logged.
     */
    @Query(
        "SELECT * FROM trigger_events " +
            "WHERE epoch_millis >= :fromInclusive AND epoch_millis < :toExclusive " +
            "ORDER BY epoch_millis ASC",
    )
    fun observeBetween(fromInclusive: Long, toExclusive: Long): Flow<List<TriggerEvent>>

    @Insert
    suspend fun insert(event: TriggerEvent): Long

    @Insert
    suspend fun insertAll(events: List<TriggerEvent>)

    @Query("SELECT COUNT(*) FROM trigger_events")
    suspend fun count(): Int
}
