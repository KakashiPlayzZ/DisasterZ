package com.kakashiplayz.disasterz.manager;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterTriggerType;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.disaster.impl.BlizzardDisaster;
import com.kakashiplayz.disasterz.disaster.impl.EarthquakeDisaster;
import com.kakashiplayz.disasterz.disaster.impl.MeteorShowerDisaster;
import com.kakashiplayz.disasterz.disaster.impl.TornadoDisaster;
import com.kakashiplayz.disasterz.disaster.impl.TsunamiDisaster;
import com.kakashiplayz.disasterz.disaster.impl.VolcanoDisaster;
import com.kakashiplayz.disasterz.disaster.impl.ZombieApocalypseDisaster;
import com.kakashiplayz.disasterz.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the registration, active lifecycle ticks, and stopping of all disasters.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterManager {

    private final DisasterZPlugin plugin;
    private final Map<DisasterType, Disaster> registeredDisasters = new EnumMap<>(DisasterType.class);
    private Disaster activeDisaster = null;
    private BukkitTask tickTask = null;

    public DisasterManager(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        registeredDisasters.clear();

        register(new TsunamiDisaster(plugin));
        register(new EarthquakeDisaster(plugin));
        register(new TornadoDisaster(plugin));
        register(new BlizzardDisaster(plugin));
        register(new VolcanoDisaster(plugin));
        register(new MeteorShowerDisaster(plugin));
        register(new ZombieApocalypseDisaster(plugin));

        startTickLoop();
    }

    private void register(@NotNull Disaster disaster) {
        registeredDisasters.put(disaster.getType(), disaster);
    }

    private void startTickLoop() {
        stopTickLoop();
        this.tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 1L, 1L);
    }

    private void stopTickLoop() {
        if (tickTask != null && !tickTask.isCancelled()) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    private void onTick() {
        if (activeDisaster != null && activeDisaster.isActive()) {
            activeDisaster.tickInternal();
            if (!activeDisaster.isActive()) {
                activeDisaster = null;
            }
        }
    }

    /**
     * Starts a disaster by type.
     *
     * @param type            Type of disaster to start.
     * @param durationSeconds Duration in seconds (-1 for default from config).
     * @param skipWarning     If true, skips countdown warning and launches immediately.
     */
    public boolean startDisaster(@NotNull DisasterType type, int durationSeconds, boolean skipWarning) {
        Disaster disaster = registeredDisasters.get(type);
        if (disaster == null) return false;

        if (activeDisaster != null) {
            return false;
        }

        if (durationSeconds <= 0) {
            durationSeconds = plugin.getConfigManager().getDisasterDuration(type);
        }

        World world = selectTargetWorld();
        if (world == null) return false;

        if (skipWarning) {
            launchDisasterImmediately(disaster, durationSeconds, world, DisasterTriggerType.MANUAL);
            return true;
        } else {
            return plugin.getWarningManager().triggerWarning(disaster, durationSeconds, world, DisasterTriggerType.MANUAL);
        }
    }

    public void launchDisasterImmediately(@NotNull Disaster disaster, int duration, @NotNull World world, @NotNull DisasterTriggerType trigger) {
        this.activeDisaster = disaster;
        disaster.start(duration, world, trigger);

        Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("disaster", disaster.getType().getDisplayName());
        placeholders.put("time", TextUtil.formatTime(duration));

        plugin.getMessagesManager().broadcast("disaster-start-broadcast", "%prefix% <red><bold>%disaster%</bold></red> has struck the world! Duration: <yellow>%time%</yellow>", placeholders);
        TextUtil.broadcastTitle(
                "<red><bold>" + disaster.getType().getDisplayName().toUpperCase() + "</bold></red>",
                "<yellow>Survive for " + TextUtil.formatTime(duration) + "!</yellow>",
                10, 60, 20
        );
    }

    /**
     * Stops the currently active disaster or warning.
     */
    public boolean stopActiveDisaster(boolean forced) {
        if (plugin.getWarningManager().isWarningActive()) {
            plugin.getWarningManager().cancelWarning();
            TextUtil.broadcast("<red>[DisasterZ] Upcoming disaster warning was cancelled.</red>");
            return true;
        }

        if (activeDisaster != null) {
            Disaster d = activeDisaster;
            activeDisaster = null;
            d.stop(forced);

            Map<String, String> placeholders = new java.util.HashMap<>();
            placeholders.put("disaster", d.getType().getDisplayName());
            plugin.getMessagesManager().broadcast("disaster-end-broadcast", "%prefix% <green>%disaster% has subsided. The world is safe once more.</green>", placeholders);
            return true;
        }
        return false;
    }

    @Nullable
    private World selectTargetWorld() {
        for (World world : Bukkit.getWorlds()) {
            if (!plugin.getConfigManager().isWorldDisabled(world.getName()) && !world.getPlayers().isEmpty()) {
                return world;
            }
        }
        for (World world : Bukkit.getWorlds()) {
            if (!plugin.getConfigManager().isWorldDisabled(world.getName())) {
                return world;
            }
        }
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
    }

    @NotNull
    public Optional<Disaster> getActiveDisaster() {
        return Optional.ofNullable(activeDisaster);
    }

    @Nullable
    public Disaster getDisaster(@NotNull DisasterType type) {
        return registeredDisasters.get(type);
    }

    @NotNull
    public Map<DisasterType, Disaster> getRegisteredDisasters() {
        return Collections.unmodifiableMap(registeredDisasters);
    }

    public void cleanup() {
        stopTickLoop();
        if (activeDisaster != null) {
            activeDisaster.stop(true);
            activeDisaster = null;
        }
        registeredDisasters.clear();
    }
}
