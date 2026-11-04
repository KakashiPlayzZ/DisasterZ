package com.kakashiplayz.disasterz.hook;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.hook.claims.GriefPreventionHook;
import com.kakashiplayz.disasterz.hook.claims.LandsHook;
import com.kakashiplayz.disasterz.hook.claims.TownyHook;
import com.kakashiplayz.disasterz.hook.claims.WorldGuardHook;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages soft-dependency claim & region protection hooks.
 * Guarantees zero runtime crashes if plugins are absent or updated.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class ClaimHookManager {

    private final DisasterZPlugin plugin;
    private final List<ClaimHook> activeHooks = new ArrayList<>();

    public ClaimHookManager(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    public void initializeHooks() {
        activeHooks.clear();

        registerHook(new WorldGuardHook());
        registerHook(new GriefPreventionHook());
        registerHook(new TownyHook());
        registerHook(new LandsHook(plugin));

        plugin.getLogger().info("[DisasterZ] Successfully linked " + activeHooks.size() + " claim protection hooks.");
    }

    private void registerHook(@NotNull ClaimHook hook) {
        if (hook.isAvailable()) {
            activeHooks.add(hook);
            plugin.getLogger().info("[DisasterZ] Region protection hooked: " + hook.getName());
        }
    }

    /**
     * Checks if a location is within any protected claim region.
     */
    public boolean isLocationProtected(@NotNull Location location) {
        for (ClaimHook hook : activeHooks) {
            try {
                if (hook.isLocationProtected(location)) {
                    return true;
                }
            } catch (Throwable ex) {
                // Ignore hook evaluation error to ensure stability
            }
        }
        return false;
    }

    /**
     * Checks if a disaster is allowed to alter blocks or cause damage at this location.
     */
    public boolean canDisasterAffect(@NotNull Location location) {
        if (!plugin.getConfigManager().isRespectClaims()) {
            return true;
        }
        return !isLocationProtected(location);
    }

    @NotNull
    public List<ClaimHook> getActiveHooks() {
        return activeHooks;
    }
}

