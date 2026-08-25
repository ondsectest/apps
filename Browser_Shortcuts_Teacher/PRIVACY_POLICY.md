# Privacy Policy — Browser Shortcuts Master

**Last updated:** August 25, 2026

## Overview

Browser Shortcuts Master is a Chrome extension with one purpose: to help you
learn Chrome and OS keyboard shortcuts. It shows a random shortcut tip in a
small floating card every 5–10 minutes, or on demand via a keyboard
shortcut, using the correct key combo for your operating system.

This policy explains exactly what data the extension touches and what it
does with it.

## Information we collect

**None.** Browser Shortcuts Master does not collect, log, or transmit any
personal information, browsing history, page content, or usage data. It has
no server, no analytics, and no account system — there is nothing for it to
send anywhere, and nowhere for it to send it to.

## What is stored, and where

The extension saves a small amount of state locally on your device, using
Chrome's `storage.local` API:

- Whether tips are currently paused
- Whether tips are snoozed, and until when
- The index of the last tip shown (so the same tip doesn't repeat twice in a
  row)
- Your detected OS family (Mac, or Windows/Linux/ChromeOS), used only to
  show the correct key combo

This data never leaves your device. It is not synced to any server, not
visible to the developer, and not shared with any third party.

## Permissions this extension uses

- **alarms** — schedules the periodic tip on a randomized 5–10 minute timer.
- **storage** — saves the local state listed above.
- **tabs** — lets the background service worker find the currently active
  tab when a tip is due, since the periodic tip isn't triggered by a fresh
  click from you.
- **scripting** — injects the small tip-card element into the active tab so
  it can be displayed.
- **host permission (`<all_urls>`)** — needed because a tip can appear on
  whatever site you happen to be browsing, which can't be known in advance.
  The injected code only ever adds the tip card; it never reads page
  content, form data, or cookies, and makes no network requests.

## Third-party sharing

None. This extension makes no network requests of any kind, includes no
third-party analytics or advertising SDKs, and shares no data with anyone,
because it doesn't collect any data to share.

## Remote code

None. All code ships inside the packaged extension. Nothing is fetched,
evaluated, or loaded from a remote server at runtime.

## Children's privacy

This extension is not directed at children and does not knowingly collect
information from anyone, regardless of age.

## Changes to this policy

If this policy changes, the "Last updated" date above will change, and the
updated version will be posted at this same URL.

## Contact

Questions about this policy can be sent to: ondsectest@gmail.com
