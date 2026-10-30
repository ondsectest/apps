# Snippet Too Long: Which Field?

A working prototype of a user experience bug in Wispr Flow's **Snippets** on Android.

## The bug

Seen on a Vivo 60e phone running Android.

**Steps to reproduce**

1. From **Home**, tap **Snippets** at the bottom.
2. Tap **+** to add a new snippet.
3. Enter the test data (naughty strings) in the first and second fields.
4. Tap **Add**.

**What happens**

- The message "Snippet is too long (max 60 characters)" appears under the second field.
- The first field's line also turns pink, like the second one.
- So it is not clear whether the first field, the second field or both are too long, or whether the limit applies to them together.
- The message does not say how many characters were entered, so there is no way to know how much to remove.

**What was expected**

- Only the field that is too long is highlighted, and the message sits under that field.
- The message says how far over the limit it is, for example a live count such as 284 / 60.
- If both fields share one limit, the message says so.

**Not yet confirmed**

- Which field or fields the 60-character limit is checked against.
- How characters are counted for text like Thai letters with many stacked marks.

## Using the prototype

- It opens on Home. Tap **Snippets**, then **+**, enter text in both fields and tap **Add**.
- The red **Bug** button above the phone shows the full bug report and a screenshot from the app, and can open Add snippet with the test data already filled in.
- **Add** stays disabled (grey) until both **Snippet** and **Expansion** have text, as in the app.
- A short snippet adds normally to the list, with a message saying it is a prototype only.
- **Dictionary** and **Style** in the bottom tabs are greyed out and show "Intentionally disabled" when tapped.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Notes

- The field placeholders, **Snippet** and **Expansion**, match the app.
- The Home and empty Snippets screens are stand-ins; they were not in the screenshots.
- On a desktop browser the prototype shows a phone frame. On a phone it fills the screen.

Prototype by Santhosh Tuppad.
