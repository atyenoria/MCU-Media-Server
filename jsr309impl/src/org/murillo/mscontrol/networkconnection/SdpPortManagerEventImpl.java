/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.networkconnection;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import javax.media.mscontrol.resource.Trigger;

/**
 *
 * @author Sergio
 */
public class SdpPortManagerEventImpl implements SdpPortManagerEvent {

    private final SdpPortManagerImpl source ;
    private final EventType eventType;
    private final boolean isSuccessful;
    private final MediaErr error;
    private final String errorMsg;

    public SdpPortManagerEventImpl(SdpPortManagerImpl source, EventType eventType) {
        this.source = source;
        this.eventType = eventType;
        this.isSuccessful = true;
        this.error = MediaErr.NO_ERROR;
        this.errorMsg = "No error";
    }

     public SdpPortManagerEventImpl(SdpPortManagerImpl source,MediaErr error,String errorMsg) {
        this.source = source;
        this.eventType = null;
        this.isSuccessful = false;
        this.error = error;
        this.errorMsg = errorMsg;
    }

    @Override
    public byte[] getMediaServerSdp() {
        try {
            return source.getMediaServerSessionDescription();
        } catch (SdpPortManagerException ex) {
            Logger.getLogger(SdpPortManagerEventImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Qualifier getQualifier() {
        return null;
    }

    public Trigger getRTCTrigger() {
        return null;
    }

    public MediaErr getError() {
        return error;
    }

    public String getErrorText() {
        return errorMsg;
    }

    public EventType getEventType() {
        return eventType;
    }

    public SdpPortManager getSource() {
        return source;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
    
    @Override
    public String toString() {
        return eventType.toString();
    }

}
