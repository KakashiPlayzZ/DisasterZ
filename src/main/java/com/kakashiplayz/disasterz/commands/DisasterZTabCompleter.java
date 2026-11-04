package com.kakashiplayz.disasterz.commands;

import com.kakashiplayz.disasterz.disaster.DisasterType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Intelligent Tab Completer for /disasterz commands.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterZTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("gui", "start", "stop", "reload", "info");
    private static final List<String> DURATION_PRESETS = List.of("60", "120", "180", "300", "600");

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completions);
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            List<String> disasterNames = new ArrayList<>();
            for (DisasterType type : DisasterType.values()) {
                disasterNames.add(type.getConfigKey());
            }
            StringUtil.copyPartialMatches(args[1], disasterNames, completions);
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("start")) {
            StringUtil.copyPartialMatches(args[2], DURATION_PRESETS, completions);
            return completions;
        }

        return completions;
    }
}

