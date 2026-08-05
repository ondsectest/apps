# WhatsApp Inactive Groups Checker

A local, single-file web app that:
1. Flags WhatsApp groups with no activity in the last **X** days (X is set by you).
2. Scans exported chat history for messages where you were tagged, so you can catch things you missed while muted or just not paying attention.

Everything runs client-side in your browser. No server, no upload, no network calls — the parsing happens entirely in JavaScript on your machine.

## How to use it

1. **Export each group chat from WhatsApp:**
   Open the group → tap the group name (or ⋮ menu) → **More** → **Export chat** → **Without media**. This produces a `.txt` file. Repeat for every group you want to check. (There is no bulk-export option in WhatsApp — this step is manual per group.)
2. **Open `index.html`** in this folder in any browser (double-click it, or `open index.html`).
3. Drag all the exported `.txt` files onto the drop zone (or click to choose them).
4. Set:
   - **Inactive after (days)** — your threshold X.
   - **Treat "today" as** — defaults to today; change it if you're analyzing old exports.
   - **Date format** — leave on Auto-detect unless results look wrong.
   - **Your name / tag to search for** — comma-separated variations (your first name, your saved contact name, `@YourName`, etc.) since people tag you differently and export text preserves whatever text form the tag rendered as.
5. Click **Analyze**. You get two tables:
   - **Group activity** — last message date, days inactive, flagged inactive/active.
   - **Messages where you were tagged** — every matching message with group, sender, timestamp.
6. Both tables have an **Export CSV** button if you want the data outside the browser.

## Caveats

- **No official WhatsApp API for this.** There's no way to programmatically pull "all my groups' history" without either violating WhatsApp's Terms of Service (unofficial libraries that log in as your account) or exporting manually, which is what this tool does. That means **no live/automatic monitoring** — you have to re-export and re-run it periodically.
- **Export format varies.** WhatsApp's `.txt` export format differs slightly by phone OS (Android vs iOS), app version, and date locale (DD/MM vs MM/DD). The parser handles both common formats and auto-detects the date order, but always spot-check a result or two against the raw `.txt` file, especially the first time.
- **"Last activity" includes any timestamped line**, including system messages (e.g. "so-and-so was added"), not just human messages. In a very quiet group this could slightly understate true inactivity.
- **Tag matching is plain text search, not WhatsApp's real mention metadata** — the export only contains whatever text was rendered (e.g. `@Santhosh`), so add every variation people might use (first name, full name, saved contact name) or you'll miss matches.
- **"Without media" exports are recommended** — media files bloat the export and aren't needed for this analysis; message text and timestamps are all that's parsed.
- Nothing here reads your phone or account directly — it only ever sees the `.txt` files you explicitly drop in.
