/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.mixer;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinableStream;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.MediaServer.XmlRPCJSR309Client;
import org.murillo.mscontrol.JoinableStreamImpl;
import org.murillo.mscontrol.MediaServer;
import org.murillo.mscontrol.MediaSessionImpl;
import org.murillo.mscontrol.mediagroup.PlayerJoinableStream;
import org.murillo.mscontrol.networkconnection.NetworkConnectionImpl;
import org.murillo.mscontrol.networkconnection.NetworkConnectionJoinableStream;

/**
 *
 * @author Sergio
 */
public class MixerAdapterJoinableStreamAudio extends JoinableStreamImpl implements JoinableStream {
    private final MediaSessionImpl sess;
    private final URI uri;
    private final Integer portId;
    private final int mixerId;

    MixerAdapterJoinableStreamAudio(MediaSessionImpl sess, MediaMixerImpl mixer, URI uri) throws MsControlException {
        //Call parent
        super(sess,mixer,StreamType.audio);
        //Store values
        this.sess = sess;
        this.uri = uri;
        this.mixerId = mixer.getMixerId(StreamType.audio);
        //Get media server
        MediaServer mediaServer = sess.getMediaServer();
        try {
            //Create endpoint
            portId = mediaServer.AudioMixerPortCreate(sess.getSessionId(), mixerId, uri.toString());
        } catch (XmlRpcException ex) {
            Logger.getLogger(NetworkConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not MixerAdapterJoinableStreamAudio",ex);
        }
    }

    public Integer getMixerId() {
        return mixerId;
    }
    public Integer getPortId() {
        return portId;
    }

    @Override
    public int setAudioCodec(Integer audioCodec) throws MsControlException {
        //Get media server
        MediaServer mediaServer = sess.getMediaServer();
        try {
            //Set it
            mediaServer.AudioMixerPortSetCodec(sess.getSessionId(),mixerId,portId,audioCodec);
        } catch (XmlRpcException ex) {
            Logger.getLogger(NetworkConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could sett audio codec",ex);
        }
        return 1;
    }

    public void release() {
        //Get media server
        MediaServer mediaServer = sess.getMediaServer();
        try {
            //Delete endpoint
            mediaServer.AudioMixerPortDelete(sess.getSessionId(), mixerId, portId);
        } catch (XmlRpcException ex) {
            Logger.getLogger(NetworkConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void attachEndpoint(NetworkConnectionJoinableStream stream) throws MsControlException {
        //Get xml rpc client
        XmlRPCJSR309Client client = sess.getMediaServer();
        try {
            //And attach
            client.AudioMixerPortAttachToEndpoint(sess.getSessionId(), mixerId, portId, stream.getConnection().getEndpointId());
        } catch (XmlRpcException ex) {
            Logger.getLogger(NetworkConnectionJoinableStream.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not attach stream",ex);
        }
    }

    @Override
    protected void attachPlayer(PlayerJoinableStream stream) throws MsControlException {
        //Get xml rpc client
        XmlRPCJSR309Client client = sess.getMediaServer();
        try {
            //And attach
            client.AudioMixerPortAttachToPlayer(sess.getSessionId(), mixerId, portId, stream.getPlayer().getPlayerId());
        } catch (XmlRpcException ex) {
            Logger.getLogger(NetworkConnectionJoinableStream.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not attach stream",ex);
        }
    }

    @Override
    protected void attachMixer(MixerAdapterJoinableStreamAudio stream) throws MsControlException {
    }

    @Override
    public void dettach() throws MsControlException {
        //Get xml rpc client
        XmlRPCJSR309Client client = sess.getMediaServer();
        try {
            //And dettach
            client.AudioMixerPortDettach(sess.getSessionId(), mixerId, portId);
        } catch (XmlRpcException ex) {
            Logger.getLogger(MixerAdapterJoinableStreamAudio.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not dettach stream",ex);
        }
    }

    @Override
    protected void attachMixer(MixerAdapterJoinableStreamVideo mixerAdapterJoinableStreamVideo) throws MsControlException {
    }
}
