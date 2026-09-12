# Death’s Door

Death’s Door is Better Content’s server-authoritative bodily injury system for Forge 1.20.1.
The runtime identity remains `downed_player_revival`.

A hit that exhausts positive HP enters Death’s Door and causes one maim. Positive HP protects
against that hit’s death roll, even with many existing injuries. At semantic zero, every further
accepted HP-damaging hit either kills or causes exactly one new maim. Players keep ordinary
movement, combat, inventory, and item controls. There is no bleed-out timer or multiplayer-only
revival rule.

Every zero crossing prevents all HP healing for 40 server ticks. Subsequent hits do not restart
the lock. Healing afterward restores the ordinary HP buffer; injuries remain until treated.
Totems do not prevent player death. Explicit special-kill sources, command kill, and void bypass
Death’s Door. Vanilla blocking, armor, absorption, and hurt immunity still apply; this mod adds no
maim cooldown. Absorption-only hits add trauma but do not maim.

## Injuries and trauma

Exactly six regions are modeled: head, torso, left/right arm, and left/right leg. Region determines
impairment; injury type determines cure. Damage direction biases region selection, with falls
strongly favoring the legs. Configured source overrides and damage/weapon tags classify injuries;
unrecognized sources deliberately produce Cracked injuries.

Each active maim contributes 5 percentage points of death probability. Each head maim adds another
10 percentage points. The server rolls against the active injuries that existed before the hit.
The total caps at 100%; zero maims means zero maim-derived death probability.

Every accepted hit adds one trauma stack for 1,200 server ticks, including damage absorbed by golden
hearts. Stacks expire independently. Trauma scales functional injury effects from 50% at rest to
100% with five stacks. Trauma does not change death probability.

Default full-strength penalties:

- Torso: maximum HP reduction `0.5 × n/(n+3)`.
- Each leg: movement reduction `0.5 × n/(n+3)`; both legs add.
- Arms: combined arm count × 10%, capped at 100%, for melee damage and attack/use reach.
- Treatment: `2 + (8/3 × arm count × functional multiplier)` seconds, using the healer’s arms.

Treatment remains possible at every finite injury count. Maximum-health changes never grant HP.
All coefficients are centralized in the common configuration and server snapshots carry the
configured values used for presentation.

## Treatment

Open your body through the inventory body icon. Right-click another player within reach and line
of sight to inspect and treat them. Select a region and a cure; the oldest matching active injury
is treated first. Damage, closing the body screen, losing reach or sight, and disconnecting interrupt
treatment. Medicine is consumed only on completion, and each cure remains in treatment history.

| Injury | Cure | Recipe |
|---|---|---|
| Cracked | Stick | Vanilla item |
| Opened | Soocher | One flint + one string |
| Burnt | Balm | One small flower + one water bottle |

Balm returns its glass bottle on successful application. Burnt includes heat, freezing, and tagged
corrosive damage. Opened denotes a major wound. Type never changes the region’s functional penalty.

Body state and treatment history survive reconnects and dimension changes within the current life.
They clear on confirmed final death. A separate recap retains the final active/treated regional
counts while the player is dead, then clears on respawn. There is no lineage integration.

## Presentation and console review

Skull marks on the heart bar represent the server’s maim-derived death probability. They remain
subdued above zero to show carried risk; urgent Pressure sound/effects run at Death’s Door. Full
coverage means 100%. Ordinary hearts remain readable. Treatment and final-death screens expose
active injuries and treatment history separately.

Operator-level commands target explicit player names and work from the server console. Use isolated
review worlds for state-changing scenarios:

```text
downedplayerrevival debug gui PLAYER inventory
downedplayerrevival debug gui VIEWER body SUBJECT LEFT_ARM active 0
downedplayerrevival debug gui VIEWER body SUBJECT LEFT_LEG history 2
downedplayerrevival debug gui PLAYER body-view regions 0
downedplayerrevival debug gui PLAYER body-view help 0
downedplayerrevival debug gui PLAYER body-view detail 120
downedplayerrevival debug gui PLAYER death-recap
downedplayerrevival debug gui PLAYER close
downedplayerrevival debug scenario PLAYER mixed
downedplayerrevival debug maim PLAYER LEFT_ARM BURNT
downedplayerrevival debug treatment start HEALER SUBJECT LEFT_ARM OPENED
downedplayerrevival debug treatment cancel HEALER
downedplayerrevival debug capture PLAYER review-label 12
downedplayerrevival debug pressure PLAYER 11 true 60
downedplayerrevival debug presentation PLAYER true false
```

