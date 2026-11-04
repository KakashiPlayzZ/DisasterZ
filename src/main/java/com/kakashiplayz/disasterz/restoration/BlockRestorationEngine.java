package com.kakashiplayz.disasterz.restoration;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.util.ChunkSafetyGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Memory-safe block restoration engine with zero TPS drops.
 * Stores manipulated blocks in a thread-safe circular rollback buffer
 * and restores them gradually over time (e.g., 25-50 blocks per tick).
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class BlockRestorationEngine {

    private final DisasterZPlugin plugin;
    private final ConcurrentLinkedQueue<BlockSnapshot> restorationQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<Location, BlockSnapshot> originalStates = new ConcurrentHashMap<>();
    private BukkitTask regenTask;

    private static final int MAX_QUEUE_SIZE = 100_000;

    public BlockRestorationEngine(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the auto-regeneration tick task.
     */
    public void start() {
        stop();
        this.regenTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickRestoration, 1L, 1L);
    }

    /**
     * Stops the auto-regeneration task.
     */
    public void stop() {
        if (regenTask != null && !regenTask.isCancelled()) {
            regenTask.cancel();
            regenTask = null;
        }
    }

    /**
     * Records a block state before it is modified by a disaster.
     * Preserves only the FIRST pristine state if modified multiple times.
     */
    public void recordBlock(@NotNull Block block) {
        Location loc = block.getLocation();
        if (originalStates.containsKey(loc)) {
            return;
        }

        if (restorationQueue.size() >= MAX_QUEUE_SIZE) {
            // Memory guard: discard oldest entry if exceeding safety capacity
            BlockSnapshot oldest = restorationQueue.poll();
            if (oldest != null) {
                originalStates.remove(oldest.location());
            }
        }

        BlockSnapshot snapshot = new BlockSnapshot(loc, block.getBlockData().clone());
        originalStates.put(loc, snapshot);
        restorationQueue.add(snapshot);
    }

    /**
     * Mutates a block safely while recording its original state and checking chunk load.
     */
    public boolean mutateBlockSafely(@NotNull Block block, @NotNull BlockData newData) {
        if (!ChunkSafetyGuard.isChunkLoaded(block.getLocation())) {
            return false;
        }
        recordBlock(block);
        block.setBlockData(newData, false);
        return true;
    }

    /**
     * Called every tick to restore a batch of blocks smoothly without TPS spikes.
     */
    private void tickRestoration() {
        if (!plugin.getConfigManager().isBlockRestorationEnabled()) {
            return;
        }

        int blocksPerTick = plugin.getConfigManager().getBlockRestorationRate();
        int count = 0;

        while (count < blocksPerTick && !restorationQueue.isEmpty()) {
            BlockSnapshot snapshot = restorationQueue.poll();
            if (snapshot != null) {
                snapshot.restore();
                originalStates.remove(snapshot.location());
                count++;
            }
        }
    }

    /**
     * Restores all pending blocks immediately.
     * Invoked during clean plugin disabling or admin force-reset.
     */
    public void restoreAllImmediate() {
        while (!restorationQueue.isEmpty()) {
            BlockSnapshot snapshot = restorationQueue.poll();
            if (snapshot != null) {
                snapshot.restore();
            }
        }
        originalStates.clear();
    }

    public int getPendingBlockCount() {
        return restorationQueue.size();
    }

    public void clearQueue() {
        restorationQueue.clear();
        originalStates.clear();
    }
}

