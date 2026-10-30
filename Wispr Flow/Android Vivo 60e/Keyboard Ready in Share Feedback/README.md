# Keyboard Ready in Share Feedback

A working prototype that saves a tap on Wispr Flow's **Share feedback** screen.

## The problem

The Share feedback screen has one job: write some feedback and send it. Apart from going back, there is nothing else to do there. Yet when it opens, the cursor is not in the text box and the keyboard is closed, so you first have to tap the box before you can start typing.

## The idea

When the screen exists only to write, be ready to write.

1. **The cursor is already in the feedback box when the screen opens,** and the keyboard is up, so you can start typing straight away. One tap saved.
2. **It does the same from the Share feedback tab,** so switching to it from Report an issue also puts the cursor straight in the box.
3. **The screen does not jump.** The form stays in place while the keyboard opens, using the same fix as the Report an issue prototype.
4. **Send stays disabled until there is text.** Tapping it early keeps the keyboard open, and closing the keyboard with an empty box shows "Feedback is required."

The placeholder text matches the app: "Tell us what's on your mind. What's working? What's not?"

This is the same idea as the Report an issue prototype, where choosing a request type moves the cursor straight into the description: on a screen with a single task, take the obvious next step for the person.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Notes

- Start on the Help & support screen and tap **Share feedback**.
- On a phone, the cursor goes in during the same tap, so the phone's own keyboard opens. On a desktop browser, a simulated keyboard slides up in the phone frame.
- The voice button types a sample piece of feedback to show dictation.
- Report an issue (link and tab) and Attach show a short note that they are disabled in this prototype. Send ends on a placeholder page; nothing is submitted.

Prototype by Santhosh Tuppad.
