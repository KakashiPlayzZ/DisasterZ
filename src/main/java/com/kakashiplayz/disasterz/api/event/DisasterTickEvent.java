package com.kakashiplayz.disasterz.api.event;

import com.kakashiplayz.disasterz.disaster.Disaster;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called on each tick iteration while a disaster is active.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterTickEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Disaster disaster;
    private final long currentTick;
    private final int remainingSeconds;

    public DisasterTickEvent(@NotNull Disaster disaster, long currentTick, int remainingSeconds) {
        this.disaster = disaster;
        this.currentTick = currentTick;
        this.remainingSeconds = remainingSeconds;
    }

    @NotNull
    public Disaster getDisaster() {
        return disaster;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
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

