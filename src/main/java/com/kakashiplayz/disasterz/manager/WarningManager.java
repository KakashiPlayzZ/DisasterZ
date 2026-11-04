package com.kakashiplayz.disasterz.manager;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.api.event.DisasterPreStartEvent;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterState;
import com.kakashiplayz.disasterz.disaster.DisasterTriggerType;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Orchestrates pre-disaster countdown warnings with escalating audio tension,
 * cinematic titles, action bars, and cancellable developer events.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class WarningManager {

    private final DisasterZPlugin plugin;
    private BukkitTask countdownTask;
    private Disaster pendingDisaster;
    private int totalWarningSeconds;
    private int remainingWarningSeconds;
    private int disasterDuration;
    private World targetWorld;
    private DisasterTriggerType triggerType;

    public WarningManager(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initiates a disaster warning countdown.
     */
    public boolean triggerWarning(@NotNull Disaster disaster, int durationSeconds, @NotNull World world, @NotNull DisasterTriggerType trigger) {
        if (isWarningActive() || plugin.getDisasterManager().getActiveDisaster().isPresent()) {
            return false;
        }

        int warningTime = plugin.getConfigManager().getWarningDelaySeconds();
        DisasterPreStartEvent event = new DisasterPreStartEvent(disaster, warningTime);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        this.pendingDisaster = disaster;
        this.totalWarningSeconds = event.getWarningSeconds();
        this.remainingWarningSeconds = this.totalWarningSeconds;
        this.disasterDuration = durationSeconds;
        this.targetWorld = world;
        this.triggerType = trigger;

        this.pendingDisaster.setState(DisasterState.WARNING);
        this.pendingDisaster.onPreWarn();

        // Broadcast initial pre-disaster title & chat announcement
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("disaster", disaster.getType().getDisplayName());
        placeholders.put("seconds", String.valueOf(remainingWarningSeconds));

        TextUtil.broadcastTitle(
                "<red><bold>WARNING</bold></red>",
                "<yellow>" + disaster.getType().getDisplayName() + " incoming!</yellow>",
                10, 40, 10
        );
        plugin.getMessagesManager().broadcast("warning-broadcast", "%prefix% <red><bold>Warning!</bold></red> A severe <yellow>%disaster%</yellow> is approaching in <red>%seconds%</red> seconds!", placeholders);

        // Start 1-second countdown task
        this.countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickCountdown, 20L, 20L);
        return true;
    }

    private void tickCountdown() {
        if (pendingDisaster == null) {
            cancelWarning();
            return;
        }

        remainingWarningSeconds--;

        // Escalating audio chimes
        plugin.getAudioEngine().playWarningChime(remainingWarningSeconds, totalWarningSeconds);

        // Action bar countdown
        String actionMsg = plugin.getMessagesManager().getRawMessage(
                "warning-actionbar",
                "<yellow>Disaster approaching in <red>%seconds%s</red>!"
        ).replace("%seconds%", String.valueOf(remainingWarningSeconds));
        TextUtil.broadcastActionBar(actionMsg);

        // Milestone title flashes (10s, 5s, 3s, 2s, 1s)
        if (remainingWarningSeconds == 10 || (remainingWarningSeconds <= 5 && remainingWarningSeconds > 0)) {
            TextUtil.broadcastTitle(
                    "<red><bold>" + remainingWarningSeconds + "</bold></red>",
                    "<yellow>Prepare for " + pendingDisaster.getType().getDisplayName() + "!</yellow>",
                    2, 16, 2
            );
        }

        // Countdown expired: Launch disaster
        if (remainingWarningSeconds <= 0) {
            cancelCountdownTaskOnly();
            Disaster d = pendingDisaster;
            int dur = disasterDuration;
            World w = targetWorld;
            DisasterTriggerType t = triggerType;

            pendingDisaster = null;
            plugin.getDisasterManager().launchDisasterImmediately(d, dur, w, t);
        }
    }

    public void cancelWarning() {
        cancelCountdownTaskOnly();
        if (pendingDisaster != null) {
            pendingDisaster.setState(DisasterState.IDLE);
            pendingDisaster = null;
        }
    }

    private void cancelCountdownTaskOnly() {
        if (countdownTask != null && !countdownTask.isCancelled()) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    public boolean isWarningActive() {
        return countdownTask != null && !countdownTask.isCancelled() && pendingDisaster != null;
    }

    @Nullable
    public Disaster getPendingDisaster() {
        return pendingDisaster;
    }

    public int getRemainingWarningSeconds() {
        return remainingWarningSeconds;
    }
}

