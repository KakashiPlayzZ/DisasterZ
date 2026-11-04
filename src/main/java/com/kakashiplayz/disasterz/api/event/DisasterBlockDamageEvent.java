package com.kakashiplayz.disasterz.api.event;

import com.kakashiplayz.disasterz.disaster.Disaster;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called whenever a disaster attempts to alter or damage a block in the world.
 * Allows other plugins (claim managers, custom protections) to cancel individual block modifications.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterBlockDamageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Disaster disaster;
    private final Block block;
    private final BlockData newBlockData;
    private boolean cancelled = false;

    public DisasterBlockDamageEvent(@NotNull Disaster disaster, @NotNull Block block, @NotNull BlockData newBlockData) {
        this.disaster = disaster;
        this.block = block;
        this.newBlockData = newBlockData;
    }

    @NotNull
    public Disaster getDisaster() {
        return disaster;
    }

    @NotNull
    public Block getBlock() {
        return block;
    }

    @NotNull
    public BlockData getNewBlockData() {
        return newBlockData;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

