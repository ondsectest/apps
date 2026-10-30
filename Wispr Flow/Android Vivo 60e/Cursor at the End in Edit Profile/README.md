# Cursor at the End in Edit Profile

A working prototype of a smoother Edit Profile dialog on Wispr Flow's **Account** screen.

## The problem

1. On the Account screen, tapping the profile area above **Manage plan** opens **Edit Profile** with First name and Last name.
2. The cursor lands **before the first character** of First name, so to fix or add to the end of a name you first have to move the cursor.
3. **Save** is active as soon as the dialog opens, even though nothing has changed.

## The idea

1. **The cursor goes after the last character.** Every time Edit Profile opens, the cursor sits at the end of First name, so you can keep typing or delete straight away. Moving to Last name with the keyboard's Next key also lands at the end.
2. **Save is enabled only when something has changed.** It starts disabled. Typing a change enables it, and editing back to the original, for example with backspace, disables it again.

| What you do | Save |
| --- | --- |
| Open the form | Disabled |
| Change either name | Enabled |
| Backspace back to the original name | Disabled again |
| Add only a space at the end | Stays disabled; spaces do not count as a change |
| Change a letter's case | Enabled; it counts as a change |

Tapping Save while it is disabled does nothing, and the cursor and keyboard stay where they are. After saving, the new name becomes the original, so Save starts disabled the next time.

## Smaller details

- The whole profile area above Manage plan (photo, name, email, plan and stats) is one tap target, and a small pencil on the photo hints that it can be edited.
- The cursor goes into the field during the same tap, so a real phone raises its keyboard straight away.
- The first time it opens, a short note explains that the cursor is placed after the last character.
- The stats row wraps instead of cutting off "WPM".
- First name cannot be saved empty.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |

## Notes

- Saving shows a message saying it is a prototype only, so no real profile was changed.
- Initials stand in for the profile photo.
- Back, Manage plan, the links and Sign out show a short note that they are disabled in this prototype.
- On a desktop browser the prototype shows a phone frame. On a phone it fills the screen.

Prototype by Santhosh Tuppad.
