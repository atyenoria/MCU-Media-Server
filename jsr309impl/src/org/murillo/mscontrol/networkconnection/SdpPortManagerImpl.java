/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.networkconnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinableStream;
import javax.media.mscontrol.networkconnection.CodecPolicy;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpException;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.MediaServer.Codecs;
import org.murillo.MediaServer.Codecs.Direction;
import org.murillo.MediaServer.Codecs.MediaType;
import org.murillo.MediaServer.Codecs.Setup;
import org.murillo.abnf.ParserException;
import org.murillo.mscontrol.MediaSessionImpl;
import org.murillo.mscontrol.ParametersImpl;
import org.murillo.mscontrol.ext.codecs;
import org.murillo.mscontrol.ext.codecs.rtcp;
import org.murillo.mscontrol.ext.codecs.rtp;
import org.murillo.mscontrol.util.H264ProfileLevelID;
import org.murillo.sdp.Attribute;
import org.murillo.sdp.Bandwidth;
import org.murillo.sdp.Connection;
import org.murillo.sdp.CryptoAttribute;
import org.murillo.sdp.ExtMapAttribute;
import org.murillo.sdp.FingerprintAttribute;
import org.murillo.sdp.FormatAttribute;
import org.murillo.sdp.MediaDescription;
import org.murillo.sdp.RTPMapAttribute;
import org.murillo.sdp.SSRCAttribute;
import org.murillo.sdp.SSRCGroupAttribute;
import org.murillo.sdp.SessionDescription;

/**
 *
 * @author Sergio
 */
public final class SdpPortManagerImpl implements SdpPortManager{
	private final NetworkConnectionImpl conn;
	private final HashSet<MediaEventListener<SdpPortManagerEvent>> listeners;
	private String recIp;
	private Integer recAudioPort;
	private Integer recVideoPort;
	private Integer recTextPort;
	private Integer sendAudioPort;
	private String  sendAudioIp;
	private Integer sendVideoPort;
	private String  sendVideoIp;
	private Integer sendTextPort;
	private String  sendTextIp;
	private Integer audioCodec;
	private Integer videoCodec;
	private Integer textCodec;
	private Boolean audioMuted;
	private Boolean videoMuted;
	private Boolean textMuted;
	private Boolean audioSupported;
	private Boolean videoSupported;
	private Boolean textSupported;

	private HashMap<String,List<Integer>> supportedCodecs = null;
	private HashSet<Integer> requiredCodecs = null;
	private HashMap<String,HashMap<Integer,Integer>> rtpInMediaMap = null;
	private HashMap<String,HashMap<Integer,Integer>> rtpOutMediaMap = null;
	private HashMap<String,HashMap<String,Integer>> supportedExtensions = null;
	private HashMap<String,HashMap<String,Integer>> rtpExtensionMap = null;
	private ArrayList<MediaDescription> rejectedMedias;
	private String videoContentType;
	private H264ProfileLevelID h264profileLevelId;
	private Integer h264packetization;
	private final String h264profileLevelIdDefault = "42801F";
	private Integer videoBitrate;
	private SessionDescription remoteSDP;
	private SessionDescription localSDP;
	private Boolean isSecure;
	private Boolean useDTLS;
	private Boolean rtcpFeedBack;
	private HashMap<String,CryptoInfo> localCryptoInfo;
	private HashMap<String,CryptoInfo> remoteCryptoInfo;
	private HashMap<String,DTLSInfo> remoteDTLSInfo;
	private Boolean useICE;
	private HashMap<String,ICEInfo> localICEInfo;
	private HashMap<String,ICEInfo> remoteICEInfo;
	private HashMap<String,HashMap<String,String>> rtpMediaProperties;
	private HashMap<String,Direction> rtpDirections;
	private HashMap<String,Setup> rtpSetups;
	private HashMap<String,String> mids;
	private boolean useRTX;
	

	private String localFingerprint;
	private String localHash = "sha-256";
	private CodecPolicy codecPolicy;
	private final ParametersImpl params;

	private static final Logger logger = Logger.getLogger(SdpPortManagerImpl.class.getName());
	private final MediaConfig mc;

	public SdpPortManagerImpl(NetworkConnectionImpl conn,MediaConfig mc,ParametersImpl params) {
		//Store connection
		this.conn = conn;
		this.mc = mc;
		//Store params
		this.params = params;
		//No sending ports
		sendAudioPort = 0;
		sendVideoPort = 0;
		sendTextPort = 0;
		//No receiving ports
		recAudioPort = 0;
		recVideoPort = 0;
		recTextPort = 0;
		//Supported media
		audioSupported = mc.hasStream(JoinableStream.StreamType.audio);
		videoSupported = mc.hasStream(JoinableStream.StreamType.video);
		textSupported = mc.hasStream(JoinableStream.StreamType.message);
		//Create supported codec map
		supportedCodecs = new HashMap<String, List<Integer>>();
		//Create media maps
		rtpInMediaMap = new HashMap<String,HashMap<Integer,Integer>>();
		rtpOutMediaMap = new HashMap<String,HashMap<Integer,Integer>>();
		//No rejected medias and no video content type
		rejectedMedias = new ArrayList<MediaDescription>();
		videoContentType = "";
		//Set default level and packetization
		h264profileLevelId = null;
		h264packetization = 0;
		//Not secure by default
		isSecure = this.params.getBooleanParameter(rtp.secure, false);
		//Not using ice by default
		useICE = this.params.getBooleanParameter(rtp.ice, false);
		//Do not use DTLS by default
		useDTLS = this.params.getBooleanParameter(rtp.dtls, false);
		//Create crypto info maps
		localCryptoInfo = new HashMap<String, CryptoInfo>();
		remoteCryptoInfo = new HashMap<String, CryptoInfo>();
		remoteDTLSInfo = new HashMap<String, DTLSInfo>();
		 //Create ICE info maps
		localICEInfo = new HashMap<String, ICEInfo>();
		remoteICEInfo = new HashMap<String, ICEInfo>();
		//RTP media properties
		rtpMediaProperties = new HashMap<String, HashMap<String, String>>(3);
		//Add default properties
		rtpMediaProperties.put("audio",new HashMap<String,String>());
		rtpMediaProperties.put("video",new HashMap<String,String>());
		rtpMediaProperties.put("text",new HashMap<String,String>());
		//RTP directions
		rtpDirections = new HashMap<String,Direction>(3);
		//Ser default directions
		rtpDirections.put("audio",Direction.SENDRECV);
		rtpDirections.put("video",Direction.SENDRECV);
		rtpDirections.put("text" ,Direction.SENDRECV);
		//RTP setup
		rtpSetups = new HashMap<String,Setup>(3);
		//Actpass by default
		rtpSetups.put("audio",Setup.ACTPASS);
		rtpSetups.put("video",Setup.ACTPASS);
		rtpSetups.put("text" ,Setup.ACTPASS);
		//Default mids
		mids = new HashMap<String,String>(3);
		//Media name by default
		mids.put("audio","audio");
		mids.put("video","video");
		mids.put("text","text");
		//Map of extensions
		supportedExtensions = new HashMap<String, HashMap<String, Integer>>();
		//Create extensions for each media
		supportedExtensions.put("audio", new HashMap<String,Integer>());
		supportedExtensions.put("video", new HashMap<String,Integer>());
		//Check them
		if (this.params.hasParameter(rtp.ext.ssrc_audio_level))
			supportedExtensions.get("audio").put("urn:ietf:params:rtp-hdrext:ssrc-audio-level",1);
		if (this.params.hasParameter(rtp.ext.toffset))
			supportedExtensions.get("video").put("urn:ietf:params:rtp-hdrext:toffset",2);
		if (this.params.hasParameter(rtp.ext.abs_send_time))
			supportedExtensions.get("video").put("http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time",3);
		//Create extension map
		rtpExtensionMap = new HashMap<String, HashMap<String, Integer>>();
		//Has RTCP feedback
		rtcpFeedBack = this.params.getBooleanParameter(rtcp.feedback, false);
		//Using rtx by default
		useRTX = this.params.getBooleanParameter(rtcp.rtx, false);
		//Create supported codec map
		supportedCodecs = new HashMap<String, List<Integer>>();
		//Create required set
		requiredCodecs = new HashSet<Integer>();
		//Create media maps
		rtpInMediaMap = new HashMap<String,HashMap<Integer,Integer>>();
		rtpOutMediaMap = new HashMap<String,HashMap<Integer,Integer>>();
		//Create listeners
		listeners = new HashSet<MediaEventListener<SdpPortManagerEvent>>();
		//Enable all audio codecs
		addSupportedCodec("audio", Codecs.PCMU);
		addSupportedCodec("audio", Codecs.PCMA);
		addSupportedCodec("audio", Codecs.GSM);
		addSupportedCodec("audio", Codecs.AMR);
		addSupportedCodec("audio", Codecs.TELEFONE_EVENT);
		addSupportedCodec("audio", Codecs.SPEEX16);
		addSupportedCodec("audio", Codecs.OPUS);
		//Enable all video codecs
		addSupportedCodec("video", Codecs.H264);
		addSupportedCodec("video", Codecs.H263_1998);
		addSupportedCodec("video", Codecs.H263_1996);
		addSupportedCodec("video", Codecs.VP8);
		addSupportedCodec("video", Codecs.RED);
		//Enable all text codecs
		addSupportedCodec("text", Codecs.T140RED);
		addSupportedCodec("text", Codecs.T140);
	}

