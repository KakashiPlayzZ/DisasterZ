package com.kakashiplayz.disasterz.restoration;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles packet-based client visual blocks via Player#sendBlockChange.
 * Delivers full visual immersion for high-volume liquid events (Tsunami, Flood)
 * without writing single modifications to the actual world save.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class GhostBlockManager {

    private final DisasterZPlugin plugin;
    private final ConcurrentHashMap<Player, Set<Location>> activeGhostBlocks = new ConcurrentHashMap<>();

    public GhostBlockManager(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a packet-level block change to a specific player.
     */
    public void sendGhostBlock(@NotNull Player player, @NotNull Location location, @NotNull BlockData data) {
        if (!player.isOnline()) return;

        player.sendBlockChange(location, data);
        activeGhostBlocks.computeIfAbsent(player, p -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(location.clone());
    }

    /**
     * Broadcasts a packet-level block change to all players within render distance of the location.
     */
    public void broadcastGhostBlock(@NotNull Location location, @NotNull BlockData data, double radius) {
        if (location.getWorld() == null) return;

        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= radius) {
                sendGhostBlock(player, location, data);
            }
        }
    }

    /**
     * Reverts a specific ghost block for a player back to true world data.
     */
    public void revertGhostBlock(@NotNull Player player, @NotNull Location location) {
        if (!player.isOnline()) return;

        if (location.getWorld() != null && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            player.sendBlockChange(location, location.getBlock().getBlockData());
        }

        Set<Location> blocks = activeGhostBlocks.get(player);
        if (blocks != null) {
            blocks.remove(location);
        }
    }

    /**
     * Cleans up and reverts all ghost blocks for all players.
     */
    public void revertAll() {
        for (var entry : activeGhostBlocks.entrySet()) {
            Player player = entry.getKey();
            if (player.isOnline()) {
                for (Location loc : entry.getValue()) {
                    if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                        player.sendBlockChange(loc, loc.getBlock().getBlockData());
                    }
                }
            }
        }
        activeGhostBlocks.clear();
    }

    /**
     * Reverts ghost blocks for a disconnecting player.
     */
    public void clearPlayer(@NotNull Player player) {
        activeGhostBlocks.remove(player);
    }
}

