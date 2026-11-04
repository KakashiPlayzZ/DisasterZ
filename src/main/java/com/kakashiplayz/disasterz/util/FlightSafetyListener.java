package com.kakashiplayz.disasterz.util;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Prevents players from being kicked by vanilla or server anti-flight checks
 * ("Flying is not enabled on this server") while active disasters like Tornados or Earthquakes
 * apply violent vertical velocities or vortex trajectories.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class FlightSafetyListener implements Listener {

    private final DisasterZPlugin plugin;

    public FlightSafetyListener(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        // Check if flying check caused this kick
        String reason = event.getReason();
        boolean isFlightKick = (reason != null && reason.toLowerCase().contains("flying"))
                || event.getCause() == PlayerKickEvent.Cause.FLYING_PLAYER
                || event.getCause() == PlayerKickEvent.Cause.FLYING_VEHICLE;

        if (!isFlightKick) {
            return;
        }

        Player player = event.getPlayer();

        // If any disaster or warning is active in this world, cancel the kick!
        if (plugin.getDisasterManager().getActiveDisaster().isPresent()) {
            event.setCancelled(true);
            player.setAllowFlight(true);
            player.setFallDistance(0);
        }
    }
}

