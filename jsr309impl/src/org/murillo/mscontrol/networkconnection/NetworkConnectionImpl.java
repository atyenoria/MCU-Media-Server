/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.networkconnection;

import java.net.URI;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.JoinableStream;
import javax.media.mscontrol.join.JoinableStream.StreamType;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.resource.Action;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.MediaServer.Codecs.MediaType;
import org.murillo.mscontrol.resource.ContainerImpl;
import org.murillo.mscontrol.MediaServer;
import org.murillo.mscontrol.MediaSessionImpl;
import org.murillo.mscontrol.ParametersImpl;
import org.murillo.mscontrol.ext.codecs.rtcp;
import org.murillo.mscontrol.ext.codecs.rtp;

/**
 *
 * @author Sergio
 */
public class NetworkConnectionImpl extends ContainerImpl implements NetworkConnection  {
	//Configuration pattern related to NetworkConnection.BASE
	public final static Configuration<NetworkConnection> BASE = new Configuration<NetworkConnection>() {};
	public final static Configuration<NetworkConnection> AUDIO  = new Configuration<NetworkConnection>() {};
	
	public final static MediaConfig BASE_CONFIG = NetworkConnectionBasicConfigImpl.getConfig();
	public final static MediaConfig AUDIO_CONFIG = NetworkConnectionBasicConfigImpl.getAudioConfig();
	
	private final int endpointId;
	private final SdpPortManagerImpl sdpPortManager;
	private final MediaServer mediaServer;
	
	public final static Parameters WEBRTC_PARAMETERS = new ParametersImpl();
	
	static 
	{
		WEBRTC_PARAMETERS.put(rtp.dtls		, true);
		WEBRTC_PARAMETERS.put(rtp.ice		, true);
		WEBRTC_PARAMETERS.put(rtp.secure	, true);
		WEBRTC_PARAMETERS.put(rtcp.feedback	, true);
		WEBRTC_PARAMETERS.put(rtcp.rtx		, true);
	}

