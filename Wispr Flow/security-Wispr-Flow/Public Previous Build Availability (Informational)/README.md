# Informational: Publicly Retrievable Previous Desktop Builds via the Auto-Update Release Feed

An informational observation from testing Wispr Flow, raised as context rather than as a vulnerability. No exploit, no data exposure, and no action required for the observation itself.

**Severity:** Informational (no direct impact)

**Type:** Information note. Loosely related to CWE-200 (Exposure of Information) in spirit only; no sensitive data is exposed by the observation itself.

## Summary

While reviewing the desktop app's update behaviour, the auto-update release feed at `dl.wisprflow.com` was observed to be publicly readable and to reference direct archive URLs for prior application builds. Requesting the feed returns release metadata, and the referenced zip URLs download previous versions directly, without authentication. This appears to be by design and is how Electron-style auto-update works, so it is raised as information rather than as a vulnerability.

## What was observed

- `GET /wispr-flow/darwin/arm64/RELEASES.json` on `dl.wisprflow.com` returns a release manifest, keyed by platform and architecture.
- The manifest references archive URLs for prior builds, and those archives download directly over HTTPS without authentication.
- The request is the app's own update check. The User-Agent identifies the Wispr Flow client and CFNetwork or Electron, confirming this is the intended update channel.

## Why this is information and not a finding

- The update feed is meant to be world-readable so unauthenticated clients can discover and fetch updates.
- Release binaries are public artifacts. Anyone can obtain the same bytes by installing the app and letting it update.
- No credentials, user data, or internal systems are exposed by the feed or the archives.
- For an Electron application, previous builds offer little unique analytical value, since the current install already contains the same extractable application bundle.

## Conditions under which it could matter

The public availability of old builds only becomes security relevant if combined with a weakness elsewhere. These are offered as items worth confirming internally, not as claims:

- Whether the updater enforces version monotonicity, so a client cannot be moved to an older, signed build (a downgrade or rollback scenario).
- Whether the downloaded build's signature and version are both verified before it is applied.
- Whether any prior build contains a secret or references an endpoint that is still valid today. If so, that would be a separate finding on its own merit, independent of version availability.

## Recommendation

- Confirm that the updater rejects downgrades to lower versions and verifies both signature and version before applying an update.
- Periodically review historical builds for any still-valid secrets or endpoints, since those would carry impact regardless of version age.
- No action is required for the feed being readable. This note is for awareness only.

## Scope note

All observation was performed on the researcher's own device and own installed client. The archives downloaded were the researcher's own copies. No testing was performed against Wispr systems beyond the app's normal, intended update request.

Observation by Santhosh Tuppad.
