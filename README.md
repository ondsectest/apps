# apps

## [Calm Control](./Calm%20Control)

An offline Android app for noticing emotional triggers and watching self-control grow. Log a
moment as "stayed calm" or "got angry", tag what set it off, and the charts build a picture of
progress rather than a record of failure.

Jetpack Compose, Room, no third-party chart library — the visualisations are hand-drawn on
Compose `Canvas` so the app works entirely offline.

- [README](./Calm%20Control/README.md) — architecture and the design rules behind it
- [INSTALL](./Calm%20Control/INSTALL.md) — prerequisites, building, sideloading, troubleshooting

## [SureStep](./SureStep)

An offline Android app for recording that you did something — locked the door, turned off the gas,
picked up your medicines — so that later, when you are not sure, you have a record to read instead
of a trip back home.

Tapping a task saves a timestamp, and optionally a location and an automatic selfie. Once recorded,
the task shows its record rather than another button to press: the app is built to replace repeated
checking, not to encourage it.

Jetpack Compose, Room, CameraX, Hilt, WorkManager. No server, no account, and no `INTERNET`
permission — nothing recorded can leave the phone.

- [README](./SureStep/README.md) — architecture and the design rules behind it
- [INSTALL](./SureStep/INSTALL.md) — prerequisites, building, sideloading, troubleshooting

## [PSSS](./PSSS)

A Chrome extension that hides your real password length from shoulder-surfers. While a password
field is focused, it shows a randomized, constantly-changing number of dots instead of the real
character count — the real password you typed is unchanged and is exactly what gets submitted.

Manifest V3, zero permissions granted at install — protection only activates on a site after you
explicitly approve it, per-site or globally, through Chrome's own permission dialog. No server, no
account, no data collected.

- [README](./PSSS/README.md) — architecture and the design decisions behind it
- [INSTALL](./PSSS/INSTALL.md) — loading it in Chrome, permissions it asks for, troubleshooting

## Licence

MIT. Fork it, copy it, modify it, ship it commercially. See [LICENSE](./LICENSE).

The one exception is the bundled Lora typeface, which stays under the SIL Open Font License 1.1.
See [THIRD-PARTY-NOTICES.md](./THIRD-PARTY-NOTICES.md).

[Contributing](./CONTRIBUTING.md) lists where the project is thinnest, which is mostly testing.
