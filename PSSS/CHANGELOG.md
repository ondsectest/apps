# Changelog

All notable changes to PSSS (Password Shoulder-Surf Shield) are documented here.

## 1.3.0

- Added a "Share on LinkedIn" link to the popup footer, next to "Report Bug". Opens LinkedIn's share dialog pointed at the Chrome Web Store listing.
- The existing "Created by: Santhosh Tuppad" footer link already covered visiting the creator's LinkedIn profile — no separate control was needed for that.
- Deliberately **not** automatic: both of the above are user-triggered from the popup, not fired on install. Opening tabs or a pre-armed share dialog the instant a privacy-focused extension installs, with no user action, was considered and rejected — it's the kind of unexpected-redirect behavior Chrome Web Store review flags, and it would have contradicted this extension's own "nothing happens without you asking" design.
- Note: LinkedIn's public share link only accepts the URL being shared — it doesn't support pre-filling custom post text (LinkedIn deprecated that years ago). The preview card LinkedIn generates comes from the Chrome Web Store listing's own description, which should state the no-network-calls/no-data-storage/no-selling-data facts directly for that to show up.

## 1.2.0

- **Fixed:** "Report Bug" link in the popup did nothing when clicked. It was a plain `mailto:` link with no `target`, which tries to navigate the popup's own window — something extension popups can't do. Now opens the [PSSS issue tracker](https://github.com/ondsectest/apps/issues/new?title=%5BPSSS%5D%20&labels=bug) in a new tab instead. ([#1](https://github.com/ondsectest/apps/issues/1), [PR #2](https://github.com/ondsectest/apps/pull/2))
- Added screenshots to the README.

## 1.1.0

Initial public release.

- Randomizes the visible dot count in password fields so the real password length can't be read by shoulder-surfing.
- No permissions granted at install — protection is requested at runtime, per-site or globally, only when explicitly turned on.
- Global and per-site protection are mutually exclusive.
- Toolbar icon reflects live protection state, including an amber "needs a refresh" badge for tabs that were already open when access was granted.
- No network requests, no data collection.

Before this release shipped, testing during development also caught and fixed (so these were never part of any published build, not something introduced and later patched):

- Real password glyphs staying visible under certain field backgrounds — `color: transparent` alone isn't enough in every browser rendering path; some paint input text via `-webkit-text-fill-color`, which can override `color`. Both are set.
- Selecting all (Cmd/Ctrl+A) in a protected field revealing the real password length through the browser's native text-selection highlight, independent of any color trick. The real selection is hidden, and the decoy overlay draws its own fake "selected" look instead.
- A stale overlay briefly shifting or clipping the underlying page's layout during a tab reload. The content script tears itself down immediately when the page starts navigating away.
- Turning protection on silently failing to persist if Chrome's permission dialog closed the extension popup before it finished saving, even though the browser had genuinely granted access. That state is persisted by the background service worker instead, which isn't subject to the same popup-closes-on-blur behavior.

(PRs [#3](https://github.com/ondsectest/apps/pull/3), [#4](https://github.com/ondsectest/apps/pull/4), [#5](https://github.com/ondsectest/apps/pull/5), [#6](https://github.com/ondsectest/apps/pull/6) document these retroactively, since they predate this repo's git history.)
