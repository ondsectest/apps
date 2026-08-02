package com.calmcontrol.domain

/**
 * @param source Where it can be checked. Null where the wording is traditional or the citation is
 *   secondhand — see the note on [Quotes.all].
 */
data class Quote(
    val text: String,
    val author: String,
    val source: String? = null,
)

object Quotes {

    /**
     * Shown after every logged moment, calm or angry alike.
     *
     * Two rules govern what is allowed in here.
     *
     * **Nothing may shame the reader.** Half the time this appears straight after someone has
     * admitted they lost their temper. "Whatever is begun in anger ends in shame" is a real
     * Franklin line and it is exactly wrong for that instant — so are the ones calling anger
     * madness or poison. What survived either treats anger as workable material or points at
     * something to do next.
     *
     * **Everything is traceable.** Anger quotations are among the most misattributed text on the
     * internet; the famous "holding onto anger is like drinking poison" line is not from the Pali
     * canon and the widely shared Emerson one about sixty seconds of peace appears nowhere in
     * Emerson. Both are absent here for that reason. Entries carry a [Quote.source] where a
     * specific passage can be pointed at, and null where the wording is traditional or reaches us
     * secondhand — those are the ones to verify first if this ships.
     */
    val all: List<Quote> = listOf(
        Quote(
            "The best revenge is to be unlike him who performed the injury.",
            "Marcus Aurelius",
            "Meditations, VI.6",
        ),
        Quote(
            "How much more grievous are the consequences of anger than the causes of it.",
            "Marcus Aurelius",
            "Meditations, XI.18",
        ),
        Quote(
            "If you are pained by external things, it is not they that disturb you, " +
                "but your own judgement of them.",
            "Marcus Aurelius",
            "Meditations, VIII.47",
        ),
        Quote(
            "The greatest remedy for anger is delay.",
            "Seneca",
            "On Anger, Book III",
        ),
        Quote(
            "Anybody can become angry — that is easy. But to be angry with the right person, " +
                "to the right degree, at the right time, for the right purpose, and in the right " +
                "way — that is not easy.",
            "Aristotle",
            "Nicomachean Ethics, II.9",
        ),
        Quote(
            "Men are disturbed not by things, but by the views which they take of them.",
            "Epictetus",
            "Enchiridion, 5",
        ),
        Quote(
            "Conquer anger by love. Conquer evil by good. Conquer the miserly by generosity, " +
                "and the liar by truth.",
            "The Dhammapada",
            "Verse 223",
        ),
        Quote(
            "Hatred is never appeased by hatred. Hatred is appeased by love. " +
                "This is an eternal law.",
            "The Dhammapada",
            "Verse 5",
        ),
        Quote(
            "The strong man is not the one who wrestles well, " +
                "but the one who controls himself when angry.",
            "Prophet Muhammad",
            "Sahih al-Bukhari",
        ),
        Quote(
            "A gentle answer turns away wrath.",
            "Proverbs 15:1",
        ),
        Quote(
            "Anger is like a howling baby, suffering and crying. Your anger is your baby.",
            "Thich Nhat Hanh",
            "Anger: Wisdom for Cooling the Flames",
        ),
        Quote(
            "In the practice of tolerance, one's enemy is the best teacher.",
            "The 14th Dalai Lama",
        ),
        Quote(
            "I will permit no man to narrow and degrade my soul by making me hate him.",
            "Booker T. Washington",
            "Up From Slavery",
        ),
        Quote(
            "Darkness cannot drive out darkness; only light can do that. " +
                "Hate cannot drive out hate; only love can do that.",
            "Martin Luther King Jr.",
            "Strength to Love",
        ),
        Quote(
            "I knew if I didn't leave my bitterness and hatred behind, I'd still be in prison.",
            "Nelson Mandela",
        ),
        Quote(
            "Use that anger. You write it. You paint it. You dance it. You march it. You vote it. " +
                "You do everything about it.",
            "Maya Angelou",
        ),
        Quote(
            "I was angry with my friend; I told my wrath, my wrath did end.",
            "William Blake",
            "A Poison Tree",
        ),
        Quote(
            "Anger is like electricity. It is just as powerful and just as useful, " +
                "but only if we use it intelligently.",
            "Mahatma Gandhi",
            "as recalled by Arun Gandhi",
        ),
        Quote(
            "When angry, count ten before you speak; if very angry, an hundred.",
            "Thomas Jefferson",
            "Letter to Thomas Jefferson Smith, 1825",
        ),
        Quote(
            "Life appears to me too short to be spent in nursing animosity or registering wrongs.",
            "Charlotte Brontë",
            "Jane Eyre",
        ),
        Quote(
            "If you are patient in one moment of anger, you will escape one hundred days of sorrow.",
            "Chinese proverb",
        ),
    )
}
