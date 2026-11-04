package com.kakashiplayz.disasterz.disaster;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.api.event.DisasterBlockDamageEvent;
import com.kakashiplayz.disasterz.api.event.DisasterEndEvent;
import com.kakashiplayz.disasterz.api.event.DisasterStartEvent;
import com.kakashiplayz.disasterz.api.event.DisasterTickEvent;
import com.kakashiplayz.disasterz.util.ChunkSafetyGuard;
import com.kakashiplayz.disasterz.util.TextUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Unified abstract base class for all DisasterZ natural disasters.
 * Enforces strict lifecycle contracts: onPreWarn(), onStart(), onTick(), onEnd(), onCleanup().
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public abstract class Disaster {

    protected final DisasterZPlugin plugin;
    protected final DisasterType type;

    protected DisasterState state = DisasterState.IDLE;
    protected DisasterTriggerType triggerType = DisasterTriggerType.MANUAL;
    protected World world;
    protected int totalDurationSeconds = 180;
    protected int remainingSeconds = 180;
    protected long currentTick = 0;

    protected BossBar bossBar;

    public Disaster(@NotNull DisasterZPlugin plugin, @NotNull DisasterType type) {
        this.plugin = plugin;
        this.type = type;
    }

    // --- Lifecycle Contracts ---

    /**
     * Called when the warning countdown begins.
     */
    public abstract void onPreWarn();

    /**
     * Called when the disaster officially begins active execution.
     */
    public abstract void onStart();

    /**
     * Called once every server tick while the disaster is active.
     */
    public abstract void onTick();

    /**
     * Called when the disaster reaches its duration limit or is stopped.
     */
    public abstract void onEnd();

    /**
     * Called to tear down all residual visual effects, temporary blocks, or tasks.
     */
    public abstract void onCleanup();

    // --- Execution Pipeline ---

    public void start(int durationSeconds, @NotNull World targetWorld, @NotNull DisasterTriggerType trigger) {
        this.totalDurationSeconds = durationSeconds;
        this.remainingSeconds = durationSeconds;
        this.currentTick = 0;
        this.world = targetWorld;
        this.triggerType = trigger;
        this.state = DisasterState.ACTIVE;

        initializeBossBar();
        onStart();

        DisasterStartEvent event = new DisasterStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void tickInternal() {
        if (state != DisasterState.ACTIVE) return;

        currentTick++;

        // Every 20 ticks (1 second), reduce countdown
        if (currentTick % 20 == 0) {
            remainingSeconds--;
            updateBossBar();

            if (remainingSeconds <= 0) {
                stop(false);
                return;
            }
        }

        onTick();

        DisasterTickEvent event = new DisasterTickEvent(this, currentTick, remainingSeconds);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void stop(boolean forced) {
        if (state == DisasterState.IDLE) return;

        this.state = DisasterState.CLEANUP;

        try {
            onEnd();
        } catch (Throwable ex) {
            plugin.getLogger().severe("Error ending disaster " + type.name() + ": " + ex.getMessage());
        }

        try {
            onCleanup();
        } catch (Throwable ex) {
            plugin.getLogger().severe("Error cleaning up disaster " + type.name() + ": " + ex.getMessage());
        }

        removeBossBar();
        this.state = DisasterState.IDLE;

        DisasterEndEvent event = new DisasterEndEvent(this, forced);
        Bukkit.getPluginManager().callEvent(event);
    }

    // --- BossBar Management ---

    private void initializeBossBar() {
        BossBar.Color color = plugin.getConfigManager().getDisasterBossBarColor(type);
        BossBar.Overlay overlay = plugin.getConfigManager().getDisasterBossBarStyle(type);

        this.bossBar = BossBar.bossBar(getBossBarTitle(), 1.0f, color, overlay);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bossBar);
        }
    }

    protected void updateBossBar() {
        if (bossBar == null) return;

        float progress = Math.max(0.0f, Math.min(1.0f, (float) remainingSeconds / (float) totalDurationSeconds));
        bossBar.progress(progress);
        bossBar.name(getBossBarTitle());

        // Ensure newly joined players see the BossBar
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bossBar);
        }
    }

    private Component getBossBarTitle() {
        String template = plugin.getMessagesManager().getRawMessage(
                "bossbar-format",
                "<bold><red>%disaster_name%</red></bold> <gray>| Time Left: <yellow>%time_remaining%</yellow></gray>"
        );
        String formatted = template
                .replace("%disaster_name%", type.getDisplayName())
                .replace("%time_remaining%", TextUtil.formatTime(remainingSeconds));
        return TextUtil.parse(formatted);
    }

    private void removeBossBar() {
        if (bossBar != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.hideBossBar(bossBar);
            }
            bossBar = null;
        }
    }

    // --- Safe Block & Claim Operations ---

    /**
     * Safely mutates a block in the world respecting claims and recording for restoration.
     */
    public boolean mutateBlock(@NotNull Block block, @NotNull BlockData newData) {
        Location loc = block.getLocation();
        if (!ChunkSafetyGuard.isChunkLoaded(loc)) {
            return false;
        }
        if (!plugin.getClaimHookManager().canDisasterAffect(loc)) {
            return false;
        }

        DisasterBlockDamageEvent event = new DisasterBlockDamageEvent(this, block, newData);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        return plugin.getBlockRestorationEngine().mutateBlockSafely(block, newData);
    }

    // --- Getters & Status ---

    @NotNull
    public DisasterType getType() {
        return type;
    }

    @NotNull
    public DisasterState getState() {
        return state;
    }

    public void setState(@NotNull DisasterState state) {
        this.state = state;
    }

    @NotNull
    public DisasterTriggerType getTriggerType() {
        return triggerType;
    }

    @Nullable
    public World getWorld() {
        return world;
    }

    public int getTotalDurationSeconds() {
        return totalDurationSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public boolean isActive() {
        return state == DisasterState.ACTIVE;
    }
}

