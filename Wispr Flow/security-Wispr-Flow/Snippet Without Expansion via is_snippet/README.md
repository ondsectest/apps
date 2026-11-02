# A Snippet Can Be Created With No Expansion by Flipping is_snippet

A working prototype of a business logic finding in Wispr Flow, and a proposed fix. Built with mock or dummy data only; any real captured evidence that contains live data is kept private.

## The finding

Platform: Wispr Flow Pro desktop on macOS (Electron build WisprFlow/1.6.1337).
Endpoint: `POST /api/v1/dictionary/personal`.
Area: Dictionary and Snippets.

**What was observed**

1. In the Add snippet form, the UI will not let you save a snippet without an expansion. Save stays disabled until an expansion is entered.
2. A dictionary word has no expansion. Intercepting the add-word request and changing `is_snippet` from `false` to `true`, while leaving `replacement` as `null`, submits a snippet with a name and no expansion.
3. The server responds `HTTP 200` with body `true` and stores the record with `is_snippet` true and `replacement` null.
4. The result is a snippet the product itself considers invalid, created by bypassing a rule the UI enforces. The business rule is enforced only on the client.

**Why it matters**

- Business logic enforced only in the UI can be bypassed by editing the request directly.
- An expansion-less snippet is a broken state: triggering it produces nothing, and any view or sync that assumes a snippet always has an expansion may misbehave.
- It signals that other field relationships (for example a required snippet name, or type consistency) may also be unenforced on the server, which is worth confirming.

## Classification

- CWE-840 Business Logic Errors
- CWE-20 Improper Input Validation
- CWE-602 Client-Side Enforcement of Server-Side Security

## The proposed fix

**On the server**

- Enforce the same rule as the UI: when `is_snippet` is true, require a non-empty `replacement`. Reject a request that lacks one and create no record.
- Return a structured validation error naming the field and the rule.

**Shared**

- Define the "a snippet needs an expansion" rule once and apply it to both the form and the API contract.
- Review related field relationships (name required, type consistency) and enforce them server-side as well.
- Add an API test that posts `is_snippet` true with an empty expansion and expects a rejection.

## Using the prototype

- Two buttons at the top: **Issue** (red) and **Improvement** (green).
- The Add snippet form shows the UI rule: Save is disabled until an expansion is entered.
- In **Issue**, the request and response panels show the bypass: `is_snippet` set to true with `replacement` null is accepted with `HTTP 200`, and the Snippets list then shows a snippet with no expansion, marked invalid.
- In **Improvement**, the server rejects the request with a `422` validation error stating that a snippet requires a non-empty expansion.

A screen recording of this finding is kept in this folder (added separately). The sample word is a harmless placeholder (`bye`), so the published material contains no real or sensitive data.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Note: demonstrated on the researcher's own account and traffic only, using a locally trusted proxy. Any captures that contain live tokens or real data are shared with Wispr privately, not published here.

Finding by Santhosh Tuppad.
