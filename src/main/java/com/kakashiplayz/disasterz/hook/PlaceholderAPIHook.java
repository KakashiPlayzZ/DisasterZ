package com.kakashiplayz.disasterz.hook;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.util.TextUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * PlaceholderAPI expansion offering real-time disaster status variables.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final DisasterZPlugin plugin;

    public PlaceholderAPIHook(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "disasterz";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "KakashiPlayz";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    @Nullable
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        Optional<Disaster> activeOpt = plugin.getDisasterManager().getActiveDisaster();

        switch (params.toLowerCase()) {
            case "active" -> {
                if (activeOpt.isPresent()) {
                    return activeOpt.get().getType().getDisplayName();
                } else if (plugin.getWarningManager().isWarningActive() && plugin.getWarningManager().getPendingDisaster() != null) {
                    return plugin.getWarningManager().getPendingDisaster().getType().getDisplayName() + " (Warning)";
                }
                return "None";
            }

            case "time_left" -> {
                if (activeOpt.isPresent()) {
                    return TextUtil.formatTime(activeOpt.get().getRemainingSeconds());
                }
                return "00:00";
            }

            case "time_left_seconds" -> {
                if (activeOpt.isPresent()) {
                    return String.valueOf(activeOpt.get().getRemainingSeconds());
                }
                return "0";
            }

            case "next_disaster" -> {
                DisasterType next = plugin.getDisasterScheduler().getNextScheduledDisaster();
                return (next != null) ? next.getDisplayName() : "Unknown";
            }

            case "next_countdown" -> {
                int secs = plugin.getDisasterScheduler().getSecondsUntilNext();
                return TextUtil.formatTime(secs);
            }

            case "warning_countdown" -> {
                if (plugin.getWarningManager().isWarningActive()) {
                    return String.valueOf(plugin.getWarningManager().getRemainingWarningSeconds());
                }
                return "0";
            }

            default -> {
                return null;
            }
        }
    }
}

