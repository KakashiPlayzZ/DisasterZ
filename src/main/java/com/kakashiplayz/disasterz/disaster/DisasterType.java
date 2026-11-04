package com.kakashiplayz.disasterz.disaster;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Enumeration of all supported natural disasters in DisasterZ.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public enum DisasterType {

    TSUNAMI("tsunami", "Tsunami", Material.HEART_OF_THE_SEA, "<aqua>Massive moving wall of water with extreme knockback and drowning hazard.</aqua>"),
    EARTHQUAKE("earthquake", "Earthquake", Material.CRACKED_STONE_BRICKS, "<gold>Severe ground fissures, screen tremors, and structural destabilization.</gold>"),
    TORNADO("tornado", "Tornado", Material.WIND_CHARGE, "<gray>Violent double-helix vortex hurling players and blocks into the sky.</gray>"),
    BLIZZARD("blizzard", "Blizzard", Material.POWDER_SNOW_BUCKET, "<white>Freezing squall inflicting hypothermia and freezing vision unless sheltered near heat.</white>"),
    VOLCANO("volcano", "Volcanic Eruption", Material.MAGMA_BLOCK, "<red>Ash clouds choke the atmosphere while magma bombs and lava streams ignite the land.</red>"),
    METEOR_SHOWER("meteor_shower", "Meteor Shower", Material.FIRE_CHARGE, "<dark_red>High-velocity fiery meteors rain from space detonating in massive fiery craters.</dark_red>"),
    ZOMBIE_APOCALYPSE("zombie_apocalypse", "Zombie Apocalypse", Material.ZOMBIE_HEAD, "<dark_purple>Blood moon sky tint, colossal giant zombies, and relentless undead hordes.</dark_purple>");

    private final String configKey;
    private final String displayName;
    private final Material icon;
    private final String description;

    DisasterType(@NotNull String configKey, @NotNull String displayName, @NotNull Material icon, @NotNull String description) {
        this.configKey = configKey;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    @NotNull
    public String getConfigKey() {
        return configKey;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public Material getIcon() {
        return icon;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    public static DisasterType fromString(@NotNull String name) {
        for (DisasterType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.configKey.equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
