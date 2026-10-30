# Better UX: Shorter User Journeys in Single-Task Forms

A working prototype of a smoother "Report an issue" screen for Wispr Flow.

## The idea

The "Report an issue" screen has one job. When a form has a single task and nowhere else to go, the product can take the obvious next step for the user.

1. **The issue type list opens on its own.** No tap on the dropdown first, and nothing is pre-selected, so the choice stays deliberate.
2. **Radio buttons replace checkboxes,** because only one issue type can be chosen.
3. **Choosing a type moves the cursor into the description field** with the keyboard up, ready to type or dictate with Flow.
4. **Send stays disabled until the description has text.** Tapping it early does nothing and keeps the keyboard open.

That saves two taps on a screen people often open when they are already frustrated. The same pattern fits feedback forms, support requests, cancellation reasons and quick surveys.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |
| `Report_an_Issue_Prototype_Demo.mp4` | A 26 second vertical walkthrough of the flow. |

## Notes

- On a desktop browser the prototype shows a phone frame with a simulated keyboard. On a phone it fills the screen and uses the phone's own keyboard.
- Only the Report an issue flow is built. Other links, the Share feedback tab and Attach are disabled and show a short note when tapped.
- The page after Send is a placeholder and submits nothing.

Prototype by Santhosh Tuppad.
