/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.resource;

import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.ResourceContainer;

/**
 *
 * @author Sergio
 */
public class AllocationEventImpl implements AllocationEvent {
    private final ContainerImpl container;
    private final EventType type;
    private final MediaErr err;
    private final String errMsg;

    public AllocationEventImpl(ContainerImpl container,EventType type) {
        //Storea values
        this.container = container;
        this.type = type;
        this.err = MediaErr.NO_ERROR;
        this.errMsg = "";
    }

    public AllocationEventImpl(ContainerImpl container,MediaErr err, String message) {
        //Storea values
        this.container = container;
        this.type = IRRECOVERABLE_FAILURE;
        this.err = err;
        this.errMsg = message;
    }

    @Override
    public ResourceContainer getSource() {
        return container;
    }

    @Override
    public EventType getEventType() {
        return type;
    }

    @Override
    public boolean isSuccessful() {
        return err.equals(MediaErr.NO_ERROR);
    }

    @Override
    public MediaErr getError() {
        return err;
    }

    @Override
    public String getErrorText() {
        return errMsg;
    }
}
