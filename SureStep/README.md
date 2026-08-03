# SureStep

An offline Android app for recording that you did something — locked the door,
turned off the gas, picked up your medicines — so that later, when you are not
sure, you have a record to read instead of a trip back home.

Everything stays on the phone. There is no server, no account, no sync, and the
app holds no `INTERNET` permission.

## Getting it running

`SureStep-1.0-release.apk` in this folder installs straight onto any phone
running Android 9 or newer. It is signed with a debug key — fine for sideloading,
not for the Play Store.

```bash
adb install SureStep-1.0-release.apk
```

To build from source you need JDK 17 and the Android SDK (platform 36,
build-tools 36):

```bash
./gradlew assembleRelease
```

[INSTALL.md](./INSTALL.md) covers prerequisites, SDK paths, release signing and
troubleshooting in full.

Tests and static analysis:

```bash
./gradlew testDebugUnitTest lintDebug
```

## What happens when you confirm a task

1. The timestamp is taken **at the moment of the tap** — not when the shutter
   fires. The record should say when you confirmed the task; the countdown is an
   implementation detail.
2. Battery, device model and network state are read immediately.
3. A location fix starts in the background, so it costs no waiting.
4. If selfies are on, the camera opens, counts down, and takes one frame.
5. One row is written to Room, and you are back on the checklist.

If the camera fails, is denied, has no lens, or you back out — **the record is
still written**, without a photo. Losing a confirmation someone already made is
the worst thing this app could do, so every failure path still saves.

## Design decisions worth knowing

**A recorded task shows the record, not another button.** Once a task is
confirmed, its card switches to "Recorded at 4:22:03 PM" and a *View the record*
link. There is no second confirm button inviting you to do it again. Re-recording
is possible, but it is not offered where repetition would be easiest.

**Reminders go quiet when there is nothing to say.** If everything is already
recorded, the scheduled reminder fires and posts nothing. A notification that
arrives when there is nothing to do teaches you to re-check for no reason.

**Photo capture is a setting, not a fixture.** Some people find the selfie
reassuring. For others it turns one record into another ritual. Turning it off
leaves the timestamp record completely intact.

**Nothing is scored before you started.** A fresh install shows zero missed days
and a blank calendar, not a month of red for days its owner never saw.

**Deleting a task keeps its records.** Task rows carry a snapshot of the title,
and there is no foreign key onto the task table. A record is a statement about
the past; removing a task from today's checklist must not retract it.

**Location comes from the platform, not Play Services.** `LocationManager`
rather than the fused provider — slightly worse fixes, but no Google dependency
in an app whose whole promise is that records do not leave the device.

**Reverse geocoding needs no `INTERNET` permission.** The platform `Geocoder`
runs in a system process. Offline, the lookup returns nothing and the record
keeps its coordinates.

## Architecture

MVVM, single activity, Jetpack Compose throughout.

```
data/local      Room entities, DAOs, database
data/prefs      DataStore settings, PIN hashing
data/repository Task, log and reminder repositories
domain          Models and CompletionRules (day status, streaks)
capture         CaptureCoordinator, location, device info, photo storage
camera          CameraX capture screen
export          CSV and PDF writers
reminders       WorkManager scheduling and notifications
ui              Screens, theme, navigation
di              Hilt modules
```

`CaptureCoordinator` is application-scoped, not screen-scoped, on purpose: if the
camera screen is recreated mid-countdown or the process is backgrounded, the
pending capture survives and the record is still written.

History is paged (Paging 3 over a Room `PagingSource`), so the list is bounded
work whether there are twenty records or two hundred thousand.

## Storage

- Records: Room, `surestep.db`
- Photos: app-private internal storage, `files/records/`. Not in MediaStore, so
  they never appear in the gallery and no other app can read them.
- Exports: `files/exports/`, shared only through the system share sheet when you
  choose to.
- Cloud backup and device-to-device transfer are disabled in the manifest.

Optional PIN lock stores a salted SHA-256 digest; the PIN itself is never
written. There is no recovery — nothing is stored off the device, so nobody can
reset it for you. The app re-locks whenever it leaves the foreground.

## Tests

22 unit tests cover the logic that decides whether the app tells you that you did
well or badly, and when reminders fire:

- `CompletionRulesTest` — day status, streak counting (including the morning case
  where today is incomplete but the streak should hold), missed-day accounting
- `ReminderScheduleTest` — next-occurrence across day masks, week wrapping, and
  the Sunday bit-index that would silently fire on the wrong day if wrong

## A note on compulsive checking

This app is a memory aid. It is built to help you make one intentional record and
then rely on it.

It is not a treatment for OCD, and it is not designed to settle repeated doubt.
If you notice yourself opening the same record over and over, or adding extra
confirmations to feel sure, that is worth mentioning to a doctor or therapist.
The same note appears in the app's Settings screen.

## Deliberately not built

Voice confirmation and the AI packing-list generator appeared in an earlier draft
of the brief but not in the final offline specification, so they are not here.
Task groups (Home / Office / Travel / Shopping / Custom) cover the packing-list
use case without an on-device model.

SQLCipher database encryption was listed as optional and is not wired in. Photos
and the database sit in app-private storage, which is already covered by
full-disk encryption on any modern Android device.
