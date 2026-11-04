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
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Meteor Shower: Catastrophic extraterrestrial impactors that blast apart blocks,
 * forge molten impact craters, fling destructive rubble, and spread raging surface fires.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class MeteorShowerDisaster extends Disaster {

    public MeteorShowerDisaster(@NotNull DisasterZPlugin plugin) {
        super(plugin, DisasterType.METEOR_SHOWER);
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

        // Spawn falling meteor projectiles every 16 ticks
        if (currentTick % 16 == 0) {
            for (Player player : world.getPlayers()) {
                if (ThreadLocalRandom.current().nextDouble() < 0.7) {
                    spawnMeteorIncoming(player.getLocation());
                }
            }
        }
    }

    private void spawnMeteorIncoming(Location playerLoc) {
        double targetOffsetX = ThreadLocalRandom.current().nextDouble(-22, 22);
        double targetOffsetZ = ThreadLocalRandom.current().nextDouble(-22, 22);
        Location targetGround = playerLoc.clone().add(targetOffsetX, 0, targetOffsetZ);

        if (!ChunkSafetyGuard.isChunkLoaded(targetGround)) return;

        int groundY = world.getHighestBlockYAt(targetGround.getBlockX(), targetGround.getBlockZ());
        targetGround.setY(groundY);

        Location spawnSky = targetGround.clone().add(
                ThreadLocalRandom.current().nextDouble(-14, 14),
                44,
                ThreadLocalRandom.current().nextDouble(-14, 14)
        );

        Vector velocity = targetGround.toVector().subtract(spawnSky.toVector()).normalize().multiply(1.55);

        // Meteor ballistic projectile simulator
        new BukkitRunnable() {
            private Location current = spawnSky.clone();
            private int life = 0;

            @Override
            public void run() {
                if (!isActive() || life++ > 45) {
                    cancel();
                    return;
                }

                current.add(velocity);

                if (!ChunkSafetyGuard.isChunkLoaded(current)) {
                    cancel();
                    return;
                }

                // Dense blazing fireball particle trail
                world.spawnParticle(Particle.FLAME, current, 16, 0.4, 0.4, 0.4, 0.08);
                world.spawnParticle(Particle.LAVA, current, 4, 0.3, 0.3, 0.3, 0.0);
                world.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, current, 6, 0.3, 0.3, 0.3, 0.03);

                // Check terrain or ground impact
                if (current.getY() <= targetGround.getY() + 1.2 || current.getBlock().getType().isSolid()) {
                    detonateMeteor(current);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void detonateMeteor(Location impactLoc) {
        if (!ChunkSafetyGuard.isChunkLoaded(impactLoc)) return;

        // 1. Sonic Boom Explosion & Camera Tremor
        world.spawnParticle(Particle.EXPLOSION_EMITTER, impactLoc, 2);
        world.spawnParticle(Particle.LAVA, impactLoc, 35, 2.0, 1.5, 2.0, 0.15);
        plugin.getAudioEngine().playEruptiveBlast(impactLoc, 2.5f);
        CameraShakeUtil.applyRadialTremor(impactLoc, 35.0, 1.0);

        // Knockback & damage nearby living entities
        BoundingBox blastBox = BoundingBox.of(impactLoc, 6.0, 4.0, 6.0);
        for (Entity e : ChunkSafetyGuard.getSafeNearbyEntities(world, blastBox)) {
            if (e instanceof LivingEntity living) {
                Vector kb = living.getLocation().toVector().subtract(impactLoc.toVector()).normalize().multiply(1.4).setY(0.6);
                living.setVelocity(kb);
                living.damage(8.0);
                living.setFireTicks(100);
            }
        }

        // 2. Destructive Crater Excavation
        int craterRadius = 4;
        for (int x = -craterRadius; x <= craterRadius; x++) {
            for (int y = -3; y <= 2; y++) {
                for (int z = -craterRadius; z <= craterRadius; z++) {
                    double distSq = (x * x) + (y * y * 1.5) + (z * z);
                    if (distSq <= craterRadius * craterRadius) {
                        int blockX = impactLoc.getBlockX() + x;
                        int blockY = impactLoc.getBlockY() + y;
                        int blockZ = impactLoc.getBlockZ() + z;

                        if (!ChunkSafetyGuard.isChunkLoaded(world, blockX, blockZ)) continue;

                        Block b = world.getBlockAt(blockX, blockY, blockZ);
                        if (!b.getType().isAir() && b.getType() != Material.BEDROCK) {
                            if (distSq < (craterRadius - 1.2) * (craterRadius - 1.2)) {
                                // Destroy internal blocks entirely
                                mutateBlock(b, Bukkit.createBlockData(Material.AIR));

                                // Fling occasional falling debris
                                if (ThreadLocalRandom.current().nextDouble() < 0.15) {
                                    FallingBlock debris = world.spawnFallingBlock(b.getLocation().add(0.5, 1.0, 0.5), Bukkit.createBlockData(Material.MAGMA_BLOCK));
                                    debris.setDropItem(false);
                                    debris.setVelocity(new Vector(
                                            ThreadLocalRandom.current().nextDouble(-0.4, 0.4),
                                            ThreadLocalRandom.current().nextDouble(0.4, 0.8),
                                            ThreadLocalRandom.current().nextDouble(-0.4, 0.4)
                                    ));
                                }
                            } else {
                                // Crater rim becomes molten magma, crying obsidian, or netherrack
                                Material rimMat = switch (ThreadLocalRandom.current().nextInt(4)) {
                                    case 0 -> Material.CRYING_OBSIDIAN;
                                    case 1 -> Material.MAGMA_BLOCK;
                                    case 2 -> Material.NETHERRACK;
                                    default -> Material.BLACKSTONE;
                                };
                                mutateBlock(b, Bukkit.createBlockData(rimMat));
                            }
                        }
                    }
                }
            }
        }

        // 3. Spreading Fire Wave: Spreads fire across ground in wide radius
        int fireRadius = 8;
        for (int fx = -fireRadius; fx <= fireRadius; fx += 2) {
            for (int fz = -fireRadius; fz <= fireRadius; fz += 2) {
                if (ThreadLocalRandom.current().nextDouble() < 0.55) {
                    int fireX = impactLoc.getBlockX() + fx;
                    int fireZ = impactLoc.getBlockZ() + fz;

                    if (!ChunkSafetyGuard.isChunkLoaded(world, fireX, fireZ)) continue;

                    int topY = world.getHighestBlockYAt(fireX, fireZ);
                    Block groundBlock = world.getBlockAt(fireX, topY, fireZ);
                    Block above = groundBlock.getLocation().add(0, 1, 0).getBlock();

                    if (above.getType().isAir() && groundBlock.getType().isSolid()) {
                        mutateBlock(above, Bukkit.createBlockData(Material.FIRE));
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
