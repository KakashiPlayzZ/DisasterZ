package com.kakashiplayz.disasterz.disaster.impl;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.util.CameraShakeUtil;
import com.kakashiplayz.disasterz.util.ChunkSafetyGuard;
import com.kakashiplayz.disasterz.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Zombie Apocalypse: Full Blood Moon atmospheric sky transformation,
 * towering Big Size Colossal Zombies (scaled via Attribute.GENERIC_SCALE),
 * terrifying Giant Dreadnought bosses, and aggressive undead hordes.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class ZombieApocalypseDisaster extends Disaster {

    private final Particle.DustOptions bloodMoonDust = new Particle.DustOptions(Color.fromRGB(230, 20, 20), 1.5f);
    private final List<LivingEntity> spawnedApocalypseMobs = new ArrayList<>();
    private boolean giantBossSpawned = false;

    private static final Attribute GENERIC_SCALE = resolveAttribute("GENERIC_SCALE", "SCALE");
    private static final Attribute GENERIC_MAX_HEALTH = resolveAttribute("GENERIC_MAX_HEALTH", "MAX_HEALTH");
    private static final Attribute GENERIC_ATTACK_DAMAGE = resolveAttribute("GENERIC_ATTACK_DAMAGE", "ATTACK_DAMAGE");
    private static final Attribute GENERIC_STEP_HEIGHT = resolveAttribute("GENERIC_STEP_HEIGHT", "STEP_HEIGHT");

    public ZombieApocalypseDisaster(@NotNull DisasterZPlugin plugin) {
        super(plugin, DisasterType.ZOMBIE_APOCALYPSE);
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
            world.setTime(18000); // Midnight Blood Moon
        }
        giantBossSpawned = false;
        spawnedApocalypseMobs.clear();

        // Blood Moon Announcement
        TextUtil.broadcastTitle(
                "<red><bold>☠ BLOOD MOON RISES ☠</bold></red>",
                "<dark_red>Colossal undead titans emerge from the dark!</dark_red>",
                10, 60, 20
        );
        TextUtil.broadcast("<gradient:#550000:#ff0000><bold>[!] THE BLOOD MOON HAS RISEN!</bold></gradient> <red>The sky glows crimson and gigantic mutated zombies walk the earth!</red>");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.WEATHER, 1.0f, 0.6f);
            p.playSound(p.getLocation(), Sound.AMBIENT_CRIMSON_FOREST_MOOD, SoundCategory.AMBIENT, 1.2f, 0.5f);
        }
    }

    @Override
    public void onTick() {
        if (world == null) return;

        // 1. Keep midnight locked for the Blood Moon
        if (currentTick % 60 == 0) {
            world.setTime(18000);
        }

        // 2. Atmospheric Blood Moon Particle Sky Storm & Actionbar
        for (Player player : world.getPlayers()) {
            Location pLoc = player.getLocation();

            // Raining crimson blood mist from the sky
            world.spawnParticle(Particle.DUST, pLoc.clone().add(0, 4, 0), 20, 10, 3, 10, bloodMoonDust);

            if (currentTick % 10 == 0) {
                TextUtil.sendActionBar(player, "<gradient:#ff0000:#880000><bold>☠ BLOOD MOON</bold></gradient> <gray>| Colossal Undead Swarm The Land</gray>");
            }

            if (currentTick % 40 == 0) {
                player.playSound(pLoc, Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.8f, 0.4f);
            }
        }

        // 3. Spawn waves of Regular and Big-Size Colossal Zombies every 25 ticks
        if (currentTick % 25 == 0) {
            for (Player player : world.getPlayers()) {
                if (ThreadLocalRandom.current().nextDouble() < 0.8) {
                    boolean spawnColossal = ThreadLocalRandom.current().nextDouble() < 0.4;
                    spawnZombie(player.getLocation(), spawnColossal);
                }
            }
        }

        // 4. Spawn Giant Zombie Boss Encounter halfway through disaster
        if (!giantBossSpawned && remainingSeconds <= (totalDurationSeconds / 2)) {
            spawnApocalypseGiantBoss();
            giantBossSpawned = true;
        }

        // Clean dead mobs from memory
        spawnedApocalypseMobs.removeIf(e -> e.isDead() || !e.isValid());
    }

    private void spawnZombie(Location nearPlayer, boolean isColossal) {
        double offsetX = ThreadLocalRandom.current().nextDouble(-18, 18);
        double offsetZ = ThreadLocalRandom.current().nextDouble(-18, 18);
        Location spawnLoc = nearPlayer.clone().add(offsetX, 0, offsetZ);

        if (!ChunkSafetyGuard.isChunkLoaded(spawnLoc)) return;

        int topY = world.getHighestBlockYAt(spawnLoc.getBlockX(), spawnLoc.getBlockZ());
        spawnLoc.setY(topY + 1);

        Zombie zombie = (Zombie) world.spawnEntity(spawnLoc, EntityType.ZOMBIE);
        zombie.setCustomNameVisible(true);
        zombie.setGlowing(true);

        EntityEquipment eq = zombie.getEquipment();

        if (isColossal) {
            // Big Size Zombie (Scale = 2.8x to 3.5x size!)
            double scale = ThreadLocalRandom.current().nextDouble(2.6, 3.4);
            if (GENERIC_SCALE != null) {
                AttributeInstance scaleAttr = zombie.getAttribute(GENERIC_SCALE);
                if (scaleAttr != null) {
                    scaleAttr.setBaseValue(scale);
                }
            }
            if (GENERIC_STEP_HEIGHT != null) {
                AttributeInstance stepAttr = zombie.getAttribute(GENERIC_STEP_HEIGHT);
                if (stepAttr != null) {
                    stepAttr.setBaseValue(2.0); // Big zombies easily step over blocks
                }
            }
            if (GENERIC_MAX_HEALTH != null) {
                AttributeInstance healthAttr = zombie.getAttribute(GENERIC_MAX_HEALTH);
                if (healthAttr != null) {
                    healthAttr.setBaseValue(120.0);
                    zombie.setHealth(120.0);
                }
            }
            if (GENERIC_ATTACK_DAMAGE != null) {
                AttributeInstance dmgAttr = zombie.getAttribute(GENERIC_ATTACK_DAMAGE);
                if (dmgAttr != null) {
                    dmgAttr.setBaseValue(14.0);
                }
            }

            zombie.customName(TextUtil.parse("<gradient:#ff0000:#550000><bold>☠ COLOSSAL TITAN ZOMBIE ☠</bold></gradient>"));
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 1, false, false, true));
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 99999, 1, false, false, true));

            if (eq != null) {
                ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
                ItemStack chestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
                sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);

                eq.setHelmet(helmet);
                eq.setChestplate(chestplate);
                eq.setItemInMainHand(sword);
                eq.setHelmetDropChance(0.0f);
                eq.setChestplateDropChance(0.0f);
                eq.setItemInMainHandDropChance(0.0f);
            }

            // Ground smash effect upon rising
            world.spawnParticle(Particle.EXPLOSION, spawnLoc, 2);
            world.playSound(spawnLoc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.2f, 0.5f);
        } else {
            // Standard Blood Moon Stalker
            zombie.customName(TextUtil.parse("<gradient:#aa0000:#ff5555><bold>Bloodborne Stalker</bold></gradient>"));
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 1, false, false, true));

            if (eq != null) {
                eq.setHelmet(new ItemStack(Material.IRON_HELMET));
                eq.setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                eq.setHelmetDropChance(0.0f);
                eq.setItemInMainHandDropChance(0.0f);
            }
        }

        spawnedApocalypseMobs.add(zombie);
    }

    private void spawnApocalypseGiantBoss() {
        Player target = world.getPlayers().stream().findFirst().orElse(null);
        Location spawnLoc = (target != null) ? target.getLocation().clone().add(15, 0, 15) : world.getSpawnLocation().clone();

        if (!ChunkSafetyGuard.isChunkLoaded(spawnLoc)) return;
        int topY = world.getHighestBlockYAt(spawnLoc.getBlockX(), spawnLoc.getBlockZ());
        spawnLoc.setY(topY + 1);

        Giant giant = (Giant) world.spawnEntity(spawnLoc, EntityType.GIANT);
        giant.customName(TextUtil.parse("<gradient:#550000:#ff0000><bold>☠ APOCALYPSE COLOSSUS BOSS ☠</bold></gradient>"));
        giant.setCustomNameVisible(true);
        giant.setGlowing(true);

        if (GENERIC_MAX_HEALTH != null) {
            AttributeInstance hp = giant.getAttribute(GENERIC_MAX_HEALTH);
            if (hp != null) {
                hp.setBaseValue(300.0);
                giant.setHealth(300.0);
            }
        }

        // Ground smash impact and sound
        world.spawnParticle(Particle.EXPLOSION_EMITTER, spawnLoc, 4);
        plugin.getAudioEngine().playEruptiveBlast(spawnLoc, 2.5f);
        CameraShakeUtil.applyRadialTremor(spawnLoc, 35.0, 1.0);

        TextUtil.broadcast("<dark_red><bold>[!] AN APOCALYPSE COLOSSUS HAS RISEN FROM THE EARTH!</bold></dark_red>");
        spawnedApocalypseMobs.add(giant);
    }

    @Nullable
    private static Attribute resolveAttribute(String... names) {
        for (String name : names) {
            try {
                return Attribute.valueOf(name);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    @Override
    public void onEnd() {
        for (LivingEntity mob : spawnedApocalypseMobs) {
            if (mob.isValid()) {
                mob.remove();
            }
        }
        spawnedApocalypseMobs.clear();
    }

    @Override
    public void onCleanup() {
        spawnedApocalypseMobs.clear();
    }
}
