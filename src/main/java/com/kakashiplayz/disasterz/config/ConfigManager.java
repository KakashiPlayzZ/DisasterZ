package com.kakashiplayz.disasterz.config;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles all core configuration values from config.yml with runtime setters.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class ConfigManager {

    private final DisasterZPlugin plugin;
    private FileConfiguration config;

    private boolean autoSchedulerEnabled;
    private int autoSchedulerInterval;
    private int warningDelaySeconds;
    private int minimumPlayers;
    private final Set<String> disabledWorlds = new HashSet<>();
    private boolean respectClaims;
    private boolean blockRestorationEnabled;
    private int blockRestorationRate;
    private boolean useGhostBlocksForLiquids;

    public ConfigManager(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        this.autoSchedulerEnabled = config.getBoolean("settings.auto-scheduler.enabled", true);
        this.autoSchedulerInterval = config.getInt("settings.auto-scheduler.interval-seconds", 3600);
        this.warningDelaySeconds = config.getInt("settings.auto-scheduler.warning-delay-seconds", 30);
        this.minimumPlayers = config.getInt("settings.auto-scheduler.minimum-players", 1);

        this.disabledWorlds.clear();
        List<String> worlds = config.getStringList("settings.disabled-worlds");
        this.disabledWorlds.addAll(worlds);

        this.respectClaims = config.getBoolean("settings.protection.respect-claims", true);
        this.blockRestorationEnabled = config.getBoolean("settings.block-restoration.enabled", true);
        this.blockRestorationRate = config.getInt("settings.block-restoration.blocks-per-tick", 25);
        this.useGhostBlocksForLiquids = config.getBoolean("settings.block-restoration.use-ghost-blocks-for-liquids", false);
    }

    public boolean isAutoSchedulerEnabled() {
        return autoSchedulerEnabled;
    }

    public void setAutoSchedulerEnabled(boolean enabled) {
        this.autoSchedulerEnabled = enabled;
        config.set("settings.auto-scheduler.enabled", enabled);
        plugin.saveConfig();
    }

    public int getAutoSchedulerInterval() {
        return autoSchedulerInterval;
    }

    public void setAutoSchedulerInterval(int seconds) {
        this.autoSchedulerInterval = Math.max(60, seconds);
        config.set("settings.auto-scheduler.interval-seconds", this.autoSchedulerInterval);
        plugin.saveConfig();
    }

    public int getWarningDelaySeconds() {
        return warningDelaySeconds;
    }

    public void setWarningDelaySeconds(int seconds) {
        this.warningDelaySeconds = Math.max(5, seconds);
        config.set("settings.auto-scheduler.warning-delay-seconds", this.warningDelaySeconds);
        plugin.saveConfig();
    }

    public int getMinimumPlayers() {
        return minimumPlayers;
    }

    public void setMinimumPlayers(int count) {
        this.minimumPlayers = Math.max(0, count);
        config.set("settings.auto-scheduler.minimum-players", this.minimumPlayers);
        plugin.saveConfig();
    }

    public boolean isWorldDisabled(@NotNull String worldName) {
        return disabledWorlds.contains(worldName);
    }

    public boolean isRespectClaims() {
        return respectClaims;
    }

    public boolean isBlockRestorationEnabled() {
        return blockRestorationEnabled;
    }

    public int getBlockRestorationRate() {
        return Math.max(1, blockRestorationRate);
    }

    public boolean isUseGhostBlocksForLiquids() {
        return useGhostBlocksForLiquids;
    }

    public boolean isDisasterEnabled(@NotNull DisasterType type) {
        return config.getBoolean("disasters." + type.getConfigKey() + ".enabled", true);
    }

    public boolean isRandomCycleEnabled(@NotNull DisasterType type) {
        return config.getBoolean("disasters." + type.getConfigKey() + ".random-cycle-enabled", true);
    }

    public void setRandomCycleEnabled(@NotNull DisasterType type, boolean enabled) {
        config.set("disasters." + type.getConfigKey() + ".random-cycle-enabled", enabled);
        plugin.saveConfig();
    }

    public int getDisasterDuration(@NotNull DisasterType type) {
        return config.getInt("disasters." + type.getConfigKey() + ".duration", 180);
    }

    public int getDisasterWeight(@NotNull DisasterType type) {
        return config.getInt("disasters." + type.getConfigKey() + ".weight", 10);
    }

    @NotNull
    public BossBar.Color getDisasterBossBarColor(@NotNull DisasterType type) {
        String colorStr = config.getString("disasters." + type.getConfigKey() + ".bossbar.color", "RED").toUpperCase();
        try {
            return BossBar.Color.valueOf(colorStr);
        } catch (IllegalArgumentException ex) {
            return BossBar.Color.RED;
        }
    }

    @NotNull
    public BossBar.Overlay getDisasterBossBarStyle(@NotNull DisasterType type) {
        String styleStr = config.getString("disasters." + type.getConfigKey() + ".bossbar.style", "PROGRESS").toUpperCase();
        try {
            return BossBar.Overlay.valueOf(styleStr);
        } catch (IllegalArgumentException ex) {
            return BossBar.Overlay.PROGRESS;
        }
    }

    public double getMobHealthMultiplier(@NotNull DisasterType type) {
        return config.getDouble("disasters." + type.getConfigKey() + ".mob-modifiers.health-multiplier", 2.0);
    }

    public double getMobDamageMultiplier(@NotNull DisasterType type) {
        return config.getDouble("disasters." + type.getConfigKey() + ".mob-modifiers.damage-multiplier", 1.5);
    }

    public double getMobSpeedMultiplier(@NotNull DisasterType type) {
        return config.getDouble("disasters." + type.getConfigKey() + ".mob-modifiers.speed-multiplier", 1.2);
    }

    @NotNull
    public FileConfiguration getRawConfig() {
        return config;
    }
}
