# Better UX: Back and Close in Confirmation Dialogs

A working prototype of clearer choices in Wispr Flow's Local data storage confirmations.

## The problem

In Settings, **Local data storage** offers three options: Keep stored, Auto-delete after 24 hours and Never store. Choosing either delete option opens a confirmation dialog with **Cancel** and a confirm button ("Turn on auto-delete" or "Delete all data").

Cancel is ambiguous. It is not clear whether it returns to the three options, so you can pick a different one, or ends the task. If you picked the wrong option by mistake, there is no obvious way back to choose again.

## The idea

Give people three clear choices instead of two, so nothing happens that they did not choose.

1. **Back replaces Cancel.** It returns to the three options with nothing changed, and the saved choice is still selected, so a different option can be picked straight away.
2. **Close is explicit.** An X in the top-right corner ends the whole task with nothing changed. Its label says so for screen readers and on hover: "Close without changing anything".
3. **The confirm button stays as it is,** with the app's own wording. "Turn on auto-delete" keeps "Turn on" on the first line and "auto-delete" on the second, matching the app.

Both delete options (Auto-delete after 24 hours and Never store) work the same way. Keep stored deletes nothing, so it applies straight away.

## Other details

- Every change shows a short message saying it is a prototype only, so no real setting was changed and no data was deleted.
- On a computer, Escape works like Android's back gesture: from a confirmation it goes back one step, and from the options it closes.
- Each time the dialog opens it shows the saved choice, never a half-made one.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Notes

- On a desktop browser the prototype shows a phone frame. On a phone it fills the screen, and it works in both light and dark mode.
- Only Local data storage and the two toggles work. Other rows show a short note that they are disabled in this prototype.

Prototype by Santhosh Tuppad.
