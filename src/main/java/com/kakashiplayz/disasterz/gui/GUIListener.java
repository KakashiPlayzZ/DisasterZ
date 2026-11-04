package com.kakashiplayz.disasterz.gui;

import com.kakashiplayz.disasterz.DisasterZPlugin;
import com.kakashiplayz.disasterz.disaster.Disaster;
import com.kakashiplayz.disasterz.disaster.DisasterType;
import com.kakashiplayz.disasterz.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * Click handler and dynamic lore refresher for DisasterGUI, SchedulerSettingsGUI,
 * and DisasterToggleGUI. Prevents item duplication and shift-click exploits.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class GUIListener implements Listener {

    private final DisasterZPlugin plugin;
    private BukkitTask refreshTask;

    public GUIListener(@NotNull DisasterZPlugin plugin) {
        this.plugin = plugin;
        startRefreshTask();
    }

    private void startRefreshTask() {
        this.refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Inventory top = player.getOpenInventory().getTopInventory();
                if (top.getHolder() instanceof DisasterGUI mainGui) {
                    mainGui.refreshLiveItems();
                } else if (top.getHolder() instanceof SchedulerSettingsGUI schedGui) {
                    schedGui.refreshLiveItems();
                } else if (top.getHolder() instanceof DisasterToggleGUI toggleGui) {
                    toggleGui.refreshLiveItems();
                }
            }
        }, 20L, 20L);
    }

    public void stop() {
        if (refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInv = event.getView().getTopInventory();

        if (topInv.getHolder() instanceof DisasterGUI mainGui) {
            handleMainGUIClick(event, mainGui);
        } else if (topInv.getHolder() instanceof SchedulerSettingsGUI schedGui) {
            handleSchedulerGUIClick(event, schedGui);
        } else if (topInv.getHolder() instanceof DisasterToggleGUI toggleGui) {
            handleToggleGUIClick(event, toggleGui);
        }
    }

    private void handleMainGUIClick(InventoryClickEvent event, DisasterGUI gui) {
        event.setCancelled(true);

        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        ClickType click = event.getClick();

        // 1. Disaster Slots
        DisasterType type = gui.getDisasterAtSlot(slot);
        if (type != null) {
            handleDisasterAction(player, gui, type, click);
            return;
        }

        // 2. Action Bar Slots
        switch (slot) {
            case DisasterGUI.SLOT_STOP_ALL -> {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.0f);
                boolean stopped = plugin.getDisasterManager().stopActiveDisaster(true);
                if (stopped) {
                    TextUtil.sendMessage(player, "<green>Successfully halted active disaster and restored world states.</green>");
                } else {
                    TextUtil.sendMessage(player, "<yellow>No disaster is currently running.</yellow>");
                }
                gui.refreshLiveItems();
            }
            case DisasterGUI.SLOT_TRIGGER_RANDOM -> {
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 1.2f);
                DisasterType randomType = plugin.getDisasterScheduler().pickRandomWeightedDisaster();
                if (randomType != null) {
                    plugin.getDisasterManager().startDisaster(randomType, -1, false);
                    TextUtil.sendMessage(player, "<gold>Rolling enabled random disaster: <yellow>" + randomType.getDisplayName() + "</yellow>!</gold>");
                } else {
                    TextUtil.sendMessage(player, "<red>No disasters are currently enabled in the random-cycle pool.</red>");
                }
                gui.refreshLiveItems();
            }
            case DisasterGUI.SLOT_TOGGLE_FILTER -> {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.3f);
                new DisasterToggleGUI(plugin).open(player);
            }
            case DisasterGUI.SLOT_OPEN_SCHEDULER -> {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
                new SchedulerSettingsGUI(plugin).open(player);
            }
            case DisasterGUI.SLOT_RELOAD -> {
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.9f, 1.2f);
                plugin.reloadAllConfigurations();
                TextUtil.sendMessage(player, "<green>DisasterZ configurations and tasks reloaded successfully!</green>");
                gui.refreshLiveItems();
            }
        }
    }

    private void handleToggleGUIClick(InventoryClickEvent event, DisasterToggleGUI gui) {
        event.setCancelled(true);

        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();

        DisasterType type = gui.getDisasterAtSlot(slot);
        if (type != null) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.4f);
            boolean current = plugin.getConfigManager().isRandomCycleEnabled(type);
            plugin.getConfigManager().setRandomCycleEnabled(type, !current);
            String status = (!current) ? "<green><bold>ALLOWED</bold></green>" : "<red><bold>BLOCKED</bold></red>";
            TextUtil.sendMessage(player, "<yellow>" + type.getDisplayName() + " is now " + status + " in random cycles.</yellow>");
            gui.refreshLiveItems();
            return;
        }

        switch (slot) {
            case DisasterToggleGUI.SLOT_ALLOW_ALL -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
                for (DisasterType t : DisasterType.values()) {
                    plugin.getConfigManager().setRandomCycleEnabled(t, true);
                }
                TextUtil.sendMessage(player, "<green><bold>Enabled all disasters for automated random cycles.</bold></green>");
                gui.refreshLiveItems();
            }
            case DisasterToggleGUI.SLOT_BLOCK_ALL -> {
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.0f);
                for (DisasterType t : DisasterType.values()) {
                    plugin.getConfigManager().setRandomCycleEnabled(t, false);
                }
                TextUtil.sendMessage(player, "<red><bold>Blocked all disasters from random cycles (Admin / Targeted only).</bold></red>");
                gui.refreshLiveItems();
            }
            case DisasterToggleGUI.SLOT_BACK -> {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.1f);
                new DisasterGUI(plugin).open(player);
            }
        }
    }

    private void handleSchedulerGUIClick(InventoryClickEvent event, SchedulerSettingsGUI gui) {
        event.setCancelled(true);

        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        ClickType click = event.getClick();

        switch (slot) {
            case SchedulerSettingsGUI.SLOT_STATUS -> {
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.9f, 1.2f);
                boolean current = plugin.getConfigManager().isAutoSchedulerEnabled();
                plugin.getConfigManager().setAutoSchedulerEnabled(!current);
                TextUtil.sendMessage(player, "<yellow>Auto-Scheduler is now: " + (!current ? "<green>ENABLED</green>" : "<red>DISABLED</red>") + "</yellow>");
                gui.refreshLiveItems();
            }
            case SchedulerSettingsGUI.SLOT_INTERVAL -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.3f);
                int currentSec = plugin.getConfigManager().getAutoSchedulerInterval();
                int delta = (click.isShiftClick()) ? 3600 : 900;
                int newSec = click.isRightClick() ? Math.max(60, currentSec - delta) : currentSec + delta;
                plugin.getConfigManager().setAutoSchedulerInterval(newSec);
                TextUtil.sendMessage(player, "<aqua>Set scheduler interval to <gold>" + (newSec / 60) + " minutes</gold> (" + newSec + "s).</aqua>");
                gui.refreshLiveItems();
            }
            case SchedulerSettingsGUI.SLOT_WARNING -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
                int currentWarn = plugin.getConfigManager().getWarningDelaySeconds();
                int delta = (click.isShiftClick()) ? 15 : 5;
                int newWarn = click.isRightClick() ? Math.max(5, currentWarn - delta) : currentWarn + delta;
                plugin.getConfigManager().setWarningDelaySeconds(newWarn);
                TextUtil.sendMessage(player, "<aqua>Set pre-disaster warning delay to <gold>" + newWarn + " seconds</gold>.</aqua>");
                gui.refreshLiveItems();
            }
            case SchedulerSettingsGUI.SLOT_PLAYERS -> {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.1f);
                int currentMin = plugin.getConfigManager().getMinimumPlayers();
                int newMin = click.isRightClick() ? Math.max(0, currentMin - 1) : currentMin + 1;
                plugin.getConfigManager().setMinimumPlayers(newMin);
                TextUtil.sendMessage(player, "<aqua>Set minimum online players required to <gold>" + newMin + "</gold>.</aqua>");
                gui.refreshLiveItems();
            }
            case SchedulerSettingsGUI.SLOT_NEXT_DISASTER -> {
                player.playSound(player.getLocation(), Sound.ITEM_LODESTONE_COMPASS_LOCK, 0.8f, 1.2f);
                if (click.isRightClick()) {
                    plugin.getDisasterScheduler().setNextScheduledDisaster(null);
                    TextUtil.sendMessage(player, "<aqua>Next scheduled disaster reset to <gold>Weighted Random</gold>.</aqua>");
                } else {
                    DisasterType currentNext = plugin.getDisasterScheduler().getNextScheduledDisaster();
                    DisasterType[] allTypes = DisasterType.values();
                    int nextIdx = 0;
                    if (currentNext != null) {
                        for (int i = 0; i < allTypes.length; i++) {
                            if (allTypes[i] == currentNext) {
                                nextIdx = (i + 1) % allTypes.length;
                                break;
                            }
                        }
                    }
                    DisasterType newTarget = allTypes[nextIdx];
                    plugin.getDisasterScheduler().setNextScheduledDisaster(newTarget);
                    TextUtil.sendMessage(player, "<aqua>Next scheduled disaster targeted to <yellow>" + newTarget.getDisplayName() + "</yellow>.</aqua>");
                }
                gui.refreshLiveItems();
            }
            case SchedulerSettingsGUI.SLOT_TRIGGER_NOW -> {
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.9f, 1.2f);
                plugin.getDisasterScheduler().triggerScheduledCycle();
                TextUtil.sendMessage(player, "<gold>Forced scheduled cycle to execute immediately!</gold>");
                player.closeInventory();
            }
            case SchedulerSettingsGUI.SLOT_BACK -> {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.1f);
                new DisasterGUI(plugin).open(player);
            }
        }
    }

    private void handleDisasterAction(Player player, DisasterGUI gui, DisasterType type, ClickType click) {
        Disaster disaster = plugin.getDisasterManager().getDisaster(type);
        if (disaster == null) return;

        if (click == ClickType.LEFT || click == ClickType.SHIFT_LEFT) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
            boolean started = plugin.getDisasterManager().startDisaster(type, -1, false);
            if (started) {
                TextUtil.sendMessage(player, "<green>Initiated warning countdown for <yellow>" + type.getDisplayName() + "</yellow>!</green>");
            } else {
                TextUtil.sendMessage(player, "<red>Another disaster or warning is already in progress.</red>");
            }
        } else if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
            if (disaster.isActive()) {
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.0f);
                plugin.getDisasterManager().stopActiveDisaster(true);
                TextUtil.sendMessage(player, "<red>Stopped <yellow>" + type.getDisplayName() + "</yellow>.</red>");
            } else {
                TextUtil.sendMessage(player, "<yellow>" + type.getDisplayName() + " is not currently active.</yellow>");
            }
        } else if (click == ClickType.MIDDLE) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.4f);
            int current = plugin.getConfigManager().getDisasterDuration(type);
            int nextDuration = switch (current) {
                case 60 -> 120;
                case 120 -> 180;
                case 180 -> 300;
                case 300 -> 600;
                default -> 60;
            };
            plugin.getConfig().set("disasters." + type.getConfigKey() + ".duration", nextDuration);
            plugin.saveConfig();
            TextUtil.sendMessage(player, "<aqua>Set default duration for <yellow>" + type.getDisplayName() + "</yellow> to <gold>" + nextDuration + "s</gold>.</aqua>");
        }

        gui.refreshLiveItems();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof DisasterGUI || top.getHolder() instanceof SchedulerSettingsGUI || top.getHolder() instanceof DisasterToggleGUI) {
            event.setCancelled(true);
        }
    }
}
