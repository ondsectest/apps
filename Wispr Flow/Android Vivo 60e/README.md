# Wispr Flow on Android (Vivo 60e)

UX prototypes and bugs found while testing the Wispr Flow Android app on a Vivo 60e phone. Each folder is a small, working page that shows one improvement. Open a folder to read about the idea, or open its `index.html` in any browser to try it.

| Prototype | What it improves |
| --- | --- |
| [Better UX: Shorter User Journeys in Single-Task Forms](./Better%20UX%20Shorter%20User%20Journeys%20in%20Single-Task%20Forms) | The "Report an issue" screen. The issue type list opens on its own, radio buttons replace checkboxes, and choosing a type puts the cursor straight into the description. Two taps saved. Includes a demo video. |
| [Better UX: Explaining Settings Locked by an Admin](./Better%20UX%20Explaining%20Settings%20Locked%20by%20an%20Admin) | The "Improve the model for everyone" toggle that an organization has locked. It keeps the same look and off colour as the other toggles, so colours still mean the same states, and adds a lock icon and a "Managed by your organization" badge. A tap or hover explains the lock instead of doing nothing. |
| [Better UX: Consistent Punctuation in Short Descriptions](./Better%20UX%20Consistent%20Punctuation%20in%20Short%20Descriptions) | The Local data storage dialog, whose one-sentence description ends with a full stop while others in the app do not. A marker and callout point it out and ask the team which style should be the standard, with a preview of the alternative. |
| [Better UX: Back and Close in Confirmation Dialogs](./Better%20UX%20Back%20and%20Close%20in%20Confirmation%20Dialogs) | The Local data storage confirmations for Auto-delete and Never store. Back replaces Cancel and returns to the three options with nothing changed, and an explicit Close ends the task, so nothing happens that people did not choose. |
| [Connect or Add Wispr Flow to LinkedIn](./Connect%20or%20Add%20Wispr%20Flow%20to%20LinkedIn) | A bug on the "Add Wispr Flow to your LinkedIn profile" screen, seen on a Vivo 60e with Android: after locking and unlocking the phone, the help animation goes black until the screen is closed. The prototype reproduces the bug and shows a fix where the animation restarts and never goes black. |
| [Deselect All and Select All Options](./Deselect%20All%20and%20Select%20All%20Options) | The Languages screen, where switching from many languages to one meant deselecting each card. A slim row shows the count and one button that reads Deselect all or Select all, with Undo after clearing. Many to one in two taps. |
| [Cursor at the End in Edit Profile](./Cursor%20at%20the%20End%20in%20Edit%20Profile) | The Edit Profile dialog on the Account screen. The cursor now lands after the last character of First name instead of before the first, and Save is enabled only when a name has actually changed, turning off again if the edit is undone. |
| [Keyboard Ready in Share Feedback](./Keyboard%20Ready%20in%20Share%20Feedback) | The Share feedback screen, which has only one task. It now opens with the cursor in the feedback box and the keyboard up, ready to type, instead of waiting for a tap on the box. |
| [Truncate Long Names with Ellipsis](./Truncate%20Long%20Names%20with%20Ellipsis) | The Account card, where a very long saved name filled the card over many lines. The name now stays on one line and ends with "..." when it does not fit, with no tooltip. |

All prototypes are plain HTML, CSS and JavaScript in a single file, with no build step.

Prototypes by Santhosh Tuppad.
