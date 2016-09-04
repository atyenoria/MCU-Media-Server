/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.mixer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.JoinableStream.StreamType;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.mixer.MixerAdapter;
import javax.media.mscontrol.mixer.MixerEvent;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.video.VideoRenderer;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.mscontrol.JoinableImpl;
import org.murillo.mscontrol.resource.ContainerImpl;
import org.murillo.mscontrol.MediaServer;
import org.murillo.mscontrol.MediaSessionImpl;
import org.murillo.mscontrol.ParametersImpl;
import org.murillo.mscontrol.mediagroup.PlayerImpl;
import org.murillo.mscontrol.resource.video.VideoRendererImpl;

/**
 *
 * @author Sergio
 */
public class MediaMixerImpl extends ContainerImpl implements MediaMixer{

    public final static MediaConfig AUDIO_CONFIG = MixerAudioConfig.getConfig();
    public final static MediaConfig AUDIO_VIDEO_CONFIG = MixerAudioVideoConfig.getConfig();
    public final static MediaConfig AUDIO_VIDEO_RENDERING_CONFIG = MixerAudioVideoRenderingConfig.getConfig();

    private final MediaSessionImpl sess;
    private final URI uri;
    private final HashMap<URI,MixerAdapterImpl> adaptors;
    private final HashMap<JoinableImpl,MixerAdapterImpl> hidden;
    private EnumMap<StreamType,Integer> mixers;
    private final VideoRendererImpl videoRenderer;

