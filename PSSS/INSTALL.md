# Installing PSSS in Chrome

This isn't on the Chrome Web Store yet — load it as an unpacked extension.

1. Open `chrome://extensions`.
2. Turn on **Developer mode** (top-right toggle).
3. Click **Load unpacked** and select the `PSSS` folder.
4. Click the extension's icon in the toolbar, and turn protection on — either
   **Globally** or **Per site** for whichever page you're on. Chrome will
   show a permission dialog; approving it is what actually activates the
   shield. Nothing is granted automatically at install.

### Permissions it asks for

Nothing at install. Everything below is requested only at the moment you
turn protection on, via Chrome's own permission dialog — never up front:

| Permission | Why |
| --- | --- |
| `storage` | Saves your on/off preference locally |
| `tabs` | Lets the popup know which site you're on, and lets the toolbar icon reflect protection state as you switch tabs |
| `scripting` | Activates the shield on a tab immediately after you grant access, without needing a reload |
| host access (a specific site, or all sites) | Only requested when you explicitly turn protection on |

There's no `activeTab` permission and no static `host_permissions` entry —
see the README's "Permission model" section for why.

### After making code changes

Click the reload icon on the extension's card at `chrome://extensions`. Tabs
that were already open when you reload won't automatically pick up JS/CSS
changes — refresh those too.

### Troubleshooting

- **Toggle shows on, but dots aren't randomizing on an already-open tab** —
  open the popup; if the tab wasn't reachable, a "Refresh this tab" prompt
  appears there, and the toolbar icon shows an amber `!` badge for the same
  reason.
- **Testing on `file://` pages** — extensions don't get file access by
  default. Enable "Allow access to file URLs" on the extension's Details
  page at `chrome://extensions`, or test against an `http(s)://` page
  instead.
- **Global and per-site both look off after switching modes** — expected:
  turning one on always revokes the other's permission first (see the
  README). Only one is ever active at a time.
