/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.mediagroup;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinableStream.StreamType;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.Recorder;
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalGenerator;
import javax.media.mscontrol.resource.Action;
import org.murillo.mscontrol.resource.ContainerImpl;
import org.murillo.mscontrol.MediaSessionImpl;
import org.murillo.mscontrol.ParametersImpl;

/**
 *
 * @author Sergio
 */
public class MediaGroupImpl extends ContainerImpl implements MediaGroup {

    public final static MediaConfig PLAYER_CONFIG = PlayerConfigImpl.getConfig();
    private final PlayerImpl player;
    private final RecorderImpl recorder;
    
    
    public MediaGroupImpl(MediaSessionImpl session,URI uri,ParametersImpl params) throws MsControlException {
        //Call parent
        super(session,uri,params);
        //Create player
        player = new PlayerImpl(session,this,createChildUri("player",uri));
        //Create recorder
        recorder = new RecorderImpl(session,this,createChildUri("recorder",uri));
        //Add streams
        AddStream(StreamType.audio, new PlayerJoinableStream(session,player,StreamType.audio));
        AddStream(StreamType.video, new PlayerJoinableStream(session,player,StreamType.video));
        AddStream(StreamType.message, new PlayerJoinableStream(session,player,StreamType.message));
    }

    @Override
    public Player getPlayer() throws MsControlException {
        //Return player
        return player;
    }

    @Override
    public Recorder getRecorder() throws MsControlException {
        //Return recorder
        return recorder;
    }

    @Override
    public SignalDetector getSignalDetector() throws MsControlException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SignalGenerator getSignalGenerator() throws MsControlException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stop() {
        //Stop all
        player.stop(true);
        recorder.stop();
    }

    @Override
    public void triggerAction(Action action) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R> R getResource(Class<R> type) throws MsControlException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MediaConfig getConfig() {
        return PLAYER_CONFIG;
    }

    
    @Override
    public void release() {
        //Free joins
        releaseJoins();
        //Delete player
        player.release();
        //Delete recorder
        recorder.release();
    }

    @Override
    public Iterator<MediaObject> getMediaObjects() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T extends MediaObject> Iterator<T> getMediaObjects(Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private URI createChildUri(String child, URI uri) {
        try {
            //Create dummy
            return  new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath() + "/"+child, null, null);
        } catch (URISyntaxException ex) {
        }
        return null;
    }
}
