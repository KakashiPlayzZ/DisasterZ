package com.kakashiplayz.disasterz.disaster.impl;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.util.AsyncMathEngine;
import com.kakashiplayz.disasterz.util.ChunkSafetyGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tsunami: A colossal moving wall of water particles and synchronized wave fronts
 * applying continuous forward knockback, drowning hazards, and deluge soundscapes.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class TsunamiDisaster extends Disaster {

    private Location waveOrigin;
    private Vector waveDirection;
    private double currentDistance = 0.0;
    private final double waveWidth = 40.0;
    private final double waveHeight = 9.0;
    private final double speedPerTick = 0.65;
    private final BlockData waterData = Bukkit.createBlockData(Material.WATER);

    public TsunamiDisaster(@NotNull DisasterZPlugin plugin) {
        super(plugin, DisasterType.TSUNAMI);
    }

    @Override
    public void onPreWarn() {
        // Warning announcements handled by WarningManager
    }

    @Override
    public void onStart() {
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().get(0);
        }
        if (world == null) return;

        // Choose wave origin slightly offset from first active player
        Player targetPlayer = world.getPlayers().stream().findFirst().orElse(null);
        if (targetPlayer != null) {
            waveOrigin = targetPlayer.getLocation().clone().subtract(35, 0, 35);
            waveDirection = targetPlayer.getLocation().toVector().subtract(waveOrigin.toVector()).setY(0).normalize();
        } else {
            waveOrigin = world.getSpawnLocation().clone().subtract(30, 0, 30);
            waveDirection = new Vector(1, 0, 1).normalize();
        }
        currentDistance = 0.0;
    }

    @Override
    public void onTick() {
        if (world == null || waveOrigin == null) return;

        currentDistance += speedPerTick;
        Location currentWaveCenter = waveOrigin.clone().add(waveDirection.clone().multiply(currentDistance));

        // Adjust Y to terrain height
        if (ChunkSafetyGuard.isChunkLoaded(currentWaveCenter)) {
            int highestY = world.getHighestBlockYAt(currentWaveCenter.getBlockX(), currentWaveCenter.getBlockZ());
            currentWaveCenter.setY(highestY);
        }

        // 1. Play atmospheric deluge sound
        if (currentTick % 15 == 0) {
            plugin.getAudioEngine().playDelugeAmbience(currentWaveCenter, 1.8f);
        }

        // 2. Offload wave particle matrix calculation to AsyncMathEngine
        AsyncMathEngine.calculateTsunamiWaveWallAsync(waveWidth, waveHeight, waveDirection, currentDistance)
                .thenAccept(points -> {
                    // Sync particle dispatch to main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!isActive()) return;
                        renderWaveParticles(currentWaveCenter, points);
                    });
                });

        // 3. Physical wave interactions (Entity knockback, drowning, fluid placement)
        applyWaveImpacts(currentWaveCenter);
    }

    private void renderWaveParticles(Location center, List<AsyncMathEngine.VectorPoint> points) {
        for (AsyncMathEngine.VectorPoint pt : points) {
            Location loc = pt.toLocation(center);
            if (ChunkSafetyGuard.isChunkLoaded(loc)) {
                world.spawnParticle(Particle.SPLASH, loc, 1, 0.15, 0.15, 0.15, 0.02);
                if (loc.getY() >= center.getY() + waveHeight - 1.5) {
                    world.spawnParticle(Particle.BUBBLE_POP, loc, 1, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }
    }

    private void applyWaveImpacts(Location center) {
        BoundingBox box = BoundingBox.of(center, waveWidth / 2, waveHeight, 4.0);
        for (Entity entity : ChunkSafetyGuard.getSafeNearbyEntities(world, box)) {
            if (entity instanceof LivingEntity living) {
                // Violent water surge knockback
                Vector surgeImpulse = waveDirection.clone().multiply(1.2).setY(0.4);
                living.setVelocity(surgeImpulse);

                // Drowning & submersion hazard
                int remainingAir = Math.max(0, living.getRemainingAir() - 15);
                living.setRemainingAir(remainingAir);
                if (remainingAir <= 0) {
                    living.damage(2.5);
                }
            }
        }

        // Temporary fluid placement or ghost blocks
        if (currentTick % 5 == 0) {
            int sampleCount = 6;
            Vector perpendicular = new Vector(-waveDirection.getZ(), 0, waveDirection.getX()).normalize();
            for (int i = -sampleCount; i <= sampleCount; i++) {
                Location fluidLoc = center.clone().add(perpendicular.clone().multiply(i * (waveWidth / (sampleCount * 2))));
                if (ChunkSafetyGuard.isChunkLoaded(fluidLoc)) {
                    Block block = fluidLoc.getBlock();
                    if (block.getType().isAir()) {
                        if (plugin.getConfigManager().isUseGhostBlocksForLiquids()) {
                            plugin.getGhostBlockManager().broadcastGhostBlock(fluidLoc, waterData, 48);
                        } else {
                            mutateBlock(block, waterData);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnd() {
        if (plugin.getConfigManager().isUseGhostBlocksForLiquids()) {
            plugin.getGhostBlockManager().revertAll();
        }
    }

    @Override
    public void onCleanup() {
        currentDistance = 0.0;
        waveOrigin = null;
    }
}