    public MediaMixerImpl(MediaSessionImpl sess,URI uri,ParametersImpl params) throws MsControlException {
        //Call parent
        super(sess,uri,params);
        //Store values
        this.sess = sess;
        this.uri = uri;
        //Create ports and adaptors maps
        adaptors = new HashMap<URI,MixerAdapterImpl>();
        hidden = new HashMap<JoinableImpl,MixerAdapterImpl>();
        //Create supported media types
        mixers = new EnumMap<StreamType,Integer>(StreamType.class);
        //Get media server
        MediaServer mediaServer = sess.getMediaServer();
        try {
            //Get audio uri
            URI audioUri = createChildUri("audio", uri);
            //Create audio mixer
            Integer audioMixerId = mediaServer.AudioMixerCreate(sess.getSessionId(), audioUri.toString());
            //Add it
            mixers.put(StreamType.audio, audioMixerId);
            //Get video uri
            URI videoUri = createChildUri("video", uri);
            //Create audio mixer
            Integer videoMixerId = mediaServer.VideoMixerCreate(sess.getSessionId(), videoUri.toString());
            //Add it
            mixers.put(StreamType.video, videoMixerId);
        } catch (XmlRpcException ex) {
            Logger.getLogger(PlayerImpl.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not create media mixer",ex);
        }
        //Create video renderer resource
        videoRenderer = new VideoRendererImpl(this,0);
    }

    @Override
    public JoinableImpl AddJoin(Direction direction, JoinableImpl joinable) throws MsControlException{
        //Create new adapter
        MixerAdapterImpl adapter = (MixerAdapterImpl)createMixerAdapter(getConfig(), Parameters.NO_PARAMETER);
        //Add hidden adaptor for join
        hidden.put(joinable, adapter);
        //And joint to it
        return adapter.AddJoin(direction, joinable);
    }

    @Override
    public JoinableImpl RemoveJoin(JoinableImpl joinable)  throws MsControlException{
        //Remove hidden adpator
        MixerAdapterImpl adapter = hidden.remove(joinable);
        //Release it
        adapter.release();
        //Remove from it
        adapter.RemoveJoin(joinable);
        //And return
        return adapter;
    }

    public Set<StreamType> getSupportedTypes() {
        return mixers.keySet();
    }

    @Override
    public MixerAdapter createMixerAdapter(Configuration<MixerAdapter> config) throws MsControlException {
        return createMixerAdapter(config, Parameters.NO_PARAMETER);
    }

    @Override
    public MixerAdapter createMixerAdapter(Configuration<MixerAdapter> config, Parameters input) throws MsControlException {
        //Check configuration
        if (config==null)
            //Should not be null
            throw new MsControlException("MediaGroup configuration cannot be null");
	//Sanetize params
	ParametersImpl params = ParametersImpl.sanetize(input);
        //Get uid
        String uid = "MG." + UUID.randomUUID().toString().replace("-", "");
        //Check id
        if (params.containsKey(MEDIAOBJECT_ID))
            //Set it
            uid = (String) params.get(MEDIAOBJECT_ID);
        //Create chidl uri
        URI child = createChildUri(uid,uri);
        //Create adapter
        MixerAdapterImpl mixerAdapterImpl = new MixerAdapterImpl(sess, this, child, params);
        //Add it to map
        adaptors.put(child, mixerAdapterImpl);
        //Return mixer
        return mixerAdapterImpl;
    }

    @Override
    public MixerAdapter createMixerAdapter(MediaConfig config, Parameters input) throws MsControlException {
        //Get uid
        String uid = "MG." + UUID.randomUUID().toString().replace("-", "");
	//Sanetize params
	ParametersImpl params = ParametersImpl.sanetize(input);
        //Check id
        if (params.containsKey(MEDIAOBJECT_ID))
            //Set it
            uid = (String) params.get(MEDIAOBJECT_ID);
        //Create chidl uri
        URI child = createChildUri(uid,uri);
        //Create adapter
        MixerAdapterImpl mixerAdapterImpl = new MixerAdapterImpl(sess, this, child,params);
        //Add it to map
        adaptors.put(child, mixerAdapterImpl);
        //Return mixer
        return mixerAdapterImpl;
    }

    @Override
    public void triggerAction(Action action) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R> R getResource(Class<R> type) throws MsControlException {
        if(type.equals(VideoRenderer.class))
            return (R) videoRenderer;
        //NOt found
        throw new MsControlException("Resource not found");
    }

    @Override
    public MediaConfig getConfig() {
        return AUDIO_CONFIG;
    }
    
    @Override
    public void release() {
        //Free joins
        releaseJoins();
        //Get media server
        MediaServer mediaServer = sess.getMediaServer();
        //For each mixer
        for(Entry<StreamType,Integer> pair : mixers.entrySet())
        {
            //Depending on the type
            switch(pair.getKey())
            {
                case audio:
                    try {
                        //Remove media session
                        mediaServer.AudioMixerDelete(sess.getSessionId(),pair.getValue());
                    } catch (XmlRpcException ex) {
                        Logger.getLogger(MediaSessionImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case video:
                    try {
                        //Remove media session
                        mediaServer.VideoMixerDelete(sess.getSessionId(),pair.getValue());
                    } catch (XmlRpcException ex) {
                        Logger.getLogger(MediaSessionImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
            }
        }
    }
    
    @Override
    public Iterator<MediaObject> getMediaObjects() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T extends MediaObject> Iterator<T> getMediaObjects(Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addListener(MediaEventListener<MixerEvent> ml) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeListener(MediaEventListener<MixerEvent> ml) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private URI createChildUri(String uid, URI uri) throws MsControlException {
        //NO uri
        URI child = null;
        //Check uid for first invalid characters
        if (uid.indexOf('*')==0 ||
                uid.indexOf('+')==0 ||
                uid.indexOf(' ')==0 ||
                uid.indexOf('-')==0 ||
                uid.indexOf(':')==0 ||
                uid.indexOf('_')==0 ||
                uid.indexOf('<')==0 ||
                uid.indexOf('>')==0)
            throw new MsControlException("Ilegar character in object id");
        
        try {
            //Create child
            child = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath() + "/"+uid, null, null);
        } catch (URISyntaxException ex) {
            //Rethrow
            throw new MsControlException("Invalid child uri",ex);
        }
        //Check if it is duplicated
        if (adaptors.containsKey(child))
            //Error
            throw new MsControlException("Duplicated URI for given object id "+uri.toString());
        //Return uri
        return child;
    }

    public int getMixerId(StreamType type) {
        return mixers.get(type);
    }
}
