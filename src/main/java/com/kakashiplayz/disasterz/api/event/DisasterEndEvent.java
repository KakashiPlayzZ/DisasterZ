package com.kakashiplayz.disasterz.api.event;

import com.kakashiplayz.disasterz.disaster.Disaster;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a disaster ends naturally or is stopped by an administrator.
 *
 * Developed by KakashiPlayz (https://kakashiplayz.vercel.app)
 */
public class DisasterEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Disaster disaster;
    private final boolean forced;

    public DisasterEndEvent(@NotNull Disaster disaster, boolean forced) {
        this.disaster = disaster;
        this.forced = forced;
    }

    @NotNull
    public Disaster getDisaster() {
        return disaster;
    }

    public boolean isForced() {
        return forced;
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

