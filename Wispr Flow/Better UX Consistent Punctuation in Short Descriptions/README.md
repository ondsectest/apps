# Better UX: Consistent Punctuation in Short Descriptions

A working prototype that flags a small but visible inconsistency in Wispr Flow's Settings, and asks the team which style should be the standard.

## The problem

When you tap **Local data storage** in Settings, the dialog says "This applies to every device where you use Wispr Flow." with a full stop at the end.

Other one-sentence descriptions in the app have no full stop, for example:

- "Stores transcription data on Wispr servers to enable features that require cross-device access to your data"
- "Your organization does not allow data to be used for model training"

Each case is small on its own. Together, details like these decide whether an app feels carefully made.

## The idea

The prototype does not pick a rule. It marks the inconsistency and asks the question, so the team can decide once and apply it everywhere.

1. **The full stop is marked.** It is highlighted in yellow with a numbered marker, so it is easy to spot.
2. **A callout explains it.** A note above the dialog points at the marked full stop: "Full stop here, but not elsewhere. Which is the standard?"
3. **The detail is one tap away.** Tapping the marker or "Why it matters" shows examples from other screens and the question for the team: should single sentences end with a full stop or not? Either works, as long as every screen follows the same rule.
4. **A preview shows the alternative.** "Preview without full stop" removes it and marks where it was, so both versions can be compared in place.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Notes

- The prototype opens with the Local data storage dialog already showing. Choosing an option updates the value on the Settings page, and Cancel closes the dialog.
- On a desktop browser it shows a phone frame. On a phone it fills the screen, and it works in both light and dark mode.
- Other rows show a short note that they are disabled in this prototype.

Prototype by Santhosh Tuppad.
