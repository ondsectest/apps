# Calm Control

An offline Android app for noticing emotional triggers and seeing self-control grow.

Two screens, reached from a bottom bar:

- **Log** — two large circular buttons, "Stayed calm" and "Got angry", then a one-tap trigger
  picker. This is the start destination.
- **Progress** — the charts: today's ring, the week, the 30/90-day trend, trigger analysis and a
  monthly reflection.

## Getting it running

Full setup, troubleshooting and signing notes are in **[INSTALL.md](INSTALL.md)**. The short
version — needs **JDK 17** and Android SDK platform 35:

```bash
./gradlew assembleDebug
```

```bash
./gradlew :app:testDebugUnitTest
```

Debug builds seed about 60 days of sample history so the charts have something to show. **Use a
release build on a phone you actually want to track yourself with** — the seeder is debug-only.

## How it fits together

```
Room  →  ReportsRepository  →  ReportsCalculator  →  ReportsViewModel  →  ReportsScreen
```

One database Flow feeds every chart. The repository observes a single rolling 90-day window and
all six visualisations are derived from that one list by pure functions in `ReportsCalculator`.
That is why charts refresh the moment an event is logged, with no refresh plumbing: Room emits,
everything downstream recomputes.

The 90-day window is slightly wider than the longest chart so the trend line's 7-day moving
average has real history at its left edge instead of ramping up from zero.

## What Progress shows

| Section | Source |
| --- | --- |
| Today's ring | `dailySummary` |
| Mon–Sun bars, tap for numbers | `weekBuckets` |
| 30/90-day trend | `trend` |
| Trigger analysis + strongest area | `triggerBreakdown`, `strongestArea` |
| Monthly reflection | `monthlyReflection` |

## Design rules worth keeping

These are the constraints the screen is built around. Changing them changes what the app is for.

- **The centre of the ring is the self-control rate, never the anger rate.** Which number sits
  there is the most consequential decision on the screen.
- **Green is the protagonist.** It is the most saturated colour in the palette, and it is the only
  line on the trend chart with a gradient fill.
- **Red is terracotta, not alarm red.** Anger events are information, not indictments. See
  `CalmColors` in `ui/theme/Theme.kt`; no chart hardcodes a colour.
- **No generated sentence describes a decline.** A worse month is reported as the work done and the
  awareness behind it. `ReportsCalculatorTest` asserts this against a word list, so a future edit
  that reintroduces "decreased" or "dropped" fails the build.
- **Neither log button is styled as the wrong answer.** Same size, same finish, same prominence;
  only the hue differs. The moment the Log screen makes honesty feel like confession, people stop
  logging the red ones and every chart downstream starts lying.
- **The trigger step is skippable.** A logged moment with a vague cause beats no logged moment,
  and this screen gets used seconds after something went wrong, by someone who is not calm.
- **The strongest-area callout's sample threshold scales with the month** (at least 5 events, and
  at least 5% of everything logged). A fixed floor stops meaning anything once the month is busy —
  three-for-three is real in a quiet week and a rounding error in a month of 140 moments.
- **The trend line never extends back past the history that exists.** Someone two months in who
  opens the 90-day view would otherwise meet a month of flat zero before their data begins, which
  is accurate and reads exactly like a failure report.

## Deliberate choices

- **No chart library.** Charts are Compose `Canvas`. Zero dependencies, works offline by
  construction, and the calm palette and animation timing stay under our control.
- **No dynamic colour.** Wallpaper-derived theming would hand the app whatever accent the home
  screen has, and green-means-controlled is the whole point.
- **No DI framework.** The graph is one database and one repository.
- **No navigation library.** Two screens, no arguments, no back stack worth modelling — a single
  saveable enum in `CalmControlRoot`.
- **Charts morph rather than replay.** Bars, ring and scale animate to new values, so an event
  logged mid-session nudges the chart instead of restarting it. Only the trend line's draw-on
  reveal replays, and only when the 30/90 toggle changes.

## Not built yet

- Editing or deleting a logged moment. Everything is append-only.
- `TriggerEvent.intensity` and `note` are stored but never captured by the Log screen, and never
  charted.
