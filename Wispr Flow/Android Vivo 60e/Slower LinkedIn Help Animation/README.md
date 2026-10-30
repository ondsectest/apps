# Slower LinkedIn Help Animation

A working prototype of a calmer, readable help animation on Wispr Flow's **Add Wispr Flow to your LinkedIn profile** screen, with a visual glitch removed.

## The problem

1. **The animation is too fast to read.** It loops every 2.5 seconds. The profile shows for about 1.5 seconds, the screen dims, and the details sheet (Top user, Capabilities and the Manage button) is fully visible for only about 0.3 seconds before it disappears.
2. **Dark wedges in the bottom corners.** While the details sheet is up, small dark shapes show in the bottom-left and bottom-right corners of the animation box, below Manage. The sheet and the box have different corner shapes, so the dimmed background shows through.

## The idea

1. **Give people time to read.**

| Step | App today | Slower version |
| --- | --- | --- |
| Profile shown | 1.5 s | 2.5 s |
| Screen dims | 0.3 s | 0.5 s |
| Sheet slides up | very quick | 0.65 s, smooth |
| Sheet stays visible | about 0.3 s | 3.5 s |
| Sheet slides away | very quick | 0.65 s |
| One full loop | about 2.5 s | about 8.7 s |

2. **Clip everything with one rounded box.** The profile, the dimming and the sheet all sit inside the same rounded container, so the sheet's bottom corners always match the box and no dark wedges appear.

## Smaller details

- A switch above the phone compares **Current speed** with **Slower (proposed)**, and a tag on the animation shows which one is playing.
- The animation restarts cleanly after the phone is locked and unlocked.
- With the phone's reduce motion setting on, the sheet appears without sliding.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Notes

- Timings for the current speed were measured from a screen recording of the app on a Vivo 60e with Android.
- The help illustration is an original drawing. It does not use LinkedIn's logo or any real user's name.
- Close and Connect LinkedIn show a short note that they are disabled in this prototype.
- On a desktop browser the prototype shows a phone frame. On a phone it fills the screen.

Prototype by Santhosh Tuppad.
