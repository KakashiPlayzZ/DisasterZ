package com.kakashiplayz.disasterz.manager;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterTriggerType;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Automates natural disaster cycles on a configurable interval timer with
 * weighted random selection, random-cycle toggle filtering, and player count safety checks.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterScheduler {

    private final DisasterZPlugin plugin;
    private BukkitTask timerTask;
    private int secondsUntilNext = 3600;
    private DisasterType nextScheduledDisaster = null;

    public DisasterScheduler(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        this.secondsUntilNext = plugin.getConfigManager().getAutoSchedulerInterval();
        this.nextScheduledDisaster = pickRandomWeightedDisaster();
        this.timerTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickScheduler, 20L, 20L);
    }

    public void stop() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private void tickScheduler() {
        if (!plugin.getConfigManager().isAutoSchedulerEnabled()) {
            return;
        }

        // Check if an active disaster or warning is running
        if (plugin.getDisasterManager().getActiveDisaster().isPresent() || plugin.getWarningManager().isWarningActive()) {
            return;
        }

        // Check minimum player requirements
        int onlineCount = Bukkit.getOnlinePlayers().size();
        if (onlineCount < plugin.getConfigManager().getMinimumPlayers()) {
            return;
        }

        secondsUntilNext--;

        if (secondsUntilNext <= 0) {
            triggerScheduledCycle();
            secondsUntilNext = plugin.getConfigManager().getAutoSchedulerInterval();
            nextScheduledDisaster = pickRandomWeightedDisaster();
        }
    }

    public void triggerScheduledCycle() {
        DisasterType type = (nextScheduledDisaster != null) ? nextScheduledDisaster : pickRandomWeightedDisaster();
        if (type == null) return;

        Disaster disaster = plugin.getDisasterManager().getDisaster(type);
        if (disaster == null) return;

        World world = selectEligibleWorld();
        if (world == null) return;

        int duration = plugin.getConfigManager().getDisasterDuration(type);
        plugin.getWarningManager().triggerWarning(disaster, duration, world, DisasterTriggerType.AUTOMATED);
    }

    @Nullable
    public DisasterType pickRandomWeightedDisaster() {
        List<DisasterType> pool = new ArrayList<>();
        for (DisasterType type : DisasterType.values()) {
            // Check both general enabled AND random-cycle enabled
            if (plugin.getConfigManager().isDisasterEnabled(type) && plugin.getConfigManager().isRandomCycleEnabled(type)) {
                int weight = plugin.getConfigManager().getDisasterWeight(type);
                for (int i = 0; i < Math.max(1, weight); i++) {
                    pool.add(type);
                }
            }
        }
        if (pool.isEmpty()) return null;
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    @Nullable
    private World selectEligibleWorld() {
        for (World w : Bukkit.getWorlds()) {
            if (!plugin.getConfigManager().isWorldDisabled(w.getName()) && !w.getPlayers().isEmpty()) {
                return w;
            }
        }
        for (World w : Bukkit.getWorlds()) {
            if (!plugin.getConfigManager().isWorldDisabled(w.getName())) {
                return w;
            }
        }
        return null;
    }

    public int getSecondsUntilNext() {
        return secondsUntilNext;
    }

    public void setSecondsUntilNext(int seconds) {
        this.secondsUntilNext = Math.max(0, seconds);
    }

    @Nullable
    public DisasterType getNextScheduledDisaster() {
        return nextScheduledDisaster;
    }

    public void setNextScheduledDisaster(@Nullable DisasterType next) {
        this.nextScheduledDisaster = next;
    }
}
