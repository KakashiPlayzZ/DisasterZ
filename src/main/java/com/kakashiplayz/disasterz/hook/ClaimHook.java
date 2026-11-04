package com.kakashiplayz.disasterz.hook;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Common interface for claim & territory protection plugin hooks.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public interface ClaimHook {

    @NotNull
    String getName();

    boolean isAvailable();

    boolean isLocationProtected(@NotNull Location location);
}

