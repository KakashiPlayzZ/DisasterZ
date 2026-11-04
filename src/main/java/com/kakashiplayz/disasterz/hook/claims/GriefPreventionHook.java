package com.kakashiplayz.disasterz.hook.claims;

import com.kakashiplayz.disasterz.hook.ClaimHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Resilient reflection hook for GriefPrevention.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class GriefPreventionHook implements ClaimHook {

    private boolean available = false;
    private Object dataStoreInstance;
    private Method getClaimAtMethod;

    public GriefPreventionHook() {
        try {
            if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) {
                Class<?> gpClass = Class.forName("me.ryanhamshire.GriefPrevention.GriefPrevention");
                Field instanceField = gpClass.getField("instance");
                Object gpInstance = instanceField.get(null);

                Field dataStoreField = gpClass.getField("dataStore");
                this.dataStoreInstance = dataStoreField.get(gpInstance);

                Class<?> dataStoreClass = dataStoreInstance.getClass();
                this.getClaimAtMethod = dataStoreClass.getMethod("getClaimAt", Location.class, boolean.class, Class.forName("me.ryanhamshire.GriefPrevention.Claim"));
                this.available = true;
            }
        } catch (Throwable ignored) {
            this.available = false;
        }
    }

    @Override
    @NotNull
    public String getName() {
        return "GriefPrevention";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean isLocationProtected(@NotNull Location location) {
        if (!available || dataStoreInstance == null || getClaimAtMethod == null) return false;
        try {
            Object claim = getClaimAtMethod.invoke(dataStoreInstance, location, false, null);
            return claim != null;
        } catch (Throwable ignored) {
            return false;
        }
    }
}