- `DemoDataSeeder` can go once there is real history worth keeping. It only fires on debug builds
  when the table is empty.

## Typography

**Lora** throughout — one warm humanist serif, no second family. Mixing a display serif with a UI
sans is the safer pairing, but it makes the app sound like two different things depending on
where you look: a journal at the top of the screen, a dashboard at the bottom. One voice keeps it
the journal.

Bundled in `res/font` rather than fetched — downloadable fonts would need Play Services and a
network call, and this app is offline by construction. It is OFL; the licence ships in
`assets/licenses`. One variable file, ~207 KB, covering all four weights.

The thing a naive family swap gets wrong: Lora's x-height is much smaller than a screen sans, so
the same point size reads visibly smaller and body copy starts to feel frail. Every size below
`titleLarge` is therefore about 1sp above the Material baseline, and the small sizes carry extra
tracking — serifs need more room between letters than a sans before they close up.

Line height and tracking move in opposite directions as size grows: line height loosens towards
1.6x on small body copy and tightens towards 1.25x on headlines, while tracking runs negative on
the large sizes and positive on the small ones.

Numbers that are centred, right-aligned in a column, or that change in place use
`TextStyle.tabularFigures()`. Lora implements `tnum`, so it takes effect rather than being
silently ignored.

Variable fonts are why `minSdk 26` matters beyond `java.time` — font variation axes need API 26.

## Quotes

After every logged moment — calm or angry — a dialog shows the next quote in rotation. Two rules
govern `Quotes.all`, both enforced by `QuotesTest`:

**Nothing may shame the reader.** Half of these appear immediately after someone has admitted
they lost their temper. Lines calling anger madness, poison or shameful are the wrong thing to
hand them at that instant, however famous. Benjamin Franklin's "whatever is begun in anger ends
in shame" was cut for exactly this reason. A test fails the build if a shaming word reappears.

**Everything is traceable.** Anger quotations are among the most misattributed text online. The
famous "holding onto anger is like drinking poison" line is not from the Pali canon, and the
Emerson one about sixty seconds of peace appears nowhere in Emerson — both are deliberately
absent. `Quote.source` carries a checkable passage where one exists.

These entries have **no precise citation** and should be verified before shipping to real users:
Nelson Mandela, Maya Angelou, the 14th Dalai Lama, the Chinese proverb, and the Gandhi line
(which reaches us secondhand through Arun Gandhi).

The list mixes secular and religious sources — Stoics and novelists alongside the Dhammapada,
Proverbs and a hadith. That was a call about "credible voices around the globe"; if the app
should read as wholly secular, the three scriptural entries are the ones to drop.

Rotation is sequential, not random: random selection repeats within a few logs often enough to
read as the app not paying attention. The position is stored in `SharedPreferences` so it
survives restarts, starting at a random offset per install.

## Orb animation

The orbs run off a single monotonic clock (`rememberElapsedSeconds`) rather than looping
animations. This is not a style preference — it is a bug fix.

The first version used `rememberInfiniteTransition` with a 0→360 spin, and positioned each
sparkle at `baseAngle + spin * drift` with a fractional `drift`. Each loop was individually
smooth, but when `spin` wrapped from 360 back to 0 every sparkle jumped at once — reading as the
whole orb stopping and restarting every eleven seconds. `sin` and `cos` are periodic, so a clock
that only ever counts up has no seam to hit.

Drawing uses `drawWithCache`, so the five gradients and the star `Path` are built once per size
instead of roughly thirty allocations per frame per orb. The breath is applied in a
`graphicsLayer` block, deferring the state read to the layer phase so a 60 Hz animation does not
recompose the subtree 60 times a second.

## Licence

MIT, at the [repository root](../LICENSE). Fork it, copy it, modify it, ship it commercially.

The bundled Lora typeface is separately licensed under the SIL Open Font License 1.1, with its
notice at `app/src/main/assets/licenses/OFL-Lora.txt`. MIT does not relicense it, so keep that
file in place when redistributing.
