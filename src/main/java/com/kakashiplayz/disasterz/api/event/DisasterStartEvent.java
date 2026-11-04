package com.kakashiplayz.disasterz.api.event;

import com.kakashiplayz.disasterz.disaster.Disaster;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a disaster officially transitions from warning to active state.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Disaster disaster;

    public DisasterStartEvent(@NotNull Disaster disaster) {
        this.disaster = disaster;
    }

    @NotNull
    public Disaster getDisaster() {
        return disaster;
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

