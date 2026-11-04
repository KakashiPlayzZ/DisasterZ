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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Sub-GUI for toggling which individual disasters are allowed
 * or blocked from automatic random selection cycles.
 * Disasters disabled here will NEVER trigger randomly, but can still be started by Admins or targeted schedules.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterToggleGUI implements InventoryHolder {

    private final DisasterZPlugin plugin;
    private final Inventory inventory;
    private final Map<Integer, DisasterType> slotMap = new HashMap<>();

    private static final int[] DISASTER_SLOTS = {10, 12, 14, 16, 20, 22, 24};
    private static final int[] STATUS_SLOTS = {19, 21, 23, 25, 29, 31, 33};

    public static final int SLOT_ALLOW_ALL = 45;
    public static final int SLOT_BLOCK_ALL = 47;
    public static final int SLOT_BACK = 49;

    public DisasterToggleGUI(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.parse("<gradient:#ff5555:#ffaa00><bold>Random-Cycle Filter</bold></gradient> <dark_gray>| Toggles</dark_gray>"));
        buildBorders();
        mapSlots();
        refreshLiveItems();
    }

    private void buildBorders() {
        ItemStack borderDark = createGlassItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack borderRed = createGlassItem(Material.RED_STAINED_GLASS_PANE, " ");
        ItemStack borderOrange = createGlassItem(Material.ORANGE_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderDark);
        }

        inventory.setItem(0, borderRed);
        inventory.setItem(8, borderRed);
        inventory.setItem(44, borderRed);
        inventory.setItem(53, borderRed);

        inventory.setItem(4, borderOrange);
    }

    private void mapSlots() {
        DisasterType[] types = DisasterType.values();
        for (int i = 0; i < types.length && i < DISASTER_SLOTS.length; i++) {
            slotMap.put(DISASTER_SLOTS[i], types[i]);
        }
    }

    public void refreshLiveItems() {
        for (var entry : slotMap.entrySet()) {
            int slot = entry.getKey();
            DisasterType type = entry.getValue();
            boolean isAllowed = plugin.getConfigManager().isRandomCycleEnabled(type);

            inventory.setItem(slot, buildDisasterToggleItem(type, isAllowed));
        }

        buildActionItems();
    }

    private ItemStack buildDisasterToggleItem(@NotNull DisasterType type, boolean isAllowed) {
        ItemStack item = new ItemStack(type.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(TextUtil.parse("<bold><gradient:#ffaa00:#ff5500>" + type.getDisplayName() + "</gradient></bold>"));

        List<Component> lore = new ArrayList<>();
        lore.add(TextUtil.parse("<dark_gray>Random Cycle Eligibility Filter</dark_gray>"));
        lore.add(TextUtil.parse("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"));

        if (isAllowed) {
            lore.add(TextUtil.parse("<yellow>Random Pool:</yellow> <green><bold>[ALLOWED IN RANDOM CYCLES]</bold></green>"));
            lore.add(TextUtil.parse("<gray>This disaster is active in automated random rolls.</gray>"));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            lore.add(TextUtil.parse("<yellow>Random Pool:</yellow> <red><bold>[BLOCKED FROM RANDOM POOL]</bold></red>"));
            lore.add(TextUtil.parse("<gray>Will <bold>NEVER</bold> start randomly or via auto-scheduler!</gray>"));
            lore.add(TextUtil.parse("<aqua>Can ONLY be triggered manually by Admin or targeted schedule.</aqua>"));
        }

        lore.add(Component.empty());
        lore.add(TextUtil.parse("<dark_gray>─────────────────────────────</dark_gray>"));
        lore.add(TextUtil.parse("<yellow>▶ Left-Click:</yellow> <gold>Toggle Allowed / Blocked</gold>"));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void buildActionItems() {
        // Slot 45: Allow All
        ItemStack allowAll = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta allowMeta = allowAll.getItemMeta();
        allowMeta.displayName(TextUtil.parse("<green><bold>Allow All in Random Cycle</bold></green>"));
        List<Component> allowLore = List.of(
                TextUtil.parse("<gray>Enables every disaster in the</gray>"),
                TextUtil.parse("<gray>automated random cycle pool.</gray>"),
                Component.empty(),
                TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"),
                TextUtil.parse("<yellow>▶ Click to Enable All</yellow>")
        );
        allowMeta.lore(allowLore);
        allowAll.setItemMeta(allowMeta);
        inventory.setItem(SLOT_ALLOW_ALL, allowAll);

        // Slot 47: Block All
        ItemStack blockAll = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta blockMeta = blockAll.getItemMeta();
        blockMeta.displayName(TextUtil.parse("<red><bold>Block All from Random Cycle</bold></red>"));
        List<Component> blockLore = List.of(
                TextUtil.parse("<gray>Disables all disasters from random rolls.</gray>"),
                TextUtil.parse("<gray>Disasters will only run on explicit command.</gray>"),
                Component.empty(),
                TextUtil.parse("<dark_gray>────────────────────────</dark_gray>"),
                TextUtil.parse("<yellow>▶ Click to Block All</yellow>")
        );
        blockMeta.lore(blockLore);
        blockAll.setItemMeta(blockMeta);
        inventory.setItem(SLOT_BLOCK_ALL, blockAll);

        // Slot 49: Back to Master Control
        ItemStack back = new ItemStack(Material.OAK_DOOR);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(TextUtil.parse("<white><bold>« Back to Master Control</bold></white>"));
        List<Component> backLore = List.of(
                TextUtil.parse("<gray>Return to main DisasterZ menu.</gray>"),
                Component.empty(),
                TextUtil.parse("<yellow>▶ Click to Return</yellow>")
        );
        backMeta.lore(backLore);
        back.setItemMeta(backMeta);
        inventory.setItem(SLOT_BACK, back);
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
        return slotMap.get(slot);
    }
}

