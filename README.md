# 🌪️ DisasterZ

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21%20--%201.21.11%20%7C%2026.1%20--%2026.3-brightgreen.svg)](#)
[![Java Version](https://img.shields.io/badge/Java-21+-orange.svg)](#)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Bukkit%20%7C%20Spigot-blue.svg)](#)
[![Developer](https://img.shields.io/badge/Developer-KakashiPlayz-red.svg)](https://kakashiplayz.vercel.app)
[![Discord](https://img.shields.io/badge/Discord-Support%20Server-7289da.svg)](https://discord.gg/QdTN4BFkTv)

**DisasterZ** is an enterprise-grade, high-performance natural disaster engine engineered for modern Paper, Bukkit, and Spigot servers. Built with modern Java 21+ and a strict Zero-TPS-Drop architectural standard, DisasterZ unleashes 7 cinematic natural disasters while safeguarding server performance, chunk integrity, and player territories.

---

## 👨‍💻 Developer & Support Information
- **Plugin Name:** DisasterZ
- **Version:** 1.0.0
- **Developer:** **KakashiPlayz**
- **Portfolio Website:** [kakashiplayz.vercel.app](https://kakashiplayz.vercel.app)
- **Discord Community & Support:** [discord.gg/QdTN4BFkTv](https://discord.gg/QdTN4BFkTv)

---

## ⚡ Core Architectural Highlights

### 1. Zero-TPS-Drop Engine
- **Async Math Engine:** Offloads heavy mathematical algorithms, double-helix spiral coordinates, wave front particle matrices, and ballistic trajectories to asynchronous threads via `CompletableFuture`. Only mutations sync back to the main thread.
- **Flight & Movement Safety Guard:** Intercepts and negates false-positive "Flying is not enabled on this server" kicks during intense Tornado vortexes and Earthquake tremors, preserving smooth player controls.
- **Chunk Safety Guard:** Never triggers forced chunk loading. Evaluates chunk boundaries prior to block access or particle rendering.
- **Memory-Safe Block Restoration Engine:** Stores manipulated block states in a thread-safe circular rollback buffer (`ConcurrentLinkedQueue<BlockSnapshot>`) and gently restores blocks over time (25 blocks/tick by default) to eliminate lag spikes.
- **Packet-Based Ghost Blocks:** Optional visual mode (`use-ghost-blocks-for-liquids`) delivering full immersion for liquid events (Tsunami) via `Player#sendBlockChange` without writing single world changes.

### 2. Territory & Claim Protection
- Soft-depends and safely hooks into:
  - **WorldGuard 7.x+**
  - **GriefPrevention**
  - **Towny Advanced**
  - **Lands**
- With `settings.protection.respect-claims: true`, all disaster damage, block breaking, and explosions are halted inside protected player claims.

### 3. Modern Visual & Audio Polish
- **Kyori Adventure & MiniMessage:** Rich gradients, hover effects, and automatic fallback conversion for legacy `&` and `&#RRGGBB` hex color codes.
- **Cinematic Audio Engine:** Multi-layered soundscapes combining subterranean rumbles, pitch shifts, distance falloff, and escalating chime countdowns.
- **Client-Safe Camera Effects:** Visceral screen tremors and disorientation using non-intrusive client distortion shaders and micro ground slips without freezing player WASD movement.

---

## 🌋 The 7 Natural Disasters

| Icon | Disaster | Description |
|:---:|---|---|
| 🌊 | **Tsunami** | Colossal moving wall of water with synchronized particle crests, violent forward surge physics, and drowning hazards. |
| 🪨 | **Earthquake** | Violent tectonic fissures opening along fault lines, continuous screen-shaking tremors, stumbling mechanics without locking movement, and falling rubble. |
| 🌪️ | **Tornado** | Rotating double-helix parametric spiral vortex with vacuum physics that sucks players and mobs inside, spirals them skyward, and violently hurls them across the land. |
| ❄️ | **Blizzard** | Freezing squall that inflicts persistent Slowness and Weakness throughout the event, rapid frostbite screen vignette, hypothermia damage, and whiteout fog unless standing within radiant heat zones (campfires, torches, lava, furnaces). |
| 🌋 | **Volcanic Eruption** | Dense atmospheric ash storms, launching explosive fireballs and molten magma/basalt bombs that shatter the surface into persistent lava pools and actively spreading wildfire fronts. |
| ☄️ | **Meteor Shower** | High-velocity extraterrestrial impactors that blast apart blocks, forge molten impact craters, fling destructive rubble, and spread raging surface fires. |
| 🧟 | **Zombie Apocalypse** | Blood Moon sky tint, towering Big Size Colossal Zombies (scaled via `Attribute.GENERIC_SCALE`), giant dreadnought bosses, and relentless undead hordes. |

---

## 🎮 Interactive Master GUI & Dual Sub-GUIs (`/disasterz gui`)

- **Master Control GUI (54 slots):**
  - **Symmetric Centered Layout:** Balanced placement for all 7 natural disasters with custom icons.
  - **Animated Enchantment Glints:** Active disasters pulse with an enchanted sheen.
  - **Dynamic Live Lore:** Refreshes every 20 ticks (1s) displaying live duration countdowns, status badges (`[Active]`, `[Warning]`, `[Idle]`), and random-cycle eligibility.
  - **Click Controls:**
    - **Left-Click:** Force start / initiate warning countdown.
    - **Right-Click:** Instantly cancel or halt disaster.
    - **Middle-Click:** Cycle duration presets (60s ➔ 120s ➔ 180s ➔ 300s ➔ 600s).
  - **Global Action Bar:**
    - *Stop All Disasters*
    - *Trigger Random Disaster*
    - *Random-Cycle Filter Sub-GUI* (Replaced author info)
    - *Auto-Scheduler Settings Sub-GUI*
    - *Reload Configurations*

- **1. Random-Cycle Filter Sub-GUI (`DisasterToggleGUI`):**
  - Dedicated menu to toggle which disasters are eligible for automatic random selection.
  - Disasters toggled **OFF** will **NEVER** start randomly or via auto-scheduler.
  - Disabled disasters can still be triggered manually by Admins or specifically targeted in scheduled cycles.
  - Quick action buttons: "Allow All in Random Cycle" and "Block All from Random Cycle".

- **2. Auto-Scheduler Settings Sub-GUI (`SchedulerSettingsGUI`):**
  - **Toggle Scheduler State:** Enable/disable background automatic cycles.
  - **Adjust Cycle Interval:** +15m, -15m, +1h, -1h controls.
  - **Adjust Pre-Disaster Warning:** +5s, -5s, +15s, -15s countdown settings.
  - **Adjust Minimum Players:** Set player threshold required for automatic triggers.
  - **Next Disaster Target:** Cycle specific upcoming disaster or set to Weighted Random.
  - **Force Launch Now:** Bypasses countdown and fires scheduled cycle immediately.
  - **Seamless Navigation:** One-click return to Master Control.

---

## 💻 Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/disasterz gui` | `disasterz.admin.gui` | Opens the Master Admin GUI |
| `/disasterz start <disaster> [duration]` | `disasterz.admin.start` | Triggers a disaster warning and event |
| `/disasterz stop [disaster]` | `disasterz.admin.stop` | Halts active disaster and restores blocks |
| `/disasterz reload` | `disasterz.admin.reload` | Hot-reloads configuration files and tasks |
| `/disasterz info` | `disasterz.use` | Displays author, version, portfolio, and discord |

---

## 🔌 PlaceholderAPI Support

- `%disasterz_active%` — Name of active disaster (or "None")
- `%disasterz_time_left%` — Formatted duration remaining (`mm:ss`)
- `%disasterz_time_left_seconds%` — Raw seconds remaining
- `%disasterz_next_disaster%` — Scheduled upcoming disaster
- `%disasterz_next_countdown%` — Formatted timer until next scheduled disaster cycle
- `%disasterz_warning_countdown%` — Current pre-event warning timer

---

## 🛠️ Developer API & Custom Events

Access the static API entry point via `DisasterZAPI`:
```java
// Check if a disaster is active
if (DisasterZAPI.isAnyDisasterActive()) {
    Disaster active = DisasterZAPI.getActiveDisaster().get();
    Bukkit.broadcastMessage("Active disaster: " + active.getType().getDisplayName());
}

// Start a disaster
DisasterZAPI.startDisaster(DisasterType.TORNADO, 180);
```

### Custom Cancellable Events:
- `DisasterPreStartEvent` (Cancellable)
- `DisasterStartEvent`
- `DisasterTickEvent`
- `DisasterEndEvent`
- `DisasterBlockDamageEvent` (Cancellable)

---

## 📦 Building from Source

```bash
gradle build
```
Output jar: `build/libs/DisasterZ-1.0.0.jar`.
