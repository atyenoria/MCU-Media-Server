/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.resource.video;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.resource.Trigger;
import javax.media.mscontrol.resource.video.VideoRenderer;
import javax.media.mscontrol.resource.video.VideoRendererEvent;

/**
 *
 * @author Sergio
 */
public class VideoRendererEventImpl implements VideoRendererEvent{

    private final VideoRenderer renderer;
    private final EventType eventType;
    private final boolean isSuccessful;
    private final Qualifier qualifier;
    private final Trigger rtcTrigger;
    private final MediaErr error;
    private final String errorText;

    public VideoRendererEventImpl(VideoRenderer renderer, EventType eventType, Trigger rtcTrigger ) {
        this.renderer       = renderer;
        this.eventType      = eventType;
        this.isSuccessful   = true;
        this.qualifier      = RTC_TRIGGERED;
        this.rtcTrigger     = rtcTrigger;
        this.error          = MediaErr.NO_ERROR;
        this.errorText      = null;
    }

    public VideoRendererEventImpl(VideoRenderer renderer, EventType eventType) {
        this.renderer       = renderer;
        this.eventType      = eventType;
        this.isSuccessful   = true;
        this.qualifier      = NO_QUALIFIER;
        this.rtcTrigger     = null;
        this.error          = NO_ERROR;
        this.errorText      = null;
    }

    public VideoRendererEventImpl(VideoRenderer renderer, EventType eventType,Qualifier qualifier) {
        this.renderer       = renderer;
        this.eventType      = eventType;
        this.isSuccessful   = true;
        this.qualifier      = qualifier;
        this.rtcTrigger     = null;
        this.error          = NO_ERROR;
        this.errorText      = null;
    }


    public VideoRendererEventImpl(VideoRenderer renderer, MediaErr error, String errorText) {
        this.renderer       = renderer;
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
    public VideoRenderer getSource() {
        return this.renderer;
    }

    @Override
    public boolean isSuccessful() {
        return this.isSuccessful;
    }

}