	@Override
	public void generateSdpOffer() throws SdpPortManagerException {
		try {
			//Start receivint
			conn.startReceiving(this);
		} catch (MsControlException ex) {
			logger.log(Level.SEVERE, null, ex);
			throw new SdpPortManagerException("Could not start receiving", ex);
		}
		//Create sdp
		localSDP = createSDP();
		//Generate success
		success(SdpPortManagerEvent.OFFER_GENERATED);
	}

	@Override
	public void processSdpOffer(byte[] sdp) throws SdpPortManagerException {
		//Cacht any parsing error
		try {
			//Remove default settings so they are set by the offer
			isSecure = false;
			useDTLS = false;
			rtcpFeedBack = false;
			//Clean data before processing
			rejectedMedias.clear();
			//Store remote sdp
			remoteSDP = SessionDescription.Parse(sdp);
			//Procces it
			processSDP(remoteSDP);
		} catch (ParserException pex) {
			//Send error
			error(SdpPortManagerEvent.SDP_NOT_ACCEPTABLE, pex.getMessage());
			//exit
			throw new SdpException("SDP not acceptable: "+pex.getMessage(), pex);
		} catch (IllegalArgumentException ex) {
			//Send error
			error(SdpPortManagerEvent.SDP_NOT_ACCEPTABLE, ex.getMessage());
			//exit
			throw new SdpException("SDP not acceptable: "+ex.getMessage(), ex);
		} catch (SdpPortManagerException e) {
			//Send error
			error(SdpPortManagerEvent.SDP_NOT_ACCEPTABLE, e.getMessage());
			//exit
			throw new SdpException("SDP not acceptable: "+e.getMessage(), e);
		}
		try {
			//Start receivint
			conn.startReceiving(this);
			//Create sdp
			localSDP = createSDP();
			//Generate success
			success(SdpPortManagerEvent.ANSWER_GENERATED);
			//SDP negotiation done
			conn.onSDPNegotiationDone(this);
			//Start sending
			conn.startSending(this);
		} catch (MsControlException ex) {
			//And thow
			throw new SdpPortManagerException("Could not start", ex);
		} catch (XmlRpcException ex) {
			//And thow
			throw new SdpPortManagerException("Could not start", ex);
		}
	}

