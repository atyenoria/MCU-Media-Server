/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinEventListener;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.join.JoinableContainer;
import javax.media.mscontrol.join.JoinableStream;
import javax.media.mscontrol.join.JoinableStream.StreamType;

/**
 *
 * @author Sergio
 */
public abstract class JoinableContainerImpl extends JoinableImpl implements JoinableContainer {
    protected final EnumMap<StreamType,JoinableStreamImpl> streams;
    protected final ConcurrentLinkedQueue<JoinEventListener> listeners;
    
    public JoinableContainerImpl(MediaSessionImpl session) {
        //Call parent
        super(session);
         //Create stream maps
        streams = new EnumMap<StreamType,JoinableStreamImpl>(StreamType.class);
        //Create listeners set
        listeners = new ConcurrentLinkedQueue<JoinEventListener>();
    }

     public void AddStream(StreamType type, JoinableStreamImpl stream) {
         //Add stream to the stream map
        streams.put(type,stream);
    }
     
     public boolean HasStream(StreamType type) {
	     return streams.containsKey(type);
     }

    @Override
    public JoinableStream getJoinableStream(StreamType type) throws MsControlException {
        //Return array of streams for given type
        return (JoinableStream) streams.get(type);
    }

    @Override
    public JoinableStream[] getJoinableStreams() throws MsControlException {
        //Return object array
        return (JoinableStream[]) streams.values().toArray(new JoinableStream[streams.size()]);
    }

    public JoinableStreamImpl[] getJoinableStreamsImpl() throws MsControlException {
          return (JoinableStreamImpl[]) streams.values().toArray(new JoinableStreamImpl[streams.size()]);
    }

    @Override
    public void addListener(JoinEventListener jl) {
        //Add to lisnteners
        listeners.add(jl);
    }

    @Override
    public void removeListener(JoinEventListener jl) {
        //Remove from listeners
        listeners.remove(jl);
    }

    @Override
    public MediaSession getMediaSession() {
        //Return media session
        return session;
    }

    @Override
    public void doJoinStream(Direction dir, JoinableStreamImpl jnbl) throws MsControlException {
        //Get implementation
        JoinableStreamImpl stream = (JoinableStreamImpl)jnbl;
        //Get our stream for the type
        JoinableStreamImpl ours = (JoinableStreamImpl) getJoinableStream(stream.getType());
        //If we also have it
        if (ours!=null)
            //Join stream
            ours.doJoinStream(dir, stream);
    }

    @Override
    public void doJoinStreams(Direction dir, JoinableStreamImpl[] joinables) throws MsControlException
    {
        //For all their streasm
        for (JoinableStreamImpl theirs : joinables)
            //For all our streams
            for (JoinableStreamImpl ours : streams.values())
                //If they are of the same type
                if (ours.getType().equals(theirs.getType()))
                    //Join ours to theirs
                    ours.doJoinStream(dir, theirs);
    }

    @Override
    public void doUnjoinStream(JoinableStreamImpl stream) throws MsControlException {
        //Get our stream for the type
        JoinableStreamImpl ours = (JoinableStreamImpl) getJoinableStream(stream.getType());
        //If we also have it
        if (ours!=null)
            //Join stream
            ours.doUnjoinStream(stream);
    }

    @Override
    protected void fireJoineEvent(Joinable who, Direction direction, Joinable join, Serializable context) {
        //Create event
        JoinEventImpl event = new JoinEventImpl(who, direction, join, context);
        //For each listener
        for (JoinEventListener listener : listeners)
            //Send event
            listener.onEvent(event);
    }

    @Override
    protected void fireUnJoineEvent(Joinable who, Joinable join, Serializable context) {
        //Create event
        JoinEventImpl event = new JoinEventImpl(who, join, context);
        //For each listener
        for (JoinEventListener listener : listeners)
            //Send event
            listener.onEvent(event);
    }
}
