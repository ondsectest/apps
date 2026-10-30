# Better UX: Help Animation That Survives Lock and Unlock

A working prototype of a bug on Wispr Flow's "Add Wispr Flow to your LinkedIn profile" screen, and a proposed fix.

## The bug

Seen on a Vivo 60e phone running Android.

1. Open **Settings**, then **Add Wispr Flow to LinkedIn**. The help animation plays.
2. Lock the phone, then unlock it.
3. The help animation area is now completely black and stays black.
4. Tap **Close**. The animation flashes into view for a moment, then the screen closes and Settings appears.

The flash on close shows the animation was there all along, hidden behind a black surface that was not redrawn after the phone was unlocked.

## The prototype

Two controls sit above the phone.

- **Current (bug)** reproduces it. Tap **Lock and unlock**: a lock screen appears, and after unlocking the animation area goes black and stays black. It is marked with a red outline, a BUG tag, a bug drawing and the caption "Here is the bug", with the device noted. Tapping Close briefly shows the animation, then returns to Settings.
- **Proposed fix** shows the expected behaviour. After unlocking, the last frame stays visible with a small "Restarting the animation" label, the animation restarts from the beginning, and it never goes black. Close goes straight to Settings.

Locking and unlocking a real phone while the prototype is open has the same effect as the button.

## Suggested fix

When the app returns to the foreground, restart or re-attach the animation view instead of reusing the surface from before the lock, and keep the last frame or a static image visible until it is ready, so the area is never black.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Notes

- The help illustration is an original drawing. It does not use LinkedIn's logo or any real user's name.
- On a desktop browser the prototype shows a phone frame. On a phone it fills the screen.
- Close, Connect LinkedIn and other Settings rows show a short note that they are disabled in this prototype, except where described above.

Prototype by Santhosh Tuppad.
