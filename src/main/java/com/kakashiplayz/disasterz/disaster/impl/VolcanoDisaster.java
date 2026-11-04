package com.kakashiplayz.disasterz.disaster.impl;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Volcano: Relentless volcanic eruption unleashing dense ash plumes,
 * continuous high-velocity magma bombs, persistent lava pool formation,
 * and aggressively spreading wildfire fronts that persist throughout the disaster.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class VolcanoDisaster extends Disaster {

    private final List<Location> activeLavaOrigins = new ArrayList<>();

    public VolcanoDisaster(@NotNull DisasterZPlugin plugin) {
        super(plugin, DisasterType.VOLCANO);
    }

    @Override
    public void onPreWarn() {
    }

    @Override
    public void onStart() {
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().get(0);
        }
        activeLavaOrigins.clear();
    }

    @Override
    public void onTick() {
        if (world == null) return;

        // 1. Ash Clouds and Seismic Shudders
        for (Player player : world.getPlayers()) {
            Location pLoc = player.getLocation();
            world.spawnParticle(Particle.ASH, pLoc.clone().add(0, 5, 0), 35, 10, 4, 10, 0.08);
            world.spawnParticle(Particle.LARGE_SMOKE, pLoc.clone().add(0, 4, 0), 12, 8, 3, 8, 0.03);
            world.spawnParticle(Particle.FLAME, pLoc.clone().add(0, 3, 0), 5, 6, 2, 6, 0.02);

            if (currentTick % 25 == 0) {
                plugin.getAudioEngine().playEruptiveBlast(pLoc, 1.4f);
                CameraShakeUtil.applyCameraTremor(player, 0.55);
            }
        }

        // 2. Continuous Volcanic Bombs (Spawned every 12 ticks)
        if (currentTick % 12 == 0) {
            for (Player player : world.getPlayers()) {
                if (ThreadLocalRandom.current().nextDouble() < 0.75) {
                    launchVolcanicBomb(player.getLocation());
                }
            }
        }

        // 3. Active Fire & Lava Spreading Wave (Executed every 20 ticks)
        if (currentTick % 20 == 0 && !activeLavaOrigins.isEmpty()) {
            spreadVolcanicFireAndLava();
        }
    }

    private void launchVolcanicBomb(Location playerLoc) {
        double offsetX = ThreadLocalRandom.current().nextDouble(-20, 20);
        double offsetZ = ThreadLocalRandom.current().nextDouble(-20, 20);
        Location targetGround = playerLoc.clone().add(offsetX, 0, offsetZ);

        if (!ChunkSafetyGuard.isChunkLoaded(targetGround)) return;

        int topY = world.getHighestBlockYAt(targetGround.getBlockX(), targetGround.getBlockZ());
        targetGround.setY(topY);

        Location spawnSky = targetGround.clone().add(
                ThreadLocalRandom.current().nextDouble(-8, 8),
                36,
                ThreadLocalRandom.current().nextDouble(-8, 8)
        );

        Vector velocity = targetGround.toVector().subtract(spawnSky.toVector()).normalize().multiply(1.35);

        // Projectile simulator to guarantee impact and persistent block mutations
        new BukkitRunnable() {
            private Location current = spawnSky.clone();
            private int life = 0;

            @Override
            public void run() {
                if (!isActive() || life++ > 40) {
                    cancel();
                    return;
                }

                current.add(velocity);

                if (!ChunkSafetyGuard.isChunkLoaded(current)) {
                    cancel();
                    return;
                }

                world.spawnParticle(Particle.LAVA, current, 3, 0.2, 0.2, 0.2, 0.0);
                world.spawnParticle(Particle.FLAME, current, 8, 0.3, 0.3, 0.3, 0.05);

                if (current.getY() <= targetGround.getY() + 1.0 || current.getBlock().getType().isSolid()) {
                    detonateVolcanicImpact(current);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void detonateVolcanicImpact(Location impactLoc) {
        if (!ChunkSafetyGuard.isChunkLoaded(impactLoc)) return;

        world.spawnParticle(Particle.EXPLOSION_EMITTER, impactLoc, 1);
        world.spawnParticle(Particle.LAVA, impactLoc, 30, 1.5, 1.0, 1.5, 0.1);
        world.playSound(impactLoc, Sound.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.5f, 0.6f);
        world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.WEATHER, 1.2f, 0.7f);

        int groundY = world.getHighestBlockYAt(impactLoc.getBlockX(), impactLoc.getBlockZ());
        Location groundCenter = new Location(world, impactLoc.getBlockX(), groundY, impactLoc.getBlockZ());

        // 1. Form persistent lava source at the impact point
        Block centerBlock = groundCenter.getBlock();
        if (mutateBlock(centerBlock, Bukkit.createBlockData(Material.LAVA))) {
            activeLavaOrigins.add(groundCenter);
            if (activeLavaOrigins.size() > 40) {
                activeLavaOrigins.remove(0); // Cap tracking list for performance
            }
        }

        // 2. Form surrounding Magma block crater
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue;
                int bX = groundCenter.getBlockX() + x;
                int bZ = groundCenter.getBlockZ() + z;
                if (!ChunkSafetyGuard.isChunkLoaded(world, bX, bZ)) continue;

                int surfY = world.getHighestBlockYAt(bX, bZ);
                Block surfBlock = world.getBlockAt(bX, surfY, bZ);

                if (surfBlock.getType().isSolid() && surfBlock.getType() != Material.BEDROCK) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        mutateBlock(surfBlock, Bukkit.createBlockData(Material.MAGMA_BLOCK));
                    }
                }

                // Place persistent fire on top
                Block above = surfBlock.getLocation().add(0, 1, 0).getBlock();
                if (above.getType().isAir()) {
                    mutateBlock(above, Bukkit.createBlockData(Material.FIRE));
                }
            }
        }
    }

    private void spreadVolcanicFireAndLava() {
        // Actively expand fire from existing origins
        for (Location origin : new ArrayList<>(activeLavaOrigins)) {
            if (!ChunkSafetyGuard.isChunkLoaded(origin)) continue;

            int spreadRadius = 4;
            for (int i = 0; i < 3; i++) {
                int rx = origin.getBlockX() + ThreadLocalRandom.current().nextInt(-spreadRadius, spreadRadius + 1);
                int rz = origin.getBlockZ() + ThreadLocalRandom.current().nextInt(-spreadRadius, spreadRadius + 1);

                if (!ChunkSafetyGuard.isChunkLoaded(world, rx, rz)) continue;

                int topY = world.getHighestBlockYAt(rx, rz);
                Block surface = world.getBlockAt(rx, topY, rz);
                Block firePos = surface.getLocation().add(0, 1, 0).getBlock();

                if (firePos.getType().isAir() && surface.getType().isSolid() && surface.getType() != Material.FIRE) {
                    mutateBlock(firePos, Bukkit.createBlockData(Material.FIRE));
                }
            }
        }
    }

    @Override
    public void onEnd() {
        activeLavaOrigins.clear();
    }

    @Override
    public void onCleanup() {
        activeLavaOrigins.clear();
    }
}
