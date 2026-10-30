# Deselect All and Select All Options

A working prototype of a faster way to change many languages at once on Wispr Flow's **Languages** screen.

## The problem

On the Languages screen, every chosen language is a large card with a tick. With many languages selected, switching to just one means tapping each card to deselect it, one by one. There is no way to clear them all at once.

## The idea

Add one small control that does the right thing, without taking space from the list.

1. **A slim summary row above the list.** On the left it shows the count, for example "9 of 30 selected". On the right is a single text button.
2. **One button, two jobs.** It reads **Deselect all** while any language is selected, and **Select all** when none are. Two separate buttons would take more space and leave one of them useless most of the time.
3. **Undo, so a mistaken tap is never costly.** After Deselect all, a message says how many languages were deselected and offers Undo. As soon as a language is picked, the Undo goes away, so it cannot undo the new choice by accident.
4. **Save needs at least one language.** With nothing selected, Save is greyed out, and tapping it explains "Choose at least one language to save."

The result: from many languages to one in two taps, Deselect all and then the language you want, instead of one tap per language.

## Why this design

- **It costs one thin line.** The row is the height of a single text line, so the list keeps almost all of its space.
- **The count is useful on its own.** It tells people how many languages are on without scrolling a long list.
- **A text button, not an icon.** "Deselect all" says exactly what it does, so nobody has to guess what a checkbox icon in the header means.
- **Undo instead of a confirmation dialog.** Clearing is easy to reverse, so a dialog asking "Are you sure?" would only slow people down.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Notes

- The prototype starts with all 30 languages selected, as in the screenshot. The first nine match the app; the rest show how long the list can get.
- Every Save shows a message saying it is a prototype only, so no real setting was changed.
- Back and Add more show a short note that they are disabled in this prototype.
- On a desktop browser the prototype shows a phone frame. On a phone it fills the screen.

Prototype by Santhosh Tuppad.
