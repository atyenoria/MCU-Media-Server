/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol;

import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaConfigException;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.resource.video.VideoLayout;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.mscontrol.mediagroup.MediaGroupImpl;
import org.murillo.mscontrol.mediagroup.PlayerImpl;
import org.murillo.mscontrol.mixer.MediaMixerImpl;
import org.murillo.mscontrol.networkconnection.NetworkConnectionImpl;
import org.murillo.mscontrol.spi.DriverImpl;

/**
 *
 * @author Sergio
 */
public class MSControlFactoryImpl implements MsControlFactory, MediaServerEventQueue.Listener, MediaSessionImpl.Listener {
    
    private final MediaServer mediaServer;
    private final HashMap<URI,MediaSessionImpl> sessions;
    private Integer count;
    private int queueId;
    private String uuid;
    private MediaServerEventQueue eventQueue;
    private final DriverImpl driver;
    private final ExecutorService threadPool;

    public MSControlFactoryImpl(String uuid,MediaServer mediaServer, ExecutorService threadPool, DriverImpl driver) throws MsControlException {
	//Ensure we have media server
	 if (mediaServer==null)
		 ///Errot
		 throw new MsControlException("Error creating control factory, no media server configured");
	//Store uuid
	this.uuid = uuid;
        //store it
        this.mediaServer = mediaServer;
        //Store driver
        this.driver = driver;
        //Store executor
        this.threadPool = threadPool;
        //Init counter
        this.count = 1;
        //Create session map
        sessions = new HashMap<URI, MediaSessionImpl>();
        //NO queue
        queueId = 0;
        //No queue object
        eventQueue = null;
    }
    
    @Override
    public Properties getProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MediaSession createMediaSession() throws MsControlException 
    {
        URI uri = null;
        MediaSessionImpl session = null;

        synchronized(sessions)
        {
            //Check if there is a quee
            if (queueId==0)
            {
                //Create it
                startEventListening();
                //Register media session
                driver.registerMediaSessionList(sessions);
            }
            try {
                //Create uri
                uri = new URI("mscontrol", uuid, "/MediaSessions/" + (count++), null);
            } catch (URISyntaxException ex) {
                Logger.getLogger(MSControlFactoryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Create the object
            session = new MediaSessionImpl(uri, mediaServer,threadPool, queueId, this);
            //Append it
            sessions.put(uri, session);
        }
        //Adn return
        return session;
    }
    
    @Override
    public MediaConfig getMediaConfig(Configuration<?> cfg) throws MediaConfigException {
        if (cfg.equals(NetworkConnection.BASIC)) {
            return NetworkConnectionImpl.BASE_CONFIG;
        } else if (cfg.equals(MediaGroup.PLAYER)) {
            return MediaGroupImpl.PLAYER_CONFIG;
        } else if (cfg.equals(MediaMixer.AUDIO)) {
            return MediaMixerImpl.AUDIO_CONFIG;
        }

        return null;
    }

    @Override
    public MediaConfig getMediaConfig(Reader reader) throws MediaConfigException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Parameters createParameters() {
        //Create parameters and exit
        return new ParametersImpl();
    }

    @Override
    public VideoLayout createVideoLayout(String string, Reader reader) throws MediaConfigException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VideoLayout[] getPresetLayouts(int i) throws MediaConfigException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VideoLayout getPresetLayout(String string) throws MediaConfigException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MediaObject getMediaObject(URI uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onPlayerEndOfStream(URI  sessUri, URI playerUri) {
        try {
            //Get session
            MediaSessionImpl sess = sessions.get(sessUri);
            //Get media group
            MediaGroupImpl group = sess.getMediaGroup(playerUri);
            //Get player
            PlayerImpl player = (PlayerImpl) group.getPlayer();
            //Fire event
            player.onEndOfStream();
        } catch (MsControlException ex) {
            Logger.getLogger(MSControlFactoryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void startEventListening() throws MsControlException {
        try {
            //Create event queue
            queueId = mediaServer.EventQueueCreate();
            //Attach
            eventQueue = mediaServer.getEventQueue(queueId);
            //Set listener
            eventQueue.setListener(this);
            //Start listening for events
            eventQueue.start();
            //Start listeneing
        } catch (XmlRpcException ex) {
            //No queue
            queueId = 0;
            //Log
            Logger.getLogger(MSControlFactoryImpl.class.getName()).log(Level.SEVERE, null, ex);
            //Throw exception
            throw new MsControlException("Cound not create event queue in media server\n",ex);
        }
    }

    @Override
    protected void finalize() throws Throwable {

        synchronized (sessions)
        {
            //Clean sessions
            for (MediaSessionImpl sess : sessions.values())
                //Finalize
                sess.release();
            //Clear sessions
            sessions.clear();
            //Stop event listening
            stopEventListening();
            //UnRegister media session
            driver.unregisterMediaSessionList(sessions);
        }
        //Call parent
        super.finalize();
    }

    void stopEventListening() {
         try {
             //Check
             if (eventQueue!=null)
                //Stop waiting for events
                eventQueue.stop();
             //Check
             if (queueId!=0)
                //Delete event queue
                mediaServer.EventQueueDelete(queueId);
        } catch (XmlRpcException ex) {
            Logger.getLogger(MSControlFactoryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Clean
        queueId = 0;
        eventQueue = null;
    }

    @Override
    public void onMediaSessionReleased(URI uri, MediaSessionImpl sess) {
        synchronized(sessions)
        {
            //Remove from map
            sessions.remove(uri);
            //Check if no more session
            if (sessions.isEmpty())
            {
                //Stop events
                stopEventListening();
                //UnRegister media session
                driver.unregisterMediaSessionList(sessions);
            }
        }
    }

    public MediaSessionImpl getMediaSession(String id) throws URISyntaxException {
	//Create uri
        URI uri = new URI("mscontrol", uuid, "/MediaSessions/" + id, null);
	//Return it
	return sessions.get(uri);
    }

    public Collection<MediaSessionImpl> getMediaSessions() {
	return sessions.values();
    }
}
