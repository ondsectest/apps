package com.calmcontrol.data

import com.calmcontrol.data.local.Outcome
import com.calmcontrol.data.local.TriggerCategory
import com.calmcontrol.data.local.TriggerEvent
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random

/**
 * Sample history for debug builds, so the charts have something to show before a logging UI
 * exists. Seeded with a fixed value, so screenshots and manual comparisons are reproducible.
 *
 * The generated data deliberately improves over time. This is demo material for a screen whose
 * job is to make progress visible — flat noise would not exercise the thing being built.
 */
object DemoDataSeeder {

    private const val DAYS = 60

    /** Roughly how often each trigger comes up, relative to the others. */
    private val categoryWeights = listOf(
        TriggerCategory.FAMILY to 30,
        TriggerCategory.WORK to 25,
        TriggerCategory.TRAFFIC to 18,
        TriggerCategory.MONEY to 12,
        TriggerCategory.HEALTH to 8,
        TriggerCategory.SELF to 4,
        TriggerCategory.OTHER to 3,
    )

    /** Some triggers are simply easier to sit with than others. */
    private val categoryEase = mapOf(
        TriggerCategory.TRAFFIC to 0.12f,
        TriggerCategory.OTHER to 0.05f,
        TriggerCategory.MONEY to -0.04f,
        TriggerCategory.FAMILY to -0.08f,
    )

    fun generate(
        today: LocalDate,
        zone: ZoneId,
        random: Random = Random(20260801),
    ): List<TriggerEvent> {
        val weighted = categoryWeights.flatMap { (category, weight) -> List(weight) { category } }
        val events = mutableListOf<TriggerEvent>()

        for (dayOffset in (DAYS - 1) downTo 0) {
            val date = today.minusDays(dayOffset.toLong())
            val progress = (DAYS - 1 - dayOffset).toFloat() / (DAYS - 1)

            // 45% control rate two months ago, climbing to about 85% now, with a little wobble
            // so the line looks lived-in rather than drawn.
            val baseRate = 0.45f + 0.40f * progress + (random.nextFloat() - 0.5f) * 0.12f

            repeat(random.nextInt(2, 8)) {
                val category = weighted[random.nextInt(weighted.size)]
                val rate = (baseRate + (categoryEase[category] ?: 0f)).coerceIn(0.05f, 0.97f)
                val controlled = random.nextFloat() < rate

                val time = LocalTime.of(random.nextInt(7, 23), random.nextInt(0, 60))
                events += TriggerEvent(
                    epochMillis = date.atTime(time).atZone(zone).toInstant().toEpochMilli(),
                    outcome = if (controlled) Outcome.CONTROLLED else Outcome.ANGER_EXPRESSED,
                    category = category,
                    intensity = random.nextInt(1, 6),
                )
            }
        }
        return events
    }
}
