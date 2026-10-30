# Better UX: Explaining Settings Locked by an Admin

A working prototype of a clearer locked setting on Wispr Flow's Settings screen.

## The problem

On the Android Settings screen, "Improve the model for everyone" looks like an ordinary toggle that is switched off. Tapping it does nothing and gives no feedback. The reason, that the organization does not allow model training, is only in small grey text beside it, so it is easy to tap several times and think the app is broken.

## The idea

1. **The toggle looks locked, not off.** It is grey with a dashed outline and a lock inside the thumb.
2. **The reason is visible before anyone taps.** A "Managed by your organization" badge sits under the setting's name.
3. **Every attempt gets an answer.**
   - **Phone:** a tap gives a small shake and a short vibration where supported, scrolls the setting into view, and shows a message: "Managed by your organization. Your admin turned off model training, so you can't change this here. Ask your workspace admin if you need it."
   - **Mouse:** hovering shows a tooltip first, and a click shows the same message.
   - **Keyboard and screen readers:** the setting is announced as a disabled switch with its reason, and Enter or Space shows the message.
4. **"Dictation cloud storage" works normally,** so the difference between a locked toggle and an ordinary one is easy to see.

The same pattern fits any setting controlled by an admin, a plan limit or a policy.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Notes

- On a desktop browser the prototype shows a phone frame. On a phone it fills the screen.
- Only the locked setting and the cloud storage toggle are built. Other rows show a short note that they are disabled in this prototype.

Prototype by Santhosh Tuppad.
