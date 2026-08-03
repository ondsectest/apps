# Installing and building SureStep

Two ways in: install the APK on a phone, or build from source.

---

## 1. Just run it on a phone

Requires **Android 9.0 (API 28) or newer**.

1. Take `SureStep-1.0-release.apk` from this folder.
2. Copy it to the phone and open it. Android will ask you to allow installs from whichever app
   you transferred with — Files, Drive, Chrome. Allow it for that app only.
3. Or install over USB with debugging enabled:

   ```bash
   adb install SureStep-1.0-release.apk
   ```

   If you have both a phone and an emulator connected, `adb` will refuse to guess. Use `adb -d`
   for the physical device or `adb -e` for the emulator.

The bundled APK is signed with a **debug key**, which is fine for sideloading but not for the
Play Store. See [Signing a real release](#signing-a-real-release) below.

### Permissions it asks for

None on first launch. They are requested at the moment they first matter — on your first
confirmation, not in an upfront wall:

| Permission | Why | If you decline |
| --- | --- | --- |
| Camera | The selfie attached to a record | The record still saves, without a photo |
| Location | Coordinates and address on a record | The record still saves, without a place |
| Notifications | Reminders you schedule yourself | No reminders; everything else works |

There is no `INTERNET` permission at all. Nothing you record can leave the phone.

---

## 2. Build from source

### Prerequisites

| Need | Version | Note |
| --- | --- | --- |
| JDK | **17** | AGP 8.9 will not run on 21+ |
| Android SDK | Platform **36**, build-tools 36 | |
| Gradle | 8.11.1 | Supplied by the wrapper — do not use a system Gradle |

The easiest route is **Android Studio Meerkat or newer**, which bundles a JDK and can install the
SDK for you. Open the folder, let it sync, and it will offer to fetch anything missing.

### Point the build at your SDK

`local.properties` is intentionally not committed, since it holds a machine-specific path. Create
it in this folder:

```properties
sdk.dir=/Users/you/Library/Android/sdk
```

Android Studio writes this file for you on first sync. Common locations:

- macOS (Studio): `~/Library/Android/sdk`
- macOS (Homebrew command-line tools): `/opt/homebrew/share/android-commandlinetools`
- Linux: `~/Android/Sdk`
- Windows: `C:\Users\you\AppData\Local\Android\Sdk`

Setting the `ANDROID_HOME` environment variable works instead of the file.

### Build

```bash
./gradlew assembleDebug
```

```bash
./gradlew assembleRelease
```

APKs land in `app/build/outputs/apk/`. The release build runs R8, so it is around 4.8 MB against
the debug build's 65 MB.

If your JDK is not the one on `PATH`, point the build at it explicitly:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew assembleRelease
```

### Tests and static analysis

```bash
./gradlew testDebugUnitTest lintDebug
```

22 unit tests cover day scoring, streak counting and reminder scheduling. Lint should report only
`GradleDependency` notices, which are "a newer library version exists" and not failures.

### Signing a real release

The release build falls back to the debug key when no keystore is configured, so it always
produces something installable. For a Play Store build, generate a keystore and put these in
`~/.gradle/gradle.properties` — never in the repository:

```properties
SURESTEP_STORE_FILE=/path/to/keystore.jks
SURESTEP_STORE_PASSWORD=...
SURESTEP_KEY_ALIAS=...
SURESTEP_KEY_PASSWORD=...
```

---

## Troubleshooting

**`Unable to locate a Java Runtime`** — no JDK on `PATH`. Install one (`brew install openjdk@17`)
and either set `JAVA_HOME` as above or let Android Studio supply its bundled JDK.

**`Android resource linking failed: mipmap/ic_launcher not found`** — usually a stale Gradle
configuration cache after moving resource folders. Run `./gradlew clean` and build again. Note
that `mipmap-anydpi-v26` must keep its `-v26` qualifier even though `minSdk` is 28; adaptive icons
will not link without it, and Lint's suggestion to merge the folder is a false positive here.

**Build succeeds but the camera never opens** — check that the device or emulator actually has a
camera. SureStep prefers the front lens, falls back to the rear one, and if neither can be bound
it waits 12 seconds and then saves the record without a photo rather than stranding you.

**Emulator shows no countdown for several seconds** — CameraX initialisation retries for around
five seconds on emulators with an unusual camera configuration. The screen shows
"Starting the camera…" while that happens. Physical devices are close to instant.

**`adb: no devices/emulators found`** — the emulator died or USB debugging is off. `adb devices`
should list something before you install.
