# Stop at the Limit: Maximum Length in Add Word and Every Form

A working prototype of a UX issue in Wispr Flow's **Dictionary** on Android, a proposed improvement, and a recommendation to apply it across the whole app.

## The issue

Seen on a Vivo 60e phone running Android.

**Steps to reproduce**

1. Open **Dictionary** and tap **+**.
2. Type or paste a long word or phrase, about 100 characters.
3. Tap **Add**.

**What happens**

- Nothing stops or warns while typing past 60 characters, so it looks fine.
- Only after tapping Add does the field turn pink with "Word is too long (max 60 characters)".
- There is no count, so the user has to guess how much to delete.

**What was expected**

The field stops at 60 characters, with a live count such as 42/60, so the error can never happen.

## The improvement

- The field accepts at most **60 characters**. Extra typing is simply not added.
- A live counter, such as **42/60**, sits under the field. It turns amber near the limit and purple at 60.
- At the limit, a calm note says "You've reached the 60-character limit." It is guidance, not an error.
- Pasting longer text keeps the first 60 characters and says "Pasted text was cut to 60 characters."
- Add stays disabled until there is text. Spaces at the start and end are trimmed.

## Apply this across the whole app

The same problem appears wherever there is a text field with a limit. Validation should be **consistent** (same look and wording everywhere), **effective** (prevent the mistake, not just report it) and **useful** (say what to do).

| Form | Fields |
| --- | --- |
| Dictionary, Add word | Word |
| Snippets, Add snippet | Snippet, Expansion (each with its own limit and counter) |
| Edit Profile | First name, Last name |
| Help, Report an issue | Description |
| Help, Share feedback | Feedback |
| Team and other settings | Names, emails and any free-text field |

One validation pattern for every field:

- **Show the limit up front:** a live counter (n/max) on every limited field.
- **Prevent, don't punish:** stop input at the limit, and cut pasted text with a short note.
- **Point to the right field:** any message sits under the field it is about, and only that field changes colour.
- **Same words and colours:** one style for guidance, one for real errors, used everywhere.
- **Clean the input:** trim spaces, collapse line breaks in one-line fields, and remove invisible characters.
- **Buttons tell the truth:** Save, Add and Send stay disabled until the form is valid and has changed.
- **Same rules on the server:** the app and the server share one source for limits, so they never disagree.
- **Accessible:** the counter and messages are announced by screen readers.

## Using the prototype

- Three buttons sit above the phone: **Issue** (red), **Improvement** (green) and **Apply across the app**.
- Tap **+** on the Dictionary screen to open **Add word**. Its placeholder, buttons and field match the app.
- Inside the dialog, **Type 94 characters** and **Paste 94 characters** fill in long test text.
- On a phone, the dialog stays above the keyboard so the field and its message remain visible while typing.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Prototype by Santhosh Tuppad.
