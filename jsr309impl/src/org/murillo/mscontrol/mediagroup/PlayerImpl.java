/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.mediagroup;

import java.net.URI;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.resource.RTC;
import javax.media.mscontrol.resource.ResourceEvent;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.mscontrol.MediaServer;
import org.murillo.mscontrol.MediaSessionImpl;

/**
 *
 * @author Sergio
 */
public class PlayerImpl implements Player {
    private int current;


    
    private enum State {IDLE,ACTIVE,PAUSED};

    private final MediaGroupImpl group;
    private final MediaSessionImpl sess;
    private final Integer playerId;
    private final ConcurrentLinkedQueue<MediaEventListener<PlayerEvent>> listeners;
    private boolean opened;
    private State state;
    private final Vector<URI> queue;
    private Integer repeatCount;
    private Integer seek;

    PlayerImpl(MediaSessionImpl sess,MediaGroupImpl group, URI uri) throws MsControlException {
        //Store values
        this.sess = sess;
        this.group = group;
        //Get media server
        MediaServer mediaServer = sess.getMediaServer();
        //Not opened
        opened = false;
        //Not palying anything
        current = -1;
        //And create play list
        queue = new Vector<URI>();
        //Set state
        state = State.IDLE;
        //Create masp
        listeners = new ConcurrentLinkedQueue<MediaEventListener<PlayerEvent>>();
        try {
            //Create player
            playerId = mediaServer.PlayerCreate(sess.getSessionId(), uri.toString());
        } catch (XmlRpcException ex) {
            Logger.getLogger(PlayerImpl.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not create player",ex);
        }
    }

    @Override
    public void play(URI[] movies, RTC[] rtcs, Parameters params) throws MsControlException {
        //Reset repeat count
        repeatCount = 0;
        
        //Check parameters
        if (params!=null && params.containsKey(REPEAT_COUNT))
            try {
                //Get it
                repeatCount = (Integer) params.get(REPEAT_COUNT)-1;
            } catch (Exception e) {
            }
        //Reset seek
        
        seek = 0;
        //Check parameters
        if (params!=null && params.containsKey(START_OFFSET))
        try {
             //Get it
            seek = (Integer) params.get(START_OFFSET);
        } catch (Exception e) {
        }
                
        //Check state
        if (!state.equals(State.IDLE))
        {
            //Get default behaviour if busy
            Value busy = QUEUE_IF_BUSY;
            //Check if we have been set
            if (params!=null && params.containsKey(BEHAVIOUR_IF_BUSY))
                //Get it
                busy = (Value) params.get(BEHAVIOUR_IF_BUSY);
            //Check if we have to fail
            if (busy.equals(FAIL_IF_BUSY)) {
                //Fail
                fireEventAsync(new PlayerEventImpl(this,MediaErr.BUSY,"Player busy"));
                //Exit
                return;
            }
            //Check if we have to stop first
            if (busy.equals(STOP_IF_BUSY)) {
                //Stop all
                stop(true);
            } 
        }
        //For each one
        for (URI movie : movies)
            //Append to queue
            addToQueue(movie);

        //check params
        if (params!=null && params.containsKey(Player.START_IN_PAUSED_MODE))
            //Set paused state
            state = State.PAUSED;
        else
            //Play first
            playQueue(0);
            
    }

    @Override
    public void play(URI movie, RTC[] rtcs, Parameters params) throws MsControlException {
        //Play it
        play(new URI[]{movie},rtcs,params);
    }
    
    private void addToQueue(URI uri) {
        //add uri
        queue.add(uri);
    }

    private void clearQueue() {
        //Remove all
        queue.clear();
    }

    private boolean playNext() {
        //Play next one
        return playQueue(current+1);
    }

    private boolean playQueue(int i) {
        //Check
        if (i>=queue.size())
            //Exit
            return false;

        //Get next
        URI movie = queue.get(i);
        
        //Check uri
        if (!movie.getScheme().equals("file")) {
            //Fire event
            fireEventAsync(new PlayerEventImpl(this,MediaErr.BAD_ARG,"URI schema not valid"));
            //Remove from queue
            clearQueue();
            //Exit
            return false;
        }

        //Get media server
        MediaServer mediaServer = sess.getMediaServer();
        
        //If preivously opened
        if (opened)
        {
           try {
                //Close filename
                opened = mediaServer.PlayerClose(sess.getSessionId(), playerId);
           } catch (XmlRpcException ex) {
                //Log
                Logger.getLogger(PlayerImpl.class.getName()).log(Level.SEVERE, "Could not close file XMLRCPC exception: {0}", ex.getMessage());
                //Fire event
                fireEventAsync(new PlayerEventImpl(this,MediaErr.UNKNOWN_ERROR,"Error playing file"));
                //Remove from queue
                clearQueue();
                //Exit
                return false;
            }
        }
        
        //Not opened
        opened = false;
        //Set state
        state = State.IDLE;
        try {
            //If pre
            //Open filename
            opened = mediaServer.PlayerOpen(sess.getSessionId(), playerId, movie.getPath());
         } catch (XmlRpcException ex) {
            //Log
            Logger.getLogger(PlayerImpl.class.getName()).log(Level.SEVERE, "Could not open file XMLRCPC exception: {0}", ex.getMessage());
            //Fire event
            fireEventAsync(new PlayerEventImpl(this,MediaErr.UNKNOWN_ERROR,"Error playing file"));
            //Remove from queue
            clearQueue();
            //Exit
            return false;
        }

        //Check if we have been opened correcty
        if (!opened)
        {
            //Fire event
            fireEventAsync(new PlayerEventImpl(this,MediaErr.NOT_FOUND,"Error opening file"));
            //Remove from queue
            clearQueue();
            //Exit
            return false;
        }
        
        try {
            //Check seek
            if (seek==0)
                //PLay it
                mediaServer.PlayerPlay(sess.getSessionId(), playerId);
            else
                //Seek it
                mediaServer.PlayerSeek(sess.getSessionId(),playerId,seek);
            //Set state
            state = State.ACTIVE;
            //Set current
            current = i;
        } catch (XmlRpcException ex) {
            //Log
            Logger.getLogger(PlayerImpl.class.getName()).log(Level.SEVERE, "Could not play file XMLRCPC exception: {0}", ex.getMessage());
            //Fire event
            fireEventAsync(new PlayerEventImpl(this,MediaErr.UNKNOWN_ERROR,"Error playing file"));
            //Remove from queue
            clearQueue();
            //Exit
            return false;
        }

        //Exit
        return true;
    }

    @Override
    public void stop(boolean all)  {
        //if we have to stop all
        if (all)
            //Clear queue
            clearQueue();

        //Check state
        if (opened)
        {
            try {
                //Get media server
                MediaServer mediaServer = sess.getMediaServer();
                //Close it
                mediaServer.PlayerClose(sess.getSessionId(), playerId);
                //We are not opened
                opened = false;
            } catch (XmlRpcException ex) {
                Logger.getLogger(PlayerImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            //No current
            current = -1;
            //Set state
            state = State.IDLE;
            //Fire event
            fireEventAsync(new PlayerEventImpl(this,PlayerEvent.PLAY_COMPLETED ,ResourceEvent.STOPPED));
        }
    }

    @Override
    public MediaGroup getContainer() {
        return group;
    }

    @Override
    public void addListener(MediaEventListener<PlayerEvent> ml) {
        //Add it
        listeners.add(ml);
    }

    @Override
    public void removeListener(MediaEventListener<PlayerEvent> ml) {
        //remove it
        listeners.remove(ml);
    }

    @Override
    public MediaSession getMediaSession() {
        return sess;
    }

     public Integer getPlayerId() {
        return playerId;
    }

    public void onEndOfStream() {
        //We are idle
        state = State.IDLE;

        //Close file
        if (opened)
        {
            try {
                //Not opened
                opened = false;
                //Get media server
                MediaServer mediaServer = sess.getMediaServer();
                //Close filename
                mediaServer.PlayerClose(sess.getSessionId(), playerId);
            } catch (XmlRpcException ex) {
                //Log
                Logger.getLogger(PlayerImpl.class.getName()).log(Level.SEVERE, "Could not close file XMLRCPC exception: {0}", ex.getMessage());
            }
        }
        //Play next
        if (!playNext())
        {
            //Check if we have to repeat
            if (repeatCount>0) {
                //Decrease
                repeatCount--;
                //Start again from the beginning
                playQueue(0);
            } else 
                //If not more repetitions
                fireEventAsync(new PlayerEventImpl(this, PlayerEvent.PLAY_COMPLETED, PlayerEvent.END_OF_PLAY_LIST));
        }
    }

    private void fireEventAsync(final PlayerEventImpl event) {
        //Check listeners
        if (listeners.isEmpty())
            //Exit
            return;
        //Get media session
        MediaSessionImpl mediaSession = (MediaSessionImpl) getMediaSession();

        //Launch asing
        mediaSession.Exec(new Runnable() {
            @Override
            public void run() {
                    //For each listener in set
                    for (MediaEventListener<PlayerEvent> listener : listeners)
                        //Send it
                        listener.onEvent(event);
            }
        });
        
    }

    void release() {
        //Clear listeners
        listeners.clear();
        //Stop any pending
        stop(true);
        //Get media server
        MediaServer mediaServer = sess.getMediaServer();
        try {
            //Remove media session
            mediaServer.PlayerDelete(sess.getSessionId(),playerId);
        } catch (XmlRpcException ex) {
            Logger.getLogger(MediaSessionImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
