# Personal Dictionary Word Length Is Validated Only on the Client

A working prototype of a validation finding in Wispr Flow, and a proposed fix. Built with invented sample data only; any real captured evidence is kept private.

## The finding

Platform: Wispr Flow Pro desktop on macOS (Electron build WisprFlow/1.6.1337).
Area: Settings, Dictionary, Personal (observed via the app's own `POST /api/v1/dictionary/personal` request).

**What was observed**

1. In the Add word field, the desktop app stops accepting input at 60 characters, so the limit looks enforced.
2. Replaying the add-word request with a 100-character `word` value returns `HTTP 200` and the body `true`.
3. A follow-up read of the personal dictionary shows the record stored with all 100 characters kept, `is_deleted` false.
4. The server performs no length check of its own. The 60-character limit exists only in the client.

**Why it matters**

- Any path that skips the UI, such as an intercepting proxy, a scripted call, or a modified build, can write oversized values.
- Oversized entries can bloat storage and sync payloads for that account.
- Unbounded input is a common lead-in to further issues: truncation mismatches, layout breakage, and parsing or display bugs wherever the word is rendered.
- If length is unchecked, it is worth confirming the server also validates content, type, and the replacement fields consistently.

## Classification

- CWE-1284 Improper Validation of Specified Quantity in Input
- CWE-20 Improper Input Validation

## The proposed fix

**On the server**

- Enforce the same length limit the client shows, and treat the server as the source of truth.
- Reject an over-limit value with a structured validation error naming the field, the limit, and what was received, and create no record.
- Apply the same rule to every write field here (`word`, `replacement`).

**Shared**

- Define the length limit once, as a shared constant or schema, so the client input and the API contract cannot drift apart.
- Reuse the rule on other forms in the app so validation stays consistent.
- Add an API test that posts an over-limit value directly, so the protection cannot regress unnoticed if the UI changes.

## Using the prototype

- Two buttons at the top: **Issue** (red) and **Improvement** (green).
- In **Issue**, the Add word field caps typing at 60. The request and response panels show the bypass: a 100-character `word` is accepted with `HTTP 200`, and the stored record keeps all 100 characters.
- In **Improvement**, the server rejects the over-limit value with a `422` validation error that names the 60-character limit and the received length, and no record is created.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Note: testing was done on the researcher's own account and traffic only, using a locally trusted proxy. The real captures are shared with Wispr privately, not published here.

Finding by Santhosh Tuppad.
