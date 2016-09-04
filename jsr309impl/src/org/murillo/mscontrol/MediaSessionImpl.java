/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.vxml.VxmlDialog;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.mscontrol.mediagroup.MediaGroupImpl;
import org.murillo.mscontrol.mixer.MediaMixerImpl;
import org.murillo.mscontrol.networkconnection.NetworkConnectionImpl;

/**
 *
 * @author Sergio
 */
public class MediaSessionImpl extends MediaObjectImpl implements MediaSession {
    private final URI uri;
    private final int sessId;
    private final MediaServer mediaServer;
    private final HashMap<URI,NetworkConnectionImpl> networkconnections;
    private final HashMap<URI,MediaGroupImpl> mediaGroups;
    private final HashMap<URI,MediaMixerImpl> mixers;
    private final HashMap<String,Object> attributes;
    private final Listener listener;
    private final ExecutorService threadPool;

    public interface Listener {
        void onMediaSessionReleased(URI uri,MediaSessionImpl sess);
    }

    MediaSessionImpl(URI uri, MediaServer mediaServer,ExecutorService threadPool,int queueId, Listener listener) throws MsControlException {
        //call parent
        super(uri);
        //Store values
        this.uri = uri;
        this.mediaServer = mediaServer;
        this.threadPool = Executors.newCachedThreadPool();//threadPool;
        this.listener = listener;
        //Create attributes map
        attributes = new HashMap<String,Object>();
        //Create objetcs map
        networkconnections = new HashMap<URI,NetworkConnectionImpl>();
        mediaGroups = new  HashMap<URI,MediaGroupImpl>();
        mixers = new  HashMap<URI,MediaMixerImpl>();

        try {
            //Create media session
            sessId = mediaServer.MediaSessionCreate(uri.toString(),queueId);
        } catch (XmlRpcException ex) {
            //Log
            Logger.getLogger(MediaSessionImpl.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not create media session",ex);
        }
    }

    public MediaServer getMediaServer() {
        //Return the media server
        return mediaServer;
    }

    @Override
    public NetworkConnection createNetworkConnection(Configuration<NetworkConnection> pattern) throws MsControlException {
        //Create nc
        return createNetworkConnection(pattern,Parameters.NO_PARAMETER);
    }

    @Override
    public NetworkConnection createNetworkConnection(Configuration<NetworkConnection> pattern, Parameters input) throws MsControlException {
	MediaConfig mc;
	
        //Check pattern
        if (pattern==null)
            //Should not be null
            throw new MsControlException("NetworkConnection Configuration cannot be null");
	//Get media config
	if (pattern == NetworkConnection.BASIC )
		//Set it
		mc = NetworkConnectionImpl.BASE_CONFIG;
	else if (pattern == NetworkConnectionImpl.AUDIO)
		//Set it
		mc = NetworkConnectionImpl.AUDIO_CONFIG;
	else
		throw new MsControlException("Unsupported NetworkConnection Configuration");
	//Sanetize params
	ParametersImpl params = ParametersImpl.sanetize(input);
        //Get uid
        String uid = "NetworkConnections/" + UUID.randomUUID().toString().replace("-", "");
        //Check id
        if (params.containsKey(MEDIAOBJECT_ID))
            //Set it
            uid = (String) params.get(MEDIAOBJECT_ID);
        //Create chidl uri
        URI child = createChildURI(uid,uri);

        //Create media group
        NetworkConnectionImpl nc = new NetworkConnectionImpl(this,child,mc,params);
        //Add it
        networkconnections.put(child, nc);
        //Return  object
        return nc;
    }

    @Override
    public NetworkConnection createNetworkConnection(MediaConfig mc, Parameters input) throws MsControlException {
        //Check pattern
        if (mc==null)
            //Should not be null
            throw new MsControlException("NetworkConnection MediaConfig cannot be null");
	//Sanetize params
	ParametersImpl params = ParametersImpl.sanetize(input);
        //Get uid
        String uid = "NetworConnections/" + UUID.randomUUID().toString().replace("-", "");
                //Check id
        if (params.containsKey(MEDIAOBJECT_ID))
            //Set it
            uid = (String) params.get(MEDIAOBJECT_ID);
        //Create chidl uri
        URI child = createChildURI(uid,uri);
        //Create media group
        NetworkConnectionImpl nc = new NetworkConnectionImpl(this,child,mc,params);
        //Add it
        networkconnections.put(child, nc);
        //Return  object
        return nc;
    }

    @Override
    public MediaGroup createMediaGroup(Configuration<MediaGroup> config) throws MsControlException {
        //Create without parameters
        return createMediaGroup(config,Parameters.NO_PARAMETER);
    }

    @Override
    public MediaGroup createMediaGroup(Configuration<MediaGroup> config, Parameters input) throws MsControlException {
        //Check configuration
        if (config==null)
            //Should not be null
            throw new MsControlException("MediaGroup configuration cannot be null");
	//Sanetize params
	ParametersImpl params = ParametersImpl.sanetize(input);
        //Get uid
        String uid = "MediaGroups/" + UUID.randomUUID().toString().replace("-", "");
        //Check id
        if (params.containsKey(MEDIAOBJECT_ID))
            //Set it
            uid = (String) params.get(MEDIAOBJECT_ID);
        //Create chidl uri
        URI child = createChildURI(uid,uri);
        //Create media group
        MediaGroupImpl group = new MediaGroupImpl(this,child,params);
        //Add it
        mediaGroups.put(child, group);
        //Create object
        return group;
    }

    @Override
    public MediaGroup createMediaGroup(MediaConfig config, Parameters input) throws MsControlException {
        //Get uid
        String uid = "MediaGroups/" + UUID.randomUUID().toString().replace("-", "");
	//Sanetize params
	ParametersImpl params = ParametersImpl.sanetize(input);
        //Check id
        if (params.containsKey(MEDIAOBJECT_ID))
            //Set it
            uid = (String) params.get(MEDIAOBJECT_ID);
        //Create chidl uri
        URI child = createChildURI(uid,uri);
        //Create media group
        MediaGroupImpl group = new MediaGroupImpl(this,child,params);
        //Add it
        mediaGroups.put(child, group);
        //Create object
        return group;
    }

    public MediaGroupImpl getMediaGroup(URI groupUri) {
        return mediaGroups.get(groupUri);
    }

    public NetworkConnectionImpl getNetworkConnection(String uid) throws MsControlException {
	//Create chidl uri
        URI child = createChildURI(uid,uri);
	//Get network connection
	return networkconnections.get(child);
    }


    @Override
    public MediaMixer createMediaMixer(Configuration<MediaMixer> config) throws MsControlException {
        //Create mixer
        return createMediaMixer(config,Parameters.NO_PARAMETER);
    }

    @Override
    public MediaMixer createMediaMixer(Configuration<MediaMixer> config, Parameters input) throws MsControlException {
        //Check configuration
        if (config==null)
            //Should not be null
            throw new MsControlException("MediaMixer Configuration cannot be null");
	//Sanetize params
	ParametersImpl params = ParametersImpl.sanetize(input);
        //Get uid
        String uid = "MediaMixers/" + UUID.randomUUID().toString().replace("-", "");
        //Check id
        if (params!=null && params.containsKey(MEDIAOBJECT_ID))
            //Set it
            uid = (String) params.get(MEDIAOBJECT_ID);
        //Create chidl uri
        URI child = createChildURI(uid,uri);
        //Create media group
        MediaMixerImpl mixer = new MediaMixerImpl(this,child,params);
        //Store
        mixers.put(child, mixer);
        //Return object
        return mixer;
    }

    @Override
    public MediaMixer createMediaMixer(MediaConfig config, Parameters input) throws MsControlException {
        //Get uid
        String uid = "MediaMixers/" + UUID.randomUUID().toString().replace("-", "");
	//Sanetize params
	ParametersImpl params = ParametersImpl.sanetize(input);
        //Check id
        if (params.containsKey(MEDIAOBJECT_ID))
            //Set it
            uid = (String) params.get(MEDIAOBJECT_ID);
        //Create chidl uri
        URI child = createChildURI(uid,uri);
        //Create media group
        MediaMixerImpl mixer = new MediaMixerImpl(this,child,params);
        //Store
        mixers.put(child, mixer);
        //Return object
        return mixer;
    }

    @Override
    public VxmlDialog createVxmlDialog(Parameters prmtrs) throws MsControlException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getAttribute(String string) {
        //Return attribute
        return attributes.get(string);
    }

    @Override
    public void removeAttribute(String string) {
        //Remove it
        attributes.remove(string);
    }

    @Override
    public void setAttribute(String string, Object object) {
        //add it
        attributes.put(string, object);
    }

    @Override
    public Iterator<String> getAttributeNames() {
        //Return iterator
        return attributes.keySet().iterator();
    }

   
    @Override
    public void release() {
        try {
            //Remove media session
            mediaServer.MediaSessionDelete(sessId);
            //Clean objects
            networkconnections.clear();
            mediaGroups.clear();
            mixers.clear();
            //Call listener
            listener.onMediaSessionReleased(uri, this);
        } catch (XmlRpcException ex) {
            Logger.getLogger(MediaSessionImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getSessionId() {
        return sessId;
    }

    private URI createChildURI(String uid, URI uri) throws MsControlException {
        //NO uri
        URI child = null;
        //Check uid for invalid characters
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
        if (networkconnections.containsKey(child) || mediaGroups.containsKey(child) || mixers.containsKey(child))
            //Error
            throw new MsControlException("Duplicated URI for given object id");
        return child;
    }

    @Override
    public Iterator<MediaObject> getMediaObjects() {
        //Create new set
        HashSet<MediaObject> objects = new HashSet<MediaObject>();
        //Add nc
        objects.addAll(networkconnections.values());
        //Add mg
        objects.addAll(mediaGroups.values());
        //Add mixers
        objects.addAll(mixers.values());
        //Return them
        return objects.iterator();
    }

    @Override
    public <T extends MediaObject> Iterator<T> getMediaObjects(Class<T> type) {
        if (NetworkConnection.class.equals(type))
            return (Iterator<T>) networkconnections.values().iterator();
        else if (MediaGroup.class.equals(type))
            return (Iterator<T>) mediaGroups.values().iterator();
        else if (MediaMixer.class.equals(type))
            return (Iterator<T>) mixers.values().iterator();
        else
            return null;
    }

    public void Exec(Runnable task) {
        try {
            //Execute
            threadPool.execute(task);
        } catch (Exception ex) {
            Logger.getLogger("jsr309").log(Level.SEVERE,"Error executing task",ex);
        }
    }

    public Collection<NetworkConnectionImpl> getNetworkCollections() {
	return networkconnections.values();
    }

}
