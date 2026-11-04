package com.kakashiplayz.disasterz.hook.claims;

import com.kakashiplayz.disasterz.hook.ClaimHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Resilient reflection hook for Lands.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class LandsHook implements ClaimHook {

    private boolean available = false;
    private Object landsIntegration;
    private Method isClaimedMethod;

    public LandsHook(@NotNull Plugin plugin) {
        try {
            if (Bukkit.getPluginManager().getPlugin("Lands") != null) {
                Class<?> integrationClass = Class.forName("me.angeschossen.lands.api.LandsIntegration");
                Method ofMethod = integrationClass.getMethod("of", Plugin.class);
                this.landsIntegration = ofMethod.invoke(null, plugin);
                this.isClaimedMethod = integrationClass.getMethod("isClaimed", Location.class);
                this.available = true;
            }
        } catch (Throwable ignored) {
            this.available = false;
        }
    }

    @Override
    @NotNull
    public String getName() {
        return "Lands";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean isLocationProtected(@NotNull Location location) {
        if (!available || landsIntegration == null || isClaimedMethod == null) return false;
        try {
            return (boolean) isClaimedMethod.invoke(landsIntegration, location);
        } catch (Throwable ignored) {
            return false;
        }
    }
}