	public NetworkConnectionImpl(MediaSessionImpl sess, URI uri,MediaConfig mc,ParametersImpl params) throws MsControlException {
		//Call parent
		super(sess,uri,params);
		//Add streams
		if (mc.hasStream(StreamType.audio))
			AddStream(StreamType.audio, new NetworkConnectionJoinableStream(sess,this,StreamType.audio));
		if (mc.hasStream(StreamType.video))
			AddStream(StreamType.video, new NetworkConnectionJoinableStream(sess,this,StreamType.video));
		if (mc.hasStream(StreamType.message))
			AddStream(StreamType.message, new NetworkConnectionJoinableStream(sess,this,StreamType.message));
		//Get media server
		mediaServer = session.getMediaServer();
		//The port manager
		sdpPortManager = new SdpPortManagerImpl(this,mc,params);
		try {
			//Create endpoint
			endpointId = mediaServer.EndpointCreate(session.getSessionId(), uri.toString(), true, true, true);
		} catch (XmlRpcException ex) {
			Logger.getLogger(NetworkConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
			//Trhow it
			throw new MsControlException("Could not create network connection",ex);
		}
	}
	
	public MediaServer getMediaServer() {
		return mediaServer;
	}

	@Override
	public SdpPortManager getSdpPortManager() throws MsControlException {
		return sdpPortManager;
	}

	@Override
	public JoinableStream getJoinableStream(StreamType type) throws MsControlException {
		//Return array
		return (JoinableStream) streams.get(type);
	}

	@Override
	public JoinableStream[] getJoinableStreams() throws MsControlException {
		//Return object array
		return (JoinableStream[]) streams.values().toArray(new JoinableStream[streams.size()]);
	}

	@Override
	public Parameters createParameters() {
		//Create new map
		return new ParametersImpl();
	}

	public int getEndpointId() {
		return endpointId;
	}

	public int getSessId() {
		return session.getSessionId();
	}
	
	void startReceiving(SdpPortManagerImpl sdp) throws MsControlException
	{
		try {
			//Check if using DTLS
			if (sdp.getUseDTLS())
			{
				//Get fingerprint
				String localFingerprint = mediaServer.EndpointGetLocalCryptoDTLSFingerprint(sdp.getLocalHash());
				//Set it
				sdp.setLocalFingerprint(localFingerprint);
			}

			//If supported
			if (sdp.getAudioSupported())
			{
				//If not already receiving
				if (sdp.getRecAudioPort()==0)
				{
					//Create rtp map for audio
					sdp.createRTPMap("audio");

					//Check if we are secure
					if (sdp.getIsSecure())
					{
						//Create new cypher
						CryptoInfo info = CryptoInfo.Generate();
						//Set it
						mediaServer.EndpointSetLocalCryptoSDES(session.getSessionId(), endpointId,  MediaType.AUDIO, info.suite, info.key);
						//Set it
						sdp.setLocalCryptoInfo("audio",info);
						//Set property
						sdp.setRTPMediaProperties("audio","secure","1");
					}

					//Check if using ICE
					if (sdp.getUseIce())
					{
						//Create new ICE Info
						ICEInfo info = ICEInfo.Generate();
						//Set them
						mediaServer.EndpointSetLocalSTUNCredentials(session.getSessionId(), endpointId, MediaType.AUDIO, info.ufrag, info.pwd);
						//Set it
						sdp.setLocalIceInfo("audio", info);
					}

					//Get receiving ports
					Integer recAudioPort = mediaServer.EndpointStartReceiving(session.getSessionId(), endpointId, MediaType.AUDIO, sdp.getRtpInMediaMap("audio"));
					//Set ports
					sdp.setRecAudioPort(recAudioPort);
				}
			} else if (sdp.getRecAudioPort()>0) {
				//Stop it
				if (mediaServer.EndpointStopReceiving(session.getSessionId(), endpointId, MediaType.AUDIO))
					//Disable port
					sdp.setRecAudioPort(0);
			}

			//If supported
			if (sdp.getVideoSupported())
			{
				//If not already receiving
				if (sdp.getRecVideoPort()==0)
				{
					//Check if we are secure
					if (sdp.getIsSecure())
					{
						//Create new cypher
						CryptoInfo info = CryptoInfo.Generate();
						//Set it
						mediaServer.EndpointSetLocalCryptoSDES(session.getSessionId(), endpointId,  MediaType.VIDEO, info.suite, info.key);
						//Set it
						sdp.setLocalCryptoInfo("video",info);
						//Set property
						sdp.setRTPMediaProperties("video","secure","1");
					}

					//Check if using ICE
					if (sdp.getUseIce())
					{
						//Create new ICE Info
						ICEInfo info = ICEInfo.Generate();
						//Set them
						mediaServer.EndpointSetLocalSTUNCredentials(session.getSessionId(), endpointId, MediaType.VIDEO, info.ufrag, info.pwd);
						//Set it
						sdp.setLocalIceInfo("video", info);
					}
					//Create rtp map for video
					sdp.createRTPMap("video");
					//Get receiving ports
					Integer recVideoPort = mediaServer.EndpointStartReceiving(session.getSessionId(), endpointId, MediaType.VIDEO, sdp.getRtpInMediaMap("video"));
					//Set ports
					sdp.setRecVideoPort(recVideoPort);
				}
			} else if (sdp.getRecVideoPort()>0) {
				//Stop it
				if (mediaServer.EndpointStopReceiving(session.getSessionId(), endpointId, MediaType.VIDEO))
					//Disable port
					sdp.setRecVideoPort(0);
			}

			//If supported
			if (sdp.getTextSupported() && sdp.getRecTextPort()==0)
			{
				//If not already receiving
				if (sdp.getRecTextPort()==0)
				{
					//Check if we are secure
					if (sdp.getIsSecure())
					{
						//Create new cypher
						CryptoInfo info = CryptoInfo.Generate();
						//Set it
						mediaServer.EndpointSetLocalCryptoSDES(session.getSessionId(), endpointId,  MediaType.TEXT, info.suite, info.key);
						//Set it
						sdp.setLocalCryptoInfo("text",info);
						//Set property
						sdp.setRTPMediaProperties("text","secure","1");
					}

					//Check if using ICE
					if (sdp.getUseIce())
					{
						//Create new ICE Info
						ICEInfo info = ICEInfo.Generate();
						//Set them
						mediaServer.EndpointSetLocalSTUNCredentials(session.getSessionId(), endpointId, MediaType.TEXT, info.ufrag, info.pwd);
						//Set it
						sdp.setLocalIceInfo("text", info);
					}
					//Create rtp map for text
					sdp.createRTPMap("text");
					//Get receiving ports
					Integer recTextPort = mediaServer.EndpointStartReceiving(session.getSessionId(), endpointId, MediaType.TEXT, sdp.getRtpInMediaMap("text"));
					//Set ports
					sdp.setRecTextPort(recTextPort);
				}
			} else if (sdp.getRecTextPort()>0) {
				//Stop it
				if (mediaServer.EndpointStopReceiving(session.getSessionId(), endpointId, MediaType.TEXT))
					//Disable port
					sdp.setRecTextPort(0);
			}

			//And set the sender ip
			sdp.setRecIp(mediaServer.getIp());
		} catch (XmlRpcException ex) {
			Logger.getLogger(NetworkConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
			//Trhow it
			throw new MsControlException("Could not start receiving",ex);
		}
	}
	
	protected void startSending(SdpPortManagerImpl sdp) throws MsControlException, XmlRpcException
	{
		//Check if the stream is supported
		if (sdp.getAudioSupported())
			//Check audio
			if (sdp.getSendAudioPort()!=0 && sdp.getRTPDirection("audio").isReceving())
			{
				//Get the auido stream
				NetworkConnectionJoinableStream stream = (NetworkConnectionJoinableStream)getJoinableStream(StreamType.audio);
				//Update ssetAudioCodecneding codec
				stream.requestAudioCodec(sdp.getAudioCodec());
				//Send
				mediaServer.EndpointStartSending(session.getSessionId(), endpointId, MediaType.AUDIO, sdp.getSendAudioIp(), sdp.getSendAudioPort(), sdp.getRtpOutMediaMap("audio"));
			} else {
				//Stop sending
				mediaServer.EndpointStopSending(session.getSessionId(), endpointId, MediaType.AUDIO);
			}
		
		//Check if the stream is supported
		if (sdp.getVideoSupported())
			//Check video
			if (sdp.getSendVideoPort()!=0 && sdp.getRTPDirection("video").isReceving())
			{
				//Get the auido stream
				NetworkConnectionJoinableStream stream = (NetworkConnectionJoinableStream)getJoinableStream(StreamType.video);
				//Update sneding codec
				stream.requestVideoCodec(sdp.getVideoCodec());
				//Send
				mediaServer.EndpointStartSending(session.getSessionId(), endpointId, MediaType.VIDEO, sdp.getSendVideoIp(), sdp.getSendVideoPort(), sdp.getRtpOutMediaMap("video"));
			} else {
				//Stop sending
				mediaServer.EndpointStopSending(session.getSessionId(), endpointId, MediaType.VIDEO);
			}
		
		//Check if the stream is supported
		if (sdp.getTextSupported())
			//Check text
			if (sdp.getSendTextPort()!=0 && sdp.getRTPDirection("text").isReceving())
			{
				//Send
				mediaServer.EndpointStartSending(session.getSessionId(), endpointId, MediaType.TEXT, sdp.getSendTextIp(), sdp.getSendTextPort(), sdp.getRtpOutMediaMap("text"));
			} else {
				//Stop sending
				mediaServer.EndpointStopSending(session.getSessionId(), endpointId, MediaType.TEXT);
			}
	}
	
	protected void stopSending(SdpPortManagerImpl sdp) throws MsControlException, XmlRpcException 
	{
		//Check if the stream is supported
		if (sdp.getAudioSupported())
			//Check audio
			if (sdp.getSendAudioPort()!=0 && sdp.getRTPDirection("audio").isReceving())
				//Stop sending
				mediaServer.EndpointStopSending(session.getSessionId(), endpointId, MediaType.AUDIO);
		//Check if the stream is supported
		if (sdp.getVideoSupported())
			//Check video
			if (sdp.getSendVideoPort()!=0 && sdp.getRTPDirection("video").isReceving())
				//Stop sending
				mediaServer.EndpointStopSending(session.getSessionId(), endpointId, MediaType.VIDEO);
		//Check if the stream is supported
		if (sdp.getTextSupported())
			//Check text
			if (sdp.getSendTextPort()!=0 && sdp.getRTPDirection("text").isReceving())
				//Stop sending
				mediaServer.EndpointStopSending(session.getSessionId(), endpointId, MediaType.TEXT);
	}
	
	@Override
	public void triggerAction(Action action) {
		//Check if it is a picture_fast_update
		if (action.toString().equalsIgnoreCase("org.murillo.mscontrol.picture_fast_update"))
		{
			try {
				//Send FPU
				mediaServer.EndpointRequestUpdate(session.getSessionId(), endpointId, MediaType.VIDEO);
			} catch (XmlRpcException ex) {
				Logger.getLogger(NetworkConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public <R> R getResource(Class<R> type) throws MsControlException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MediaConfig getConfig() {
		return BASE_CONFIG;
	}

	@Override
	public void release() {
		//Free joins
		releaseJoins();
		try {
			//Delete endpoint
			mediaServer.EndpointDelete(session.getSessionId(),endpointId);
		} catch (XmlRpcException ex) {
			Logger.getLogger(NetworkConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
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

	public void onSDPNegotiationDone(SdpPortManagerImpl sdp) throws XmlRpcException {
	
		//Get conf id
		Integer sessId = session.getSessionId();

		//If supported
		if (sdp.getAudioSupported())
		{

			//Check if DTLS enabled
			if (sdp.getUseDTLS())
			{
				//Get cryto info
				DTLSInfo info = sdp.getRemoteDTLSInfo("audio");
				//If present
				if (info!=null)
					//Set it
					mediaServer.EndpointSetRemoteCryptoDTLS(sessId, endpointId, MediaType.AUDIO, info.getSetup(), info.getHash(), info.getFingerprint());
			} else {
				//Get cryto info
				CryptoInfo info = sdp.getRemoteCryptoInfo("audio");
				//If present
				if (info!=null)
					//Set it
				  mediaServer.EndpointSetRemoteCryptoSDES(sessId, endpointId, MediaType.AUDIO, info.suite, info.key);
			}

			//Get ice info
			ICEInfo ice = sdp.getRemoteICEInfo("audio");
			//If present
			if (ice!=null)
				//Set it
				mediaServer.EndpointSetRemoteSTUNCredentials(sessId, endpointId, MediaType.AUDIO, ice.ufrag, ice.pwd);
			//Set RTP properties
			mediaServer.EndpointSetRTPProperties(sessId, endpointId, MediaType.AUDIO, sdp.getRTPMediaProperties("audio"));
		}

		//If supported
		if (sdp.getVideoSupported())
		{

			//Check if DTLS enabled
			if (sdp.getUseDTLS())
			{
				//Get cryto info
				DTLSInfo info = sdp.getRemoteDTLSInfo("video");
				//If present
				if (info!=null)
					//Set it
					mediaServer.EndpointSetRemoteCryptoDTLS(sessId, endpointId, MediaType.VIDEO,  info.getSetup(), info.getHash(), info.getFingerprint());
			} else {
				//Get cryto info
				CryptoInfo info = sdp.getRemoteCryptoInfo("video");
				//If present
				if (info!=null)
					//Set it
				mediaServer.EndpointSetRemoteCryptoSDES(sessId, endpointId, MediaType.VIDEO, info.suite, info.key);
			}

			//Get ice info
			ICEInfo ice = sdp.getRemoteICEInfo("video");
			//If present
			if (ice!=null)
				//Set it
				mediaServer.EndpointSetRemoteSTUNCredentials(sessId, endpointId, MediaType.VIDEO, ice.ufrag, ice.pwd);
			//Set RTP properties
			mediaServer.EndpointSetRTPProperties(sessId, endpointId, MediaType.VIDEO, sdp.getRTPMediaProperties("video"));
		}

		//If supported
		if (sdp.getTextSupported())
		{
			//Check if DTLS enabled
			if (sdp.getUseDTLS())
			{
				//Get cryto info
				DTLSInfo info = sdp.getRemoteDTLSInfo("text");
				//If present
				if (info!=null)
					//Set it
					mediaServer.EndpointSetRemoteCryptoDTLS(sessId, endpointId, MediaType.TEXT, info.getSetup(), info.getHash(), info.getFingerprint());
			} else {
				//Get cryto info
				CryptoInfo info = sdp.getRemoteCryptoInfo("text");
				//If present
				if (info!=null)
					//Set it
					mediaServer.EndpointSetRemoteCryptoSDES(sessId, endpointId, MediaType.TEXT, info.suite, info.key);
			}

			//Get ice info
			ICEInfo ice = sdp.getRemoteICEInfo("text");
			//If present
			if (ice!=null)
				//Set it
				mediaServer.EndpointSetRemoteSTUNCredentials(sessId, endpointId, MediaType.TEXT, ice.ufrag, ice.pwd);
			//Set RTP properties
			mediaServer.EndpointSetRTPProperties(sessId, endpointId, MediaType.TEXT, sdp.getRTPMediaProperties("text"));
		}
	}
	
}
