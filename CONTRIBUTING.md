# Contributing

Fork it, copy it, change it, ship your own version. The code is MIT, so you do not need
permission and you do not need to contribute anything back. If you do want to send something
here, this page is only about making that easy.

## The honest state of things

Calm Control is thinly tested. There are unit tests covering the report arithmetic and the rules
about what generated sentences may say, and nothing else. No UI tests, no instrumentation tests.
It has run on one emulator configuration, Android 14 at 320x640.

That means the most valuable contribution right now is probably not a feature.

## Where it is most likely broken

Untested territory, roughly in order of how much it would help to have covered:

- **Dates and timezones.** Everything is bucketed by local date. Crossing midnight, changing
  timezone mid-week, DST transitions, and leaving the app open overnight are all plausible ways to
  get wrong numbers. `ReportsCalculator` is pure and takes an explicit `ZoneId`, so these are cheap
  to write as unit tests.
- **Accessibility.** TalkBack has never been switched on. The charts are `Canvas` drawings with
  almost no semantics, so a screen reader currently gets very little from the Progress screen.
- **Large font and display sizes.** The layout survived a 320dp-wide screen, but nothing has been
  checked at the larger accessibility font scales, where the daily summary card is the obvious
  first casualty.
- **RTL layouts.** Never tried. The charts compute positions from left to right by hand.
- **Tablets and foldables.** There is no adaptive layout at all; everything assumes one column.
- **Low-end devices.** The orbs animate continuously off a frame clock. That has been measured on
  an emulator and nowhere else.
- **Long histories.** The reports load a rolling 90-day window and aggregate in memory. Fine for
  ordinary use, unverified for someone who logs very heavily over a year.

## Known gaps, if you would rather build than test

- No way to edit or delete a logged moment. Everything is append-only, so a mis-tap is permanent.
- `TriggerEvent` stores `intensity` and `note`, but nothing captures or displays them.
- No export. The database never leaves the device, which also means there is no backup story.
- No home screen widget or quick-settings tile, which is exactly where a "log this now" button
  belongs.
- `DemoDataSeeder` should eventually go. It only runs on debug builds when the database is empty.

## Design rules worth reading first

Some things in this app look like arbitrary choices and are not. The
[README](./Calm%20Control/README.md) has a "Design rules worth keeping" section. In short:

- The centre of the ring is the self-control rate, never the anger rate.
- Neither log button is styled as the wrong answer.
- No generated sentence describes a decline. A test enforces this.
- No quote shames the reader. A test enforces this too.

A change that breaks one of those is not a bug fix, it changes what the app is for. That does not
make it unwelcome, but say so in the pull request so it gets discussed rather than merged quietly.

## Getting set up

See [INSTALL.md](./Calm%20Control/INSTALL.md). Short version: JDK 17, Android SDK platform 35,
then `./gradlew assembleDebug`.

Run the tests with:

```bash
./gradlew :app:testDebugUnitTest
```

## Sending a change

Fork, branch, open a pull request. Keep it focused. If it changes behaviour, add a test, since
the calculator layer is pure and takes an injectable clock and zone, which makes most report logic
testable without an emulator.

Bug reports are just as welcome as code. Include your device, Android version, and what you
expected to happen.
