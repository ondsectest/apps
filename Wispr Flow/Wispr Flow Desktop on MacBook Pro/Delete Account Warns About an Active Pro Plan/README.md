# Delete Account Warns About an Active Pro Plan

A working prototype of an issue in the Wispr Flow **desktop app** (Settings, Account) and a proposed improvement.

## The issue

Seen in the Wispr Flow desktop app v1.6.1337 on a MacBook Pro (macOS), signed in with an account on an active Pro plan.

**Steps to reproduce**

1. Open **Settings**, then **Account**.
2. Click **Delete account**.
3. Read the confirmation dialog.

**What happens**

- The dialog only says it will delete all data, memory and dictionary, locally and on the servers.
- It says nothing about the Pro plan. Wispr Flow's help docs say that deleting an account with an active subscription cancels it immediately with no refund, not even a prorated one, and that referral credits are permanently deleted.
- A paying user can lose the rest of their plan without being told. As a Pro user, the tester felt anxious and unsure whether Pro would be lost.

**What was expected**

- When the account has an active paid plan, the dialog says the plan is cancelled immediately with no refund, and that referral credits are lost.
- It offers ways to manage the plan or contact support first, so people can get a refund or change their mind.

## The improvement

- For accounts with an active paid plan, an amber warning in the dialog: the plan is cancelled right away, there is no refund, and referral credits are deleted. To get a refund, contact support first; to keep Pro, go back.
- **Manage plan** and **Contact support** buttons inside the warning.
- **Step 1:** tick "I understand my Pro plan will be cancelled immediately with no refund." **Step 2:** type the email. **Delete account** turns on only after both.
- Accounts on the free plan see the dialog as it is today.

## Using the prototype

- Two buttons sit above the window: **Issue** (red) and **Improvement** (green). Each opens a short explanation and switches the prototype to that version.
- The window recreates the desktop Settings window, with the Account page showing the Pro plan. It scales to fit smaller screens.
- Click **Delete account** to open the dialog. Going through with it shows what happens to the Pro plan; nothing is really deleted.
- The email in the prototype is a placeholder, `you@example.com`.

Plain HTML, CSS and JavaScript in a single file, with no build step. Open `index.html` in any browser.

Prototype by Santhosh Tuppad.
