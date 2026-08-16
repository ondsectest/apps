# PSSS — Password Shoulder-Surf Shield

A Chrome extension that hides your real password length from anyone watching
your screen. While a password field is focused, it shows a randomized number
of dots instead of the real character count. The real password you typed is
unchanged and is exactly what gets submitted — only what's visible on screen
is altered.

No account, no server, no data collected. [INSTALL.md](./INSTALL.md) covers
loading it in Chrome.

## How it works

- A content script (`content.js`) detects `<input type="password">` fields
  and draws a separate overlay on top showing a decoy dot count, randomized
  on every keystroke, independent of the real length.
- The real input's text is made invisible via both `color` and
  `-webkit-text-fill-color` (some browsers paint glyphs through the latter,
  bypassing the former — especially with autofill-style backgrounds), its
  caret is hidden, and its native text-selection highlight is suppressed too
  (selecting all would otherwise reveal the real length through the
  highlight's width alone, independent of any color trick).
- The overlay draws its own fake caret and its own fake "selected" look, so
  hiding the real ones doesn't leave the field looking dead.
- The real `<input>`'s `type` and `value` are never touched, so native
  autofill and "save password" prompts keep working normally.

## Permission model

Nothing is granted at install. `manifest.json` declares `<all_urls>` only as
an `optional_host_permission`, and the extension requests it at runtime — via
`chrome.permissions.request()` — only when you explicitly turn protection on
for a site (or for every site, if you choose "Globally"). Chrome's own
permission dialog confirms it either way.

Global and per-site protection are mutually exclusive: turning one on revokes
the other's grant first, both so there's never a redundant overlapping
permission and so Chrome always shows a genuine prompt (it silently
auto-grants a request for anything already covered by a broader permission
you hold, so the broader one has to be gone before the narrower one is
requested).

A tab that was already open before you grant permission doesn't need a
reload — `background.js` injects the content script into it immediately. If
that injection doesn't land for some reason, the popup pings the tab, and if
it doesn't answer, offers a one-click "Refresh this tab" — the toolbar icon
also shows an amber badge for the same "needs a refresh" state.

## Design decisions worth knowing

**Two mechanisms hide the real dots, not one.** `color: transparent` alone
isn't sufficient — some browsers render input glyphs via
`-webkit-text-fill-color`, which can stay visible even with `color` hidden.
Both are set.

**The decoy caret sits after the decoy dots, not the real cursor position.**
The real caret is hidden entirely (`caret-color: transparent`), because its
position always tracks the real character count — a leak regardless of what
the dots show.

**Text selection is suppressed on the real field, replicated on the decoy.**
Selecting all (Cmd/Ctrl+A) draws a highlight sized to the real selected
range, independent of any color trick — a second leak vector, closed by
hiding the real selection (`::selection { background: transparent }`) and
giving the overlay its own fake "selected" look using the OS's real
highlight colors.

**Popup permission grants are persisted by the background script, not the
popup.** Chrome closes extension popups the instant a native dialog (like
the permission prompt) steals focus — which can kill the popup's own JS
mid-await, before it saves the grant to storage, even though the
browser-level permission was genuinely granted. `chrome.permissions.onAdded`
/ `onRemoved` listeners in `background.js` own persisting that state
instead, since the service worker has no such focus-loss lifecycle.

**The content script tears itself down the instant the page starts
navigating away.** Without a `pagehide` listener, the continuous
position-sync loop can catch one last frame mid-teardown and write a
stale/wild position to the overlay — an absolutely-positioned element on
`document.body` — which can expand the page's scrollable area and visibly
shift content for that last frame before navigation completes. Tearing down
naively caused a second bug: removing the overlay is itself a DOM mutation,
which re-triggered the mutation observer, which recreated a fresh guard on
the same still-present input. A `torndown` flag, set before detaching and
checked in `scan()`, fixes that.

**The overlay stays transparent, not opaque.** An earlier version painted
the overlay with a solid, live-matched background so it would physically
cover the real row even if the color trick ever failed. In practice, any
small error in the overlay's computed size or position — which real sites'
layouts made easy to hit — painted over neighboring UI (labels, "Show"
links) instead. The narrower, safer fix was keeping `-webkit-text-fill-color`
above, and nothing else.

## File structure

```
manifest.json    MV3 manifest — no static content_scripts, optional_host_permissions only
background.js    Service worker: permission-state persistence, dynamic content script
                 registration, toolbar icon/badge state
content.js       DecoyGuard: the overlay itself, per password field
content.css      Styling for the overlay and the real-input masking rules
popup.html/js    Toggle UI — global vs per-site, light/dark themed
icons/           Toolbar icons (eyes open/closed states)
images/          Popup artwork (static image + animated GIF)
store-assets/    Chrome Web Store listing assets (screenshots, store icon)
```

## Privacy

No network requests anywhere in the codebase. The only thing stored is the
user's own on/off preference, via `chrome.storage.sync` (local to the
browser, synced only across the user's own signed-in Chrome instances).
