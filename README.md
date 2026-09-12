# Downed Player Revival

Revival is the Better Content pack's Forge 1.20.1 cooperative downed-player system. Eligible lethal damage leaves a player down but not out, giving nearby players a short, tactile rescue window before the ordinary death pipeline resumes.

Downed players are forced into Minecraft's prone swimming pose until they are revived or die.

## Player controls

- Hold the configured **Use Item** binding while aiming at a downed player to revive them. The aid intent follows the binding and automatically recovers from transient server-side range, sight, or facing rejection while the target remains valid.
- Press **Sneak** while downed to give up immediately. Servers may configure an unlock delay or longer hold if desired.

## Downed HUD

While downed, the local player sees a persistent high-contrast screen frame, a **YOU ARE DOWNED** banner, the bleed-out timer, and a bottom action panel. The presentation distinguishes waiting, active revival, the final ten-second critical period, give-up lock/ready/hold states, and disappears as soon as revival state clears. Every state has a text label and numeric or filled progress indicator; color is supplemental and the HUD does not flash.

The helper-facing targeted revival HUD is unchanged. The mod does not add teammate beacons or global rescue markers.

## Build and verification

```text
./gradlew verifyFast
./gradlew headlessGameTest
./gradlew assemble
```

The deployable artifact is the reobfuscated `build/libs/downed-player-revival-<version>.jar` staged by `stageRuntimeJar`.

## Community and support

For modpack and mod discussion, playtest feedback, and bug reports, join the [Better Content Discord](https://discord.gg/EkRnZbzqS9).

## Canonical identity

- Repository and release artifact: `downed-player-revival`
- Mod ID and resource namespace: `downed_player_revival`
- Java package: `com.bettercontent.downedplayerrevival`
- Validation: `./gradlew verifyFull`

This normalization is a clean break. Worlds, configuration files, and integrations created for earlier identities are not migrated or aliased.
