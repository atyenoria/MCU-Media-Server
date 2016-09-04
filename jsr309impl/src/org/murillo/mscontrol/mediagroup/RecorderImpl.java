/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.mediagroup;

import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Recorder;
import javax.media.mscontrol.mediagroup.RecorderEvent;
import javax.media.mscontrol.resource.RTC;
import javax.media.mscontrol.resource.ResourceEvent;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.mscontrol.MediaServer;
import org.murillo.mscontrol.MediaSessionImpl;

/**
 *
 * @author Sergio
 */
class RecorderImpl implements Recorder {

    private enum State {IDLE,RECORDING};

    private final MediaGroupImpl group;
    private final MediaSessionImpl sess;
    private final Integer recorderId;
    private final ConcurrentLinkedQueue<MediaEventListener<RecorderEvent>> listeners;
    private State state;
    private boolean recording;

    RecorderImpl(MediaSessionImpl sess,MediaGroupImpl group, URI uri) throws MsControlException {
        //Store values
        this.sess = sess;
        this.group = group;
        //Get media server
        MediaServer mediaServer = sess.getMediaServer();
        //Not recording
        recording = false;
        //Set state
        state = State.IDLE;
        //Create masp
        listeners = new ConcurrentLinkedQueue<MediaEventListener<RecorderEvent>>();
        try {
            //Create player
            recorderId = mediaServer.RecorderCreate(sess.getSessionId(), uri.toString());
        } catch (XmlRpcException ex) {
            Logger.getLogger(PlayerImpl.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not create player",ex);
        }
    }

    @Override
    public void record(URI uri, RTC[] rtcs, Parameters prmtrs) throws MsControlException {
        //Check uri
        if (!uri.getScheme().equals("file")) {
            //Fire event
            fireEventAsync(new RecorderEventImpl(this,MediaErr.BAD_ARG,"URI schema not valid"));
            //Exit
            return;
        }
        
        //Get media server
        MediaServer mediaServer = sess.getMediaServer();

        try {
            //Close filename
            recording = mediaServer.RecorderRecord(sess.getSessionId(), recorderId, uri.getPath());
        } catch (XmlRpcException ex) {
            //Log
            Logger.getLogger(RecorderImpl.class.getName()).log(Level.SEVERE, "Could not close file XMLRCPC exception: {0}", ex.getMessage());
            //Fire event
            fireEventAsync(new RecorderEventImpl(this,MediaErr.UNKNOWN_ERROR,"Error recording file"));
            //Exit
            return ;
        }
        //Check if we have been started correcty
        if (!recording)
        {
            //Fire event
            fireEventAsync(new RecorderEventImpl(this,MediaErr.NOT_FOUND,"Error opening file"));
            //Exit
            return;
        }
        //Set state
        state = State.RECORDING;
    }

    @Override
    public void stop() {
        //Check state
        if (recording)
        {
            try {
                //Get media server
                MediaServer mediaServer = sess.getMediaServer();
                //Close it
                mediaServer.PlayerClose(sess.getSessionId(), recorderId);
            } catch (XmlRpcException ex) {
                Logger.getLogger(PlayerImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Set state
            state = State.IDLE;
            //Fire event
            fireEventAsync(new RecorderEventImpl(this,RecorderEvent.RECORD_COMPLETED,ResourceEvent.STOPPED));
        }
    }

    @Override
    public void addListener(MediaEventListener<RecorderEvent> ml) {
        //Add it
        listeners.add(ml);
    }

    @Override
    public void removeListener(MediaEventListener<RecorderEvent> ml) {
        //remove it
        listeners.remove(ml);
    }

    @Override
    public MediaSession getMediaSession() {
        return sess;
    }

     public Integer getRecorderId() {
        return recorderId;
    }

    @Override
    public MediaGroup getContainer() {
        return group;
    }

    private void fireEventAsync(final RecorderEventImpl event) {
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
                    for (MediaEventListener<RecorderEvent> listener : listeners)
                        //Send it
                        listener.onEvent(event);
            }
        });
    }

    void release() {
        //Clear listeners
        listeners.clear();
        //Get media server
        MediaServer mediaServer = sess.getMediaServer();
        try {
            //Remove media session
            mediaServer.RecorderDelete(sess.getSessionId(),recorderId);
        } catch (XmlRpcException ex) {
            Logger.getLogger(MediaSessionImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