	@Override
	public void processSdpAnswer(byte[] sdp) throws SdpPortManagerException {
		try {
		//Store remote sdp
		remoteSDP = SessionDescription.Parse(sdp);
			//Procces it
			processSDP(remoteSDP);
			//Generate success
			success(SdpPortManagerEvent.ANSWER_PROCESSED);
		} catch (IllegalArgumentException ex) {
		//Send error
			error(SdpPortManagerEvent.SDP_NOT_ACCEPTABLE, ex.getMessage());
			//exit
			throw new SdpException("SDP not acceptable "+ex.getMessage(), ex);
	} catch (ParserException ex) {
		//Send error
			error(SdpPortManagerEvent.SDP_NOT_ACCEPTABLE, ex.getMessage());
			//exit
			throw new SdpException("SDP not acceptable "+ex.getMessage(), ex);
	} catch (SdpPortManagerException e) {
			//Send error
			error(SdpPortManagerEvent.SDP_NOT_ACCEPTABLE, e.getMessage());
			//exit
			throw new SdpException("SDP not acceptable "+e.getMessage(), e);
		}
		
		try {
		//SDP negotiation done
		conn.onSDPNegotiationDone(this);
			//Start sending
			conn.startSending(this);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "processSdpAnswer error", ex);
			throw new SdpPortManagerException("Could not start sending", ex);
		}

	}

	@Override
	public void rejectSdpOffer() throws SdpPortManagerException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public byte[] getMediaServerSessionDescription() throws SdpPortManagerException {
		return localSDP.toString().getBytes();
	}

	@Override
	public byte[] getUserAgentSessionDescription() throws SdpPortManagerException {
		return remoteSDP.toString().getBytes();
	}

	@Override
	public void setCodecPolicy(CodecPolicy codecPolicy) throws SdpPortManagerException {
		//Get policies
		String[] mediaTypeCapabilities = codecPolicy.getMediaTypeCapabilities();
		String[] codecCapabilities = codecPolicy.getCodecCapabilities();
		String[] requiredCodecsNames = codecPolicy.getRequiredCodecs();
		String[] excludedCodecs = codecPolicy.getExcludedCodecs();
		String[] codecPreferences = codecPolicy.getCodecPreferences();

		 //The temporal support
		HashSet<MediaType> mediaSupported = new HashSet<MediaType>();

		//If there are restrictions about media types supported
		if (mediaTypeCapabilities.length>0)
		{
			//Check supported medias
			for (String media: mediaTypeCapabilities)
			{
				//Check media
				if ("audio".equalsIgnoreCase(media)) {
					//It is enabled
					mediaSupported.add(MediaType.AUDIO);
				} else if ("video".equalsIgnoreCase(media)) {
					//It is enabled
					mediaSupported.add(MediaType.VIDEO);
				} else if ("text".equalsIgnoreCase(media)) {
					//It is enabled
					mediaSupported.add(MediaType.TEXT);
				}
			}
		} else {
			//Add all
			mediaSupported.add(MediaType.AUDIO);
			mediaSupported.add(MediaType.VIDEO);
			mediaSupported.add(MediaType.TEXT);
		}

		//Create temporal required
		HashSet<Integer> required = new HashSet<Integer>();

		//For each codec
		for (String codec : requiredCodecsNames)
		{
				//Get codec info
				Codecs.CodecInfo info = Codecs.getCodecInfoForname(codec);
				//If we do not know it
				if (info==null)
					//Exit
					throw new SdpPortManagerException("Required codec "+codec+" not supported");
				//Put codecs for that media
				required.add(info.getCodec());
		}

		//The new codecs
		EnumMap<MediaType,HashSet<Integer>> codecs = new EnumMap<MediaType, HashSet<Integer>>(MediaType.class);
		
		//Create codecs
		HashSet<Integer> audioCodecs = new HashSet<Integer>();
		HashSet<Integer> videoCodecs = new HashSet<Integer>();
		HashSet<Integer> textCodecs = new HashSet<Integer>();

		//If media supported
		if (mediaSupported.contains(MediaType.AUDIO))
			//Put in codecs
			codecs.put(MediaType.AUDIO, audioCodecs);
		//If media supported
		if (mediaSupported.contains(MediaType.VIDEO))
			//Put in codecs
			codecs.put(MediaType.VIDEO, videoCodecs);
		//If media supported
		if (mediaSupported.contains(MediaType.TEXT))
			//Put in codecs
			codecs.put(MediaType.TEXT, textCodecs);

		//Create temporal capabilities
		HashSet<Integer> capabilities = new HashSet<Integer>();

		//Check if there codec policies
		if (codecCapabilities.length>0)
		{
			//For each codec
			for(String codec : codecCapabilities)
			{
				//Get codec info
				Codecs.CodecInfo info = Codecs.getCodecInfoForname(codec);
				//If we know it and media is supported
				if (info!=null && mediaSupported.contains(info.getMedia()))
				{
					//Put codecs for that media
					codecs.get(info.getMedia()).add(info.getCodec());
					//And in the capabilities
					capabilities.add(info.getCodec());
				}
			}
		}

		//Check audio codecs
		if (mediaSupported.contains(MediaType.AUDIO) && audioCodecs.isEmpty())
		{
			//Enable all audio codecs
			audioCodecs.add(Codecs.PCMU);
			audioCodecs.add(Codecs.PCMA);
			audioCodecs.add(Codecs.GSM);
			audioCodecs.add(Codecs.AMR);
			audioCodecs.add(Codecs.TELEFONE_EVENT);
		}

		//Check video codecs
		if (mediaSupported.contains(MediaType.VIDEO) && videoCodecs.isEmpty())
		{
			//Enable all video codecs
			videoCodecs.add(Codecs.H264);
			videoCodecs.add(Codecs.H263_1998);
			videoCodecs.add(Codecs.H263_1996);
		}

		//Check text codecs
		if (mediaSupported.contains(MediaType.TEXT) && textCodecs.isEmpty())
		{
			//Enable all text codecs
			textCodecs.add(Codecs.T140RED);
			textCodecs.add(Codecs.T140);
		}

		//Create temporal excluded
		HashSet<Integer> excluded = new HashSet<Integer>();

		//For each exludec codec
		for(String codec : excludedCodecs)
		{
			//Get codec info
			Codecs.CodecInfo info = Codecs.getCodecInfoForname(codec);
			//If we know it
			if (info!=null)
			{
				//First check if it is in the required
				if (required.contains(info.getCodec()))
					//Exception
					throw new SdpPortManagerException("Codec "+codec +" in required and excluded list");
				//Also in the capabilities
				if (capabilities.contains(info.getCodec()))
					//Exception
					throw new SdpPortManagerException("Codec "+codec +" in required and capability list");
				//Remove that codec
				codecs.get(info.getMedia()).remove(info.getCodec());
				//Add to excluded list
				excluded.add(info.getCodec());
			}
		}

		//Disable all
		audioSupported = false;
		videoSupported = false;
		textSupported = false;

		//If audio is enabled
		if (codecs.containsKey(MediaType.AUDIO))
		{
			//Check if we have any codec to offer
			if (codecs.get(MediaType.AUDIO).isEmpty())
				//Exception
				throw new SdpPortManagerException("No supported codecs for audio");
			//Audio enabled
			audioSupported = true;
		 }

		//If video is enabled
		if (codecs.containsKey(MediaType.VIDEO))
		{
			//Check if we have any codec to offer
			if (codecs.get(MediaType.VIDEO).isEmpty())
				//Exception
				throw new SdpPortManagerException("No supported codecs for video");
			//Video enabled
			videoSupported = true;
		}

		//If text is enabled
		if (codecs.containsKey(MediaType.TEXT))
		{
			//Check if we have any codec to offer
			if (codecs.get(MediaType.TEXT).isEmpty())
				throw new SdpPortManagerException("No supported codecs for text");
			//Video  enabled
			videoSupported = true;
		}

		//Store it
		this.codecPolicy = codecPolicy;

		//Clear audio codecs
		clearSupportedCodec("audio");
		//Clear video  codecs
		clearSupportedCodec("video");
		//Clear text codecs
		clearSupportedCodec("text");

		//Set preference
		for (String pref : codecPreferences)
		{
			//Get codec info
			Codecs.CodecInfo info = Codecs.getCodecInfoForname(pref);
			//If we know it and media is suported
			if (info!=null && mediaSupported.contains(info.getMedia()))
			{
				//Check it was not excluded
				if (excluded.contains(info.getCodec()))
					//Exit
					throw new SdpPortManagerException("Prefered codec "+pref+" in exclude list");
				//Get codecs for that media
				HashSet<Integer> mediaCodecs = codecs.get(info.getMedia());
				//If we had it enabled
				if (mediaCodecs!=null && mediaCodecs.contains(info.getCodec()))
				{
					//Add supported codec
					addSupportedCodec(info.getMedia().getName(), info.getCodec());
					//Remove from set
					mediaCodecs.remove(info.getCodec());
				} 
			}
		}

		//For all media
		for (MediaType type : codecs.keySet())
		{
			//For all pending codecs
			for(Integer codec : codecs.get(type))
			{
				//Add supported codec
				addSupportedCodec(type.getName(),codec);
			}
		}

		//Clear required
		requiredCodecs.clear();

		//Set new required
		requiredCodecs.addAll(required);
	}

	@Override
	public CodecPolicy getCodecPolicy() {
		//Return policy
		return codecPolicy;
	}

	@Override
	public NetworkConnection getContainer() {
		//return connection
		return conn;
	}

	@Override
	public void addListener(MediaEventListener<SdpPortManagerEvent> ml) {
		//Add it
		listeners.add(ml);
	}

	@Override
	public void removeListener(MediaEventListener<SdpPortManagerEvent> ml) {
		//Remove it
		listeners.remove(ml);
	}

	@Override
	public MediaSession getMediaSession() {
		//Return media sessions
		return conn.getMediaSession();
	}

	public void clearSupportedCodec(String media) {
		 //Check if we have the media
		 if (supportedCodecs.containsKey(media))
			//clear it
			supportedCodecs.get(media).clear();
	 }

	 public void addSupportedCodec(String media,Integer codec) {
		 //Check if we have the media
		 if (!supportedCodecs.containsKey(media))
			 //Create it
			 supportedCodecs.put(media, new Vector<Integer>());
		 //Add codec to media
		 supportedCodecs.get(media).add(codec);
	 }

	 public Integer getRecAudioPort() {
		return recAudioPort;
	}

	public void setRecAudioPort(Integer recAudioPort) {
		this.recAudioPort = recAudioPort;
	}

	public Integer getRecTextPort() {
		return recTextPort;
	}

	public void setRecTextPort(Integer recTextPort) {
		this.recTextPort = recTextPort;
	}

	public String getRecIp() {
		return recIp;
	}

	public void setRecIp(String recIp) {
		this.recIp = recIp;
	}

	public Integer getRecVideoPort() {
		return recVideoPort;
	}

	public void setRecVideoPort(Integer recVideoPort) {
		this.recVideoPort = recVideoPort;
	}

	public Integer getSendAudioPort() {
		return sendAudioPort;
	}

	public void setSendAudioPort(Integer sendAudioPort) {
		this.sendAudioPort = sendAudioPort;
	}

	public Integer getSendVideoPort() {
		return sendVideoPort;
	}

	public void setSendVideoPort(Integer sendVideoPort) {
		this.sendVideoPort = sendVideoPort;
	}

	public Integer getSendTextPort() {
		return sendTextPort;
	}

	public void setSendTextPort(Integer sendTextPort) {
		this.sendTextPort = sendTextPort;
	}

	public Integer getAudioCodec() {
		return audioCodec;
	}

	public void setAudioCodec(Integer audioCodec) {
		this.audioCodec = audioCodec;
	}

	public Integer getTextCodec() {
		return textCodec;
	}

	public void setTextCodec(Integer textCodec) {
		this.textCodec = textCodec;
	}

	public Integer getVideoCodec() {
		return videoCodec;
	}

	public void setVideoCodec(Integer videoCodec) {
		this.videoCodec = videoCodec;
	}

	public Boolean getAudioMuted() {
		return audioMuted;
	}

	public void setAudioMuted(Boolean audioMuted) {
		this.audioMuted = audioMuted;
	}

	public Boolean getVideoMuted() {
		return videoMuted;
	}

	public void setVideoMuted(Boolean videoMuted) {
		this.videoMuted = videoMuted;
	}

	public Boolean getTextMuted() {
		return textMuted;
	}

	public void setTextMuted(Boolean TextMuted) {
		this.textMuted = TextMuted;
	}

	public Boolean getAudioSupported() {
		return audioSupported;
	}

	public Boolean getTextSupported() {
		return textSupported;
	}

	public Boolean getVideoSupported() {
		return videoSupported;
	}

	public HashMap<Integer,Integer> getRtpInMediaMap(String media) {
		//Return rtp mapping for media
		return rtpInMediaMap.get(media);
	}

	HashMap<Integer, Integer> getRtpOutMediaMap(String media) {
		//Return rtp mapping for media
		return rtpOutMediaMap.get(media);
	}

	public SessionDescription createSDP() {

		SessionDescription sdp = new SessionDescription();

		//Set origin
		sdp.setOrigin("-", conn.getEndpointId(), new Date().getTime(), "IN", "IP4", getRecIp());
		//Set name
		sdp.setSessionName("MediaMixerSession");
		//Set connection info
		sdp.setConnection("IN", "IP4", getRecIp());
		//Set time
		sdp.addTime(0,0);
		//Using ice?
		if (useICE)
			//Add ice lite attribute
			sdp.addAttribute("ice-lite");
		//Check if supported
		if (audioSupported)
			//Add audio related lines to the sdp
			sdp.addMedia(createMediaDescription("audio",recAudioPort));
		//Check if supported
		if (videoSupported)
			//Add video related lines to the sdp
			sdp.addMedia(createMediaDescription("video",recVideoPort));
		//Check if supported
		if (textSupported)
			//Add text related lines to the sdp
			sdp.addMedia(createMediaDescription("text",recTextPort));

		//Add rejecteds medias
		for (MediaDescription md : rejectedMedias)
			//Add it
			sdp.addMedia(md);

		//Return
		return sdp;
	}

	public void createRTPMap(String media)
	{
		//Get supported codecs for media
		List<Integer> codecs = supportedCodecs.get(media);

		//Check if it supports audio
		if (codecs!=null)
		{
			//Create map
			HashMap<Integer, Integer> rtpInMap = new HashMap<Integer, Integer>();
			//Check if rtp map exist already for outgoing
			HashMap<Integer, Integer> rtpOutMap = rtpOutMediaMap.get(media);
			//If we do not have it
			if (rtpOutMap==null)
			{
				//Add all supported audio codecs with default values
				for(Integer codec : codecs)
					//Append it
					rtpInMap.put(codec, codec);
			} else {
				//Add all supported audio codecs with already known mappings
				for(Map.Entry<Integer,Integer> pair : rtpOutMap.entrySet())
					//Check if it is supported
					if (codecs.contains(pair.getValue()))
						//Append it
						rtpInMap.put(pair.getKey(), pair.getValue());
			}

			//Put the map back in the map
			rtpInMediaMap.put(media, rtpInMap);
		}
	}

	private Integer findTypeForCodec(HashMap<Integer, Integer> rtpMap, Integer codec) {
		for (Map.Entry<Integer,Integer> pair  : rtpMap.entrySet())
			if (pair.getValue().equals(codec))
				return pair.getKey();
		return -1;
	}

	private MediaDescription createMediaDescription(String mediaName, Integer port)
	{
		//Create AVP profile
		String rtpProfile = "AVP";
		//If is secure
		if (isSecure)
			//Prepend S
			rtpProfile = "S"+rtpProfile;
		//If has feedback
		if (rtcpFeedBack)
			//Append F
			rtpProfile += "F";
		//Check DTLS
		if (useDTLS)
			//DTLS rtp profile
			rtpProfile = "UDP/TLS/RTP/"+rtpProfile;
		else
			//Normal rtp profile
			rtpProfile = "RTP/"+rtpProfile;
		//Create new meida description with default values
		MediaDescription md = new MediaDescription(mediaName,port,rtpProfile);
		
		//Send and receive
		md.addAttribute(rtpDirections.get(mediaName).reverse().valueOf());

		//Enable rtcp muxing
		md.addAttribute("rtcp-mux");

		//Get mid
		String mid = mids.get(mediaName);
		
		//If found 
		if (mid!=null)
			//Set media id
			md.addAttribute("mid",mid);

		//Check if rtp map exist
		HashMap<Integer, Integer> rtpInMap = rtpInMediaMap.get(mediaName);

		//Check not null
		if (rtpInMap==null)
		{
			//Log
			logger.log(Level.FINE, "addMediaToSdp rtpInMap is null. Disabling media {0} ", new Object[]{mediaName});
			//Disable media
			md.setPort(0);
			//Return empty media
			return md;
		}

		//If we are using ice
		if (useICE)
		{
			//Add host candidate for RTP
			md.addCandidate("1", 1, "UDP", 33554432-1, getRecIp(), port, "host");
			//Check if not using rtcp mux
			if (!rtpMediaProperties.get(mediaName).containsKey("rtcp-mux"))
				//Add host candidate for RTCP
				md.addCandidate("1", 2, "UDP", 33554432-2, getRecIp(), port+1, "host");
			//Get ICE info
			ICEInfo info = localICEInfo.get(mediaName);
			//If ge have it
			if (info!=null)
			{
				//Set credentials
				md.addAttribute("ice-ufrag",info.ufrag);
				md.addAttribute("ice-pwd",info.pwd);
			}
		}

		//If we use dtls
		if (useDTLS)
		{
			//Add fingerprint attribute
			md.addAttribute(new FingerprintAttribute(localHash, localFingerprint));
			//Get our setup
			Setup setup = rtpSetups.get(mediaName);
			//If not set up
			if (setup==null)
				//Then set it to actpass
				setup = Setup.ACTPASS;
			//Add setup atttribute
			md.addAttribute("setup",setup.valueOf());
			//Add connection attribute
			md.addAttribute("connection","new");
		} else if (isSecure) {
			//Get Crypto info for local media
			CryptoInfo info = localCryptoInfo.get(mediaName);

			//f we have crytpo info
			if (info!=null)
				//Append attribute
				md.addAttribute(new CryptoAttribute(1, info.suite, "inline", info.key));
		}

		//Only for webrtc participants
		if (rtcpFeedBack)
		{
			//Create ramdon ssrc
			Long ssrc = Math.round(Math.random()*Integer.MAX_VALUE);
			//The retransmission ssrc
			Long ssrcRTX = Math.round(Math.random()*Integer.MAX_VALUE);
			//Set cname
			String cname = params.getStringParameter(rtcp.cname,conn.getEndpointId()+"@"+conn.getEndpointId());
			//Label
			String msid =  params.getStringParameter(rtcp.msid,conn.getEndpointId()+"@"+conn.getEndpointId());
			//Set app id
			String appId = mediaName.substring(0,1)+"0";
			//If using RTX add the group attribute, only for video
			if (useRTX && "video".equals(mediaName))
				//Add RTX stream as ssrc+1
				md.addAttribute(new SSRCGroupAttribute("FID",Arrays.asList(ssrc.toString(),ssrcRTX.toString())));
			//Add ssrc info
			md.addAttribute(new SSRCAttribute(ssrc, "cname"	  ,cname));
			md.addAttribute(new SSRCAttribute(ssrc, "mslabel" ,msid));
			md.addAttribute(new SSRCAttribute(ssrc, "msid"	  ,msid + " " + appId));
			md.addAttribute(new SSRCAttribute(ssrc, "label"	  ,msid + appId));
			//Set attributes
			rtpMediaProperties.get(mediaName).put("ssrc", ssrc.toString());
			rtpMediaProperties.get(mediaName).put("cname", cname);
			//Add ssrc for RTX, only for video
			if (useRTX && "video".equals(mediaName))
			{
				//Allow nack
				rtpMediaProperties.get(mediaName).put("useNACK", "1");
				//Add ssrc info
				md.addAttribute(new SSRCAttribute(ssrcRTX, "cname"	 ,cname));
				md.addAttribute(new SSRCAttribute(ssrcRTX, "mslabel"     ,msid));
				md.addAttribute(new SSRCAttribute(ssrcRTX, "msid"	 ,msid + " " + appId));
				md.addAttribute(new SSRCAttribute(ssrcRTX, "label"	 ,msid + appId));
				//Set attributes
				rtpMediaProperties.get(mediaName).put("ssrcRTX", ssrcRTX.toString());
			}
		}

		//Add rtmpmap for each codec in supported order
		for (Integer codec : supportedCodecs.get(mediaName))
		{
			//Search for the codec
			for (Entry<Integer,Integer> mapping : rtpInMap.entrySet())
			{
				//Check codec
				if (mapping.getValue().equals(codec))
				{
					//Get fmt mapping
					Integer fmt = mapping.getKey();
					//Append fmt
					md.addFormat(fmt);
					//Opus is stero
					if (!Codecs.OPUS.equals(codec))
						//Add rtmpmap
						md.addRTPMapAttribute(fmt, Codecs.getNameForCodec(mediaName, codec), Codecs.getRateForCodec(mediaName,codec));
					else
						//Add rtmpmap
						md.addRTPMapAttribute(fmt, Codecs.getNameForCodec(mediaName, codec), Codecs.getRateForCodec(mediaName,codec),"2");
					//Depending on the codec
					if (Codecs.H264.equals(codec))
					{
						//Check if we are offering first
						if (h264profileLevelId == null)
							//Set default params
							h264profileLevelId = new H264ProfileLevelID(h264profileLevelIdDefault);
						//Create format
						FormatAttribute fmtp = new FormatAttribute(fmt);
						//Set params-level-id
						fmtp.addParameter("params-level-id",h264profileLevelId.toString());

						//Check packetization mode
						if (h264packetization>0)
							//Add params and packetization mode
							fmtp.addParameter("packetization-mode",h264packetization);

						//Add max mbps
						if (params.hasParameter(codecs.h264.max_mbps))
							//Add param
							fmtp.addParameter("max-mbps", params.getIntParameter(codecs.h264.max_mbps,40500));
						//Add max fs
						if (params.hasParameter(codecs.h264.max_fs))
							//Add param
							fmtp.addParameter("max-fs", params.getIntParameter(codecs.h264.max_fs,1344));
						//Add max br
						if (params.hasParameter(codecs.h264.max_br))
							//Add param
							fmtp.addParameter("max-br", params.getIntParameter(codecs.h264.max_br,906));
						//Add max smbps
						if (params.hasParameter(codecs.h264.max_smbps))
							//Add param
							fmtp.addParameter("max-smbps", params.getIntParameter(codecs.h264.max_smbps,40500));
						//Add max fps
						if (params.hasParameter(codecs.h264.max_fps))
							//Add param
							fmtp.addParameter("max-fps", params.getIntParameter(codecs.h264.max_fps,3000));
						//Add h264 params support
						md.addAttribute(fmtp);
					} else if (Codecs.H263_1996.equals(codec)) {
						//Add h263 supported sizes
						md.addFormatAttribute(fmt,"CIF=1;QCIF=1");
					} else if (Codecs.OPUS.equals(codec)) {
						//Create format
						FormatAttribute fmtp = new FormatAttribute(fmt);
						//P time
						if (params.hasParameter(codecs.opus.ptime))
							//Add param
							fmtp.addParameter("ptime", params.getIntParameter(codecs.opus.ptime,10));
						//Max p time
						if (params.hasParameter(codecs.opus.maxptime))
							//Add param
							fmtp.addParameter("maxptime", params.getIntParameter(codecs.opus.maxptime,10));
						//Min p time
						if (params.hasParameter(codecs.opus.minptime))
							//Add param
							fmtp.addParameter("minptime", params.getIntParameter(codecs.opus.minptime,10));
						//In band fec
						if (params.hasParameter(codecs.opus.useinbandfec))
							//Add param
							fmtp.addParameter("useinbandfec", params.getIntParameter(codecs.opus.useinbandfec,1));
						//Discontious transmission
						if (params.hasParameter(codecs.opus.usedtx))
							//Add param
							fmtp.addParameter("usedtx", params.getIntParameter(codecs.opus.usedtx,1));
						//Stereo
						if (params.hasParameter(codecs.opus.stereo))
							//Add param
							fmtp.addParameter("stereo", params.getIntParameter(codecs.opus.stereo,1));
						//Check codec parameter in params
						if (params.hasParameter(codecs.opus.maxaveragebitrate))
							//Add param
							fmtp.addParameter("maxaveragebitrate", params.getIntParameter(codecs.opus.maxaveragebitrate,32000));
						//Check if anything is there
						if (!fmtp.isEmpty())
							//Add opus params support
							md.addAttribute(fmtp);
					} else if (Codecs.ULPFEC.equals(codec)) {
						//Enable fec
						rtpMediaProperties.get(mediaName).put("useFEC", "1");
					} else if (Codecs.RTX.equals(codec)) {
						//Find VP8 codec
						Integer vp8 = findTypeForCodec(rtpInMap,Codecs.VP8);
						//Check that we have founf it
						if (vp8>0)
							//Add redundancy fmt
							md.addFormatAttribute(fmt,"apt="+vp8);
					} else if (Codecs.T140RED.equals(codec)) {
						//Find t140 codec
						Integer t140 = findTypeForCodec(rtpInMap,Codecs.T140);
						//Check that we have founf it
						if (t140>0)
							//Add redundancy fmt
							md.addFormatAttribute(fmt,t140 + "/" + t140 + "/" + t140);
					}
					
					//Add rtcp-fb fir/pli only for video codecs
					if (mediaName.equals("video") && !Codecs.RED.equals(codec) && !Codecs.ULPFEC.equals(codec) && !Codecs.RTX.equals(codec))
					{
						//Add rtcp-fb nack support
						md.addAttribute("rtcp-fb", fmt+" nack pli");
						//Add fir
						md.addAttribute("rtcp-fb", fmt+" ccm fir");
						//Add Remb
						md.addAttribute("rtcp-fb", fmt+" goog-remb");
					}
				}
			}
		}

		//If it is video and we have found the content attribute
		if (mediaName.equals("video") && !videoContentType.isEmpty())
			//Add attribute
			md.addAttribute("content",videoContentType);

		//Get extensions for media
		HashMap<String, Integer> extensions = rtpExtensionMap.get(mediaName);

		//If we don't have yet an extension map, send the supported
		if (extensions==null)
			//Get supported extensions
			extensions = supportedExtensions.get(mediaName);

		//If we have extensions
		if (extensions!=null)
			//For each one
			for (Entry<String,Integer> pair : extensions.entrySet())
				//Add new extension attribute
				md.addAttribute(new ExtMapAttribute(pair.getValue(), pair.getKey()));

		//If not format has been found
		if (md.getFormats().isEmpty())
		{
			//Log
			logger.log(Level.FINE, "addMediaToSdp no compatible codecs found for media {0} ", new Object[]{mediaName});
			//Disable
			md.setPort(0);
		}
		//Return the media descriptor
		return md;
	}

	public void processSDP(SessionDescription sdp) throws SdpPortManagerException {
		//Check required
		HashSet<Integer> required = (HashSet<Integer>)requiredCodecs.clone();

		//Connnection IP
		String ip = null;
		//ICE credentials
		String remoteICEFrag = null;
		String remtoeICEPwd = null;

		//Get the connection field
		Connection connection = sdp.getConnection();

		if (connection!=null)
		{
			//Get IP addr
			ip = connection.getAddress();
			
			//We don't support ipv6 yet
			if (!connection.getAddrType().equalsIgnoreCase("IP4"))
				//Ignore it and do natting
				ip = "0.0.0.0";
			//Check if ip should be nat for this media mixer
			if (conn.getMediaServer().isNated(ip))
				//Do natting
				ip = "0.0.0.0";
		}

		//Disable supported media
		audioSupported = false;
		videoSupported = false;
		textSupported = false;
		
		//No sending ports
		sendAudioPort = 0;
		sendVideoPort = 0;
		sendTextPort = 0;

		//NO bitrate by default
		videoBitrate = 0;

		//For each bandwith
		for (Bandwidth band : sdp.getBandwidths())
		{
			//Get bitrate value
			int rate = Integer.parseInt(band.getBandwidth());
			//Check bandwith type
			if (band.getType().equalsIgnoreCase("TIAS"))
					//Convert to kbps
					rate = rate/1000;
			// Let some room for audio.
			if (rate>=128)
				//Remove maximum rate
				rate -= 64;
			//Check if is less
			if (videoBitrate==0 || rate<videoBitrate)
				//Set it
				videoBitrate = rate;
		}

		//Check for global ice credentials
		Attribute ufragAtrr = sdp.getAttribute("ice-ufrag");
		Attribute pwdAttr = sdp.getAttribute("ice-pwd");

		//Check if both present
		if (ufragAtrr!=null && pwdAttr!=null)
		{
			//Using ice
			useICE = true;
			//Get values
			remoteICEFrag = ufragAtrr.getValue();
			remtoeICEPwd = pwdAttr.getValue();
		}

		//No DTLS fingerprint yet
		String remoteHash = null;
		String remoteFingerprint = null;

		//Check global fingerprint attribute
		FingerprintAttribute fingerprintAttr = (FingerprintAttribute) sdp.getAttribute("fingerprint");

		//Check if there is one preset
		if (fingerprintAttr!=null)
		{
			//Using DTLS
			useDTLS = true;
			//Get remote fingerprint
			remoteHash	  = fingerprintAttr.getHashFunc();
			remoteFingerprint = fingerprintAttr.getFingerprint();
		}
		
		for (MediaDescription md : sdp.getMedias())
		{
			//No default bitrate
			int mediaBitrate = 0;
			
			//Get media type
			String media = md.getMedia();

			//Get port
			Integer port = md.getPort();
			//Get transport
			ArrayList<String> proto = md.getProto();

			//If it its not RTP (i.e. RTP/(s)AVP(f) or UDP/TLS/RTP/SAVP(f) or port is 0
			if (!proto.get(proto.size()-2).equals("RTP") || port==0)
			{
				//Create media descriptor
				MediaDescription rejected = new MediaDescription(media,0,md.getProtoString());
				//set all  formats
				rejected.setFormats(md.getFormats());
				//add to rejected media
				rejectedMedias.add(rejected);
				//Not supported media type
				continue;
			}
			
			//Get bandwiths
			for (Bandwidth band : md.getBandwidths())
			{
				//Get bitrate value
				int rate = Integer.parseInt(band.getBandwidth());
				//Check bandwith type
				 if (band.getType().equalsIgnoreCase("TIAS"))
					//Convert to kbps
					rate = rate/1000;
				//Check if less than current
				if (mediaBitrate==0 || rate<mediaBitrate)
					//Set it
					mediaBitrate = rate;
			}
			
			//Check if it supports rtcp-muxing
			if (md.hasAttribute("rtcp-mux"))
				//Add attribute
				rtpMediaProperties.get(media).put("rtcp-mux", "1");
			
			//Check mid
			if (md.hasAttribute("mid"))
				//Set it
				mids.put(media,md.getAttribute("mid").getValue());

			//Check direction attributes
			if (md.hasAttribute("sendonly"))
			{
				//Part is sendonly
				rtpDirections.put(media, Direction.SENDONLY);
			} else if (md.hasAttribute("recvonly")) {
				//Part is recvonly
				rtpDirections.put(media, Direction.RECVONLY);
			} else if (md.hasAttribute("inactive")) {
				//Part is inactive
				rtpDirections.put(media, Direction.INACTIVE);
			} else {
				//Part is sendrecv
				rtpDirections.put(media, Direction.SENDRECV);
			}

			//Add support for the media
			if (media.equals("audio")) {
				//Check if it was enabled in the configuration
				if (this.mc.hasStream(JoinableStream.StreamType.audio))
				{
					//Set as supported
					audioSupported = true;
				} else {
					//Log
					logger.log(Level.FINE, "Media rejected because not allowed by configuration {0} ", new Object[]{media});
					//Create media descriptor
					MediaDescription rejected = new MediaDescription(media,0,md.getProtoString());
					//set all  formats
					rejected.setFormats(md.getFormats());
					//add to rejected media
					rejectedMedias.add(rejected);
					//Not supported media type
					continue;
				}
			} else if (media.equals("video")) {
				//Check if it was enabled in the configuration
				if (this.mc.hasStream(JoinableStream.StreamType.video))
				{
					//Get content attribute
					Attribute content = md.getAttribute("content");
					//Check if we found it inside this media
					if (content!=null)
					{
						//Get it
						String mediaContentType = content.getValue();
						//Check if it is not main
						if (!mediaContentType.equalsIgnoreCase("main"))
						{
							//Create media descriptor
							MediaDescription rejected = new MediaDescription(media,0,md.getProtoString());
							//set all  formats
							rejected.setFormats(md.getFormats());
							//Add content attribute
							rejected.addAttribute(content);
							//add to rejected media
							rejectedMedias.add(rejected);
							//Skip it
							continue;
						} else {
							//Add the content type to the line
							videoContentType = mediaContentType;
						}
					}
					//Check if we have a media rate less than the current bitrate
					if (videoBitrate==0 || (mediaBitrate>0 && mediaBitrate<videoBitrate))
						//Store bitrate
						videoBitrate = mediaBitrate;
					//Set as supported
					videoSupported = true;
				} else {
					//Log
					logger.log(Level.FINE, "Media rejected because not allowed by configuration {0} ", new Object[]{media});
					//Create media descriptor
					MediaDescription rejected = new MediaDescription(media,0,md.getProtoString());
					//set all  formats
					rejected.setFormats(md.getFormats());
					//add to rejected media
					rejectedMedias.add(rejected);
					//Not supported media type
					continue;
				}	
			} else if (media.equals("text")) {
				//Check if it was enabled in the configuration
				if (this.mc.hasStream(JoinableStream.StreamType.video))
				{
					//Set as supported
					textSupported = true;
				} else {
					//Log
					logger.log(Level.FINE, "Media rejected because not allowed by configuration {0} ", new Object[]{media});
					//Create media descriptor
					MediaDescription rejected = new MediaDescription(media,0,md.getProtoString());
					//set all  formats
					rejected.setFormats(md.getFormats());
					//add to rejected media
					rejectedMedias.add(rejected);
					//Not supported media type
					continue;
				}	
			} else {
				//Log
				logger.log(Level.FINE, "Media rejected because it is unknown {0} ", new Object[]{media});
				//Create media descriptor
				MediaDescription rejected = new MediaDescription(media,0,md.getProtoString());
				//set all  formats
				rejected.setFormats(md.getFormats());
				//add to rejected media
				rejectedMedias.add(rejected);
				//Not supported media type
				continue;
			}

			//Check if we have input map for that
			if (!rtpOutMediaMap.containsKey(media))
				//Create new map
				rtpOutMediaMap.put(media, new HashMap<Integer, Integer>());

			//Get all codecs
			//No codec priority yet
			Integer priority = Integer.MAX_VALUE;
			
			 //ICE credentials
			String remoteMediaICEFrag = remoteICEFrag;
			String remtoeMediaICEPwd = remtoeICEPwd;
			//Check for global ice credentials
			ufragAtrr = md.getAttribute("ice-ufrag");
			pwdAttr = md.getAttribute("ice-pwd");

			//Check if both present
			if (ufragAtrr!=null && pwdAttr!=null)
			{
				//Using ice
				useICE = true;
				//Get values
				remoteMediaICEFrag = ufragAtrr.getValue();
				remtoeMediaICEPwd = pwdAttr.getValue();
			}

			//By default the media IP is the general IO
			String mediaIp = ip;

			//Get connection info
			for (Connection c : md.getConnections())
			{
				//Get it
				mediaIp = c.getAddress();
				//Check if ip should be nat for this media mixer or we are using ICE
				if (conn.getMediaServer().isNated(mediaIp))
					//Do natting
					mediaIp = "0.0.0.0";
			}

			//Check if it is DTLS
			if (proto.get(0).equals("UDP") && proto.get(1).equals("TLS"))
				//Using DTLS
				useDTLS = true;

			//Get rtp profile
			String rtpProfile = proto.get(proto.size()-1);

			//Check if it is secure
			if (rtpProfile.startsWith("S"))
			{
				//Secure (WARNING: if one media is secure, all will be secured, FIX!!)
				isSecure = true;

				//Check media fingerprint attribute
				fingerprintAttr = (FingerprintAttribute) md.getAttribute("fingerprint");

				//Check if DTLS is available
				if (fingerprintAttr!=null)
				{
					//Using DTLS
					useDTLS = true;
					//Get remote fingerprint and hash
					remoteHash        = fingerprintAttr.getHashFunc();
					remoteFingerprint = fingerprintAttr.getFingerprint();
				}

				//If we have fingerprint
				if (useDTLS)
				{
					//Set deault setup
					Setup setup = Setup.ACTPASS;
					//Get setup attribute
					Attribute attr = md.getAttribute("setup");
					//Chekc it
					if (attr!=null)
						//Set it
						setup = Setup.byValue(attr.getValue());
					//Create new DTLS info
					remoteDTLSInfo.put(media, new DTLSInfo(setup,remoteHash,remoteFingerprint));
					//Set ur setup as reverese of remote
					rtpSetups.put(media, setup.reverse());
				} else {
					//Check crypto attribute
					CryptoAttribute crypto = (CryptoAttribute) md.getAttribute("crypto");

					//Check SDES key
					if (crypto!=null)
					{
						//Create media crypto params
						CryptoInfo info = new CryptoInfo();
						//Get suite
						info.suite = crypto.getSuite();
						//Get key
						info.key = crypto.getFirstKeyParam().getInfo();
						//Add it
						remoteCryptoInfo.put(media, info);
					}
				}
			}
			//Check if it is secure
			if (rtpProfile.endsWith("S"))
				//With feedback (WARNING: if one media is secure, all will have feedback, FIX!!)
				isSecure = true;
			
			//Check if has rtcp
			if (rtpProfile.endsWith("F"))
				//With feedback (WARNING: if one media has feedback, all will have feedback, FIX!!)
				rtcpFeedBack = true;

			//FIX
			Integer h264type = 0;
			H264ProfileLevelID maxh264profile = null;

			//For each format
			for (String fmt : md.getFormats())
			{
				Integer type = 0;
				try {
					//Get codec
					type = Integer.parseInt(fmt);
				} catch (Exception e) {
					//Ignore non integer codecs, like '*' on application
					continue;
				}

				//If it is dinamic
				if (type>=96)
				{
					//Get map
					RTPMapAttribute rtpMap = md.getRTPMap(type);
					//Check it has mapping
					if (rtpMap==null)
						//Skip this one
						continue;
					//Get the media type
					String codecName = rtpMap.getName();
					//Get codec for name
					Integer codec = Codecs.getCodecForName(media,codecName);
					//if it is h264
					if (Codecs.H264.equals(codec))
					{
						int k = -1;
						//Get ftmp line
						Map<String,String> params = md.getFormatParameters(type);
						//Check if it has it
						if (params!=null && params.containsKey("profile-level-id"))
						{
							//Get profile level indication
							H264ProfileLevelID profileLevelId = new H264ProfileLevelID(params.get("profile-level-id"));
							//Convert and compare
							if (profileLevelId.getProfile()<=Codecs.MaxH264SupportedProfile && (maxh264profile==null || profileLevelId.getProfile()>maxh264profile.getProfile()) )
							{
								//Store this type provisionally
								h264type = type;
								//store new profile value
								maxh264profile = profileLevelId;
								//Check if it has packetization parameter
								if (params.containsKey("packetization-mode"))
									//Set it
									h264packetization = Integer.parseInt(params.get("packetization-mode"));
							}
						} else {
							//check if no profile has been received so far
							if (maxh264profile==null)
								//Store this type provisionally
								h264type = type;
						}
					} else if (Codecs.SPEEX16.equals(codec)  ) {
						//Check it is 16khz
						if (rtpMap.getRate()==16000)
							//Add it
							rtpOutMediaMap.get(media).put(type,codec);
					} else if (codec!=-1) {
						//Set codec mapping
						rtpOutMediaMap.get(media).put(type,codec);
					}
				} else {
					//Static, put it in the map
					rtpOutMediaMap.get(media).put(type,type);
				}
			}

			//Check if we have type for h264
			if (h264type>0)
			{
				//Store profile level
				h264profileLevelId = maxh264profile;
				//add it
				rtpOutMediaMap.get(media).put(h264type,Codecs.H264);
			}

			//For each entry
			for (Map.Entry<Integer,Integer> entry : rtpOutMediaMap.get(media).entrySet())
			{
				//Get Codec and type
				Integer type = entry.getKey();
				Integer codec = entry.getValue();
				//Check the media type
				if (media.equals("audio"))
				{
					//Get suppoorted codec for media
					List<Integer> audioCodecs = supportedCodecs.get("audio");
					//Get index
					for (int index=0;index<audioCodecs.size();index++)
					{
						//Check codec
						if (audioCodecs.get(index).equals(codec))
						{
							//Check if it is first codec for audio
							if (priority==Integer.MAX_VALUE)
							{
								//Set port
								setSendAudioPort(port);
								//And Ip
								setSendAudioIp(mediaIp);
							}
							//Check if we have a lower priority
							if (index<priority)
							{
								//Store priority
								priority = index;
								//Set codec
								setAudioCodec(codec);
							}
						}
					}
				}
				else if (media.equals("video"))
				{
					//We are using rtx
					if (codec.equals(Codecs.RTX))
					{
						//Get format
						Map<String,String> fp = md.getFormatParameters(type);
						//Get associated payload type
						if (fp.containsKey("apt"))
						{
							//Enable rtx, it must have apt
							useRTX = true;
							//Set it
							rtpMediaProperties.get(media).put("useRTX", "1");
							//Set PT
							rtpMediaProperties.get(media).put("rtx.apt", fp.get("apt"));
						}
					}
					//Get suppoorted codec for media
					List<Integer> videoCodecs = supportedCodecs.get("video");
					//Get index
					for (int index=0;index<videoCodecs.size();index++)
					{
						//Check codec
						if (videoCodecs.get(index).equals(codec) && !codec.equals(Codecs.RED) && !codec.equals(Codecs.ULPFEC) && !codec.equals(Codecs.RTX))
						{
							//Check if it is first codec for video
							if (priority==Integer.MAX_VALUE)
							{
								//Set port
								setSendVideoPort(port);
								//And Ip
								setSendVideoIp(mediaIp);
							}
							//Check if we have a lower priority
							if (index<priority)
							{
								//Store priority
								priority = index;
								//Set codec
								setVideoCodec(codec);
							}
						}
					}
				}
				else if (media.equals("text"))
				{
					//Get suppoorted codec for media
					List<Integer> textCodecs = supportedCodecs.get("text");
					//Get index
					for (int index=0;index<textCodecs.size();index++)
					{
						//Check codec
						if (textCodecs.get(index).equals(codec))
						{
							//Check if it is first codec for audio
							if (priority==Integer.MAX_VALUE)
							{
								//Set port
								setSendTextPort(port);
								//And Ip
								setSendTextIp(mediaIp);
							}							
							//Check if we have a lower priority
							if (index<priority)
							{
								//Store priority
								priority = index;
								//Set codec
								setTextCodec(codec);
							}
						}
					}
				}
			}
			//Check ice credentials
			if (remoteMediaICEFrag!=null && remtoeMediaICEPwd!=null)
				//Create info and add to remote ones
				remoteICEInfo.put(media, new ICEInfo(remoteMediaICEFrag,remtoeMediaICEPwd));
			//Get extmap atrributes
			ArrayList<Attribute> extmaps = md.getAttributes("extmap");
			//Get supported extensions
			HashMap<String, Integer> supported = supportedExtensions.get(media);
			//If some extensions are supported for this media
			if (supported!=null)
			{
				boolean offer = false;
				//If it has not been created yet
				if (rtpExtensionMap.containsKey(media))
				{
					//Set it
					rtpExtensionMap.put(media, new HashMap<String, Integer>());
					//The SDP is an offer
					offer = true;
				}
				//For each one
				for (Attribute attr : extmaps)
				{
					//Cast
					ExtMapAttribute extmap = (ExtMapAttribute) attr;
					//Check if it is supperted
					if (supported.containsKey(extmap.getName()))
					{
						//If it is an offer
						if (offer)
							//Add it also to the outgoing SDP extmap
							rtpExtensionMap.get(media).put(extmap.getName(), extmap.getId());
						//Add to the
						rtpMediaProperties.get(media).put(extmap.getName(), extmap.getId().toString());
					}
				}
			}
			//Disable rtcp SR sending
			if (params.getBooleanParameter(rtcp.disabled, false))
				rtpMediaProperties.get(media).put("useRTCP","0");
		}
		
		//Check we have at least one codec
		if (audioSupported && audioCodec==null)
			//Set error
			throw new SdpPortManagerException("No suitable codec found for audio");
		//Check we have at least one codec
		if (videoSupported && videoCodec==null)
			//Set error
			throw new SdpPortManagerException("No suitable codec found for video");
		//Check we have at least one codec
		if (textSupported && textCodec==null)
			//Set error
			throw new SdpPortManagerException("No suitable codec found for text");
	}


	private void success(EventType eventType) {
		//Create and fire event
		fireEvent(new SdpPortManagerEventImpl(this, eventType));
	}

	private void error(MediaErr error,String errorMsg) {
	   //Create and fire event
		fireEvent(new SdpPortManagerEventImpl(this, error, errorMsg));
	}

	private void fireEvent(final SdpPortManagerEventImpl event) {
		//Get media session
		MediaSessionImpl session = (MediaSessionImpl) conn.getMediaSession();
		//exec async
		session.Exec(new Runnable() {
			@Override
			public void run() {
				//For each listener in set
				 for (MediaEventListener<SdpPortManagerEvent> listener : listeners)
					 //Send it
					listener.onEvent(event);
			}
		});
	}

	public String getSendAudioIp() {
		return sendAudioIp;
	}

	public void setSendAudioIp(String sendAudioIp) {
		this.sendAudioIp = sendAudioIp;
	}

	public String getSendTextIp() {
		return sendTextIp;
	}

	public void setSendTextIp(String sendTextIP) {
		this.sendTextIp = sendTextIP;
	}

	public String getSendVideoIp() {
		return sendVideoIp;
	}

	public void setSendVideoIp(String sendVideoIp) {
		this.sendVideoIp = sendVideoIp;
	}
   
	boolean getUseDTLS() {
		return useDTLS;
	}

	public String getLocalHash() {
		return localHash;
	}

	public void setLocalFingerprint(String localFingerprint) {
		this.localFingerprint = localFingerprint;
	}

	boolean getIsSecure() {
		return isSecure;
	}

	boolean getUseIce() {
		return useICE;
	}

	void setLocalCryptoInfo(String media, CryptoInfo info) {
		//Add to local info
		localCryptoInfo.put(media, info);
	}

	void setLocalIceInfo(String media, ICEInfo info) {
		//Add to local info
		localICEInfo.put(media, info);
	}


	CryptoInfo getRemoteCryptoInfo(String media) {
		return remoteCryptoInfo.get(media);
	}
	
	ICEInfo getRemoteICEInfo(String media) {
		return remoteICEInfo.get(media);
	}

	DTLSInfo getRemoteDTLSInfo(String media) {
		return remoteDTLSInfo.get(media);
	}

	HashMap<String, String> getRTPMediaProperties(String media) {
		return rtpMediaProperties.get(media);
	}
	
	public void setRTPMediaProperties(String media,String key, String value) {
		//Set property
		rtpMediaProperties.get(media).put(key,value);
	}

	Direction getRTPDirection(String media) {
		return rtpDirections.get(media);
	}
}
