# Deleting Dictionary Words and Snippets: Confirm, Countdown, Clear Retention

A working prototype of a data and privacy finding in Wispr Flow, and a proposed fix. Built with invented sample data only; any real captured evidence is kept private.

## The finding

Area: Dictionary and Snippets sync (observed via the app's own `GET /api/v1/dictionary/personal` response).

**What was observed**

1. A word or snippet deleted in the app disappears from the list, as expected.
2. But the sync response keeps returning those deleted entries, each with its full text and a deleted flag set to true, days and weeks after deletion.
3. The list showed only three active entries while a single sync response returned those plus every deleted entry. The interface and the server disagree.
4. The app says nothing about how long deleted content is kept, or whether it is ever erased.

**Why it matters**

- People delete things for a reason: a private name, a referral link, a draft they regret. Delete should mean deleted.
- Keeping deleted content with no stated limit works against data minimisation and the right to erasure under laws such as GDPR, India's DPDP Act, CCPA and CPRA, LGPD and PIPEDA, and the missing retention period is itself a transparency gap.

## The proposed fix

**In the app**

- An honest confirmation that says the item is removed from all devices now and permanently erased from the servers within a stated period (for example 30 days).
- A Recently deleted list with a per-item countdown and Undo, so people can see what is still held and for how long.
- A Data and Privacy line stating the retention period for deleted words, snippets and dictation.

**On the server**

- After the grace period, delete the row and its text, so the sync response no longer returns it.
- During the grace period, return only what the owner's own devices need for Recently deleted and Undo, not the full text for any other purpose.
- Document the retention period and keep it the same across platforms.

## Using the prototype

- Two buttons above the phone: **Issue** (red) and **Improvement** (green).
- In **Issue**, an "Evidence: app vs server" animation (recreated, with the real deleted contents redacted) shows the list returning three words while the server returns more. Delete a word and open "What the server returns" to see the deleted entry still present.
- In **Improvement**, deleting shows the honest confirmation, the item moves to Recently deleted with a countdown and Undo, and the server view returns only an id and a purge countdown.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Note: this is a data and privacy finding about Wispr's servers. The real recording and captures are shared with Wispr privately, not published here.

Finding by Santhosh Tuppad.
