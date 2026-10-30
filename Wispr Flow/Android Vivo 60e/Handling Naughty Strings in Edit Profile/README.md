# Handling Naughty Strings in Edit Profile

A working prototype of a bug in how Wispr Flow's **Edit Profile** handles unusual names, and a proposed fix.

## Test data

Both First name and Last name start with the same text: a set of "naughty strings". It is not a real name. It mixes symbols, accents, Cyrillic, Arabic digits, superscripts, subscripts, Thai letters with many stacked marks, line breaks and comment lines. Text like this is known to break forms and screens, so it shows how the app handles unexpected input.

In the prototype, the **Test data** button shows this text in a read-only text area, all lines, next to a read-only one-line field showing what the app's field displays.

## The bug

Seen on a Vivo 60e phone running Android.

1. With the test data saved as the name, the Account card shows every line, and the Thai stacked marks spill down over the email, plan and stats.
2. Edit Profile shows **only the first line** of each name.
3. Changing First name to Santhosh and Last name to Tuppad, then saving, replaces only that first line.
4. The Account card then shows **Santhosh Tuppad followed by all the other lines**, which were never visible in Edit Profile and so could not be removed.

## The proposed fix

1. **Edit Profile shows the whole name,** so replacing it replaces all of it.
2. **The name is cleaned before it is shown and before it is saved:**
   - line breaks and tabs become spaces, and extra spaces are collapsed;
   - invisible control characters are removed;
   - long runs of stacked marks are cut to two per letter.
3. **The Account card shows the name on one line,** ending with "..." if it is too long, and clips anything that rises above or drops below the line.

After the same steps, the card shows only **Santhosh Tuppad**.

## Using the prototype

A switch above the phone has three views:

| View | What it shows |
| --- | --- |
| **Bug (app today)** | The app's behaviour: Edit Profile shows only the first line, and the old lines stay after saving. |
| **Proposed fix** | The same steps with the fix: only Santhosh Tuppad remains. |
| **Screenshots** | Three screenshots from the app, in order: the starting card, Edit Profile showing only Santhosh, and the card after saving. |

A yellow guide on the Account screen leads through the steps: tap the name, replace First name with Santhosh, replace Last name with Tuppad, then tap Save. Inside Edit Profile the hint updates as you type. After saving, the guide explains the result. **Reset test data** starts again.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Notes

- The cursor lands after the last character when Edit Profile opens, and Save is enabled only when a name has changed.
- Save works from the button itself rather than a form submission, so it also works inside a sandboxed frame such as an artifact viewer.
- Saving shows a message saying it is a prototype only, so no real profile was changed.
- On a desktop browser the prototype shows a phone frame. On a phone it fills the screen.

Prototype by Santhosh Tuppad.
