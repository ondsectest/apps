# Client-Controlled created_at and modified_at on Dictionary Entries

**Platform:** Wispr Flow Pro desktop on macOS (Electron build WisprFlow/1.6.1337)
**Endpoint:** `POST /api/v1/dictionary/personal`
**Area:** Dictionary (and Snippets, same endpoint)
**Type:** External control of a server-managed field

## Summary

The `created_at` and `modified_at` timestamps on a dictionary entry are set by the client and trusted by the server. By editing the request, an entry can be given any creation or modification time, for example a date far in the past so it sorts as the "Oldest" entry. These fields should be assigned by the server and never accepted from the client.

## The request (captured, token redacted)

```http
POST /api/v1/dictionary/personal HTTP/2
Host: api.wisprflow.ai
Content-Type: application/json
Authorization: <redacted>
User-Agent: ... WisprFlow/1.6.1337 Chrome/150 Electron/43.8.3 ...

[{"id":"1b7e16ca-a869-4c2f-9459-76052aca3deb",
  "team_dictionary_id":"00000000-0000-0000-0000-000000000000",
  "word":"Oldest",
  "replacement":null,
  "replacement_html":null,
  "is_manual":true,
  "created_at":"2025-11-02T16:53:50.856Z",   // <-- client-supplied, backdated one year
  "modified_at":"2025-11-02T16:53:50.856Z",  // <-- client-supplied, backdated one year
  "is_deleted":false,
  "frequency_used":0,
  "last_used":null,
  "source":"manual",
  "observed_source":null,
  "is_snippet":false}]
```

The two highlighted fields are the issue: the client chose both timestamps, and the server stored them as given.

## Steps to reproduce

1. Sign in to Wispr Flow on your own account.
2. Add a dictionary word and capture the `POST /api/v1/dictionary/personal` request in an intercepting proxy.
3. Change `created_at` and `modified_at` to an arbitrary time (for example one year in the past).
4. Forward the request. The server accepts it.
5. Open the Dictionary and confirm the entry carries the forged time (here it sorts as the oldest entry).

## Impact and risks

- **Data integrity:** timestamps that should record when something actually happened can be set to any value, so they cannot be trusted.
- **Ordering and display manipulation:** an entry can be forced to appear as the oldest or newest, affecting any sort, "recently added", or history view.
- **Interaction with retention:** if server-side purge or retention of deleted data is driven by these timestamps (see the deleted-data retention finding), a forged old date could trigger premature deletion, and a forged future date could keep data past its intended window.
- **Audit and forensic reliability:** any logging or review that relies on these fields is undermined, since the record's own times are attacker-chosen.
- **Signal to attackers:** trusting client-supplied, normally-immutable fields is a sign of weak server-side input hygiene. It invites a curious or motivated tester to probe the other client-controlled fields on this request (`id`, `team_dictionary_id`, `is_deleted`, `frequency_used`, `source`).

## Classification

- CWE-472 External Control of Assumed-Immutable Web Parameter (primary)
- CWE-20 Improper Input Validation
- CWE-345 Insufficient Verification of Data Authenticity

## Fix

- Set `created_at` and `modified_at` on the server from the server clock. Ignore any values the client sends for these fields.
- Apply the same rule to the other server-managed fields on this endpoint (`id` assignment, `is_deleted`, `frequency_used`, `last_used`, `source`, `observed_source`), accepting from the client only what the client legitimately owns (`word`, `replacement`, `is_snippet`, `is_manual`).
- Add an API test that sends forged timestamps and expects the server to overwrite them with its own.

## Scope note

Tested on the researcher's own account and traffic only, using a locally trusted proxy. The authorization token was redacted in all evidence.

Finding by Santhosh Tuppad.
