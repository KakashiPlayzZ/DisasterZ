package com.kakashiplayz.disasterz.disaster.impl;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.util.ChunkSafetyGuard;
import com.kakashiplayz.disasterz.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Blizzard: Colossal Arctic squall causing rapid freezing vignette,
 * howling winds, hypothermia frostbite damage, and whiteout conditions
 * unless sheltered within radiant heat zones, while inflicting persistent Slowness
 * and Weakness effects upon players until the disaster concludes.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class BlizzardDisaster extends Disaster {

    private static final Set<Material> HEAT_SOURCES = Set.of(
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.TORCH,
            Material.SOUL_TORCH,
            Material.WALL_TORCH,
            Material.SOUL_WALL_TORCH,
            Material.LANTERN,
            Material.SOUL_LANTERN,
            Material.LAVA,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.MAGMA_BLOCK
    );

    public BlizzardDisaster(@NotNull DisasterZPlugin plugin) {
        super(plugin, DisasterType.BLIZZARD);
    }

    @Override
    public void onPreWarn() {
    }

    @Override
    public void onStart() {
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().get(0);
        }
        if (world != null) {
            world.setStorm(true);
            world.setThundering(false);

            // Immediately apply persistent Slowness and Weakness to all players in the world
            for (Player player : world.getPlayers()) {
                applyPersistentBlizzardCurses(player);
            }
        }
    }

    @Override
    public void onTick() {
        if (world == null) return;

        for (Player player : world.getPlayers()) {
            Location pLoc = player.getLocation();

            // 1. Maintain Slowness & Weakness continuously until disaster finishes
            if (currentTick % 20 == 0) {
                applyPersistentBlizzardCurses(player);
            }

            // 2. Dense blinding whiteout curtain right around the player's view
            world.spawnParticle(Particle.SNOWFLAKE, pLoc.clone().add(0, 1.6, 0), 30, 2.5, 1.2, 2.5, 0.08);
            world.spawnParticle(Particle.WHITE_ASH, pLoc.clone().add(0, 1.6, 0), 12, 2.0, 1.0, 2.0, 0.04);

            if (currentTick % 15 == 0) {
                plugin.getAudioEngine().playBlizzardAmbience(player);
            }

            // 3. Rapid Freezing Mechanics (ticked every 4 ticks for fast freeze onset)
            if (currentTick % 4 == 0) {
                boolean nearHeat = isNearHeatSource(pLoc, 5);

                if (nearHeat) {
                    // Radiant heat melts the frostbite
                    int currentFreeze = player.getFreezeTicks();
                    if (currentFreeze > 0) {
                        player.setFreezeTicks(Math.max(0, currentFreeze - 30));
                    }
                    TextUtil.sendActionBar(player, "<gradient:#ffaa00:#ff5500>☀ Warmth from nearby heat source protects you</gradient>");
                } else {
                    // Rapidly freeze player (locks freeze ticks at maximum, activating frost screen overlay)
                    int currentFreeze = player.getFreezeTicks();
                    int newFreeze = Math.min(300, currentFreeze + 25);
                    player.setFreezeTicks(newFreeze);

                    if (newFreeze >= 140) {
                        TextUtil.sendActionBar(player, "<gradient:#55ffff:#0055ff>❄ <bold>FREEZING!</bold> Seek a campfire, torch, or lava! ❄</gradient>");
                        if (currentTick % 20 == 0) {
                            player.playSound(pLoc, Sound.BLOCK_POWDER_SNOW_STEP, SoundCategory.PLAYERS, 0.8f, 0.5f);
                        }
                    }

                    // Continuous freeze damage while frozen
                    if (newFreeze >= 200 && currentTick % 20 == 0) {
                        player.damage(2.5); // 1.25 hearts freeze damage
                    }
                }
            }

            // 4. Occasional temporary snow dusting on surface
            if (currentTick % 25 == 0 && ThreadLocalRandom.current().nextDouble() < 0.35) {
                int dustX = pLoc.getBlockX() + ThreadLocalRandom.current().nextInt(-8, 8);
                int dustZ = pLoc.getBlockZ() + ThreadLocalRandom.current().nextInt(-8, 8);
                if (ChunkSafetyGuard.isChunkLoaded(world, dustX, dustZ)) {
                    int topY = world.getHighestBlockYAt(dustX, dustZ);
                    Block groundBlock = world.getBlockAt(dustX, topY, dustZ);
                    Block above = groundBlock.getLocation().add(0, 1, 0).getBlock();
                    if (above.getType().isAir() && groundBlock.getType().isSolid()) {
                        mutateBlock(above, Bukkit.createBlockData(Material.SNOW));
                    }
                }
            }
        }
    }

    private void applyPersistentBlizzardCurses(@NotNull Player player) {
        // Continuous Slowness II and Weakness II refreshed throughout the disaster
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1, false, false, true));
    }

    private boolean isNearHeatSource(Location center, int radius) {
        if (!ChunkSafetyGuard.isChunkLoaded(center)) return false;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int blockX = center.getBlockX() + x;
                    int blockY = center.getBlockY() + y;
                    int blockZ = center.getBlockZ() + z;

                    Block b = ChunkSafetyGuard.getSafeBlock(world, blockX, blockY, blockZ);
                    if (b != null && HEAT_SOURCES.contains(b.getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onEnd() {
        if (world != null) {
            world.setStorm(false);
        }
        removeBlizzardEffects();
    }

    @Override
    public void onCleanup() {
        removeBlizzardEffects();
    }

    private void removeBlizzardEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setFreezeTicks(0);
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            player.removePotionEffect(PotionEffectType.WEAKNESS);
        }
    }
}
