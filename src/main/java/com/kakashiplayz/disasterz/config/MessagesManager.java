package com.kakashiplayz.disasterz.config;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages messages.yml loading and formatted placeholder replacement.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class MessagesManager {

    private final DisasterZPlugin plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private String prefix = "<gradient:#ff5555:#aa0000><bold>DisasterZ</bold></gradient> <dark_gray>»</dark_gray> ";

    public MessagesManager(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Load defaults from jar if missing
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messagesConfig.setDefaults(defaultConfig);
        }

        this.prefix = messagesConfig.getString("prefix", "<gradient:#ff5555:#aa0000><bold>DisasterZ</bold></gradient> <dark_gray>»</dark_gray> ");
    }

    @NotNull
    public String getRawMessage(@NotNull String key, @NotNull String fallback) {
        if (messagesConfig == null) return fallback;
        return messagesConfig.getString(key, fallback);
    }

    @NotNull
    public String getFormattedString(@NotNull String key, @NotNull String fallback, @NotNull Map<String, String> placeholders) {
        String raw = getRawMessage(key, fallback);
        raw = raw.replace("%prefix%", prefix);
        for (var entry : placeholders.entrySet()) {
            raw = raw.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return raw;
    }

    @NotNull
    public Component getComponent(@NotNull String key, @NotNull String fallback, @NotNull Map<String, String> placeholders) {
        String formatted = getFormattedString(key, fallback, placeholders);
        return TextUtil.parse(formatted);
    }

    public void send(@NotNull CommandSender sender, @NotNull String key, @NotNull String fallback, @NotNull Map<String, String> placeholders) {
        sender.sendMessage(getComponent(key, fallback, placeholders));
    }

    public void send(@NotNull CommandSender sender, @NotNull String key, @NotNull String fallback) {
        send(sender, key, fallback, new HashMap<>());
    }

    public void broadcast(@NotNull String key, @NotNull String fallback, @NotNull Map<String, String> placeholders) {
        Component comp = getComponent(key, fallback, placeholders);
        TextUtil.broadcast(getFormattedString(key, fallback, placeholders));
    }

    @NotNull
    public String getPrefix() {
        return prefix;
    }
}

