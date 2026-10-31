# Play Store Listing: Terms of Service Link Opens a 404

A working prototype of a bug in Wispr Flow's **Google Play Store listing**, and a proposed fix.

## The bug

Seen in the Google Play Store app on a Vivo 60e phone running Android, Wispr Flow version 2.8.3.

**Steps to reproduce**

1. Open the **Google Play Store** app and open **Wispr Flow: AI Voice-to-Text**.
2. Tap **About this app** to open the **Details** page.
3. Scroll to **Privacy & Security**.
4. Tap the link **https://wisprflow.ai/terms-of-service.** It opens in the browser.

**What happens**

- The sentence ends with a full stop right after the URL, and Google Play makes the full stop part of the link.
- The link opens `https://wisprflow.ai/terms-of-service.` with the dot, which is a **404 Page not found**.
- People checking the Terms of Service and Privacy Policy cannot reach them from the Play Store listing.

**What was expected**

The link opens `https://wisprflow.ai/terms-of-service`, the real Terms of Service page.

## The proposed fix

- In the Play Console, remove the full stop after the URL in the app's full description, so the sentence ends with the link.
- Also check other links in the Play Store and App Store listings, and in the app, that are followed by punctuation.
- Optionally, redirect `/terms-of-service.` to `/terms-of-service` on the website, so old copies of the text still work.

## Using the prototype

- Two buttons sit above the phone: **Issue** (red) and **Improvement** (green). Each opens a short explanation and switches the prototype to that version.
- The page recreates the Wispr Flow **Details** page in the Play Store app. Earlier parts of the description are shown as grey placeholder lines.
- In **Issue**, the dot is highlighted as part of the link, and tapping it opens a browser view with a 404.
- In the fix, the dot is gone and tapping the link opens the Terms of Service page.
- Each view has a link to the real address, so both outcomes can be checked live.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Reported by Santhosh Tuppad.
