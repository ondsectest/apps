# Save and Done Only When Languages Change

A working prototype of a bug and its proposed fix in Wispr Flow's **Settings, Languages** on Android.

## The bug

Seen on a Vivo 60e phone running Android.

**Steps to reproduce**

1. Open **Settings** and tap **Languages**.
2. Without changing anything, look at **Save**.
3. Tap **Add more**.
4. Without selecting a new language, look at **Done**.

**What happens**

- **Save** is enabled on Languages even though no language was added or removed.
- **Done** is enabled on Select languages even though no new language was selected.
- Tapping either one looks like an action was taken, but nothing changed.

**What was expected**

- **Save** stays disabled until the languages differ from what is saved.
- **Done** stays disabled until at least one new language is selected.
- Leaving without changes is already covered by **Back**.

## The proposed fix

- **Save** is disabled when Languages opens. It turns on when a language is deselected or a new one is added, and turns off again if the change is undone.
- **Done** is disabled until at least one new language is selected, and turns off again if it is deselected.
- Tapping a disabled button does nothing.
- Languages added with Done show a small **New** tag on the Languages screen until saved.

## Using the prototype

- Two buttons sit above the phone: **Issue** (red) and **Improvement** (green). Each opens a short explanation and switches the prototype to that version. A coloured strip at the top of the screen shows which version is active.
- It opens on **Settings**. Tap **Languages**, then **Add more**.
- In **Issue**, red notes point at Save and Done while they are enabled with nothing changed.
- In **Improvement**, green notes explain why each button is on or off. Saving updates the Languages line on Settings.
- The other Settings rows are disabled and show a message that this is a prototype of Languages only.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Prototype by Santhosh Tuppad.
