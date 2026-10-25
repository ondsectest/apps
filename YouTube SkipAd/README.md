# YouTube SkipAd

A small Chrome extension that clicks YouTube's **Skip Ad** button for you as soon as it appears.

It doesn't block ads. When a skippable ad plays, it waits for the Skip button to become clickable
and clicks it, so you don't have to reach for the mouse.

**Code:** https://github.com/ondsectest/apps/tree/main/YouTube%20SkipAd

## Get it

Either way below gives you a folder containing this extension's files.

**Option 1: Download ZIP (no Git needed)**

1. Go to https://github.com/ondsectest/apps
2. Click **Code** → **Download ZIP**, then unzip it.
3. The extension is in the `YouTube SkipAd` folder inside it.

**Option 2: Clone with Git (only this folder)**

```bash
git clone --depth 1 --filter=blob:none --sparse https://github.com/ondsectest/apps.git
cd apps
git sparse-checkout set "YouTube SkipAd"
```

## Install in Chrome

1. Open `chrome://extensions`.
2. Turn on **Developer mode** (top right).
3. Click **Load unpacked** and select the `YouTube SkipAd` folder.
4. Refresh any YouTube tabs you already have open.

## What to expect

- **A bar at the top of Chrome** saying *"SkipAd" started debugging this browser*. Chrome always
  shows this when an extension sends mouse input this way, and extensions can't hide it. Clicking
  **Cancel** stops the clicking until the next ad.
- **A permission warning on install** that the extension can read and change data on all websites.
  Chrome shows this for any extension with the `debugger` permission. The code only runs on
  `youtube.com`, and only acts when a Skip button appears.

## How it works

| File | What it does |
|---|---|
| `manifest.json` | Tells Chrome to run the extension on YouTube |
| `content.js` | Watches the page for the Skip button and finds where it is on screen |
| `background.js` | Moves the mouse to the button and clicks it, using Chrome's debugger API |

YouTube can ignore clicks made directly from a script, so `background.js` sends real mouse movement
and a real click instead, the same way a person would.

## If it stops working

YouTube changes its player from time to time. Open the console on the YouTube tab (**F12** →
**Console**) and filter for `SkipAd`:

- **No `[SkipAd]` line when an ad plays:** YouTube has probably renamed the Skip button. Update
  `SKIP_SELECTORS` in `content.js`.
- **The line appears but the ad isn't skipped:** check for errors under **service worker** on the
  extension's card in `chrome://extensions`.

## Licence

MIT. Use it, change it, share it. See [LICENSE](../LICENSE).
