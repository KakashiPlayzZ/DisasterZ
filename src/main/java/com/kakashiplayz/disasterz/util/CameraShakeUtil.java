package com.kakashiplayz.disasterz.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * High-immersion screen shake and seismic tremor utilities.
 * Delivers authentic screen wobble through client distortion shaders,
 * subtle horizontal ground sliding, and ground dust bursts WITHOUT freezing player movement
 * or triggering vanilla anti-flight kicks.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public final class CameraShakeUtil {

    private CameraShakeUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Applies genuine screen tremor and ground stumbling without locking WASD movement.
     *
     * @param player    The player experiencing the tremor.
     * @param intensity 0.0 (mild) to 1.0 (violent).
     */
    public static void applyCameraTremor(@NotNull Player player, double intensity) {
        if (!player.isOnline() || player.isDead()) return;

        // 1. Nausea pulse activates client-side screen distortion shader
        // Creates authentic wobbling screen effect without freezing the player's walking or camera controls
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 50, 0, false, false, false));

        // 2. Mild slowness for physical stumbling feeling (can still walk & sprint)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0, false, false, false));

        // 3. Subtle horizontal ground slip (ONLY on ground, never vertical, preventing flight kicks)
        if (player.isOnGround()) {
            double horizSlip = 0.035 * intensity;
            double randX = ThreadLocalRandom.current().nextDouble(-horizSlip, horizSlip);
            double randZ = ThreadLocalRandom.current().nextDouble(-horizSlip, horizSlip);

            Vector current = player.getVelocity();
            player.setVelocity(current.add(new Vector(randX, 0.0, randZ)));

            // Visual ground crack dust popping under feet
            player.getWorld().spawnParticle(Particle.DUST_PLUME, player.getLocation().add(0, 0.1, 0), 3, 0.3, 0.05, 0.3, 0.02);
        }
    }

    /**
     * Applies radial camera tremor to all players within radius of a focal location.
     */
    public static void applyRadialTremor(@NotNull Location epicenter, double maxRadius, double peakIntensity) {
        if (epicenter.getWorld() == null) return;

        for (Player player : epicenter.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(epicenter);
            if (distance <= maxRadius) {
                double falloff = 1.0 - (distance / maxRadius);
                applyCameraTremor(player, peakIntensity * falloff);
            }
        }
    }
}
