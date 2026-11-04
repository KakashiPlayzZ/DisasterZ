package com.kakashiplayz.disasterz.disaster.impl;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.util.AsyncMathEngine;
import com.kakashiplayz.disasterz.util.CameraShakeUtil;
import com.kakashiplayz.disasterz.util.ChunkSafetyGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Earthquake: Violent tectonic fractures, authentic screen-shaking tremors,
 * falling debris blocks, slowness, and structural fissures.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class EarthquakeDisaster extends Disaster {

    public EarthquakeDisaster(@NotNull DisasterZPlugin plugin) {
        super(plugin, DisasterType.EARTHQUAKE);
    }

    @Override
    public void onPreWarn() {
    }

    @Override
    public void onStart() {
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().get(0);
        }
    }

    @Override
    public void onTick() {
        if (world == null) return;

        // 1. Continuous Screen Shake: applied every 4 ticks (0.2s) for true seismic shudder
        if (currentTick % 4 == 0) {
            for (Player player : world.getPlayers()) {
                CameraShakeUtil.applyCameraTremor(player, 0.85);
            }
        }

        // 2. Heavy Ground Rumble and Anvil Clatter audio
        if (currentTick % 16 == 0) {
            for (Player player : world.getPlayers()) {
                plugin.getAudioEngine().playSeismicRumble(player.getLocation(), 1.6f);
                player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_DAMAGE, SoundCategory.BLOCKS, 0.7f, 0.5f);
            }
        }

        // 3. Periodic ground fissures & falling blocks (every 20 ticks)
        if (currentTick % 20 == 0) {
            for (Player player : world.getPlayers()) {
                Location pLoc = player.getLocation();
                double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);

                // Compute fracture path asynchronously
                AsyncMathEngine.calculateFissurePathAsync(14, angle).thenAccept(points -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!isActive()) return;
                        spawnFissure(pLoc, points);
                    });
                });
            }
        }
    }

    private void spawnFissure(Location base, java.util.List<AsyncMathEngine.VectorPoint> points) {
        for (AsyncMathEngine.VectorPoint pt : points) {
            int blockX = (int) (base.getX() + pt.x());
            int blockZ = (int) (base.getZ() + pt.z());

            if (!ChunkSafetyGuard.isChunkLoaded(world, blockX, blockZ)) continue;

            int surfaceY = world.getHighestBlockYAt(blockX, blockZ);
            Location surfaceLoc = new Location(world, blockX, surfaceY, blockZ);
            Block surfaceBlock = surfaceLoc.getBlock();

            // Spawn ground crack particles
            world.spawnParticle(Particle.BLOCK, surfaceLoc.clone().add(0.5, 1.0, 0.5), 16, 0.4, 0.2, 0.4, surfaceBlock.getBlockData());

            // Destabilize surface blocks into fissures and falling gravel/cracked stone
            if (surfaceBlock.getType().isSolid()) {
                if (ThreadLocalRandom.current().nextDouble() < 0.4) {
                    Material replacement = (ThreadLocalRandom.current().nextBoolean()) ? Material.GRAVEL : Material.CRACKED_STONE_BRICKS;
                    mutateBlock(surfaceBlock, Bukkit.createBlockData(replacement));

                    // Launch occasional falling rubble block
                    if (ThreadLocalRandom.current().nextDouble() < 0.25) {
                        FallingBlock falling = world.spawnFallingBlock(surfaceLoc.clone().add(0, 1.2, 0), Bukkit.createBlockData(Material.COBBLESTONE));
                        falling.setDropItem(false);
                        falling.setHurtEntities(true);
                        falling.setVelocity(new Vector(
                                ThreadLocalRandom.current().nextDouble(-0.2, 0.2),
                                0.42,
                                ThreadLocalRandom.current().nextDouble(-0.2, 0.2)
                        ));
                    }
                }
            }
        }
    }

    @Override
    public void onEnd() {
    }

    @Override
    public void onCleanup() {
    }
}
