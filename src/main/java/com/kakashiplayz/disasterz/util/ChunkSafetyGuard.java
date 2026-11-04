package com.kakashiplayz.disasterz.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Ensures Zero-TPS-Drop safety by guarding against forced chunk loads.
 * Disasters must NEVER trigger synchronous chunk loading or generate new chunks.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public final class ChunkSafetyGuard {

    private ChunkSafetyGuard() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Checks if the chunk containing the coordinates (x, z) is currently loaded in the world.
     */
    public static boolean isChunkLoaded(@Nullable World world, int blockX, int blockZ) {
        if (world == null) return false;
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        return world.isChunkLoaded(chunkX, chunkZ);
    }

    /**
     * Checks if the chunk at the given Location is loaded.
     */
    public static boolean isChunkLoaded(@Nullable Location location) {
        if (location == null || location.getWorld() == null) return false;
        return isChunkLoaded(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    /**
     * Safely retrieves a Block if its chunk is loaded, or null otherwise.
     */
    @Nullable
    public static Block getSafeBlock(@Nullable World world, int x, int y, int z) {
        if (world == null) return null;
        if (!isChunkLoaded(world, x, z)) return null;
        if (y < world.getMinHeight() || y >= world.getMaxHeight()) return null;
        return world.getBlockAt(x, y, z);
    }

    /**
     * Safely retrieves a Block at the given location if its chunk is loaded.
     */
    @NotNull
    public static Optional<Block> getSafeBlock(@Nullable Location location) {
        if (location == null || location.getWorld() == null) return Optional.empty();
        Block block = getSafeBlock(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return Optional.ofNullable(block);
    }

    /**
     * Safely queries nearby entities within a bounding box only if the center chunk is loaded.
     */
    @NotNull
    public static Collection<Entity> getSafeNearbyEntities(@Nullable World world, @NotNull BoundingBox box) {
        if (world == null) return Collections.emptyList();
        int centerX = (int) ((box.getMinX() + box.getMaxX()) / 2);
        int centerZ = (int) ((box.getMinZ() + box.getMaxZ()) / 2);
        if (!isChunkLoaded(world, centerX, centerZ)) {
            return Collections.emptyList();
        }
        return world.getNearbyEntities(box);
    }
}

