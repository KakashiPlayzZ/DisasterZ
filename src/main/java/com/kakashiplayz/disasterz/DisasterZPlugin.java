package com.kakashiplayz.disasterz;

import com.kakashiplayz.disasterz.api.DisasterZAPI;
import com.kakashiplayz.disasterz.audio.CinematicAudioEngine;
import com.kakashiplayz.disasterz.commands.DisasterZCommand;
import com.kakashiplayz.disasterz.commands.DisasterZTabCompleter;
import com.kakashiplayz.disasterz.config.ConfigManager;
import com.kakashiplayz.disasterz.config.MessagesManager;
import com.kakashiplayz.disasterz.gui.GUIListener;
import com.kakashiplayz.disasterz.hook.ClaimHookManager;
import com.kakashiplayz.disasterz.hook.PlaceholderAPIHook;
import com.kakashiplayz.disasterz.manager.DisasterManager;
import com.kakashiplayz.disasterz.manager.DisasterScheduler;
import com.kakashiplayz.disasterz.manager.MobScalingManager;
import com.kakashiplayz.disasterz.manager.WarningManager;
import com.kakashiplayz.disasterz.restoration.BlockRestorationEngine;
import com.kakashiplayz.disasterz.restoration.GhostBlockManager;
import com.kakashiplayz.disasterz.util.FlightSafetyListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * DisasterZ - Enterprise-grade Natural Disaster Plugin for Paper, Bukkit, and Spigot.
 *
 * Developed by KakashiPlayz
 * Website: https://kakashiplayz.vercel.app
 * Discord: https://discord.gg/QdTN4BFkTv
 */
public class DisasterZPlugin extends JavaPlugin implements Listener {

    private static DisasterZPlugin instance;

    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private CinematicAudioEngine audioEngine;
    private BlockRestorationEngine restorationEngine;
    private GhostBlockManager ghostBlockManager;
    private ClaimHookManager claimHookManager;
    private DisasterManager disasterManager;
    private WarningManager warningManager;
    private DisasterScheduler disasterScheduler;
    private MobScalingManager mobScalingManager;
    private GUIListener guiListener;
    private FlightSafetyListener flightSafetyListener;
    private PlaceholderAPIHook placeholderHook;

    @Override
    public void onEnable() {
        instance = this;
        DisasterZAPI.initialize(this);

        // Print modern startup banner with developer branding
        printConsoleBanner();

        // 1. Load Configurations & Messages
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.messagesManager = new MessagesManager(this);
        this.messagesManager.load();

        // 2. Initialize Core Subsystems
        this.audioEngine = new CinematicAudioEngine(this);
        this.ghostBlockManager = new GhostBlockManager(this);

        this.restorationEngine = new BlockRestorationEngine(this);
        this.restorationEngine.start();

        this.claimHookManager = new ClaimHookManager(this);
        this.claimHookManager.initializeHooks();

        this.warningManager = new WarningManager(this);
        this.disasterManager = new DisasterManager(this);
        this.disasterManager.registerAll();

        this.disasterScheduler = new DisasterScheduler(this);
        this.disasterScheduler.start();

        // 3. Register Event Listeners
        this.mobScalingManager = new MobScalingManager(this);
        Bukkit.getPluginManager().registerEvents(mobScalingManager, this);

        this.guiListener = new GUIListener(this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);

        this.flightSafetyListener = new FlightSafetyListener(this);
        Bukkit.getPluginManager().registerEvents(flightSafetyListener, this);

        Bukkit.getPluginManager().registerEvents(this, this);

        // 4. Register Command & Tab Completer
        PluginCommand command = Objects.requireNonNull(getCommand("disasterz"), "Command 'disasterz' missing in plugin.yml");
        DisasterZCommand cmdHandler = new DisasterZCommand(this);
        command.setExecutor(cmdHandler);
        command.setTabCompleter(new DisasterZTabCompleter());

        // 5. PlaceholderAPI Integration
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholderHook = new PlaceholderAPIHook(this);
            this.placeholderHook.register();
            getLogger().info("[DisasterZ] PlaceholderAPI expansion successfully registered.");
        }

        getLogger().info("[DisasterZ] Successfully enabled v" + getDescription().getVersion() + " by KakashiPlayz.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[DisasterZ] Initiating clean shutdown and world restoration...");

        if (disasterScheduler != null) {
            disasterScheduler.stop();
        }

        if (warningManager != null) {
            warningManager.cancelWarning();
        }

        if (disasterManager != null) {
            disasterManager.cleanup();
        }

        if (guiListener != null) {
            guiListener.stop();
        }

        if (ghostBlockManager != null) {
            ghostBlockManager.revertAll();
        }

        if (restorationEngine != null) {
            restorationEngine.stop();
            restorationEngine.restoreAllImmediate();
        }

        if (placeholderHook != null) {
            try {
                placeholderHook.unregister();
            } catch (Throwable ignored) {
            }
        }

        getLogger().info("[DisasterZ] Plugin disabled. All worlds restored safely.");
        instance = null;
    }

    /**
     * Hot-reloads all configuration files and restarts timers without dropping state.
     */
    public void reloadAllConfigurations() {
        configManager.load();
        messagesManager.load();
        claimHookManager.initializeHooks();
        disasterScheduler.start();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (ghostBlockManager != null) {
            ghostBlockManager.clearPlayer(player);
        }
    }

    private void printConsoleBanner() {
        getLogger().info("======================================================");
        getLogger().info("  ____  _               _            _____ ");
        getLogger().info(" |  _ \\(_)___  __ _ ___| |_ ___ _ __|__  / ");
        getLogger().info(" | | | | / __|/ _` / __| __/ _ \\ '__| / /  ");
        getLogger().info(" | |_| | \\__ \\ (_| \\__ \\ ||  __/ |   / /_  ");
        getLogger().info(" |____/|_|___/\\__,_|___/\\__\\___|_|  /____| ");
        getLogger().info("                                                  ");
        getLogger().info(" Enterprise Natural Disaster Plugin - v1.0.0");
        getLogger().info(" Developed by: KakashiPlayz");
        getLogger().info(" Portfolio:    https://kakashiplayz.vercel.app");
        getLogger().info(" Discord:      https://discord.gg/QdTN4BFkTv");
        getLogger().info("======================================================");
    }

    public static DisasterZPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public CinematicAudioEngine getAudioEngine() {
        return audioEngine;
    }

    public BlockRestorationEngine getBlockRestorationEngine() {
        return restorationEngine;
    }

    public GhostBlockManager getGhostBlockManager() {
        return ghostBlockManager;
    }

    public ClaimHookManager getClaimHookManager() {
        return claimHookManager;
    }

    public DisasterManager getDisasterManager() {
        return disasterManager;
    }

    public WarningManager getWarningManager() {
        return warningManager;
    }

    public DisasterScheduler getDisasterScheduler() {
        return disasterScheduler;
    }
}
