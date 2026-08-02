# Installing and building Calm Control

Two ways in: install the APK on a phone, or build from source.

---

## 1. Just run it on a phone

Requires **Android 8.0 (API 26) or newer**.

1. Build an APK (see below) or take one from a release.
2. Copy it to the phone and open it. Android will ask you to allow installs from whichever app
   you transferred with — Files, Drive, Chrome. Allow it for that app only.
3. Or install over USB with debugging enabled:

   ```bash
   adb install CalmControl-1.0-release.apk
   ```

   If you have both a phone and an emulator connected, `adb` will refuse to guess. Use `adb -d`
   for the physical device or `adb -e` for the emulator.

The app needs no permissions and no network. Everything stays in a local database on the device.

---

## 2. Build from source

### Prerequisites

| Need | Version | Note |
| --- | --- | --- |
| JDK | **17** | AGP 8.7 will not run on 21+ |
| Android SDK | Platform **35**, build-tools 35+ | |
| Gradle | 8.9 | Supplied by the wrapper — do not use a system Gradle |

The easiest route is **Android Studio Ladybug or newer**, which bundles a JDK and can install the
SDK for you. Open the folder, let it sync, and it will offer to fetch anything missing.

### Point the build at your SDK

`local.properties` is intentionally not committed, since it holds a machine-specific path. Create
it in the project root:

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

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

If `java -version` on your machine is not 17, point the build at a 17 JDK for the command only:

```bash
JAVA_HOME=/path/to/jdk-17 ./gradlew assembleDebug
```

### Run the tests

They cover the report arithmetic and the wording rules, and need no emulator or device:

```bash
./gradlew :app:testDebugUnitTest
```

### Install what you built

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.calmcontrol/.MainActivity
```

---

## Debug vs release — this matters

**Debug builds seed about 60 days of fabricated history** the first time the database is empty, so
the charts have something to show during development. Do not use a debug build on a phone you
actually want to track yourself with: the demo data mixes into your real entries and the only way
out is wiping app data.

For real use, build release:

```bash
./gradlew assembleRelease
```

No seeding, R8-minified, about 1.2 MB instead of 9.7 MB. Output at
`app/build/outputs/apk/release/app-release.apk`.

### About release signing

Release builds are signed with **Android's standard debug keystore** (`~/.android/debug.keystore`)
purely so the APK installs on a device without setup. That key's password is public and shared by
every Android installation — anyone could sign an update over this app. It is fine for testing on
your own phone and unfit for anything else.

Before publishing, generate your own key and replace the `signingConfigs` block in
[app/build.gradle.kts](app/build.gradle.kts):

```bash
keytool -genkey -v -keystore upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Keep that keystore and its passwords out of the repository — use `gradle.properties` in your
Gradle home, or environment variables.

If no debug keystore exists on the machine, `assembleRelease` still succeeds and produces an
**unsigned** APK. Unsigned APKs cannot be installed; sign it yourself with `apksigner`.

---

## Running on an emulator

```bash
# List available virtual devices
$ANDROID_HOME/emulator/emulator -list-avds

# Boot one
$ANDROID_HOME/emulator/emulator -avd <name>

# Then install as above
```

Any AVD on API 26+ works. The layout is built to survive a 320dp-wide screen, so a small device is
a useful thing to test against.

---

## Troubleshooting

**`Unable to locate a Java Runtime`** — no JDK on `PATH`. Install one, or prefix the command with
`JAVA_HOME=...` as shown above. Android Studio's bundled JDK lives at
`/Applications/Android Studio.app/Contents/jbr/Contents/Home` on macOS.

**`SDK location not found`** — create `local.properties` or set `ANDROID_HOME`.

**`Failed to install ... INSTALL_FAILED_UPDATE_INCOMPATIBLE`** — a build signed with a different
key is already installed. `adb uninstall com.calmcontrol` first.

**Charts look empty on a release build** — that is correct. Only debug builds seed demo data;
release starts from nothing and fills in as you log.

**Today's card is empty on a debug build after a few days** — the seed ends on the day it was
generated. `adb shell pm clear com.calmcontrol` regenerates it through today.

**Gradle sync complains about the AGP version** — you are likely on a JDK newer than 17. Check
with `./gradlew -version`.
