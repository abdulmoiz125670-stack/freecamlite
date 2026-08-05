# Freecam Lite

A minimal, no-GUI detached flying camera for Fabric 1.21.11, in the spirit of the
"Freecam" mod.

## Controls

- **R** — toggle freecam on/off (rebindable in vanilla **Options > Controls >
  Freecam Lite** — this mod has no custom settings screen, it just uses
  Minecraft's own key-bind menu)
- **W A S D** — fly horizontally
- **Space** — fly up
- **Shift** — fly down
- **Sprint key (default Left Ctrl)** — hold to fly 3x faster
- Your player body stays exactly where it was and doesn't rotate while
  freecam is active; only the detached camera moves and looks around.
  Toggle off and you're back in your body, unmoved.

Speed and sprint multiplier are constants at the top of
`FreecamManager.java` (`BASE_SPEED`, `SPRINT_MULTIPLIER`) — edit and rebuild
if you want them different. No config screen by design.

## How it works (short version)

- `MinecraftClient#cameraEntity` gets swapped from the player to an invisible,
  non-colliding `MarkerEntity` that we drive by hand every client tick.
- A mixin on `Mouse#updateMouse` redirects the look-direction call to the
  freecam entity instead of the player while active.
- A mixin on `ClientPlayerEntity#tickMovement` cancels it entirely while
  active, so the real player never moves and never sends changed position
  packets — no server-side rubber-banding.

## Building

Requires JDK 21.

```
./gradlew build
```

(No wrapper jar is bundled here since it has to be downloaded — either open
this folder directly in **IntelliJ IDEA** with the Minecraft/Fabric plugins,
which will bootstrap Gradle for you, or run `gradle wrapper --gradle-version
8.14` once if you have Gradle installed locally, then use `./gradlew build`.)

The built jar shows up in `build/libs/freecamlite-1.0.0.jar`. Drop it in your
`.minecraft/mods` folder alongside Fabric Loader and **Fabric API** for
1.21.11 (Fabric API is a required dependency, listed in `gradle.properties`
as `fabric_version`).

## A heads-up on verification

I pulled the current Yarn mappings / Loader / Fabric API / Loom versions for
1.21.11 live and wrote this against long-stable Minecraft/Fabric APIs
(`cameraEntity`, `changeLookDirection`, `tickMovement`, `MarkerEntity`,
`refreshPositionAndAngles`) specifically to minimize breakage. But I can't
actually compile it in this sandbox (no internet access here to pull
Minecraft/Yarn/Fabric API jars), so I haven't test-built it myself. If
`./gradlew build` throws a "method/field not found"-type error, it's almost
always a one-line mapping name mismatch — paste me the error and I'll fix it,
or use Loom's `genSources`/your IDE to look up the current name yourself.
