package com.calmcontrol.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class QuotesTest {

    @Test
    fun `every quote has text and a named source of authority`() {
        Quotes.all.forEach { quote ->
            assertTrue("Empty quote text", quote.text.isNotBlank())
            assertTrue("Quote with no author: ${quote.text}", quote.author.isNotBlank())
        }
    }

    @Test
    fun `no duplicate quotes`() {
        val texts = Quotes.all.map { it.text }
        assertEquals(texts.size, texts.toSet().size)
    }

    @Test
    fun `enough quotes that repeats are rare`() {
        assertTrue("Rotation is too short to feel varied", Quotes.all.size >= 15)
    }

    @Test
    fun `quotes stay short enough to read in a dialog`() {
        Quotes.all.forEach { quote ->
            assertTrue(
                "Too long for the dialog (${quote.text.length} chars): ${quote.author}",
                quote.text.length <= 220,
            )
        }
    }

    /**
     * Half of these appear immediately after someone has admitted they lost their temper. A line
     * calling anger madness, poison or shameful is the wrong thing to hand them at that moment,
     * however famous it is — this test is what stops one being added back in later.
     */
    @Test
    fun `no quote shames the reader`() {
        val shaming = listOf(
            "shame", "madness", "poison", "stupid", "fool", "weak", "sin", "disgrace", "curse",
        )
        // Matched at a word boundary, not as a bare substring: "nursing" contains "sin", and
        // Brontë is not shaming anyone.
        Quotes.all.forEach { quote ->
            val text = quote.text.lowercase(Locale.ROOT)
            shaming.forEach { word ->
                val pattern = Regex("\\b$word")
                assertTrue(
                    "Quote attributed to ${quote.author} contains shaming word '$word'",
                    !pattern.containsMatchIn(text),
                )
            }
        }
    }

    @Test
    fun `authors are spread beyond a single tradition`() {
        val authors = Quotes.all.map { it.author }.toSet()
        assertTrue("Wanted a globally varied set of voices", authors.size >= 12)
    }
}
