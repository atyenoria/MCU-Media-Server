/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol;

import java.io.Serializable;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.join.Joinable.Direction;

/**
 *
 * @author Sergio
 */
public class JoinEventImpl implements JoinEvent {
    private final Direction direction;
    private final Joinable jnbl;
    private final Serializable context;
    private final Joinable who;
    private final EventType type;

    JoinEventImpl(Joinable who, Direction direction, Joinable jnbl, Serializable context) {
        //Store values
        this.who = who;
        this.direction = direction;
        this.jnbl = jnbl;
        this.context = context;
        this.type = JOINED;
    }

    JoinEventImpl(Joinable who, Joinable jnbl, Serializable context) {
        //Store values
        this.who = who;
        this.direction = null;
        this.jnbl = jnbl;
        this.context = context;
        this.type = UNJOINED;
    }


    @Override
    public Joinable getOtherJoinable() {
        return jnbl;
    }

    @Override
    public Joinable getThisJoinable() {
        return who;
    }

    @Override
    public Serializable getContext() {
        return context;
    }

    @Override
    public Joinable getSource() {
        return who;
    }

    @Override
    public EventType getEventType() {
        return type;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public MediaErr getError() {
        return null;
    }

    @Override
    public String getErrorText() {
        return null;
    }

}
