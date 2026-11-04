package com.kakashiplayz.disasterz.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text & MiniMessage formatting utilities with automatic legacy color code fallback.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");

    private TextUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Parses a message string into a Kyori Adventure Component.
     * Supports MiniMessage tags (<gradient:#ff5555:#aa0000>, <bold>, etc.)
     * and automatically converts legacy '&' and '&#RRGGBB' color codes.
     */
    @NotNull
    public static Component parse(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String prepared = convertLegacyToMiniMessage(text);
        try {
            return MINI_MESSAGE.deserialize(prepared);
        } catch (Exception ex) {
            // Fallback to legacy serializer if MiniMessage parsing fails on unclosed tags
            return LEGACY_SERIALIZER.deserialize(text);
        }
    }

    /**
     * Converts legacy formatting codes (&a, &c, &l, &#RRGGBB) to MiniMessage syntax.
     */
    @NotNull
    public static String convertLegacyToMiniMessage(@NotNull String input) {
        // First convert hex &#RRGGBB to <#RRGGBB>
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(buffer);
        String result = buffer.toString();

        // Convert standard & codes to MiniMessage tags
        result = result
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");

        return result;
    }

    /**
     * Sends a parsed message to a command sender.
     */
    public static void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(parse(message));
    }

    /**
     * Sends a parsed action bar message to a player.
     */
    public static void sendActionBar(@NotNull Player player, @NotNull String message) {
        player.sendActionBar(parse(message));
    }

    /**
     * Broadcasts an action bar to all online players.
     */
    public static void broadcastActionBar(@NotNull String message) {
        Component component = parse(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(component);
        }
    }

    /**
     * Sends a cinematic title and subtitle to a player.
     */
    public static void sendTitle(@NotNull Player player, @NotNull String title, @NotNull String subtitle,
                                 long fadeInTicks, long stayTicks, long fadeOutTicks) {
        Title adventureTitle = Title.title(
                parse(title),
                parse(subtitle),
                Title.Times.times(
                        Duration.ofMillis(fadeInTicks * 50),
                        Duration.ofMillis(stayTicks * 50),
                        Duration.ofMillis(fadeOutTicks * 50)
                )
        );
        player.showTitle(adventureTitle);
    }

    /**
     * Broadcasts a cinematic title and subtitle to all online players.
     */
    public static void broadcastTitle(@NotNull String title, @NotNull String subtitle,
                                      long fadeInTicks, long stayTicks, long fadeOutTicks) {
        Title adventureTitle = Title.title(
                parse(title),
                parse(subtitle),
                Title.Times.times(
                        Duration.ofMillis(fadeInTicks * 50),
                        Duration.ofMillis(stayTicks * 50),
                        Duration.ofMillis(fadeOutTicks * 50)
                )
        );
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(adventureTitle);
        }
    }

    /**
     * Broadcasts a parsed MiniMessage to all online players and console.
     */
    public static void broadcast(@NotNull String message) {
        Component component = parse(message);
        Bukkit.broadcast(component);
    }

    /**
     * Formats seconds into mm:ss format.
     */
    @NotNull
    public static String formatTime(int totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Strips all formatting for plain text representations.
     */
    @NotNull
    public static String stripFormatting(@NotNull String text) {
        Component component = parse(text);
        return PLAIN_SERIALIZER.serialize(component);
    }
}

