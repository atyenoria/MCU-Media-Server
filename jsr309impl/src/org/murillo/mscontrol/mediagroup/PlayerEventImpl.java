package org.murillo.mscontrol.mediagroup;

import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.resource.Trigger;

/**
 * 
 * @author amit bhayani
 * 
 */
public class PlayerEventImpl implements PlayerEvent {
    private final Player player;
    private final EventType eventType;
    private final boolean isSuccessful;
    private final Qualifier qualifier;
    private final Trigger rtcTrigger;
    private final MediaErr error;
    private final String errorText;
    private int offset = 0;


    public PlayerEventImpl(Player player, EventType eventType, Trigger rtcTrigger ) {
        this.player         = player;
        this.eventType      = eventType;
        this.isSuccessful   = true;
        this.qualifier      = RTC_TRIGGERED;
        this.rtcTrigger     = rtcTrigger;
        this.error          = MediaErr.NO_ERROR;
        this.errorText      = null;
    }

    public PlayerEventImpl(Player player, EventType eventType) {
        this.player         = player;
        this.eventType      = eventType;
        this.isSuccessful   = true;
        this.qualifier      = NO_QUALIFIER;
        this.rtcTrigger     = null;
        this.error          = NO_ERROR;
        this.errorText      = null;
    }

        public PlayerEventImpl(Player player, EventType eventType,Qualifier qualifier) {
        this.player         = player;
        this.eventType      = eventType;
        this.isSuccessful   = true;
        this.qualifier      = qualifier;
        this.rtcTrigger     = null;
        this.error          = NO_ERROR;
        this.errorText      = null;
    }


    public PlayerEventImpl(Player player, MediaErr error, String errorText) {
        this.player         = player;
        this.eventType      = null;
        this.isSuccessful   = false;
        this.qualifier      = NO_QUALIFIER;
        this.rtcTrigger     = null;
        this.error          = error;
        this.errorText      = errorText;
    }

    @Override
    public Action getChangeType() {
            return null;
    }

    @Override
    public int getIndex() {
            return 0;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
    @Override
    public int getOffset() {
            return this.offset;
    }

    @Override
    public Qualifier getQualifier() {
            return this.qualifier;
    }

    @Override
    public Trigger getRTCTrigger() {
            return this.rtcTrigger;
    }

    @Override
    public MediaErr getError() {
            return this.error;
    }

    @Override
    public String getErrorText() {
            return this.errorText;
    }

    @Override
    public EventType getEventType() {
            return this.eventType;
    }

    @Override
    public Player getSource() {
            return this.player;
    }

    @Override
    public boolean isSuccessful() {
            return this.isSuccessful;
    }

    
}
