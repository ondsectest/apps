# Snippet Name Length Is Validated Only on the Client

A working prototype of a validation finding in Wispr Flow, and a proposed fix. Built with invented sample data only; any real captured evidence is kept private.

## The finding

Platform: Wispr Flow Pro desktop on macOS (Electron build WisprFlow/1.6.1337).
Area: Settings, Dictionary, Snippets (observed via the app's own `POST /api/v1/dictionary/personal` request, where a snippet is a personal dictionary entry with `is_snippet` true).

**What was observed**

1. In the Add snippet form, the desktop app stops accepting input in the snippet name field at 60 characters, so the limit looks enforced.
2. Replaying the add-snippet request with a 100-character snippet name (`word`) returns `HTTP 200` and the body `true`.
3. A follow-up read of the personal dictionary shows the snippet stored with all 100 characters kept, `is_snippet` true and `is_deleted` false.
4. The server performs no length check of its own. The 60-character limit exists only in the client. This is a boundary value violation: the documented maximum is not enforced where it is authoritative.

**Why it matters**

- Any path that skips the UI, such as an intercepting proxy, a scripted call, or a modified build, can write oversized snippet names.
- Oversized names can bloat storage and sync payloads, and break any UI that assumes a 60-character bound.
- Unbounded input is a common lead-in to further issues: truncation mismatches, layout breakage, and parsing or display bugs wherever the snippet name is shown or matched.
- Worth confirming the server also bounds the expansion field and validates content and type, not just the name.

## Classification

- CWE-1284 Improper Validation of Specified Quantity in Input
- CWE-20 Improper Input Validation
- CWE-602 Client-Side Enforcement of Server-Side Security

## The proposed fix

**On the server**

- Enforce the same 60-character limit on the snippet name, and treat the server as the source of truth for the boundary.
- Reject an over-limit name with a structured validation error naming the field, the limit, and what was received, and create no snippet.
- Bound the expansion field on the server as well.

**Shared**

- Define the limit once, as a shared constant or schema, so the client input and the API contract cannot drift apart.
- Reuse the rule on other forms in the app so validation stays consistent.
- Add an API test that posts an over-limit snippet name directly, so the boundary cannot regress unnoticed if the UI changes.

## Using the prototype

- Two buttons at the top: **Issue** (red) and **Improvement** (green).
- In **Issue**, the snippet name field caps typing at 60. The request and response panels show the bypass: a 100-character name is accepted with `HTTP 200`, and the stored record keeps all 100 characters.
- In **Improvement**, the server rejects the over-limit name with a `422` validation error that names the 60-character limit and the received length, and no snippet is created.

An evidence GIF (`Snippet_Name_Bypass.gif`) and an MP4 (`Snippet_Name_Bypass.mp4`) show the full sequence: the field capping at 60, an interceptor in the middle sending a 100-character name via a headless request, the `HTTP 200` response, and the Snippets screen then showing the over-limit name. Recreated with redacted, mock data only.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Note: testing was done on the researcher's own account and traffic only, using a locally trusted proxy. The real captures are shared with Wispr privately, not published here.

Finding by Santhosh Tuppad.
