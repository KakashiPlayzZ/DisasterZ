package com.kakashiplayz.disasterz.hook.claims;

import com.kakashiplayz.disasterz.hook.ClaimHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Resilient reflection hook for Towny Advanced.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class TownyHook implements ClaimHook {

    private boolean available = false;
    private Method getInstanceMethod;
    private Method isWildernessMethod;

    public TownyHook() {
        try {
            if (Bukkit.getPluginManager().getPlugin("Towny") != null) {
                Class<?> apiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
                this.getInstanceMethod = apiClass.getMethod("getInstance");
                this.isWildernessMethod = apiClass.getMethod("isWilderness", Location.class);
                this.available = true;
            }
        } catch (Throwable ignored) {
            this.available = false;
        }
    }

    @Override
    @NotNull
    public String getName() {
        return "Towny";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean isLocationProtected(@NotNull Location location) {
        if (!available || getInstanceMethod == null || isWildernessMethod == null) return false;
        try {
            Object apiInstance = getInstanceMethod.invoke(null);
            boolean isWilderness = (boolean) isWildernessMethod.invoke(apiInstance, location);
            return !isWilderness; // If not wilderness, it is inside a town claim
        } catch (Throwable ignored) {
            return false;
        }
    }
}

