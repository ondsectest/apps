# Blank Dictionary Entry Saved With an Empty Value

**Platform:** Wispr Flow on Android (Vivo 60e)
**Area:** Dictionary
**Endpoint:** `POST /api/v1/dictionary/personal`
**Type:** Client-side-only validation / missing required-field check

## Summary

A dictionary entry can be saved with an empty value. By intercepting the add-word request and sending a blank `word`, the server accepts it and a blank row appears in the Dictionary list. The UI should not allow an empty entry, and the server does not reject one either.

## Steps to reproduce

1. Open Wispr Flow on Android and go to Dictionary.
2. Start adding a dictionary word and capture the add request in an intercepting proxy.
3. Set the `word` value to an empty string (`""`) and forward the request.
4. The server responds with success, and the Dictionary list shows a blank row (see `dictionary-blank-entry-bug.png`).

## Impact

- The Dictionary can hold meaningless empty entries that the UI never intended to allow.
- A blank row is confusing and cannot be matched or used; it just clutters the list and the synced data.
- Confirms that the "a dictionary word is required" rule is enforced only in the client, not on the server, matching the other validation findings.

## Classification

- CWE-20 Improper Input Validation
- CWE-1284 Improper Validation of Specified Quantity in Input
- CWE-602 Client-Side Enforcement of Server-Side Security

## Fix

- Reject an empty or whitespace-only `word` on the server with a validation error, and create no record.
- Mirror the same required-field rule in the UI and the API contract.
- Add an API test that posts an empty `word` and expects a rejection.

## Note

Tested on the researcher's own account and traffic only, using a locally trusted proxy. The token was redacted in all evidence.

Finding by Santhosh Tuppad.
