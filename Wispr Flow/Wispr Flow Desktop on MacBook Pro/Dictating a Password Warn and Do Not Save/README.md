# Dictating a Password: Warn and Don't Save

A working prototype of a security issue in Wispr Flow's **desktop app**, and a proposed improvement. Everything shown uses mock data on a fictional sign-in page.

## The issue

Seen in the Wispr Flow desktop app on a MacBook Pro (macOS). Severity: High.

**Steps to reproduce (use a throwaway password)**

1. Open a sign-in page with a password field.
2. Place the cursor in the password field.
3. Dictate the password with Wispr Flow.
4. Open Wispr Flow's transcript history.

**What happens**

- Wispr Flow does not warn or stop. The password is typed into the field, which shows dots.
- The dictated password appears in Wispr Flow's history in plain text, like any other transcript.
- Depending on settings, the same text may also be synced to the cloud or be eligible for model training.

**Why it matters**

- Anyone with access to the computer or the account can read the password in history.
- People do not expect a password field to be remembered anywhere.

## The improvement

- Wispr Flow detects a password field. macOS marks these as secure text fields, and browsers expose web password inputs the same way.
- When fn is pressed there, the Flow Bar warns: "This is a password field. Dictating a password lets people nearby hear it."
- Two choices: **Type it instead** (default) or **Dictate privately**.
- With Dictate privately, the text goes into the field, but nothing is saved: no history text, no cloud sync, no training, no clipboard. History shows only "Password field, not saved".
- Optionally, the same protection for other secrets in any field, such as one-time codes, card numbers and API keys.
- A warning alone is not enough, because warnings get dismissed. The real protection is never storing it.

## Using the prototype

- Buttons above the window: **Issue** (red), **Improvement** (green) and **Play demo**, which plays both parts with captions.
- Or click the Username or Password field, then click **fn** to "speak" the mock text for that field.
- The mock values are the username `santhosh.demo` and the password "Mango Rocket 42". The site, Example Mail, is fictional.

## Files

| File | What it is |
| --- | --- |
| `index.html` | The prototype. Plain HTML, CSS and JavaScript in one file. Open it in any browser. |
| `Dictated_Password_Demo.mp4` | A 52-second recording of the demo: the issue, then the improvement. |

Reported by Santhosh Tuppad.
