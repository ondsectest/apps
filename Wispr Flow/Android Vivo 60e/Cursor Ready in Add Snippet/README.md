# Cursor Ready in Add Snippet

A working prototype of an issue and its improvement in Wispr Flow's **Snippets** on Android.

## The issue

Seen on a Vivo 60e phone running Android.

**Steps to reproduce**

1. From **Home**, tap **Snippets** at the bottom.
2. Tap **+** to add a new snippet.

**What happens**

- The Add snippet dialog opens, but no field has the cursor.
- The keyboard does not open.
- The user has to tap the **Snippet** field before they can start typing.

**Why it matters**

- Add snippet is a single-task form: typing a snippet is the only thing to do there.
- An extra tap every time a snippet is added adds friction.

## The improvement

- When **+** is tapped, the cursor is placed in the first field, **Snippet**.
- The keyboard tray opens automatically, so the user can start typing (or dictating) straight away.
- Everything else stays the same: the placeholders, Cancel, and Add staying disabled until both fields have text.

## Using the prototype

- Two buttons sit above the phone: **Issue** (red) and **Improvement** (green). Each opens a short explanation and switches the prototype to that version. A coloured strip at the top of the screen shows which version is active.
- Tap **Try the issue** or **Try the improvement**, then **+**.
- In **Issue**, Add snippet opens with no cursor and no keyboard, as in the app today.
- In **Improvement**, the cursor is in **Snippet** and the keyboard is up. In the desktop phone frame a drawn keyboard appears; on a real phone, the phone's own keyboard opens.
- The empty Snippets screen reads "Your Snippets show up here.", as in the app.
- **Dictionary** and **Style** in the bottom tabs are greyed out and show "Intentionally disabled" when tapped.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Prototype by Santhosh Tuppad.
