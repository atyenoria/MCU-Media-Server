/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol;

import org.murillo.mscontrol.resource.DirectionTools;
import java.io.Serializable;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.join.JoinableStream;

/**
 *
 * @author Sergio
 */
public abstract class JoinableImpl implements Joinable{

    private final HashSet<JoinableImpl> sendingTo;
    private JoinableImpl receivingFrom;
    protected final MediaSessionImpl session;


    public JoinableImpl(MediaSessionImpl session) {
        //Store values
        this.session = session;
        //Create set
        sendingTo = new HashSet<JoinableImpl>();
        //Not receiving
        receivingFrom = null;
    }

    @Override
    public Joinable[] getJoinees(Direction dir) throws MsControlException {
        Joinable arr[] = null;

        switch(dir)
        {
            case DUPLEX:
                //Create array
                if (receivingFrom!=null)
                {
                    //send + recv
                    arr = new Joinable[sendingTo.size()+1];
                    //Create array
                    arr = sendingTo.toArray(arr);
                    //Set rec
                    arr[sendingTo.size()] = receivingFrom;
                } else {
                    //Create array
                    arr = sendingTo.toArray(arr);
                }
                break;
            case SEND:
                arr = sendingTo.toArray(arr);
                break;
            case RECV:
                if (receivingFrom!=null)
                    //Nothing
                    return null;
                //Create array
                arr = new Joinable[1];
                //Set value
                arr[0] = receivingFrom;
                break;
        }
        //Return joinees
        return arr;
    }

    @Override
    public Joinable[] getJoinees() throws MsControlException {
        Joinable arr[] = null;
        //Create array
        if (receivingFrom!=null)
        {
            //send + recv
            arr = new Joinable[sendingTo.size()+1];
            //Create array
            arr = sendingTo.toArray(arr);
            //Set rec
            arr[sendingTo.size()] = receivingFrom;
        } else {
            //Create array
            arr = sendingTo.toArray(arr);
        }
        //Return joinees
        return arr;
    }

    @Override
    public void join(Direction dir, Joinable jnbl) throws MsControlException {
        //Convert
        JoinableImpl joinable = (JoinableImpl)jnbl;
        //Add joinables and get origin
        JoinableImpl origin = AddJoin(dir,joinable);
        //Also add joinable in reverse direcction and get destination
        JoinableImpl destination = joinable.AddJoin(DirectionTools.reverse(dir),this);
        
        //Check type
        if (destination instanceof JoinableContainerImpl)
        {
            //Cast
            JoinableContainerImpl container = (JoinableContainerImpl) destination;
            //Get streams
            JoinableStreamImpl[] joinableStreams = container.getJoinableStreamsImpl();
            //Join stream
            origin.doJoinStreams(dir,joinableStreams);
       } else if (destination instanceof JoinableStreamImpl) {
            //Cast
            JoinableStreamImpl stream = (JoinableStreamImpl) destination;
            //Join stream
            origin.doJoinStream(dir,(JoinableStreamImpl)stream);
       }
    }

    @Override
    public void unjoin(Joinable jnbl) throws MsControlException {
        //Convert
        JoinableImpl joinable = (JoinableImpl)jnbl;
        //Remove joinables
        JoinableImpl origin = RemoveJoin(joinable);
        //Also remove joinable in reverse direcction
        JoinableImpl destination = joinable.RemoveJoin(this);
        //Check type
        if (destination instanceof JoinableContainerImpl)
        {
            //Cast
            JoinableContainerImpl conn = (JoinableContainerImpl) destination;
            //Get streams
            JoinableStream[] joinableStreams = conn.getJoinableStreams();
            //For each joinable stream in destiny
            for (JoinableStream stream : joinableStreams)
                //Join stream
                origin.doUnjoinStream((JoinableStreamImpl)stream);
       } else if (destination instanceof JoinableStreamImpl) {
            //Cast
            JoinableStreamImpl stream = (JoinableStreamImpl) destination;
            //UnJoin stream
            origin.doUnjoinStream((JoinableStreamImpl)stream);
       }
    }

    @Override
    public void joinInitiate(final Direction dir, final Joinable jnbl, final Serializable context) throws MsControlException {
        //Who is the originating of the event
        final Joinable who = this;
        //Launch async
        session.Exec(new Runnable() {
            @Override
            public void run() {
                try {
                    //Join
                    join(dir, jnbl);
                    //Fire join event
                    fireJoineEvent(who,dir,jnbl,context);
                } catch (MsControlException ex) {
                    Logger.getLogger(JoinableImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


        });
    }

    @Override
    public void unjoinInitiate(final Joinable jnbl, final Serializable context) throws MsControlException {
        //Who is the originating of the event
        final Joinable who = this;
        //Launch async
        session.Exec(new Runnable() {
            @Override
            public void run() {
                try {
                    //Join
                    unjoin(jnbl);
                    //Fire join event
                    fireUnJoineEvent(who,jnbl,context);
                } catch (MsControlException ex) {
                    Logger.getLogger(JoinableImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void releaseJoins()
    {
        //If receiving
        if (receivingFrom!=null)
            //Unjoin
            try {
                //Unjoin
                unjoin(receivingFrom);
            } catch (MsControlException ex) {
            }
        //for each sender
        for(JoinableImpl joinable : sendingTo)
            //Unjoin
            try {
                //Unjoin
                unjoin(joinable);
            } catch (MsControlException ex) {
            }
    }

    protected abstract void fireJoineEvent(Joinable who, Direction direction, Joinable dir, Serializable jnbl);
    protected abstract void fireUnJoineEvent(Joinable who, Joinable jnbl, Serializable context);

    public JoinableImpl AddJoin(Direction direction, JoinableImpl joinable)  throws MsControlException{
        switch(direction)
        {
            case DUPLEX:
                //Add to sending
                sendingTo.add(joinable);
                //Set receiving
                receivingFrom = joinable;
                break;
            case SEND:
                //Add to sending
                sendingTo.add(joinable);
                break;
            case RECV:
                //Set receiving
                receivingFrom = joinable;
                break;
        }
        //We are the join
        return this;
    }

    public JoinableImpl RemoveJoin(JoinableImpl joinable)  throws MsControlException{
        //Remove from sending
        sendingTo.remove(joinable);
        //Check if it was the receiving one
        if (receivingFrom!=null && receivingFrom.equals(joinable))
            //Not receiving
            receivingFrom = null;
        //Ge are the join
        return this;
    }

    public abstract void doJoinStream(Direction direction, JoinableStreamImpl dir)  throws MsControlException;
    public abstract void doJoinStreams(Direction direction, JoinableStreamImpl[] streams)   throws MsControlException;;
    public abstract void doUnjoinStream(JoinableStreamImpl stream)                  throws MsControlException;



}
