package com.kakashiplayz.disasterz.gui;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterState;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 54-Slot Interactive Master Admin GUI with enhanced visual boxes,
 * dynamic live lore refreshes, animated glowing glints, and navigation to sub-GUIs.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterGUI implements InventoryHolder {

    private final DisasterZPlugin plugin;
    private final Inventory inventory;
    private final Map<Integer, DisasterType> disasterSlots = new HashMap<>();

    // Balanced 7-disaster centered layout
    private static final int[] SLOTS = {10, 12, 14, 16, 20, 22, 24};

    // Bottom Action Bar slots
    public static final int SLOT_STOP_ALL = 45;
    public static final int SLOT_TRIGGER_RANDOM = 47;
    public static final int SLOT_TOGGLE_FILTER = 49;
    public static final int SLOT_OPEN_SCHEDULER = 51;
    public static final int SLOT_RELOAD = 53;

    public DisasterGUI(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.parse("<gradient:#ff5555:#aa0000><bold>DisasterZ</bold></gradient> <dark_gray>| Master Control</dark_gray>"));
        buildBorders();
        mapDisasterSlots();
        refreshLiveItems();
    }

    private void buildBorders() {
        ItemStack borderDark = createGlassItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack borderAccent = createGlassItem(Material.RED_STAINED_GLASS_PANE, " ");
        ItemStack borderGray = createGlassItem(Material.GRAY_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderDark);
        }

        // Perimeter accents
        inventory.setItem(0, borderAccent);
        inventory.setItem(8, borderAccent);
        inventory.setItem(36, borderAccent);
        inventory.setItem(44, borderAccent);

        // Divider row above action bar
        for (int i = 37; i <= 43; i++) {
            inventory.setItem(i, borderGray);
        }
    }

    private void mapDisasterSlots() {
        DisasterType[] types = DisasterType.values();
        for (int i = 0; i < types.length && i < SLOTS.length; i++) {
            disasterSlots.put(SLOTS[i], types[i]);
        }
    }

    public void refreshLiveItems() {
        for (var entry : disasterSlots.entrySet()) {
            int slot = entry.getKey();
            DisasterType type = entry.getValue();
            inventory.setItem(slot, buildDisasterIcon(type));
        }

        buildActionBarItems();
    }

    private ItemStack buildDisasterIcon(@NotNull DisasterType type) {
        ItemStack item = new ItemStack(type.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Disaster disaster = plugin.getDisasterManager().getDisaster(type);
        DisasterState state = (disaster != null) ? disaster.getState() : DisasterState.IDLE;
        boolean randomAllowed = plugin.getConfigManager().isRandomCycleEnabled(type);

        meta.displayName(TextUtil.parse("<bold><gradient:#ffaa00:#ff5500>" + type.getDisplayName() + "</gradient></bold>"));

        List<Component> lore = new ArrayList<>();
        lore.add(TextUtil.parse("<dark_gray>Cataclysmic Phenomenon</dark_gray>"));
        lore.add(TextUtil.parse("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"));
        lore.add(TextUtil.parse("<gray>" + type.getDescription() + "</gray>"));
        lore.add(Component.empty());

        // Dynamic State Badge
        String stateBadge = switch (state) {
            case ACTIVE -> "<green><bold>[ACTIVE NOW]</bold></green>";
            case WARNING -> "<gold><bold>[WARNING COUNTDOWN]</bold></gold>";
            case CLEANUP -> "<aqua><bold>[CLEANING UP]</bold></aqua>";
            case IDLE -> "<dark_gray>[IDLE / READY]</dark_gray>";
        };
        lore.add(TextUtil.parse("<yellow>Current Status:</yellow> " + stateBadge));

        // Random cycle eligibility indicator
        String randomBadge = randomAllowed ? "<green>Allowed in Random Cycle</green>" : "<red>Blocked from Random Pool</red>";
        lore.add(TextUtil.parse("<yellow>Random Cycle:</yellow> " + randomBadge));

        if (disaster != null && state == DisasterState.ACTIVE) {
            lore.add(TextUtil.parse("<yellow>Time Left:</yellow> <aqua>" + TextUtil.formatTime(disaster.getRemainingSeconds()) + "</aqua>"));
            lore.add(TextUtil.parse("<yellow>Trigger Mode:</yellow> <gray>" + disaster.getTriggerType().name() + "</gray>"));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else if (plugin.getWarningManager().isWarningActive() && plugin.getWarningManager().getPendingDisaster() != null
                && plugin.getWarningManager().getPendingDisaster().getType() == type) {
            lore.add(TextUtil.parse("<yellow>Warning Countdown:</yellow> <red>" + plugin.getWarningManager().getRemainingWarningSeconds() + "s</red>"));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            int defaultDur = plugin.getConfigManager().getDisasterDuration(type);
            lore.add(TextUtil.parse("<yellow>Default Duration:</yellow> <gray>" + TextUtil.formatTime(defaultDur) + "</gray>"));
        }

        lore.add(Component.empty());
        lore.add(TextUtil.parse("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"));
        lore.add(TextUtil.parse("<yellow>▶ Left-Click:</yellow> <green>Force Start / Warning</green>"));
        lore.add(TextUtil.parse("<yellow>▶ Right-Click:</yellow> <red>Cancel / Halt Event</red>"));
        lore.add(TextUtil.parse("<yellow>▶ Middle-Click:</yellow> <aqua>Cycle Duration (+60s)</aqua>"));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void buildActionBarItems() {
        // Slot 45: Stop All
        ItemStack stopAll = new ItemStack(Material.BARRIER);
        ItemMeta stopMeta = stopAll.getItemMeta();
        stopMeta.displayName(TextUtil.parse("<red><bold>Stop All Disasters</bold></red>"));
        List<Component> stopLore = List.of(
                TextUtil.parse("<gray>Immediately halts active events</gray>"),
                TextUtil.parse("<gray>and restores affected world blocks.</gray>"),
                Component.empty(),
                TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"),
                TextUtil.parse("<yellow>▶ Click to Execute</yellow>")
        );
        stopMeta.lore(stopLore);
        stopAll.setItemMeta(stopMeta);
        inventory.setItem(SLOT_STOP_ALL, stopAll);

        // Slot 47: Trigger Random
        ItemStack randomItem = new ItemStack(Material.FIREWORK_ROCKET);
        ItemMeta randMeta = randomItem.getItemMeta();
        randMeta.displayName(TextUtil.parse("<gold><bold>Trigger Random Disaster</bold></gold>"));
        List<Component> randLore = List.of(
                TextUtil.parse("<gray>Rolls an enabled disaster from the</gray>"),
                TextUtil.parse("<gray>allowed random pool and starts warning.</gray>"),
                Component.empty(),
                TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"),
                TextUtil.parse("<yellow>▶ Click to Roll</yellow>")
        );
        randMeta.lore(randLore);
        randomItem.setItemMeta(randMeta);
        inventory.setItem(SLOT_TRIGGER_RANDOM, randomItem);

        // Slot 49: Random-Cycle Filter Sub-GUI Opener (Replaces Author Info!)
        ItemStack filterItem = new ItemStack(Material.COMPARATOR);
        ItemMeta filterMeta = filterItem.getItemMeta();
        filterMeta.displayName(TextUtil.parse("<gradient:#ff5555:#ffaa00><bold>Random-Cycle Filter / Toggles »</bold></gradient>"));
        List<Component> filterLore = List.of(
                TextUtil.parse("<gray>Manage which disasters are allowed</gray>"),
                TextUtil.parse("<gray>to trigger in automated random cycles.</gray>"),
                Component.empty(),
                TextUtil.parse("<yellow>Disabled disasters:</yellow> <white>Will NEVER run randomly</white>"),
                TextUtil.parse("<aqua>Can only be started by Admins or targeted schedules.</aqua>"),
                Component.empty(),
                TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"),
                TextUtil.parse("<yellow>▶ Click to Open Filter Sub-GUI</yellow>")
        );
        filterMeta.lore(filterLore);
        filterItem.setItemMeta(filterMeta);
        inventory.setItem(SLOT_TOGGLE_FILTER, filterItem);

        // Slot 51: Open Scheduler Sub-GUI
        boolean schedulerEnabled = plugin.getConfigManager().isAutoSchedulerEnabled();
        ItemStack schedItem = new ItemStack(Material.CLOCK);
        ItemMeta schedMeta = schedItem.getItemMeta();
        schedMeta.displayName(TextUtil.parse("<gold><bold>Auto-Scheduler Settings »</bold></gold>"));
        List<Component> schedLore = List.of(
                TextUtil.parse("<gray>Configure automated disaster cycles,</gray>"),
                TextUtil.parse("<gray>timings, player counts, and targets.</gray>"),
                Component.empty(),
                TextUtil.parse("<yellow>Current Status:</yellow> " + (schedulerEnabled ? "<green><bold>[ENABLED]</bold></green>" : "<red><bold>[DISABLED]</bold></red>")),
                TextUtil.parse("<yellow>Interval:</yellow> <aqua>" + (plugin.getConfigManager().getAutoSchedulerInterval() / 60) + "m</aqua>"),
                TextUtil.parse("<yellow>Next Event:</yellow> <white>" + TextUtil.formatTime(plugin.getDisasterScheduler().getSecondsUntilNext()) + "</white>"),
                Component.empty(),
                TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"),
                TextUtil.parse("<yellow>▶ Click to Open Configuration Menu</yellow>")
        );
        schedMeta.lore(schedLore);
        if (schedulerEnabled) {
            schedMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
            schedMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        schedItem.setItemMeta(schedMeta);
        inventory.setItem(SLOT_OPEN_SCHEDULER, schedItem);

        // Slot 53: Reload Config
        ItemStack reloadItem = new ItemStack(Material.COMPASS);
        ItemMeta relMeta = reloadItem.getItemMeta();
        relMeta.displayName(TextUtil.parse("<aqua><bold>Reload Configurations</bold></aqua>"));
        List<Component> relLore = List.of(
                TextUtil.parse("<gray>Hot-reloads config.yml and messages.yml</gray>"),
                TextUtil.parse("<gray>without dropping current sessions.</gray>"),
                Component.empty(),
                TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"),
                TextUtil.parse("<yellow>▶ Click to Reload</yellow>")
        );
        relMeta.lore(relLore);
        reloadItem.setItemMeta(relMeta);
        inventory.setItem(SLOT_RELOAD, reloadItem);
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

    public DisasterType getDisasterAtSlot(int slot) {
        return disasterSlots.get(slot);
    }
}
