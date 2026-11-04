package com.kakashiplayz.disasterz.restoration;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * Thread-safe snapshot of a block's prior state before disaster manipulation.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public record BlockSnapshot(
        @NotNull Location location,
        @NotNull BlockData originalData,
        long timestamp
) {
    public BlockSnapshot(@NotNull Location location, @NotNull BlockData originalData) {
        this(location, originalData, System.currentTimeMillis());
    }

    /**
     * Restores the original block data to the world.
     * Must be called on the Bukkit main thread.
     */
    public void restore() {
        if (location.getWorld() != null && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            location.getBlock().setBlockData(originalData, false);
        }
    }
}

