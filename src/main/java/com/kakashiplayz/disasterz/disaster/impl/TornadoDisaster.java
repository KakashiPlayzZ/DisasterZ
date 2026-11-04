package com.kakashiplayz.disasterz.disaster.impl;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.util.AsyncMathEngine;
import com.kakashiplayz.disasterz.util.CameraShakeUtil;
import com.kakashiplayz.disasterz.util.ChunkSafetyGuard;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tornado: Colossal rotating vortex funnel that vacuums players and mobs inside,
 * spins them along double-helix trajectories, and violently hurls them across the sky,
 * with comprehensive flight-safety safeguards against kick exploits.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class TornadoDisaster extends Disaster {

    private Location tornadoCenter;
    private Vector wanderVelocity;
    private double currentAngle = 0.0;
    private final double funnelHeight = 32.0;
    private final double baseRadius = 2.5;
    private final double topRadius = 15.0;
    private final double suctionRadius = 20.0;
    private final double eyeRadius = 7.0;

    private final Set<Player> airbornePlayers = new HashSet<>();

    public TornadoDisaster(@NotNull DisasterZPlugin plugin) {
        super(plugin, DisasterType.TORNADO);
    }

    @Override
    public void onPreWarn() {
    }

    @Override
    public void onStart() {
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().get(0);
        }
        if (world == null) return;

        Player target = world.getPlayers().stream().findFirst().orElse(null);
        if (target != null) {
            tornadoCenter = target.getLocation().clone().add(15, 0, 15);
        } else {
            tornadoCenter = world.getSpawnLocation().clone();
        }

        wanderVelocity = new Vector(
                ThreadLocalRandom.current().nextDouble(-0.35, 0.35),
                0,
                ThreadLocalRandom.current().nextDouble(-0.35, 0.35)
        );
        airbornePlayers.clear();
    }

    @Override
    public void onTick() {
        if (world == null || tornadoCenter == null) return;

        // Roam dynamically across terrain
        tornadoCenter.add(wanderVelocity);

        // Ground lock Y to surface
        if (ChunkSafetyGuard.isChunkLoaded(tornadoCenter)) {
            int groundY = world.getHighestBlockYAt(tornadoCenter.getBlockX(), tornadoCenter.getBlockZ());
            tornadoCenter.setY(groundY);
        }

        // Steer towards nearby players periodically
        if (currentTick % 40 == 0) {
            Player nearest = findNearestPlayer(tornadoCenter);
            if (nearest != null) {
                Vector toPlayer = nearest.getLocation().toVector().subtract(tornadoCenter.toVector()).setY(0).normalize().multiply(0.28);
                wanderVelocity = toPlayer;
            }
        }

        // Advance vortex angle rotation
        currentAngle += 0.5;
        if (currentAngle > Math.PI * 2) {
            currentAngle -= Math.PI * 2;
        }

        // 1. Audio Ambiance
        if (currentTick % 10 == 0) {
            plugin.getAudioEngine().playTornadoVortexSound(tornadoCenter, 2.0f);
        }

        // 2. Double-Helix Particle Funnel Calculation
        AsyncMathEngine.calculateTornadoFunnelAsync(funnelHeight, baseRadius, topRadius, currentAngle, 2)
                .thenAccept(points -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!isActive()) return;
                        renderFunnelParticles(points);
                    });
                });

        // 3. Entity Suction, Swirl & Ejection Physics
        applyVortexPhysics();

        // 4. Tear up ground blocks into swirling rubble
        if (currentTick % 3 == 0) {
            tearSurfaceBlocks();
        }

        // 5. Clean up safely landed players
        cleanupLandedPlayers();
    }

    private void renderFunnelParticles(List<AsyncMathEngine.VectorPoint> points) {
        for (AsyncMathEngine.VectorPoint pt : points) {
            Location loc = pt.toLocation(tornadoCenter);
            if (ChunkSafetyGuard.isChunkLoaded(loc)) {
                world.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, loc, 1, 0.1, 0.1, 0.1, 0.01);
                world.spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0.05, 0.05, 0.05, 0.0);
                if (ThreadLocalRandom.current().nextDouble() < 0.3) {
                    world.spawnParticle(Particle.CLOUD, loc, 1, 0.2, 0.2, 0.2, 0.02);
                }
            }
        }
    }

    private void applyVortexPhysics() {
        BoundingBox box = BoundingBox.of(tornadoCenter, suctionRadius, funnelHeight + 8, suctionRadius);
        for (Entity entity : ChunkSafetyGuard.getSafeNearbyEntities(world, box)) {
            if (!(entity instanceof LivingEntity living)) continue;

            Location entityLoc = living.getLocation();
            Vector toCenter = tornadoCenter.toVector().subtract(entityLoc.toVector()).setY(0);
            double horizontalDist = toCenter.length();

            if (horizontalDist > suctionRadius) continue;

            // Safe flight protection for players
            if (living instanceof Player player) {
                player.setAllowFlight(true);
                player.setFallDistance(0);
                airbornePlayers.add(player);
                CameraShakeUtil.applyCameraTremor(player, 0.65);
            }

            double relativeY = entityLoc.getY() - tornadoCenter.getY();

            // ZONE 1: TOP EJECTION ZONE (Y >= 18 blocks above base) -> THROW OUTWARD!
            if (relativeY >= 18.0) {
                Vector launchDir = entityLoc.toVector().subtract(tornadoCenter.toVector()).setY(0).normalize();
                if (launchDir.lengthSquared() < 0.01) {
                    launchDir = new Vector(1, 0, 0);
                }
                Vector flingVelocity = launchDir.multiply(2.2).setY(0.75);
                living.setVelocity(flingVelocity);

                // Play violent wind whip sound
                living.getWorld().playSound(entityLoc, Sound.ENTITY_PHANTOM_SWOOP, SoundCategory.WEATHER, 1.5f, 0.6f);
                living.getWorld().playSound(entityLoc, Sound.ITEM_TRIDENT_RIPTIDE_3, SoundCategory.WEATHER, 1.2f, 0.7f);
                world.spawnParticle(Particle.EXPLOSION, entityLoc, 2);

                if (living instanceof Player p) {
                    // Brief slow falling so they survive the massive fling
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 160, 0, false, false, false));
                }
            }
            // ZONE 2: INSIDE THE VORTEX EYE (horizontalDist <= eyeRadius) -> SPIRAL UPWARD!
            else if (horizontalDist <= eyeRadius) {
                // Rotational tangential vector
                Vector tangent = new Vector(-toCenter.getZ(), 0, toCenter.getX()).normalize().multiply(0.9);
                // Inward attraction keeping them bound to the funnel
                Vector inward = toCenter.clone().normalize().multiply(0.35);
                // Powerful vertical lift ascending the cyclone
                Vector vortexLift = tangent.add(inward).setY(0.72);

                living.setVelocity(vortexLift);
                living.damage(1.0); // Vortex debris damage
            }
            // ZONE 3: OUTER SUCTION ZONE (eyeRadius < horizontalDist <= suctionRadius) -> PULL TO EYE!
            else {
                double pullStrength = 1.0 - (horizontalDist / suctionRadius);
                Vector pullVec = toCenter.clone().normalize().multiply(0.75 * pullStrength).setY(0.35);
                living.setVelocity(living.getVelocity().add(pullVec));
            }
        }
    }

    private void tearSurfaceBlocks() {
        int radius = 5;
        for (int x = -radius; x <= radius; x += 2) {
            for (int z = -radius; z <= radius; z += 2) {
                int blockX = tornadoCenter.getBlockX() + x;
                int blockZ = tornadoCenter.getBlockZ() + z;

                if (!ChunkSafetyGuard.isChunkLoaded(world, blockX, blockZ)) continue;

                int topY = world.getHighestBlockYAt(blockX, blockZ);
                Block block = world.getBlockAt(blockX, topY, blockZ);

                Material type = block.getType();
                if (type.isAir()) continue;

                if (type.name().contains("LEAVES") || type.name().contains("GRASS") || type.name().contains("FLOWER") || type == Material.DIRT || type == Material.SAND) {
                    if (mutateBlock(block, Bukkit.createBlockData(Material.AIR))) {
                        FallingBlock debris = world.spawnFallingBlock(block.getLocation().add(0.5, 1.0, 0.5), Bukkit.createBlockData(type));
                        debris.setDropItem(false);
                        debris.setVelocity(new Vector(
                                ThreadLocalRandom.current().nextDouble(-0.4, 0.4),
                                0.85,
                                ThreadLocalRandom.current().nextDouble(-0.4, 0.4)
                        ));
                    }
                }
            }
        }
    }

    private void cleanupLandedPlayers() {
        airbornePlayers.removeIf(player -> {
            if (!player.isOnline()) return true;
            // When player touches ground away from tornado, revert allowFlight
            if (player.isOnGround()) {
                double dist = player.getLocation().distance(tornadoCenter);
                if (dist > suctionRadius + 5) {
                    if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                        player.setAllowFlight(false);
                    }
                    player.setFallDistance(0);
                    return true;
                }
            }
            return false;
        });
    }

    private Player findNearestPlayer(Location loc) {
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : world.getPlayers()) {
            double dist = p.getLocation().distance(loc);
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    @Override
    public void onEnd() {
        for (Player player : airbornePlayers) {
            if (player.isOnline() && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                player.setAllowFlight(false);
            }
        }
        airbornePlayers.clear();
    }

    @Override
    public void onCleanup() {
        airbornePlayers.clear();
        tornadoCenter = null;
    }
}
