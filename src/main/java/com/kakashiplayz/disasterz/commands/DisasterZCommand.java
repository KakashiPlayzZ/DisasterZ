package com.kakashiplayz.disasterz.commands;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.gui.DisasterGUI;
import com.kakashiplayz.disasterz.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Command handler for /disasterz suite.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterZCommand implements CommandExecutor {

    private final DisasterZPlugin plugin;

    public DisasterZCommand(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "gui" -> {
                if (!checkPermission(sender, "disasterz.admin.gui")) return true;
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("The GUI command can only be executed by players.");
                    return true;
                }
                new DisasterGUI(plugin).open(player);
                return true;
            }

            case "start" -> {
                if (!checkPermission(sender, "disasterz.admin.start")) return true;
                if (args.length < 2) {
                    TextUtil.sendMessage(sender, "<red>Usage: /disasterz start <disaster> [durationSeconds]</red>");
                    return true;
                }

                DisasterType type = DisasterType.fromString(args[1]);
                if (type == null) {
                    TextUtil.sendMessage(sender, "<red>Unknown disaster type: " + args[1] + ". Use tab completion for list.</red>");
                    return true;
                }

                int duration = -1;
                if (args.length >= 3) {
                    try {
                        duration = Integer.parseInt(args[2]);
                        if (duration <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException ex) {
                        TextUtil.sendMessage(sender, "<red>Duration must be a positive integer in seconds.</red>");
                        return true;
                    }
                }

                boolean started = plugin.getDisasterManager().startDisaster(type, duration, false);
                if (started) {
                    TextUtil.sendMessage(sender, "<green>Successfully triggered warning countdown for <yellow>" + type.getDisplayName() + "</yellow>!</green>");
                } else {
                    TextUtil.sendMessage(sender, "<red>Cannot start disaster: another disaster or warning is already active.</red>");
                }
                return true;
            }

            case "stop" -> {
                if (!checkPermission(sender, "disasterz.admin.stop")) return true;
                boolean stopped = plugin.getDisasterManager().stopActiveDisaster(true);
                if (stopped) {
                    TextUtil.sendMessage(sender, "<green>Stopped active disaster and restored world states.</green>");
                } else {
                    TextUtil.sendMessage(sender, "<yellow>No disaster is currently running.</yellow>");
                }
                return true;
            }

            case "reload" -> {
                if (!checkPermission(sender, "disasterz.admin.reload")) return true;
                plugin.reloadAllConfigurations();
                TextUtil.sendMessage(sender, "<green>DisasterZ configurations and tasks reloaded successfully.</green>");
                return true;
            }

            case "info" -> {
                if (!checkPermission(sender, "disasterz.use")) return true;
                sendInfo(sender);
                return true;
            }

            default -> {
                sendHelp(sender);
                return true;
            }
        }
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission) && !sender.hasPermission("disasterz.admin")) {
            TextUtil.sendMessage(sender, "<red>You do not have permission to execute this command (" + permission + ").</red>");
            return false;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        TextUtil.sendMessage(sender, "<gradient:#ff5555:#aa0000><bold>─── DisasterZ Commands ───</bold></gradient>");
        TextUtil.sendMessage(sender, "<yellow>/disasterz gui</yellow> <gray>─ Open the Master Admin GUI</gray>");
        TextUtil.sendMessage(sender, "<yellow>/disasterz start <disaster> [duration]</yellow> <gray>─ Trigger a disaster</gray>");
        TextUtil.sendMessage(sender, "<yellow>/disasterz stop [disaster]</yellow> <gray>─ Halt active disaster</gray>");
        TextUtil.sendMessage(sender, "<yellow>/disasterz reload</yellow> <gray>─ Reload configs & tasks</gray>");
        TextUtil.sendMessage(sender, "<yellow>/disasterz info</yellow> <gray>─ Plugin details & credits</gray>");
    }

    private void sendInfo(CommandSender sender) {
        TextUtil.sendMessage(sender, "<gradient:#ff5555:#aa0000><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>");
        TextUtil.sendMessage(sender, " <gradient:#ff5555:#aa0000><bold>DisasterZ</bold></gradient> <gray>v1.0.0 by</gray> <yellow><bold>KakashiPlayz</bold></yellow>");
        TextUtil.sendMessage(sender, " <gray>Enterprise Natural Disaster Architecture for Modern Paper/Bukkit</gray>");
        TextUtil.sendMessage(sender, " <yellow>Developer Portfolio:</yellow> <aqua>https://kakashiplayz.vercel.app</aqua>");
        TextUtil.sendMessage(sender, " <yellow>Discord Support:</yellow> <light_purple>https://discord.gg/QdTN4BFkTv</light_purple>");
        TextUtil.sendMessage(sender, "<gradient:#ff5555:#aa0000><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>");
    }
}

