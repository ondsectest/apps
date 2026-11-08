# Same Tab Links

A Chrome extension that makes links open in the tab you are already in. Some sites
(magicbricks.com was the one that started this) give almost every link
`target="_blank"`, so one search turns into a dozen tabs. This forces those links to
load in the current tab instead, on the sites you choose.

No account, no server, no data collected.

## Using it

1. Open the site, for example magicbricks.com.
2. Click the Same Tab Links icon in the toolbar and switch the toggle on.
3. Chrome asks for permission for that one site. Accept it.

The icon shows a green **ON** badge on sites where it is active. To turn it off, open the
popup and switch the toggle off, or use **Turn off** next to a site in the list. Turning a site
off also revokes the permission Chrome granted for it.

Middle-click, Ctrl-click, Cmd-click and Shift-click still open new tabs, so you can still
choose a new tab on purpose.

## How it works

- A content script (`content.js`) catches clicks before the browser acts on them. If the link
  (or its `<base target="_blank">`) asks for a new tab, it changes the target to `_self`. Forms
  and `formtarget` buttons are handled the same way.
- A second script (`page.js`) runs in the page's own context and redirects a bare
  `window.open(url)` call made from a click. Calls that pass window features, such as the
  `width=...` popups used by login and payment flows, are left alone so those keep working.
- Only the top frame is touched. Embedded iframes, ads included, behave as before.

## Permission model

Nothing is granted at install. `manifest.json` declares `<all_urls>` only as an
`optional_host_permission`. The extension asks for a single site when you switch that site on,
through Chrome's own permission dialog, and registers its scripts for granted sites only.
`storage` keeps your site list in Chrome sync, `scripting` registers the scripts, and
`activeTab` lets the popup read the address of the tab you click it on.

## Known limits

- Some sites open new tabs with their own JavaScript instead of a link or `window.open`. Those
  are not intercepted.
- A site that checks the return value of `window.open` may behave oddly, because the redirected
  call returns `null`.

## Install

See [INSTALL.md](./INSTALL.md).

## Credits

Built by Santhosh Tuppad.

## Licence

MIT, like the rest of this repository. See [LICENSE](../LICENSE).
