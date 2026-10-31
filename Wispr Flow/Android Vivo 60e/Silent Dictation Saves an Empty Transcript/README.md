# Silent Dictation Saves an Empty Transcript

A working prototype of a bug in Wispr Flow's dictation history on Android, and a proposed fix.

## The bug

Seen on a Vivo 60e phone running Android. A double press of fn on the desktop app behaves the same.

**Steps to reproduce**

1. Start a dictation: tap the Flow Bubble (or double-press fn on desktop).
2. Do not speak. Wait 3 to 5 seconds.
3. Tap the **check mark** to finish.
4. Open Wispr Flow and look at **Home**.

**What happens**

- Nothing is typed, which is correct.
- But Home shows a new transcript card with only a time, Copy, Report and more. There is no text in it.
- Repeating this fills the history with blank cards, and Copy on a blank card copies nothing.

**What was expected**

- When no speech is detected, nothing is saved to history.
- A short message says "No speech detected", so the user knows why nothing was typed.

## The proposed fix

- After the check mark, if no speech was detected, show: "No speech detected. Nothing was typed, and nothing was saved to your history."
- Add no transcript to Home, so history only holds what was actually said.
- Optionally, hide or clean up blank transcripts that already exist.

## Using the prototype

- Two buttons sit above the phone: **Issue** (red) and **Improvement** (green). Each opens a short explanation and switches the prototype to that version.
- It starts in a sample chat app. Tap the **Flow Bubble**, wait without speaking, then tap the **check mark**.
- Tap **Open Wispr Flow to check history** to see Home. In **Issue**, each silent dictation adds an empty card, outlined in red. In the fix, no card is added. **Chat** goes back to try again.
- Home matches the app: the word count card, the Today heading and transcript cards with Copy, Report and more.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Prototype by Santhosh Tuppad.
