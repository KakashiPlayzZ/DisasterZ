package com.kakashiplayz.disasterz.hook.claims;

import com.kakashiplayz.disasterz.hook.ClaimHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Resilient reflection hook for WorldGuard 7.x+.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class WorldGuardHook implements ClaimHook {

    private boolean available = false;
    private Method getPlatformMethod;
    private Method getRegionContainerMethod;
    private Method createQueryMethod;
    private Method getApplicableRegionsMethod;
    private Method sizeMethod;

    public WorldGuardHook() {
        try {
            if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
                Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
                Method getInstance = wgClass.getMethod("getInstance");
                Object wgInstance = getInstance.invoke(null);
                getPlatformMethod = wgClass.getMethod("getPlatform");
                Object platform = getPlatformMethod.invoke(wgInstance);

                Class<?> platformClass = platform.getClass();
                getRegionContainerMethod = platformClass.getMethod("getRegionContainer");
                Object container = getRegionContainerMethod.invoke(platform);

                Class<?> containerClass = container.getClass();
                createQueryMethod = containerClass.getMethod("createQuery");

                Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
                // Verify classes are loaded
                available = true;
            }
        } catch (Throwable ignored) {
            available = false;
        }
    }

    @Override
    @NotNull
    public String getName() {
        return "WorldGuard";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean isLocationProtected(@NotNull Location location) {
        if (!available) return false;
        try {
            Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object wgInstance = wgClass.getMethod("getInstance").invoke(null);
            Object platform = getPlatformMethod.invoke(wgInstance);
            Object container = getRegionContainerMethod.invoke(platform);
            Object query = createQueryMethod.invoke(container);

            Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Method adaptLocation = bukkitAdapter.getMethod("adapt", Location.class);
            Object adaptedLoc = adaptLocation.invoke(null, location);

            Method getApplicable = query.getClass().getMethod("getApplicableRegions", Class.forName("com.sk89q.worldedit.util.Location"));
            Object regionSet = getApplicable.invoke(query, adaptedLoc);

            Method sizeM = regionSet.getClass().getMethod("size");
            int regionCount = (int) sizeM.invoke(regionSet);
            return regionCount > 0;
        } catch (Throwable ignored) {
            return false;
        }
    }
}

