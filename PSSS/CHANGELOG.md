# Changelog

All notable changes to PSSS (Password Shoulder-Surf Shield) are documented here.

## 1.2.0

Fixes for five bugs found during real-world testing after the initial release. See [PRs #2-#6](https://github.com/ondsectest/apps/pulls?q=is%3Apr+is%3Aclosed) for the full diff and writeup on each.

- **Fixed:** "Report Bug" link in the popup did nothing when clicked. It was a plain `mailto:` link with no `target`, which tries to navigate the popup's own window — something extension popups can't do. Now opens the [PSSS issue tracker](https://github.com/ondsectest/apps/issues/new?title=%5BPSSS%5D%20&labels=bug) in a new tab instead.
- **Fixed:** real password glyphs could stay visible under certain field backgrounds. `color: transparent` alone isn't enough in every browser rendering path — some paint input text via `-webkit-text-fill-color`, which can override `color`. Both are now set.
- **Fixed:** selecting all (Cmd/Ctrl+A) in a protected field revealed the real password length through the browser's native text-selection highlight, independent of any color trick. The real selection is now hidden, and the decoy overlay draws its own fake "selected" look instead.
- **Fixed:** a stale overlay could briefly shift or clip the underlying page's layout during a tab reload. The content script now tears itself down immediately when the page starts navigating away.
- **Fixed:** turning protection on could silently fail to persist if Chrome's permission dialog closed the extension popup before it finished saving — even though the browser had genuinely granted access. That state is now persisted by the background service worker instead, which isn't subject to the same popup-closes-on-blur behavior.

## 1.1.0

Initial public release.

- Randomizes the visible dot count in password fields so the real password length can't be read by shoulder-surfing.
- No permissions granted at install — protection is requested at runtime, per-site or globally, only when explicitly turned on.
- Global and per-site protection are mutually exclusive.
- Toolbar icon reflects live protection state, including an amber "needs a refresh" badge for tabs that were already open when access was granted.
- No network requests, no data collection.
