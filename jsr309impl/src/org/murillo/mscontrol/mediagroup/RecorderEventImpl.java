/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.mediagroup;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.mediagroup.Recorder;
import javax.media.mscontrol.mediagroup.RecorderEvent;
import javax.media.mscontrol.resource.Trigger;

/**
 *
 * @author Sergio
 */
public class RecorderEventImpl implements RecorderEvent {
    private final Recorder recorder;
    private final EventType eventType;
    private final boolean isSuccessful;
    private final Qualifier qualifier;
    private final Trigger rtcTrigger;
    private final MediaErr error;
    private final String errorText;
    private int duration;

    public RecorderEventImpl(Recorder recorder, EventType eventType, Trigger rtcTrigger ) {
        this.recorder       = recorder;
        this.eventType      = eventType;
        this.isSuccessful   = true;
        this.qualifier      = RTC_TRIGGERED;
        this.rtcTrigger     = rtcTrigger;
        this.error          = MediaErr.NO_ERROR;
        this.errorText      = null;
    }

    public RecorderEventImpl(Recorder recorder, EventType eventType) {
        this.recorder       = recorder;
        this.eventType      = eventType;
        this.isSuccessful   = true;
        this.qualifier      = NO_QUALIFIER;
        this.rtcTrigger     = null;
        this.error          = NO_ERROR;
        this.errorText      = null;
    }

    public RecorderEventImpl(Recorder recorder, EventType eventType,Qualifier qualifier) {
        this.recorder       = recorder;
        this.eventType      = eventType;
        this.isSuccessful   = true;
        this.qualifier      = qualifier;
        this.rtcTrigger     = null;
        this.error          = NO_ERROR;
        this.errorText      = null;
    }


    public RecorderEventImpl(Recorder recorder, MediaErr error, String errorText) {
        this.recorder       = recorder;
        this.eventType      = null;
        this.isSuccessful   = false;
        this.qualifier      = NO_QUALIFIER;
        this.rtcTrigger     = null;
        this.error          = error;
        this.errorText      = errorText;
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
    public Recorder getSource() {
        return this.recorder;
    }

    @Override
    public boolean isSuccessful() {
        return this.isSuccessful;
    }

    @Override
    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

}
