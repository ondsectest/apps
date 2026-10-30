# Truncate Long Names with Ellipsis

A working prototype that keeps a very long name tidy on Wispr Flow's **Account** screen.

## The problem

Edit Profile accepts first and last names of any length. After saving a very long name, the Account card shows all of it, wrapping over many lines. The name pushes the email, plan, stats and Manage plan far down the card, and the screen looks chaotic.

## The idea

Keep the name to a single line.

1. **If the name fits on one line, it shows in full,** for example "Santhosh Tuppad".
2. **If it is longer than one line, it is cut at the end of the line and ends with "...",** for example "Santhosh Santhosh Santh...".
3. **No tooltip or hover text.** The full name is still saved and shows in full in Edit Profile; only the display on the card is shortened.

The rest of the card stays exactly where it is, whatever the length of the name.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Testing with special characters

The saved name is a set of strings known to cause rendering problems: symbols and accents, Cyrillic, Arabic digits, superscripts and subscripts, and Thai text with many stacked marks.

- **Current app** (switch above the phone): a screenshot of the Account screen today. The name wraps over many lines, and the Thai stacked marks run down the screen over the text below.
- **Prototype:** the same name stays on one line and ends with "...". The name area clips anything that rises above or drops below the line, so stacked marks cannot spill over the email, plan or stats.

## Notes

- The prototype starts with the special-character name already saved, so the truncation is visible straight away. Tap the name to open Edit Profile and try your own; a short name shows in full.
- Name fields cannot hold line breaks, so each block of test strings is joined with spaces.
- Save works from the button itself rather than a form submission, so it also works when the page runs inside a sandboxed frame such as an artifact viewer.
- Saving shows a message saying it is a prototype only, so no real profile was changed.
- Initials stand in for the profile photo. Back, Manage plan, the links and Sign out show a short note that they are disabled in this prototype.
- On a desktop browser the prototype shows a phone frame. On a phone it fills the screen.

Prototype by Santhosh Tuppad.
