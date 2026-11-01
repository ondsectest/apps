# Snippet Form Saves Despite a Client-Side Validation Error

A working prototype of a validation finding in Wispr Flow, and a proposed fix. Built with invented sample data only; any real captured evidence that contains live data is kept private.

## The finding

Platform: Wispr Flow Pro desktop on macOS.
Area: Settings, Dictionary, Snippets, Edit snippet (and Add new snippet).
Field: Expansion (the snippet body), client-side maximum 4000 characters.

**What was observed**

1. Editing a snippet, the expansion field was filled past its 4000-character limit (4853 characters).
2. The character counter turned red and read `4853 / 4000`, and the field showed an error border. So the client detected the problem.
3. The `Save changes` button stayed active. Pressing it saved the snippet, and a confirmation toast "Updated snippet hello" appeared.
4. The validation error was shown but never wired to the submit action, so it did not block the save. Consistent with the related dictionary and snippet length findings, the server also appears to accept the over-limit value, so there is no backstop either.

**Why it matters**

- The error is cosmetic. A user can save content that the product's own rules forbid, without realising the error did nothing.
- Oversized expansions can bloat storage and sync payloads, and break any view that assumes the 4000-character bound.
- A validation control that reports a problem but does not enforce it is a reliability and data-integrity gap, and a signal to an attacker that input rules here are advisory rather than enforced.

## Classification

- CWE-20 Improper Input Validation
- CWE-1284 Improper Validation of Specified Quantity in Input
- CWE-1173 Improper Use of Validation Framework (the error state is not tied to the submit action)
- CWE-602 Client-Side Enforcement of Server-Side Security (if the server also fails to enforce the limit)

## The proposed fix

**In the app**

- Tie the validation state to the submit control: while any field is invalid, `Save changes` is disabled.
- Show a specific, actionable message (how many characters to remove), not only a red counter.

**On the server**

- Enforce the same 4000-character limit on the expansion and reject over-limit values with a structured validation error, so a bypassed or buggy client cannot save them.

**Shared**

- Apply the pattern consistently across the snippet name, the expansion, and other forms, and add tests that attempt to save an invalid form and expect it to be blocked.

## Using the prototype

- Two buttons at the top: **Issue** (red) and **Improvement** (green).
- Press "Fill to 4853 characters" to exceed the 4000 limit.
- In **Issue**, the counter turns red and the field shows an error, but `Save changes` stays active and saving succeeds with a confirmation (reproducing the bug).
- In **Improvement**, `Save changes` is disabled while over the limit, with a message saying how much to remove, and the server rejects an over-limit value as a backstop.

An evidence recording (`evidence-save-despite-error.mov`) from the researcher's own account shows the Edit snippet dialog at `4853 / 4000` with the red error state, the `Save changes` button being pressed, and the "Updated snippet hello" confirmation. The data in the recording is dummy test content (digit rulers and placeholder snippets), so it contains no real or sensitive information.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Note: demonstrated on the researcher's own account only. Any captures that contain live tokens or real data are shared with Wispr privately, not published here.

Finding by Santhosh Tuppad.
