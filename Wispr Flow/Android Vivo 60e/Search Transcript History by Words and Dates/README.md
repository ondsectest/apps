# Search Transcript History by Words and Dates

A working prototype of an improvement to Wispr Flow's **Home** transcript history on Android.

## The issue

Seen on a Vivo 60e phone running Android.

**Steps to reproduce**

1. Open **Home**, which lists every transcript grouped by day.
2. Try to find something said a few days or weeks ago.

**What happens**

- There is no search and no way to jump to a date or a date range.
- The only way back is to scroll through every transcript, past the stats cards, day by day.
- Wispr Flow's help docs describe search at the top of the desktop Hub, but on the desktop app tested, no search was found either.

**Why it matters**

- People dictate messages, notes and drafts they later need again: an address, a figure, a reply they sent.
- The history is already stored; it just cannot be found.

## The improvement

- A **search icon** at the top right of Home opens a search box with the cursor ready. Results update as you type, matching words are highlighted, and the day headings stay so you know when it was said.
- A **calendar** button opens a month view with **From** and **To**. Days with transcripts have a dot; while a search is typed, only days with matches have a dot.
- Tap one day for From and another for To to search a date range. **If only From is chosen, results are for that one day only.** Tapping an earlier day second swaps them.
- Words and dates work together, for example "invoice" from 21 to 26 October. The dates show as a chip with × to clear them, next to a result count.
- Clear messages when nothing matches. **Cancel** returns to the normal Home.
- **Tapping a transcript copies it**, and "Copied" appears right on that transcript, not elsewhere on the screen. The Copy button does the same.

## Using the prototype

- Two buttons sit above the phone: **Issue** (red) and **Improvement** (green). Each opens a short explanation and switches the prototype to that version.
- Home matches the app: the word count card in green serif type, serif day headings, and transcript cards with Copy, Report and more.
- The history is sample text. Try searching "invoice" to see results across three dates.
- **Dictionary**, **Style** and **Snippets** in the bottom tabs are greyed out and show "Intentionally disabled" when tapped.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Prototype by Santhosh Tuppad.
