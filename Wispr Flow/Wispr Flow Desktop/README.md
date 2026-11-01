# Wispr Flow Desktop

Findings and prototypes from testing the Wispr Flow desktop application on macOS. Each folder is a working prototype of the issue and a proposed fix, built with mock or dummy data only.

| Finding | What it covers |
| --- | --- |
| [Snippet Form Saves Despite Client-Side Error](./Snippet%20Form%20Saves%20Despite%20Client-Side%20Error) | In Edit snippet, the expansion field shows a red over-limit error at 4853/4000 characters, but Save changes stays active and the save succeeds. The validation error is shown but not enforced, so the form saves more than 4000 characters. The fix disables save while invalid and enforces the limit server-side. Includes a screen recording. |

Findings by Santhosh Tuppad.