Fixture names include `healthy`, `mixed`, `severe`, `missing_medicine`, `long_history`,
`healing_lock`, `trauma_expiry`, and `final_death`. The final-death fixture runs real death.
Commands invoke production screens and handlers; they do not simulate player input.

`pressure PLAYER MAIMS AT_DOOR MAX_HP` creates real server-owned leg injuries and health,
then reports the actual configured death probability. `presentation PLAYER REDUCED_MOTION SOUND`
controls the viewing client's presentation settings. History pages are zero-based and retain the
actual item applied even when treatment tags change. Detailed history uses six-record pages with
no total-history limit. The death-recap command requires an actual completed death.

`./gradlew runInjuryVisual` launches the isolated real Minecraft review client under
`build/injury-visual/`, creates a disposable flat world, invokes console commands, and captures its
actual framebuffer. The review source set is excluded from runtime JARs. Its standard matrix covers
1280×720, 1280×960, and 1920×1080 at requested GUI scales 2, 3, and 4; Minecraft clamps scales that
would violate its minimum GUI dimensions. It includes treatment states, history pagination,
probability/health/absorption combinations, HUD fading, reduced motion, and native final death.

For two actual connected clients, start the primary and wait for `INJURY_REVIEW_WAITING_HELPER`:

```sh
INJURY_REVIEW_MULTIPLAYER=1 ./gradlew runInjuryVisual
```

Then run in a second terminal:

```sh
./gradlew runInjuryHelper
```

The primary exposes its isolated development world on port 56694 without account authentication;
`InjuryHelper` joins through Minecraft's production connection flow. Console operations open the
helper's production treatment screen, apply treatment, and capture progress, success, and damage
interruption on both clients. The harness shuts down both clients on completion. Keep the review
port confined to the local development environment.

Screenshots are saved in `build/injury-visual/screenshots/` and
`build/injury-helper/screenshots/`. For a persistent console-driven review, set
`INJURY_REVIEW_MANUAL=1` on the primary; write one production server command per line to
`build/injury-visual/review.commands`. The harness consumes that file on the server thread.
A harness-only line `viewport 1920 1080 3` changes the actual window and GUI scale.
`body-view` selects the region picker, help, or detail view; its final argument is a pixel scroll
offset through the actual screen content. The visible Region, Help and tab buttons use the same
navigation. Normal entry opens the six-region overview. Only active injury types appear in the treatment list; small screens scroll complete
treatment cards and history records rather than compressing them.
A line containing `stop` ends the primary. No mouse, key, or player-control events are synthesized.

On lanes without a display, an isolated user-namespace Xvfb avoids system temporary files:

```sh
unshare -Ur Xvfb :94 -screen 0 1920x1080x24 -nolisten unix -nolock -listen tcp -ac -noreset
```

Run clients with `DISPLAY=localhost:94 TMPDIR=/home/dev/.tmp`. Stop that exact Xvfb process after
review. Native toasts pause during Death’s Door and while bodily screens occupy their space, then resume
with their presentation lifetime preserved. Trauma duration text comes from server tuning.

Every acceptance screenshot must actually be opened and reviewed; image generation or layout tests
alone do not establish GUI quality. Render inspection does not prove pointer usability or natural
combat pacing. The lane's OpenAL device was unavailable during automated rendering, so these
screenshots are not evidence of audible sound quality or audio balance.

## Integration and verification

`InjuryApi` exposes read-only server injury counts, semantic HP, Death’s Door state, and snapshots.
`InjuryEvent` publishes committed entry, healing exit, maim, treatment, and final-death changes.
Client packets contain bounded aggregates and paged history, not authoritative mutation decisions.

RPG Stats and Configurable Death retain final-death ownership. Surviving an episode does not run
their final-death consequences. Depth Director scales reinforcement cadence by active injuries;
Pillager Campaigns keeps fighting through Death’s Door; Player Traces records until actual death.
Legacy Revival API/event consumers require updates before deploying this provider. Teaching-surface
changes are intentionally separate from this implementation.

```sh
./gradlew verifyFull stageRuntimeJar
```

The deployable JAR is the reobfuscated `build/libs/downed-player-revival-<version>.jar`.
Local verification and staging do not deploy into the modpack or authorize pack suites/distributions.
