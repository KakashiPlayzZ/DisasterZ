package com.kakashiplayz.disasterz.gui;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced Sub-GUI for granular customization of the automated disaster scheduler,
 * cycle intervals, pre-disaster warnings, player thresholds, and upcoming disaster targets.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class SchedulerSettingsGUI implements InventoryHolder {

    private final DisasterZPlugin plugin;
    private final Inventory inventory;

    public static final int SLOT_STATUS = 11;
    public static final int SLOT_INTERVAL = 13;
    public static final int SLOT_WARNING = 15;
    public static final int SLOT_PLAYERS = 29;
    public static final int SLOT_NEXT_DISASTER = 31;
    public static final int SLOT_TRIGGER_NOW = 33;
    public static final int SLOT_BACK = 49;

    public SchedulerSettingsGUI(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.parse("<gradient:#ffaa00:#ff5500><bold>Scheduler Settings</bold></gradient> <dark_gray>| Control Panel</dark_gray>"));
        buildBorders();
        refreshLiveItems();
    }

    private void buildBorders() {
        ItemStack borderDark = createGlassItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack borderOrange = createGlassItem(Material.ORANGE_STAINED_GLASS_PANE, " ");
        ItemStack borderYellow = createGlassItem(Material.YELLOW_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderDark);
        }

        // Accent corners & perimeter highlights
        inventory.setItem(0, borderOrange);
        inventory.setItem(8, borderOrange);
        inventory.setItem(45, borderOrange);
        inventory.setItem(53, borderOrange);

        inventory.setItem(4, borderYellow);
        inventory.setItem(22, borderYellow);
    }

    public void refreshLiveItems() {
        buildStatusItem();
        buildIntervalItem();
        buildWarningItem();
        buildPlayersItem();
        buildNextDisasterItem();
        buildTriggerNowItem();
        buildBackItem();
    }

    private void buildStatusItem() {
        boolean enabled = plugin.getConfigManager().isAutoSchedulerEnabled();
        Material mat = enabled ? Material.DAYLIGHT_DETECTOR : Material.LEVER;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(TextUtil.parse("<yellow><bold>Scheduler Engine State</bold></yellow>"));

        List<Component> lore = new ArrayList<>();
        lore.add(TextUtil.parse("<dark_gray>Automated Background Execution</dark_gray>"));
        lore.add(Component.empty());
        lore.add(TextUtil.parse("<gray>Status:</gray> " + (enabled ? "<green><bold>[ENABLED]</bold></green>" : "<red><bold>[DISABLED]</bold></red>")));
        lore.add(TextUtil.parse("<gray>Next Trigger In:</gray> <aqua>" + TextUtil.formatTime(plugin.getDisasterScheduler().getSecondsUntilNext()) + "</aqua>"));
        lore.add(Component.empty());
        lore.add(TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"));
        lore.add(TextUtil.parse("<yellow>▶ Left-Click:</yellow> <gold>Toggle ON / OFF</gold>"));

        if (enabled) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(SLOT_STATUS, item);
    }

    private void buildIntervalItem() {
        int seconds = plugin.getConfigManager().getAutoSchedulerInterval();
        int minutes = seconds / 60;

        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(TextUtil.parse("<gold><bold>Cycle Interval Timer</bold></gold>"));

        List<Component> lore = new ArrayList<>();
        lore.add(TextUtil.parse("<dark_gray>Global delay between disaster occurrences</dark_gray>"));
        lore.add(Component.empty());
        lore.add(TextUtil.parse("<yellow>Current Interval:</yellow> <aqua>" + minutes + "m</aqua> <gray>(" + seconds + " seconds)</gray>"));
        lore.add(TextUtil.parse("<yellow>Remaining:</yellow> <white>" + TextUtil.formatTime(plugin.getDisasterScheduler().getSecondsUntilNext()) + "</white>"));
        lore.add(Component.empty());
        lore.add(TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"));
        lore.add(TextUtil.parse("<yellow>▶ Left-Click:</yellow> <green>+15 Minutes</green>"));
        lore.add(TextUtil.parse("<yellow>▶ Right-Click:</yellow> <red>-15 Minutes</red>"));
        lore.add(TextUtil.parse("<yellow>▶ Shift + Left-Click:</yellow> <aqua>+1 Hour</aqua>"));
        lore.add(TextUtil.parse("<yellow>▶ Shift + Right-Click:</yellow> <aqua>-1 Hour</aqua>"));

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(SLOT_INTERVAL, item);
    }

    private void buildWarningItem() {
        int warningSecs = plugin.getConfigManager().getWarningDelaySeconds();

        ItemStack item = new ItemStack(Material.BELL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(TextUtil.parse("<aqua><bold>Pre-Disaster Warning Delay</bold></aqua>"));

        List<Component> lore = new ArrayList<>();
        lore.add(TextUtil.parse("<dark_gray>Pre-event countdown audio/title warning</dark_gray>"));
        lore.add(Component.empty());
        lore.add(TextUtil.parse("<yellow>Warning Duration:</yellow> <red>" + warningSecs + " seconds</red>"));
        lore.add(Component.empty());
        lore.add(TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"));
        lore.add(TextUtil.parse("<yellow>▶ Left-Click:</yellow> <green>+5 Seconds</green>"));
        lore.add(TextUtil.parse("<yellow>▶ Right-Click:</yellow> <red>-5 Seconds</red>"));
        lore.add(TextUtil.parse("<yellow>▶ Shift + Left-Click:</yellow> <aqua>+15 Seconds</aqua>"));
        lore.add(TextUtil.parse("<yellow>▶ Shift + Right-Click:</yellow> <aqua>-15 Seconds</aqua>"));

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(SLOT_WARNING, item);
    }

    private void buildPlayersItem() {
        int minPlayers = plugin.getConfigManager().getMinimumPlayers();
        int onlineCount = Bukkit.getOnlinePlayers().size();

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(TextUtil.parse("<green><bold>Player Count Threshold</bold></green>"));

        List<Component> lore = new ArrayList<>();
        lore.add(TextUtil.parse("<dark_gray>Minimum online players required to run cycle</dark_gray>"));
        lore.add(Component.empty());
        lore.add(TextUtil.parse("<yellow>Required Threshold:</yellow> <gold>" + minPlayers + " player(s)</gold>"));
        lore.add(TextUtil.parse("<yellow>Current Online:</yellow> <white>" + onlineCount + " player(s)</white>"));
        lore.add(TextUtil.parse("<yellow>Condition Met:</yellow> " + (onlineCount >= minPlayers ? "<green>YES</green>" : "<red>NO (Paused)</red>")));
        lore.add(Component.empty());
        lore.add(TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"));
        lore.add(TextUtil.parse("<yellow>▶ Left-Click:</yellow> <green>+1 Player</green>"));
        lore.add(TextUtil.parse("<yellow>▶ Right-Click:</yellow> <red>-1 Player</red>"));

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(SLOT_PLAYERS, item);
    }

    private void buildNextDisasterItem() {
        DisasterType next = plugin.getDisasterScheduler().getNextScheduledDisaster();

        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(TextUtil.parse("<light_purple><bold>Next Scheduled Target</bold></light_purple>"));

        List<Component> lore = new ArrayList<>();
        lore.add(TextUtil.parse("<dark_gray>Disaster chosen for the next automatic cycle</dark_gray>"));
        lore.add(Component.empty());
        String targetName = (next != null) ? "<yellow><bold>" + next.getDisplayName() + "</bold></yellow>" : "<gold>Weighted Random</gold>";
        lore.add(TextUtil.parse("<yellow>Target Selection:</yellow> " + targetName));
        lore.add(Component.empty());
        lore.add(TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"));
        lore.add(TextUtil.parse("<yellow>▶ Left-Click:</yellow> <green>Cycle Next Disaster</green>"));
        lore.add(TextUtil.parse("<yellow>▶ Right-Click:</yellow> <aqua>Set to Weighted Random</aqua>"));

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(SLOT_NEXT_DISASTER, item);
    }

    private void buildTriggerNowItem() {
        ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(TextUtil.parse("<red><bold>Launch Scheduled Cycle Immediately</bold></red>"));

        List<Component> lore = new ArrayList<>();
        lore.add(TextUtil.parse("<gray>Bypasses remaining timer countdown and starts</gray>"));
        lore.add(TextUtil.parse("<gray>the upcoming disaster warning right now.</gray>"));
        lore.add(Component.empty());
        lore.add(TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"));
        lore.add(TextUtil.parse("<yellow>▶ Click to Force Trigger</yellow>"));

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(SLOT_TRIGGER_NOW, item);
    }

    private void buildBackItem() {
        ItemStack item = new ItemStack(Material.OAK_DOOR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(TextUtil.parse("<white><bold>« Back to Master Control</bold></white>"));

        List<Component> lore = List.of(
                TextUtil.parse("<gray>Return to the main DisasterZ overview.</gray>"),
                Component.empty(),
                TextUtil.parse("<yellow>▶ Click to Return</yellow>")
        );

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(SLOT_BACK, item);
    }

    private ItemStack createGlassItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(TextUtil.parse(name));
        item.setItemMeta(meta);
        return item;
    }

    public void open(@NotNull Player player) {
        player.openInventory(inventory);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

