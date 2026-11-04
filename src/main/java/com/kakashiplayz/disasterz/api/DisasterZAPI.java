package com.kakashiplayz.disasterz.api;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * Public developer API entry point for DisasterZ.
 *
 * Developed by KakashiPlayz
 * Website: https://kakashiplayz.vercel.app
 * Discord: https://discord.gg/QdTN4BFkTv
 */
public final class DisasterZAPI {

    private static DisasterZPlugin plugin;

    private DisasterZAPI() {
        throw new UnsupportedOperationException("Utility/API class cannot be instantiated.");
    }

    public static void initialize(@NotNull DisasterZPlugin instance) {
        plugin = instance;
    }

    @NotNull
    public static DisasterZPlugin getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("DisasterZAPI is not initialized yet!");
        }
        return plugin;
    }

    /**
     * Checks if any disaster is currently active.
     */
    public static boolean isAnyDisasterActive() {
        return plugin.getDisasterManager().getActiveDisaster().isPresent();
    }

    /**
     * Gets the currently active disaster, if any.
     */
    @NotNull
    public static Optional<Disaster> getActiveDisaster() {
        return plugin.getDisasterManager().getActiveDisaster();
    }

    /**
     * Checks if a warning is currently running for an upcoming disaster.
     */
    public static boolean isWarningActive() {
        return plugin.getWarningManager().isWarningActive();
    }

    /**
     * Gets all registered disaster types.
     */
    @NotNull
    public static Collection<Disaster> getAllDisasters() {
        return plugin.getDisasterManager().getRegisteredDisasters().values();
    }

    /**
     * Gets a disaster instance by its type enum.
     */
    @Nullable
    public static Disaster getDisaster(@NotNull DisasterType type) {
        return plugin.getDisasterManager().getDisaster(type);
    }

    /**
     * Starts a disaster by type with default or specified duration.
     */
    public static boolean startDisaster(@NotNull DisasterType type, int durationSeconds) {
        return plugin.getDisasterManager().startDisaster(type, durationSeconds, false);
    }

    /**
     * Stops the currently active disaster.
     */
    public static boolean stopActiveDisaster() {
        return plugin.getDisasterManager().stopActiveDisaster(true);
    }
}

