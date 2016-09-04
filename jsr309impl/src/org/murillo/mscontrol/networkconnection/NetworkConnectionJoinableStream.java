/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.networkconnection;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinableStream;
import javax.media.mscontrol.join.JoinableStream.StreamType;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.MediaServer.Codecs;
import org.murillo.MediaServer.Codecs.MediaType;
import org.murillo.MediaServer.XmlRPCJSR309Client;
import org.murillo.mscontrol.JoinableStreamImpl;
import org.murillo.mscontrol.MediaSessionImpl;
import org.murillo.mscontrol.mediagroup.PlayerJoinableStream;
import org.murillo.mscontrol.mixer.MixerAdapterJoinableStreamAudio;
import org.murillo.mscontrol.mixer.MixerAdapterJoinableStreamVideo;

/**
 *
 * @author Sergio
 */
public class NetworkConnectionJoinableStream extends JoinableStreamImpl implements JoinableStream {
    private final NetworkConnectionImpl conn;
    private final StreamType type;
    private final MediaType media;

    NetworkConnectionJoinableStream(MediaSessionImpl session, NetworkConnectionImpl conn, StreamType type)
    {
        //Call parent
        super(session,conn,type);
        //Store values
        this.conn = conn;
        this.type = type;
        //Get mapping
        if (type.equals(StreamType.audio))
            //Audio
            media = Codecs.MediaType.AUDIO;
        else if(type.equals(StreamType.video))
            //Video
            media = Codecs.MediaType.VIDEO;
        else if(type.equals(StreamType.message))
            //Text
            media = Codecs.MediaType.TEXT;
        else
            //uuh??
            media = null;
    }

    public NetworkConnectionImpl getConnection() {
        //Return parent object
        return conn;
    }

    @Override
    protected void attachEndpoint(NetworkConnectionJoinableStream stream) throws MsControlException {
        //Get Media session
        MediaSessionImpl mediaSession = (MediaSessionImpl)conn.getMediaSession();
        //Get xml rpc client
        XmlRPCJSR309Client client = mediaSession.getMediaServer();
        try {
            //And attach
            client.EndpointAttachToEndpoint(mediaSession.getSessionId(), conn.getEndpointId(), stream.getConnection().getEndpointId(), media);
        } catch (XmlRpcException ex) {
            Logger.getLogger(NetworkConnectionJoinableStream.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not attach stream",ex);
        }
    }

    @Override
    protected void attachPlayer(PlayerJoinableStream stream) throws MsControlException {
        //Get Media session
        MediaSessionImpl mediaSession = (MediaSessionImpl)conn.getMediaSession();
        //Get xml rpc client
        XmlRPCJSR309Client client = mediaSession.getMediaServer();
        try {
            //And attach
            client.EndpointAttachToPlayer(mediaSession.getSessionId(), conn.getEndpointId(), stream.getPlayer().getPlayerId(), media);
        } catch (XmlRpcException ex) {
            Logger.getLogger(NetworkConnectionJoinableStream.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not attach stream",ex);
        }
    }

    @Override
    protected void attachMixer(MixerAdapterJoinableStreamAudio stream) throws MsControlException {
        //Get Media session
        MediaSessionImpl mediaSession = (MediaSessionImpl)conn.getMediaSession();
        //Get xml rpc client
        XmlRPCJSR309Client client = mediaSession.getMediaServer();
        //Get sdp
        SdpPortManagerImpl sdp = (SdpPortManagerImpl) conn.getSdpPortManager();
        try {
            //Get  codec
            Integer audioCodec = sdp.getAudioCodec();
            //Check if present already
            if (audioCodec!=-1)
                //Try to set codec
                stream.setAudioCodec(audioCodec);
            //And attach
            client.EndpointAttachToAudioMixerPort(mediaSession.getSessionId(), conn.getEndpointId(), stream.getMixerId(),stream.getPortId());
        } catch (XmlRpcException ex) {
            Logger.getLogger(NetworkConnectionJoinableStream.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not attach stream",ex);
        }
    }

    @Override
    protected void attachMixer(MixerAdapterJoinableStreamVideo stream) throws MsControlException {
        //Get Media session
        MediaSessionImpl mediaSession = (MediaSessionImpl)conn.getMediaSession();
        //Get sdp
        SdpPortManagerImpl sdp = (SdpPortManagerImpl) conn.getSdpPortManager();
        //Get xml rpc client
        XmlRPCJSR309Client client = mediaSession.getMediaServer();
        try {
            //Get video codec
            Integer videoCodec = sdp.getVideoCodec();
            //Check if present already
            if (videoCodec!=-1)
                //Try to set codec
                stream.setVideoCodec(videoCodec);
            //And attach
            client.EndpointAttachToVideoMixerPort(mediaSession.getSessionId(), conn.getEndpointId(), stream.getMixerId(),stream.getPortId());
        } catch (XmlRpcException ex) {
            Logger.getLogger(NetworkConnectionJoinableStream.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not attach stream",ex);
        }    }

    @Override
    public void dettach() throws MsControlException {
        //Get Media session
        MediaSessionImpl mediaSession = (MediaSessionImpl)conn.getMediaSession();
        //Get xml rpc client
        XmlRPCJSR309Client client = mediaSession.getMediaServer();
        try {
            //And dettach
            client.EndpointDettach(mediaSession.getSessionId(), conn.getEndpointId(), media);
        } catch (XmlRpcException ex) {
            Logger.getLogger(NetworkConnectionJoinableStream.class.getName()).log(Level.SEVERE, null, ex);
            //Trhow it
            throw new MsControlException("Could not dettach stream",ex);
        }
    }
}
