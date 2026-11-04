package com.kakashiplayz.disasterz.audio;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Cinematic multi-layered audio engine.
 * Crafts immersive soundscapes using synchronized ambient rumbles, pitch shifting,
 * distance falloff, and escalating tension chimes.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class CinematicAudioEngine {

    private final DisasterZPlugin plugin;

    public CinematicAudioEngine(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Plays layered seismic ground rumble across a world or near a location.
     */
    public void playSeismicRumble(@NotNull Location location, float volume) {
        if (location.getWorld() == null) return;

        float lowPitch = (float) ThreadLocalRandom.current().nextDouble(0.4, 0.6);
        float subPitch = (float) ThreadLocalRandom.current().nextDouble(0.5, 0.7);

        // Thunder rumbling at ultra-low pitch creates earthquake tectonic grinding
        location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, volume * 0.8f, lowPitch);
        // Cave ambient delivers eerie atmospheric tension
        location.getWorld().playSound(location, Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, volume * 1.0f, subPitch);
        // Beacon deactivation provides mechanical tectonic hum
        location.getWorld().playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.AMBIENT, volume * 0.6f, 0.5f);
    }

    /**
     * Broadcasts atmospheric warning chime with escalating pitch.
     *
     * @param remainingSeconds Time left in countdown.
     * @param totalWarningSeconds Total countdown duration.
     */
    public void playWarningChime(int remainingSeconds, int totalWarningSeconds) {
        // Escalate pitch from 0.6 up to 2.0 as seconds tick down
        double progress = 1.0 - ((double) remainingSeconds / (double) Math.max(1, totalWarningSeconds));
        float pitch = (float) (0.6 + (progress * 1.4));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 0.9f, pitch);
            if (remainingSeconds <= 5) {
                // Heartbeat / Bass drum emphasis in final 5 seconds
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.MASTER, 1.2f, 0.8f);
            }
        }
    }

    /**
     * Plays an eruptive explosion soundscape (Volcano, Meteor impact).
     */
    public void playEruptiveBlast(@NotNull Location location, float volume) {
        if (location.getWorld() == null) return;
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.WEATHER, volume, 0.6f);
        location.getWorld().playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.WEATHER, volume * 0.8f, 0.7f);
        location.getWorld().playSound(location, Sound.ITEM_TRIDENT_THUNDER, SoundCategory.WEATHER, volume * 0.7f, 0.5f);
    }

    /**
     * Plays blizzard freezing wind soundscape.
     */
    public void playBlizzardAmbience(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, SoundCategory.AMBIENT, 0.25f, 0.4f);
        player.playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_STEP, SoundCategory.AMBIENT, 0.35f, 0.6f);
    }

    /**
     * Plays roaring tsunami water deluge sound.
     */
    public void playDelugeAmbience(@NotNull Location location, float volume) {
        if (location.getWorld() == null) return;
        location.getWorld().playSound(location, Sound.ITEM_BUCKET_EMPTY_AXOLOTL, SoundCategory.WEATHER, volume, 0.5f);
        location.getWorld().playSound(location, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, SoundCategory.WEATHER, volume, 0.6f);
    }

    /**
     * Plays tornado vortex howling wind sound.
     */
    public void playTornadoVortexSound(@NotNull Location location, float volume) {
        if (location.getWorld() == null) return;
        location.getWorld().playSound(location, Sound.ENTITY_PHANTOM_SWOOP, SoundCategory.WEATHER, volume * 0.7f, 0.5f);
        location.getWorld().playSound(location, Sound.ITEM_ELYTRA_FLYING, SoundCategory.WEATHER, volume * 0.8f, 0.6f);
    }
}

