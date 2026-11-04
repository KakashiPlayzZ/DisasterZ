package com.kakashiplayz.disasterz.api.event;

import com.kakashiplayz.disasterz.disaster.Disaster;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a disaster is about to begin its warning countdown.
 * Cancelling this event stops the disaster from starting.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterPreStartEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Disaster disaster;
    private int warningSeconds;
    private boolean cancelled = false;

    public DisasterPreStartEvent(@NotNull Disaster disaster, int warningSeconds) {
        this.disaster = disaster;
        this.warningSeconds = warningSeconds;
    }

    @NotNull
    public Disaster getDisaster() {
        return disaster;
    }

    public int getWarningSeconds() {
        return warningSeconds;
    }

    public void setWarningSeconds(int warningSeconds) {
        this.warningSeconds = warningSeconds;
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

