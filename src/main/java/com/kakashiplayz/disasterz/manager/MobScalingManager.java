package com.kakashiplayz.disasterz.manager;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Intercepts mob spawns during active disasters to apply dynamic attribute scaling,
 * custom armaments, glowing auras, and MiniMessage titles.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class MobScalingManager implements Listener {

    private final DisasterZPlugin plugin;

    private static final Attribute MAX_HEALTH = resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");
    private static final Attribute MOVEMENT_SPEED = resolveAttribute("MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED");
    private static final Attribute ATTACK_DAMAGE = resolveAttribute("ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE");

    public MobScalingManager(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Only scale natural/wild spawns during an active disaster
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        Optional<Disaster> activeDisasterOpt = plugin.getDisasterManager().getActiveDisaster();
        if (activeDisasterOpt.isEmpty()) return;

        Disaster active = activeDisasterOpt.get();
        DisasterType type = active.getType();

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster)) return;

        // Apply attribute multipliers from config
        double healthMultiplier = plugin.getConfigManager().getMobHealthMultiplier(type);
        double damageMultiplier = plugin.getConfigManager().getMobDamageMultiplier(type);
        double speedMultiplier = plugin.getConfigManager().getMobSpeedMultiplier(type);

        if (MAX_HEALTH != null) {
            AttributeInstance healthAttr = entity.getAttribute(MAX_HEALTH);
            if (healthAttr != null) {
                double newHealth = healthAttr.getBaseValue() * healthMultiplier;
                healthAttr.setBaseValue(newHealth);
                entity.setHealth(newHealth);
            }
        }

        if (ATTACK_DAMAGE != null) {
            AttributeInstance damageAttr = entity.getAttribute(ATTACK_DAMAGE);
            if (damageAttr != null) {
                damageAttr.setBaseValue(damageAttr.getBaseValue() * damageMultiplier);
            }
        }

        if (MOVEMENT_SPEED != null) {
            AttributeInstance speedAttr = entity.getAttribute(MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.setBaseValue(speedAttr.getBaseValue() * speedMultiplier);
            }
        }

        // Apply visual polish: glowing and title
        entity.setGlowing(true);
        String mobTitle = "<gradient:#ff5555:#aa0000><bold>Empowered " + entity.getType().name() + "</bold></gradient>";
        entity.customName(TextUtil.parse(mobTitle));
        entity.setCustomNameVisible(true);

        // Equip reinforced armor & weapons
        EntityEquipment eq = entity.getEquipment();
        if (eq != null) {
            if (ThreadLocalRandom.current().nextDouble() < 0.4) {
                eq.setHelmet(new ItemStack(Material.IRON_HELMET));
                eq.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
                eq.setItemInMainHand(sword);

                eq.setHelmetDropChance(0.05f);
                eq.setChestplateDropChance(0.05f);
                eq.setItemInMainHandDropChance(0.05f);
            }
        }
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
}

