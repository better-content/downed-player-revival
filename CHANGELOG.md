# Changelog

## Unreleased

### Changed

- Made held Use Item revival binding-aware and self-refreshing so acquiring a target late or a transient server rejection no longer leaves aid stalled.
- Made the authored Sneak give-up action available immediately while retaining configurable unlock and hold durations.
- Made downed-player constraints consistently server-authoritative across item use, containers, block breaking, item and experience pickup, dropping, mounting, sprinting, jumping, and flight.
- Standardized the project as **Downed Player Revival** with mod ID `downed_player_revival`, artifact `downed-player-revival`, and package `com.bettercontent.downedplayerrevival`.
- Adopted Java 17 and Forge 1.20.1-47.4.13 as the build baseline without changing the project version.
- This is a clean break; legacy worlds, configurations, and integrations are not migrated.
