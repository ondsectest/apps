# Installing Same Tab Links

The extension is not on the Chrome Web Store, so load it from the folder.

1. Download or clone this repository and find the `Same Tab Links` folder.
2. Open `chrome://extensions` in Chrome.
3. Switch on **Developer mode** (top right).
4. Click **Load unpacked** and choose the `Same Tab Links` folder.
5. Click the puzzle-piece icon in the toolbar and pin **Same Tab Links**.

Requires Chrome 111 or newer.

## Turning it on for a site

Open the site, click the icon, switch the toggle on, and accept Chrome's permission prompt for
that site. The icon shows **ON** while it is active there.

## Turning it off

Switch the toggle off in the popup, or press **Turn off** next to the site in the list. To
disable everything at once, switch the extension off at `chrome://extensions`.

## Troubleshooting

- **Links still open in new tabs right after switching on.** Reload the page once. If it still
  happens, the site probably opens tabs from its own JavaScript, which this extension does not
  intercept.
- **The toggle flips back off.** The permission prompt was dismissed or declined. Switch it on
  again and choose **Allow**.
- **A login or payment popup stopped appearing.** It should not, since popups with window
  features are left alone. If one does, switch the site off and open an issue.
- **After updating the folder,** click the reload arrow on the extension's card in
  `chrome://extensions`.

## Credits

Built by Santhosh Tuppad.
