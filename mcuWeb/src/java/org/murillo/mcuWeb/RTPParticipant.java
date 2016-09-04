/*
 * RTPParticipant.java
 *
 * Copyright (C) 2007  Sergio Garcia Murillo
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.murillo.mcuWeb;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlElement;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.MediaServer.Codecs;
import org.murillo.MediaServer.Codecs.Direction;
import org.murillo.MediaServer.Codecs.MediaType;
import org.murillo.MediaServer.Codecs.Setup;
import org.murillo.MediaServer.XmlRpcMcuClient;
import org.murillo.MediaServer.XmlRpcMcuClient.MediaStatistics;
import org.murillo.abnf.ParserException;
import org.murillo.mcuWeb.Participant.State;
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
import org.murillo.util.H264ProfileLevelID;

/**
 *
 * @author Sergio Garcia Murillo
 */
public class RTPParticipant extends Participant {
	private final static Logger logger = Logger.getLogger(RTPParticipant.class.getName());

	private Address address;
	private SipSession session = null;
	private SipApplicationSession appSession = null;
	private SipServletRequest inviteRequest = null;
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
	private String location;
	private Integer totalPacketCount;
	@XmlElement
	private Map<String, MediaStatistics> stats;

	private HashMap<String,List<Integer>> supportedCodecs = null;
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
	private boolean sessionTimersEnabled;
	private boolean timerSupported;
	private Integer sessionExpires;
	private Date lastSessionRefresh;
	private SipURI proxy;
	private boolean useInfo;
	private boolean useUpdate;
	private boolean useRTPTimeout;
	private boolean useRTX;


	public static final String ALLOWED = "INVITE, ACK, CANCEL, UPDATE, INFO, OPTIONS, BYE";
	private String localFingerprint;
	private String localHash = "sha-256";

	private static class CryptoInfo
	{
		String suite;
		String key;

		public static CryptoInfo Generate()
		{
			//Create crypto info for media
			CryptoInfo info = new CryptoInfo();
			//Set suite
			info.suite = "AES_CM_128_HMAC_SHA1_80";
			//Get random
			SecureRandom random = new SecureRandom();
			//Create key bytes
			byte[] key = new byte[30];
			//Generate it
			random.nextBytes(key);
			//Encode to base 64
			info.key = DatatypeConverter.printBase64Binary(key);
			//return it
			return info;
		}
		
		private CryptoInfo() {

		}

		public CryptoInfo(String suite, String key) {
			this.suite = suite;
			this.key = key;
		}
	}

	private static class DTLSInfo {
	Setup setup;
	String hash;
	String fingerprint;

	public DTLSInfo(Setup setup,String hash, String fingerprint) {
   		this.setup = setup;
		this.hash = hash;
		this.fingerprint = fingerprint;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public String getHash() {
		return hash;
	}

	public Setup getSetup() {
		return setup;
	}

	}

	private static class ICEInfo
	{
		String ufrag;
		String pwd;

		public static ICEInfo Generate()
		{
			//Create ICE info for media
			ICEInfo info = new ICEInfo();
			 //Get random
			SecureRandom random = new SecureRandom();
			//Create key bytes
			byte[] frag = new byte[8];
			byte[] pwd = new byte[22];
			//Generate them
			random.nextBytes(frag);
			random.nextBytes(pwd);
			//Create ramdom pwd
			info.ufrag = DatatypeConverter.printHexBinary(frag);
			info.pwd   =  DatatypeConverter.printHexBinary(pwd);
			//return it
			return info;
		}

		private ICEInfo() {
			
		}
		
		public ICEInfo(String ufrag, String pwd) {
			this.ufrag = ufrag;
			this.pwd = pwd;
		}
	}

	RTPParticipant(Integer id,Integer partId,String name,String token,Integer mosaicId,Integer sidebarId,Conference conf) throws XmlRpcException {
		//Call parent
		super(id,partId,name,token,mosaicId,sidebarId,conf,Type.SIP);
		//No sending ports
		sendAudioPort = 0;
		sendVideoPort = 0;
		sendTextPort = 0;
		//No receiving ports
		recAudioPort = 0;
		recVideoPort = 0;
		recTextPort = 0;
		//Supported media
		audioSupported = true;
		videoSupported = true;
		textSupported = true;
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
		//Enable session refresh support
		sessionTimersEnabled = true;
		//Session refresh supported by both peers
		timerSupported = false;
		sessionExpires = 0;
		//Not secure by default
		isSecure = false;
		//Not using ice by default
		useICE = false;
		//Do no use info or update if not allowed explicitally
		useInfo = false;
		useUpdate = false;
		//Use RTP timeout by default
		useRTPTimeout = true;
		//Do not use DTLS by default
		useDTLS = false;
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
		//Add them
		supportedExtensions.get("audio").put("urn:ietf:params:rtp-hdrext:ssrc-audio-level",1);
		supportedExtensions.get("video").put("urn:ietf:params:rtp-hdrext:toffset",2);
		supportedExtensions.get("video").put("http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time",3);
		//Create extension map
		rtpExtensionMap = new HashMap<String, HashMap<String, Integer>>();
		//Has RTCP feedback
		rtcpFeedBack = false;
		//Using rtx by default
		useRTX = true;
	}

	@Override
	public void restart(Integer partId) {
		//Store new id
		this.partId = partId;
		try {
		//Clear ports so we can start receiving again
		sendAudioPort = 0;
		sendVideoPort = 0;
		sendTextPort = 0;
		recAudioPort = 0;
		recVideoPort = 0;
		recTextPort = 0;
		//Reset RTP setup
		rtpSetups = new HashMap<String,Setup>(3);
		//Actpass by default
		rtpSetups.put("audio",Setup.ACTPASS);
		rtpSetups.put("video",Setup.ACTPASS);
		rtpSetups.put("text" ,Setup.ACTPASS);
		//Start receiving media
			startReceiving();
		// create an UPDATE
		SipServletRequest updateRequest = session.createRequest("UPDATE");
		//Check session refresh request
		if (timerSupported && sessionExpires>0)
			{
		//RFC 4208 Session Timers
		//	If the UAS wishes to accept the request, it copies the value of the
		//	Session-Expires header field from the request into the 2xx response.
		updateRequest.addHeader("Session-expires", sessionExpires.toString()+";refresher=uac");
		//Add require and supported
		updateRequest.addHeader("Supported","timer");
		updateRequest.addHeader("Require","timer");
			}
		//add allowed header
		updateRequest.addHeader("Allow", ALLOWED);
		//Add custom headers with conf id and participant id
			updateRequest.addHeader("X-Conference-ID", conf.getUID());
		updateRequest.addHeader("X-Participant-ID", getId().toString());
		updateRequest.addHeader("X-Participant-Token", getToken());
		updateRequest.addHeader("X-Conference-Mixer-ID", conf.getId().toString());
		updateRequest.addHeader("X-Conference-Mixer-PartID", partId.toString());
		
			//Create sdp
			localSDP = createSDP();
			//Convert to
			String sdp = localSDP.toString();
			//Attach body
			updateRequest.setContent(sdp,"application/sdp");
			//Send it
			updateRequest.send();
			//Log
			logger.log(Level.WARNING, "doInvite [idSession:{0}]",new Object[]{session.getId()});
		} catch (Exception ex) {
	   throw new RuntimeException(ex);
		}
	}

	public void addSupportedCodec(String media,Integer codec) {
		 //Check if we have the media
		 if (!supportedCodecs.containsKey(media))
			 //Create it
			 supportedCodecs.put(media, new Vector<Integer>());
		 //Add codec to media
		 supportedCodecs.get(media).add(codec);
	 }

	public Address getAddress() {
		return address;
	}

	@XmlElement(name="uri")
	public String getUri() {
		//Return uri as string
		return address!=null?address.getURI().toString():null;
	}

	public String getUsername() {
		 //Get sip uris
		SipURI uri = (SipURI) address.getURI();
		//Return username
		return uri.getUser();
	}

	public String getDomain() {
		//Get sip uris
		SipURI uri = (SipURI) address.getURI();
		//Return username
		return uri.getHost();
	}

	public String getUsernameDomain() {
		//Get sip uris
		SipURI uri = (SipURI) address.getURI();
		//Return username
		return uri.getUser()+"@"+uri.getHost();
	}

	 boolean equalsUser(Address user) {
		//Get sip uris
		SipURI us = (SipURI) address.getURI();
		SipURI them = (SipURI) user.getURI();
		//If we have the same username and host/domain
	return us.getUser().equals(them.getUser()) && us.getHost().equals(them.getHost());
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

	public String getSendAudioIp() {
		return sendAudioIp;
	}

	public void setSendAudioIp(String sendAudioIp) {
		this.sendAudioIp = sendAudioIp;
	}

	public String getSendTextIp() {
		return sendTextIp;
	}

	public void setSendTextIp(String sendTextIp) {
		this.sendTextIp = sendTextIp;
	}

	public String getSendVideoIp() {
		return sendVideoIp;
	}

	public void setSendVideoIp(String sendVideoIp) {
		this.sendVideoIp = sendVideoIp;
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

	@Override
	public Boolean isSending(String media) {
	return rtpDirections.get(media).isSending();
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setIsSecure(Boolean isSecure) {
		this.isSecure = isSecure;
	}

	public void setUseDTLS(boolean useDTLS) {
	this.useDTLS = useDTLS;
	}

	public void setUseICE(boolean useICE) {
		this.useICE = useICE;
	}

	public void setRtcpFeedBack(Boolean rtcpFeedBack) {
		this.rtcpFeedBack = rtcpFeedBack;
	}

	public void setH264profileLevelId(H264ProfileLevelID h264profileLevelId) {
	this.h264profileLevelId = h264profileLevelId;
	}

	public void setUseRTPTimeout(boolean useRTPTimeout) {
	this.useRTPTimeout = useRTPTimeout;
	}

	@Override
	public boolean setVideoProfile(Profile profile) {
		//Check video is supported
		if (!getVideoSupported())
			//Exit
			return false;
		//Set new video profile
		this.profile = profile;
		try {
			//Get client
			XmlRpcMcuClient client = conf.getMCUClient();
			//Get conf id
			Integer confId = conf.getId();
			//If it is sending video
			if (getSendVideoPort()!=0)
			{
				//Stop sending video
				client.StopSending(confId, partId, MediaType.VIDEO);
				//Get profile bitrate
				int bitrate = profile.getVideoBitrate();
				//Reduce to the maximum in SDP
				if (videoBitrate>0 && videoBitrate<bitrate)
						//Reduce it
						bitrate = videoBitrate;
				//Create specific codec paraemter
				HashMap<String,String> params = new HashMap<String, String>();
				//Check codec
				if (Codecs.H264.equals(getVideoCodec()))
					//Add profile level id
					params.put("h264.profile-level-id", h264profileLevelId.toString());
				//FIX: Should not be here
				if (profile.hasProperty("rateEstimator.minRate"))
					//Add it
					params.put("rateEstimator.minRate",profile.getProperty("rateEstimator.minRate"));
				//FIX: Should not be here
				if (profile.hasProperty("rateEstimator.maxRate"))
					//Add it
					params.put("rateEstimator.maxRate",profile.getProperty("rateEstimator.maxRate"));
				//Setup video with new profile
				client.SetVideoCodec(confId, partId, getVideoCodec(), profile.getVideoSize(), profile.getVideoFPS(), bitrate,  profile.getIntraPeriod(), params);
				//Send video & audio
				client.StartSending(confId, partId, MediaType.VIDEO, getSendVideoIp(), getSendVideoPort(), getRtpOutMediaMap("video"));
			}
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
			return false;
		}
		return true;
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
		sdp.setOrigin("-", getId().toString(), Long.toString(new Date().getTime()), "IN", "IP4", getRecIp());
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

		//Check if it supports media
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
		//Create new meida description with default values
		MediaDescription md = new MediaDescription(mediaName,port,"RTP/"+rtpProfile);

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

		//Check if we need to set the maximun bitrate
		if (mediaName.equals("video") && profile.getMaxVideoBitrate()>0)
			//Add bandwidth
			md.addBandwidth("AS",profile.getMaxVideoBitrate());

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

		//If we are secure
	if (isSecure)
	{
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
		} else {
		//Get Crypto info for local media
		CryptoInfo info = localCryptoInfo.get(mediaName);

		//f we have crytpo info
		if (info!=null)
			//Append attribute
			md.addAttribute(new CryptoAttribute(1, info.suite, "inline", info.key));
		}
	}

		//Only for webrtc participants
		if (rtcpFeedBack)
		{
			//Create ramdon ssrc
			Long ssrc = Math.round(Math.random()*Integer.MAX_VALUE);
		//The retransmission ssrc
		Long ssrcRTX = Math.round(Math.random()*Integer.MAX_VALUE);
			//Set cname
			String cname = getId()+"@"+conf.getUID();
			//Label
			String label = conf.getUID();
			//Set app id
			String appId = mediaName.substring(0,1)+"0";
		//If using RTX add the group attribute, only for video
		if (useRTX && "video".equals(mediaName))
			//Add RTX stream as ssrc+1
			md.addAttribute(new SSRCGroupAttribute("FID",Arrays.asList(ssrc.toString(),ssrcRTX.toString())));
			//Add ssrc info
			md.addAttribute(new SSRCAttribute(ssrc, "cname"	 ,cname));
			md.addAttribute(new SSRCAttribute(ssrc, "mslabel"   ,label));
			md.addAttribute(new SSRCAttribute(ssrc, "msid"	  ,label+" " + appId));
			md.addAttribute(new SSRCAttribute(ssrc, "label"	 ,label+appId));
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
		md.addAttribute(new SSRCAttribute(ssrcRTX, "mslabel"   ,label));
		md.addAttribute(new SSRCAttribute(ssrcRTX, "msid"	  ,label+" " + appId));
		md.addAttribute(new SSRCAttribute(ssrcRTX, "label"	 ,label+appId));
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
							//Set default profile
							h264profileLevelId = new H264ProfileLevelID(h264profileLevelIdDefault);
			//Create format
						FormatAttribute fmtp = new FormatAttribute(fmt);
			//Set profile-level-id
			fmtp.addParameter("profile-level-id",h264profileLevelId.toString());

						//Check packetization mode
						if (h264packetization>0)
							//Add profile and packetization mode
						   fmtp.addParameter("packetization-mode=",h264packetization);

			//Add max mbps
			if (profile.hasProperty("codecs.h264.max-mbps"))
				//Add param
				fmtp.addParameter("max-mbps", profile.getIntProperty("codecs.h264.max-mbps",40500));
			//Add max fs
			if (profile.hasProperty("codecs.h264.max-fs"))
				//Add param
				fmtp.addParameter("max-fs", profile.getIntProperty("codecs.h264.max-fs",1344));
			//Add max br
			if (profile.hasProperty("codecs.h264.max-br"))
				//Add param
				fmtp.addParameter("max-br", profile.getIntProperty("codecs.h264.max-br",906));
			//Add max smbps
			if (profile.hasProperty("codecs.h264.max-smbps"))
				//Add param
				fmtp.addParameter("max-smbps", profile.getIntProperty("codecs.h264.max-smbps",40500));
			//Add max fps
			if (profile.hasProperty("codecs.h264.max-fps"))
				//Add param
				fmtp.addParameter("max-fps", profile.getIntProperty("codecs.h264.max-fps",3000));
			//Add h264 params support
			md.addAttribute(fmtp);
					} else if (Codecs.H263_1996.equals(codec)) {
						//Add h263 supported sizes
						md.addFormatAttribute(fmt,"CIF=1;QCIF=1");
					} else if (Codecs.OPUS.equals(codec)) {
						//Create format
						FormatAttribute fmtp = new FormatAttribute(fmt);
						//Add parameters
						fmtp.addParameter("ptime"	   ,20);
						fmtp.addParameter("maxptime"	,20);
						fmtp.addParameter("minptime"	,20);
						fmtp.addParameter("useinbandfec",0);
						fmtp.addParameter("usedtx"	  ,0);
						//Mono
						fmtp.addParameter("stereo",0);
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

	private void proccesContent(String type, Object content) throws IOException {
		//No SDP
		String sdp = null;
		//Depending on the type
		if (type.equalsIgnoreCase("application/sdp"))
		{
			//Check object type
			if (content instanceof String)
				//Get content
				sdp = (String) content;
			else
			//Get it
			sdp = new String((byte[])content);
		} else if (type.startsWith("multipart/mixed")) {
			try {
				//Get multopart
				Multipart multipart = (Multipart) content;
				//For each content
				for (int i = 0; i < multipart.getCount(); i++)
				{
					//Get content type
					BodyPart bodyPart = multipart.getBodyPart(i);
					//Get body type
					String bodyType = bodyPart.getContentType();
					//Check type
					if (bodyType.equalsIgnoreCase("application/sdp"))
					{
						//Get input stream
						InputStream inputStream = bodyPart.getInputStream();
						//Create array
						byte[] arr = new byte[inputStream.available()];
						//Read them
						inputStream.read(arr, 0, inputStream.available());
						//Set length
						sdp = new String(arr);
					} else if (bodyType.equalsIgnoreCase("application/pidf+xml")) {
						//Get input stream
						InputStream inputStream = bodyPart.getInputStream();
						//Create array
						byte[] arr = new byte[inputStream.available()];
						//Read them
						inputStream.read(arr, 0, inputStream.available());
						//Set length
						location = new String(arr);
					}
				}
			} catch (MessagingException ex) {
				logger.log(Level.SEVERE, null, ex);
			}
		}
		try {
			//Parse sdp
			remoteSDP = processSDP(sdp);
			//Check if also have local SDP
			if (localSDP!=null)
				//Negotiation done
				onSDPNegotiationDone();
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
		} catch (IllegalArgumentException ex) {
			logger.log(Level.SEVERE, null, ex);
		} catch (ParserException ex) {
			logger.log(Level.SEVERE, null, ex);
	}
	}

	public SessionDescription processSDP(String body) throws IllegalArgumentException, ParserException
	{
	 //Parse conent
		SessionDescription sdp = SessionDescription.Parse(body);

	//Process it
	return processSDP(sdp);
	}

	public SessionDescription processSDP(SessionDescription sdp) throws IllegalArgumentException
	{
		//Connnection IP
		String ip = null;
		//ICE credentials
		String remoteICEFrag = null;
		String remtoeICEPwd = null;

		//Get the connection field
		Connection conn = sdp.getConnection();

		if (conn!=null)
		{
			//Get IP addr
			ip = conn.getAddress();

			//We don't support ipv6 yet
			if (!conn.getAddrType().equalsIgnoreCase("IP4"))
				//Ignore it and do natting
				ip = "0.0.0.0";
		//Check if ip should be nat for this media mixer
			else if (conf.getMixer().isNated(ip) || useICE)
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
			remoteHash		  = fingerprintAttr.getHashFunc();
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
				//Set as supported
				audioSupported = true;
			} else if (media.equals("video")) {
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
			} else if (media.equals("text")) {
				//Set as supported
				textSupported = true;
			} else {
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
				if (conf.getMixer().isNated(mediaIp) || useICE)
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
					remoteHash		= fingerprintAttr.getHashFunc();
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
		}
		return sdp;
	}

	public void onInfoRequest(SipServletRequest request) throws IOException {
		//Check content type
		if (request.getContentType().equals("application/media_control+xml"))
		{
			//Send FPU
			sendFPU();
			//Send OK
			SipServletResponse req = request.createResponse(200, "OK");
			//Send it
			req.send();
		} else {
			SipServletResponse req = request.createResponse(500, "Not supported");
			//Send it
			req.send();
		}
	}

	public void onCancelRequest(SipServletRequest request) {
		try {
			//Create final response
			SipServletResponse resp = request.createResponse(200, "Ok");
			//Send it
			resp.send();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		//Disconnect
		setState(State.DISCONNECTED);
		//Check cdr
		//And terminate
		destroy();
	}

	public void onCancelResponse(SipServletResponse resp) {
		//Teminate
		destroy();
	}

	public void onInfoResponse(SipServletResponse resp) {
	}

	public void onOptionsRequest(SipServletRequest request) {
		try {
			//Linphone and other UAs may send options before invite to determine public IP
			SipServletResponse response = request.createResponse(200);
			//return it
			response.send();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}

	public void onInviteRequest(SipServletRequest request) throws IOException {
		//Store invite request
		inviteRequest = request;
		//If it session timers are enabled
		if (sessionTimersEnabled)
		{
			//RFC 4208 Session Timers
			//   If the incoming request contains a Supported header field with a
			//   value 'timer' but does not contain a Session-Expires header, it means
			//   that the UAS is indicating support for timers but is not requesting
			//   one.

			//Get supported
			ListIterator<String> supported = inviteRequest.getHeaders("Supported");
			//Check if timer found
			while(supported.hasNext() && !timerSupported)
			{
				//parse supported list
				StringTokenizer tokenizer = new StringTokenizer(supported.next(), ",");
				//Check each one
				while (tokenizer.hasMoreTokens())
				{
					//Check if found
					if ("timer".equals(tokenizer.nextToken()))
					{
						//If it is
						timerSupported = true;
						//Exit
						break;
					}
				}
			}
			//Get expire header
			String sessionExpiresHeader = inviteRequest.getHeader("Session-Expires");

			//If found
			if (sessionExpiresHeader!=null)
		{
		//Parse the expire time
				String expire = sessionExpiresHeader;
				//Find delimiter
				int i = sessionExpiresHeader.indexOf(';');
				//If it contains the ;refresher=uas
				if (i!=-1)
					//Remove it
					expire = expire.substring(0, i);
				//Parse
				sessionExpires = Integer.parseInt(expire);
		}
		}
		//Get allow header
		String allowHeader = inviteRequest.getHeader("Allow");
		//Check if found
		if (allowHeader!=null)
		{
			//Check if update is supported
			useInfo	 = allowHeader.contains("INFO");
			useUpdate   = allowHeader.contains("UPDATE");
		}
		//Store address
		address = inviteRequest.getFrom();
		//Get uri
		SipURI uri = (SipURI)address.getURI();
		//Get Pin
		pin = uri.getUserPassword();
		//Remove it from uri
		uri.setUserPassword(null);
		//Get call id
		setSessionId(inviteRequest.getCallId());
	//Get call info
	ListIterator<String> iterator = inviteRequest.getHeaders("Call-Info");
	//Append each value
	while (iterator.hasNext())
	{
		//Get next
		String callInfo = iterator.next();
		//Strip < and >
		if (callInfo.startsWith("<"))
			callInfo = callInfo.substring(1);
		if (callInfo.endsWith(">"))
			callInfo = callInfo.substring(0,callInfo.length()-1);
		//Append to info
		info.add(callInfo);
	}
		//Create ringing
		SipServletResponse resp = inviteRequest.createResponse(180, "Ringing");
		//Send it
		resp.send();
		//Get sip session
		session = inviteRequest.getSession();
		//Get sip application session
		appSession = session.getApplicationSession();
		//Set expire time to wait for ACK
		appSession.setExpires(1);
		//Set reference in sessions
		appSession.setAttribute("user", this);
		session.setAttribute("user", this);
		//Do not invalidate
		appSession.setInvalidateWhenReady(false);
		session.setInvalidateWhenReady(false);
		//Check if it has content
		if (inviteRequest.getContentLength()>0)
			//Process it
			proccesContent(inviteRequest.getContentType(),inviteRequest.getContent());
		//Set state
		setState(State.WAITING_ACCEPT);
		//Check if we need to autoaccapt
		if (isAutoAccept())
			//Accept it
			accept();
	}

	public void onUpdatesRequest(SipServletRequest request) throws IOException
	{
		 //Update target
		session = request.getSession();
		//Set attribute
		session.setAttribute("user", this);
		//Do not invalidate
		session.setInvalidateWhenReady(false);
		//RFC 4208 Session Timers
		//   If the incoming request contains a Supported header field with a
		//   value 'timer' but does not contain a Session-Expires header, it means
		//   that the UAS is indicating support for timers but is not requesting
		//   one.
		//Get expire header
		String sessionExpiresHeader = inviteRequest.getHeader("Session-Expires");
		//If found
		if (sessionExpiresHeader!=null)
		{
			//Parse the expire time
			String expire = sessionExpiresHeader;
			//Find delimiter
			int i = sessionExpiresHeader.indexOf(';');
			//If it contains the ;refresher=uas
			if (i!=-1)
				//Remove it
				expire = expire.substring(0, i);
			//Parse
			sessionExpires = Integer.parseInt(expire);
		}
		//Update expire time
		lastSessionRefresh = new Date();
		//Create response
		SipServletResponse resp = request.createResponse(200, "OK");
		//Check session refresh request
		if (timerSupported && sessionExpires>0)
		{
			//RFC 4208 Session Timers
			//	If the UAS wishes to accept the request, it copies the value of the
			//	Session-Expires header field from the request into the 2xx response.
			resp.addHeader("Session-expires", sessionExpires.toString()+";refresher=uac");
			//Add require and supported
			resp.addHeader("Supported","timer");
			resp.addHeader("Require","timer");
		}
		//add allowed header
		resp.addHeader("Allow", ALLOWED);
		//Body
		String body = null;
		//Get content
		Object content = request.getContent();
		//Check object type
		if (content instanceof String)
		//Get content
		body = (String)content;
	else if (content!=null)
		//Get it
		body = new String((byte[])content);
		//Check body and type
		if (body!=null && request.getContentType().equalsIgnoreCase("application/sdp"))
		{
			SessionDescription sdp;

			try {
				//Parse conent
				sdp = SessionDescription.Parse(body);
			} catch (ParserException ex) {
				//Log
				logger.log(Level.SEVERE, null, ex);
				//Not supported yet
				request.createResponse(500, "SDP error "+ex.getReason()).send();
				//Exit
				return;
			}

			//Check version
			if (!timerSupported || !sdp.getOrigin().getSessVersion().equals(remoteSDP.getOrigin().getSessVersion()))
		{
		try {
			//Stop sending
			stopSending();
			//Clean data before processing
			rejectedMedias.clear();
			//Process nre remote sdp
			remoteSDP = processSDP(sdp);
			//Start receving again, just in case we added new media
			startReceiving();
			//Create local SDP
			localSDP = createSDP();
					//Negotiation done
					onSDPNegotiationDone();
			//Start sending again
			startSending();
			//Call listeners
			changedMedia();
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, "excetpion procesing updated SDP", ex);
			//Not supported yet
			request.createResponse(500, "SDP error "+ex.getMessage()).send();
			//Exit
			return;
		}
		}
			//Resend sdp
			resp.setContent(localSDP.toString(),"application/sdp");
		}
		//Send it
		resp.send();
	}

	@Override
	public boolean accept() {
		//Check state
		if (state!=State.WAITING_ACCEPT)
		{
			//LOG
			logger.log(Level.WARNING, "Accepted participant is not in WAITING_ACCEPT state  [id:{0},state:{1}].", new Object[]{getId(),state});
			//Error
			return false;
		}

		try {
			//Start receiving
			startReceiving();
			//Create final response
			SipServletResponse resp = inviteRequest.createResponse(200, "Ok");
			//Add custom headers with conf id and participant id
			resp.addHeader("X-Conference-ID", conf.getUID());
		resp.addHeader("X-Participant-ID", getId().toString());
		resp.addHeader("X-Participant-Token", getToken());
		resp.addHeader("X-Conference-Mixer-ID", conf.getId().toString());
		resp.addHeader("X-Conference-Mixer-PartID", partId.toString());
			//Add alow headers
			resp.addHeader("Allow",ALLOWED);
			//Check session refresh request
			if (timerSupported && sessionExpires>0)
			{
				//RFC 4208 Session Timers
				//	If the UAS wishes to accept the request, it copies the value of the
				//	Session-Expires header field from the request into the 2xx response.
				resp.addHeader("Session-expires", sessionExpires.toString()+";refresher=uac");
				//Add require and supported
				resp.addHeader("Supported","timer");
				resp.addHeader("Require","timer");
				//Set last session refresh
				lastSessionRefresh = new Date();
			}
			//Create local SDP
			localSDP = createSDP();
			//If also have remote SDP on the previoues INVITE
			if (remoteSDP!=null)
				//Negotiation done
				onSDPNegotiationDone();
			//Attach body
			resp.setContent(localSDP.toString(),"application/sdp");
			//Send it
			resp.send();
		} catch (Exception ex) {
			try {
		//Log
				logger.log(Level.SEVERE, null, ex);
				//Create final response
				SipServletResponse resp = inviteRequest.createResponse(500, ex.getMessage());
				//Send it
				resp.send();
			} catch (IOException ex1) {
				//Log
				logger.log(Level.SEVERE, null, ex1);
			}
			//Terminate
			error(State.ERROR, "Error");
			//Error
			return false;
		}
		//Ok
		return true;
	}

	@Override
	public boolean reject(Integer code, String reason) {
		//Check state
		if (state!=State.WAITING_ACCEPT)
		{
			//LOG
			logger.log(Level.WARNING, "Rejected participant is not in WAITING_ACCEPT state [id:{0},state:{1}].", new Object[]{getId(),state});
			//Error
			return false;
		}

		try {
			//Create final response
			SipServletResponse resp = inviteRequest.createResponse(code, reason);
			//Send it
			resp.send();
		} catch (IOException ex1) {
			//Log
			logger.log(Level.SEVERE, null, ex1);
			//Terminate
			error(State.ERROR, "Error");
			//Error
			return false;
		}
		//Terminate
		error(State.DECLINED,"Rejected");
		//Exit
		return true;
	}

	void doInvite(SipFactory sf, Address from,Address to) throws IOException, XmlRpcException {
		doInvite(sf,from,to,null,1,null);
	}

	void doInvite(SipFactory sf, Address from,Address to,int timeout) throws XmlRpcException {
		doInvite(sf,from,to,null,timeout,null);
	}

	void doInvite(SipFactory sf, Address from,Address to,SipURI proxy) throws XmlRpcException  {
		doInvite(sf, from, to,proxy,1,null);
	}

	void doInvite(SipFactory sf, Address from,Address to,SipURI proxy,int timeout,String location) throws XmlRpcException {
		try
		{
			//Store to as participant address
			address = to;
			//Store proxy
			this.proxy = proxy;
			//Start receiving media
			startReceiving();
			//Create the application session
			appSession = sf.createApplicationSession();
			// create an INVITE request to the first party from the second
			inviteRequest = sf.createRequest(appSession, "INVITE", from, to);
			//Add custom headers with conf id and participant id
			inviteRequest.addHeader("X-Conference-ID", conf.getUID());
		inviteRequest.addHeader("X-Participant-ID", getId().toString());
		inviteRequest.addHeader("X-Participant-Token", getToken());
		inviteRequest.addHeader("X-Conference-Mixer-PartID", partId.toString());
		inviteRequest.addHeader("X-Conference-Mixer-ID", conf.getId().toString());
			//Add allow header
			inviteRequest.addHeader("Allow",ALLOWED);
			//Check if we have a proxy
			if (proxy!=null)
				//Set proxy
				inviteRequest.pushRoute(proxy);
				//Check if we should use session timers on onging request
				if (sessionTimersEnabled)
				{
					//Add support for session refresh
					inviteRequest.addHeader("Supported","timer");
					//Request refresh each 5 minutes
					inviteRequest.addHeader("Session-expires", "300;refresher=uas");
				}
				//add allowed header
				inviteRequest.addHeader("Allow", ALLOWED);
			//Get call id
			setSessionId(inviteRequest.getCallId());
			//Get sip session
			session = inviteRequest.getSession();
			//Set reference in sessions
			appSession.setAttribute("user", this);
			session.setAttribute("user", this);
			//Do not invalidate
			appSession.setInvalidateWhenReady(false);
			session.setInvalidateWhenReady(false);
			//Set expire time
			appSession.setExpires(timeout);
			//Create sdp
			localSDP = createSDP();
			//Convert to
			String sdp = localSDP.toString();
			//If it has location info
			if (location!=null && !location.isEmpty())
			{
				try {

					//Get SIP uri of calling user
					SipURI uri = (SipURI)from.getURI();
					//Add location header
					inviteRequest.addHeader("Geolocation","<cid:"+uri.getUser()+"@"+uri.getHost()+">;routing-allowed=yes");
					inviteRequest.addHeader("Geolocation-Routing","yes");
					//Create multipart body
					Multipart body = new MimeMultipart();
					//Create sdp body
					BodyPart sdpPart = new MimeBodyPart();
					//Set content
					sdpPart.setContent(sdp, "application/sdp");
					//Set content headers
					sdpPart.setHeader("Content-Type","application/sdp");
					sdpPart.setHeader("Content-Length", Integer.toString(sdp.length()));
					//Add sdp
					body.addBodyPart(sdpPart);
					//Create slocation body
					BodyPart locationPart = new MimeBodyPart();
					//Set content
					locationPart.setContent(location, "application/pidf+xml");
					//Set content headers
					locationPart.setHeader("Content-Type","application/pidf+xml");
					locationPart.setHeader("Content-ID","<"+uri.getUser()+"@"+uri.getHost()+">");
					locationPart.setHeader("Content-Length", Integer.toString(location.length()));
					//Add sdp
					body.addBodyPart(locationPart);
					//Add content
					inviteRequest.setContent(body, body.getContentType().replace(" \r\n\t",""));
				} catch (MessagingException ex) {
					logger.log(Level.SEVERE, null, ex);
				}
			} else {
				//Attach body
				inviteRequest.setContent(sdp,"application/sdp");
			}
			//Set state
			setState(State.CONNECTING);
			//Send it
			inviteRequest.send();
			//Log
			Logger.getLogger(RTPParticipant.class.getName()).log(Level.WARNING, "doInvite [idSession:{0}]",new Object[]{session.getId()});
	   } catch (IOException ex) {
			logger.log(Level.SEVERE, "Error doInvite", ex);
		}
	}

	public void onInviteResponse(SipServletResponse resp) throws IOException {
		//Check state
		if (state!=State.CONNECTING)
		{
			//Log
			logger.log(Level.WARNING, "onInviteResponse while not CONNECTING [id:{0},state:{1}]",new Object[]{getId(),state});
			//Exit
			return;
		}
		//Get code
		Integer code = resp.getStatus();
		//Check response code
		if (code<200) {
			//Check code
			switch (code)
			{
				case 180:
					break;
				default:
					//DO nothing
			}
		} else if (code >= 200 && code < 300) {
			//Get supported
			ListIterator<String> supported = resp.getHeaders("Supported");
			//Check if timer found
			while(supported.hasNext() && !timerSupported)
			{
				//parse supported list
				StringTokenizer tokenizer = new StringTokenizer(supported.next(), ",");
				//Check each one
				while (tokenizer.hasMoreTokens())
				{
					//Check if found
					if ("timer".equals(tokenizer.nextToken()))
					{
						//If it is
						timerSupported = true;
						//Exit
						break;
					}
				}
			}
			//Get expire header
			String sessionExpiresHeader = resp.getHeader("Session-Expires");
			//If found
			if (sessionExpiresHeader!=null)
			{
				//Parse the expire time
				String expire = sessionExpiresHeader;
				//Find delimiter
				int i = sessionExpiresHeader.indexOf(';');
				//If it contains the ;refresher=uas
				if (i!=-1)
					//Remove it
					expire = expire.substring(0, i);
				//Parse
				sessionExpires = Integer.parseInt(expire);
			}
			//Extend expire time one minute
			appSession.setExpires(1);
			//Get confirmed session
			session = resp.getSession();
			//Set attribute
			session.setAttribute("user", this);
			//Do not invalidate
			session.setInvalidateWhenReady(false);
			//Update name
			address = resp.getTo();
		//Get uir
		SipURI uri = (SipURI)address.getURI();
		//Get name
		name = address.getDisplayName();
		//If empty
		if (name==null || name.isEmpty() || name.equalsIgnoreCase("anonymous"))
		   //Set to user
		   name = uri.getUser();
		//If still empty
		if (name==null)
		   //use host part
		   name = uri.getHost();
		
		//Get call info
		ListIterator<String> iterator = inviteRequest.getHeaders("Call-Info");
		//Append each value
		while (iterator.hasNext())
		{
			//Get next
			String callInfo = iterator.next();
			//Strip < and >
			if (callInfo.startsWith("<"))
				callInfo = callInfo.substring(1);
			if (callInfo.endsWith(">"))
				callInfo = callInfo.substring(0,callInfo.length()-1);
			//Append to info
			info.add(callInfo);

		}
		
			try {
		//Body
		String body = null;
		//Get content
		Object content = resp.getContent();
		//Check object type
		if (content instanceof String)
			//Get content
			body = (String)content;
		else
			//Get it
			body = new String((byte[])content);
				//Parse sdp
				remoteSDP = processSDP(body);
				//Check if also have local SDP
				if (localSDP!=null)
					//Negotiation done
					onSDPNegotiationDone();
				//Create ringing
				SipServletRequest ack = resp.createAck();
				//Send it
				ack.send();
				//Set state before joining
				setState(State.CONNECTED);
				//Join it to the conference
				conf.joinParticipant(this);
				//Start sending
				startSending();
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Error processing invite respose", ex);
				//Terminate
				error(State.ERROR,"Error");
			}
		} else if (code>=400) {
			//Check code
			switch (code)
			{
				case 422:
					//422 Session Timer too small, get min header
					String header = resp.getHeader("Min-SE");
					//Log
					logger.log(Level.WARNING, "422 Session Timer too small, Min-SE:{0}", header);
					//Parse it
					Integer minSessionExpires = Integer.parseInt(header);
					//Create new request
					SipServletRequest reinviteRequest = session.createRequest("INVITE");
					//Check if we have a proxy
					if (proxy!=null)
						//Set proxy
						reinviteRequest.pushRoute(proxy);
					//Add support for session refresh
					reinviteRequest.addHeader("Supported","timer");
					//Request refresh each 5 minutes
					reinviteRequest.addHeader("Session-expires", minSessionExpires.toString() +";refresher=uas");
					//add allowed header
					reinviteRequest.addHeader("Allow", ALLOWED);
					//Get original content
					reinviteRequest.setContent(inviteRequest.getContent(),inviteRequest.getContentType());
					//Update request
					inviteRequest = reinviteRequest;
					//Send it
					inviteRequest.send();
					//Not change state
					break;
				case 404:
					//Terminate
					error(State.NOTFOUND,"NOT_FOUND");
					break;
				case 486:
					//Terminate
					error(State.BUSY,"BUSY");
					break;
				case 603:
					//Terminate
					error(State.DECLINED,"DECLINED");
					break;
				case 408:
				case 480:
				case 487:
					//Terminate
					error(State.TIMEOUT,"TIMEOUT",code);
					break;
				default:
					//Terminate
					error(State.ERROR,"ERROR",code);
					break;
			}
			//Check if the session is still valid
			if (appSession.isValid())
			//Set expire time
			appSession.setExpires(1);
		}
	}

	public void onTimeout() {
		logger.log(Level.INFO, "onTimeout partId={0} state {1} totalPacketCount={2}", new Object[]{getId(),state,totalPacketCount});
		//Check state
		if (state==State.CONNECTED) {
			//Extend session 1 minutes
			appSession.setExpires(1);
			//Check session refresh
			if (sessionExpires>0 && lastSessionRefresh!=null)
			{
				//Get time diff
				Long diff =  new Date().getTime()-lastSessionRefresh.getTime();
				//check if session has expired
				if (diff/1000>sessionExpires)
				{
					//Terminate
					doBye(true);
					//Exit
					return;
				}
			}
			
			try {
		//Get client
		XmlRpcMcuClient client = conf.getMCUClient();
			//Get statiscits
		stats =  client.getParticipantStatistics(conf.getId(), partId);
			//Calculate acumulated packets
			Integer num = 0;
		//Check we found it
		if (stats!=null)
			//For each media
			for (MediaStatistics s : stats.values())
				//Increase packet count
				num += s.numRecvPackets;
			//Check
			if (!num.equals(totalPacketCount)) {
				//Update
				totalPacketCount = num;
		}  else if (useRTPTimeout) {
				//Terminate
				doBye(true);
			}
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
			}
			
		} else if (state==State.CONNECTING) {
			//Cancel request
			doCancel(true);
		} else {
			//Teminate
			destroy();
		}
	}

	public void onAckRequest(SipServletRequest request) throws IOException {
		//Check if it has content
		if (request.getContentLength()>0)
			//Process it
			proccesContent(request.getContentType(),request.getContent());
		try {
		//If it is not n ACK for a reInvite
		if (state!=State.CONNECTED)
		{
		//Get call info
		ListIterator<String> iterator = inviteRequest.getHeaders("Call-Info");
		//Append each value
		while (iterator.hasNext())
		{
			//Get next
			String callInfo = iterator.next();
			//Strip < and >
			if (callInfo.startsWith("<"))
				callInfo = callInfo.substring(1);
			if (callInfo.endsWith(">"))
				callInfo = callInfo.substring(0,callInfo.length()-1);
			//Check we are not duplicating items
			if (!info.contains(callInfo))
				//Append to info
				info.add(callInfo);
		}
			//Set state before joining
			setState(State.CONNECTED);
			//Join it to the conference
			conf.joinParticipant(this);
			//Start sending
			startSending();
		}
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}

	void doCancel(boolean timedout) {
		try{
			//Create BYE request
			SipServletRequest req = inviteRequest.createCancel();
			//Send it
			req.send();
		} catch(IOException ex){
			logger.log(Level.SEVERE, null, ex);
		} catch(IllegalStateException ex){
			logger.log(Level.SEVERE, null, ex);
		}
		//Set expire time
		appSession.setExpires(1);
		//Check which state we have to set
		if (timedout)
			//TImeout
			setState(State.TIMEOUT);
		else
			//Disconnected
			setState(State.DISCONNECTED);
		//Terminate
		destroy();
	}

	void doBye(boolean timeout) {
		try{
			//Create BYE request
			SipServletRequest req = session.createRequest("BYE");
			//Send it
			req.send();
		} catch(Exception ex){
			logger.log(Level.SEVERE, null, ex);
		}
		try {
			//Set expire time
			appSession.setExpires(1);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Error expiring user", ex);
		}
		//Check which state we have to set
		if (timeout)
			//TImeout
			setState(State.TIMEOUT);
		else
			//Disconnect
			setState(State.DISCONNECTED);

		//Terminate
		destroy();
	}

	public void onByeResponse(SipServletResponse resp) {
	}

	public void onByeRequest(SipServletRequest request) {
		try {
			//Create final response
			SipServletResponse resp = request.createResponse(200, "Ok");
			//Send it
			resp.send();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		//Set expire time
		appSession.setExpires(1);
		//Disconnect
		setState(State.DISCONNECTED);
		//Terminate
		destroy();
	}

	@Override
	public void end() {
		//Log
		logger.log(Level.INFO, "Ending RTP user id:{0} in state {1}", new Object[]{getId(),state});
		//Depending on the state
		switch (state)
		{
			case CONNECTING:
				doCancel(false);
				break;
			case CONNECTED:
				doBye(false);
				break;
			default:
				//Destroy
				destroy();
		}
	}

	@Override
	public void destroy() {
		try {
			//Get client
			XmlRpcMcuClient client = conf.getMCUClient();
		//Update stats
		Map<String, MediaStatistics> updated = client.getParticipantStatistics(conf.getId(), partId);
		//If no error
		if (updated!=null)
			//Update latest ones
			stats = updated;
	
			//Delete participant
			client.DeleteParticipant(conf.getId(), partId);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, null, ex);
		}

		try {
			//If ther was a session
			if (session!=null && session.isValid())
			{
				//Remove participant from session
				session.removeAttribute("user");
				//Invalidate the session when appropiate
				session.setInvalidateWhenReady(true);
			}
			//If there was an application session
			if (appSession!=null && appSession.isValid())
			{
				//Remove participant from session
				appSession.removeAttribute("user");
				//Set expire time to let it handle any internal stuff
				appSession.setExpires(1);
				//Invalidate the session when appropiate
				appSession.setInvalidateWhenReady(true);
			}
		} catch (Exception ex) {
			Logger.getLogger(RTPParticipant.class.getName()).log(Level.SEVERE, null, ex);
		}
		//Set state
		setState(State.DESTROYED);
	}

	public void stopSending() throws XmlRpcException {
	//Get client
		XmlRpcMcuClient client = conf.getMCUClient();
		//Get conf id
		Integer confId = conf.getId();

		//Check audio
		if (getSendAudioPort()!=0 && rtpDirections.get("audio").isReceving())
			//Stop sending
			client.StopSending(confId, partId, MediaType.AUDIO);

		//Check video
		if (getSendVideoPort()!=0 && rtpDirections.get("video").isReceving())
		//Stop sending
			client.StopSending(confId, partId, MediaType.VIDEO);

		//Check text
		if (getSendTextPort()!=0 && rtpDirections.get("text").isReceving())
		//Stop sending
			client.StopSending(confId, partId, MediaType.TEXT);
	}

	public void startSending() throws XmlRpcException {
		//Get client
		XmlRpcMcuClient client = conf.getMCUClient();
		//Get conf id
		Integer confId = conf.getId();

		//Check audio
		if (getSendAudioPort()!=0 && rtpDirections.get("audio").isReceving())
		{
			//Set codec
			client.SetAudioCodec(confId, partId, getAudioCodec());
			//Send
			client.StartSending(confId, partId, MediaType.AUDIO, getSendAudioIp(), getSendAudioPort(), getRtpOutMediaMap("audio"));
		}

		//Check video
		if (getSendVideoPort()!=0 && rtpDirections.get("video").isReceving())
		{
			//Get profile bitrat
			int bitrate = profile.getVideoBitrate();
			//Reduce to the maximum in SDP
			if (videoBitrate>0 && videoBitrate<bitrate)
					//Reduce it
					bitrate = videoBitrate;
			//Create specific codec paraemter
			HashMap<String,String> params = new HashMap<String, String>();
			//Check codec
			if (Codecs.H264.equals(getVideoCodec()))
					//Add profile level id
					params.put("h264.profile-level-id", h264profileLevelId.toString());
			//Set codec
			client.SetVideoCodec(confId, partId, getVideoCodec(), profile.getVideoSize() , profile.getVideoFPS(), bitrate,profile.getIntraPeriod(),params);
			//Send
			client.StartSending(confId, partId, MediaType.VIDEO, getSendVideoIp(), getSendVideoPort(), getRtpOutMediaMap("video"));
		}

		//Check text
		if (getSendTextPort()!=0 && rtpDirections.get("text").isReceving())
		{
			//Set codec
			client.SetTextCodec(confId, partId, getTextCodec());
			//Send
			client.StartSending(confId, partId, MediaType.TEXT, getSendTextIp(), getSendTextPort(), getRtpOutMediaMap("text"));
		}
	}

	public void startReceiving() throws XmlRpcException {
		//Get client
		XmlRpcMcuClient client = conf.getMCUClient();
		//Get conf id
		Integer confId = conf.getId();

	//Check if using DTLS
	if (useDTLS)
		//Get fingerprint
		localFingerprint = client.GetLocalCryptoDTLSFingerprint(localHash);

		//If supported
		if (getAudioSupported())
		{
		//If not already receiving
		if (recAudioPort==0)
		{
			//Create rtp map for audio
			createRTPMap("audio");

			//Check if we are secure
			if (isSecure)
			{
				//Create new cypher and add to local info
				localCryptoInfo.put("audio", CryptoInfo.Generate());
		//Set property
		rtpMediaProperties.get("audio").put("secure","1");
			}

			//Check if using ICE
			if (useICE)
			{
				//Create new ICE Info
				ICEInfo info = ICEInfo.Generate();
				//Set them
				client.SetLocalSTUNCredentials(confId, partId, MediaType.AUDIO, info.ufrag, info.pwd);
				//Add to local info
				localICEInfo.put("audio", info);
			}
			//Get receiving ports
			recAudioPort = client.StartReceiving(confId, partId, MediaType.AUDIO, getRtpInMediaMap("audio"));
		}
		} else if (recAudioPort>0) {
		//Stop it
		if (client.StopReceiving(confId, partId, MediaType.AUDIO))
			//Disable port
			recAudioPort = 0;
	}

		//If supported
		if (getVideoSupported())
		{
		//If not already receiving
		if (recVideoPort==0)
		{
			//Create rtp map for video
			createRTPMap("video");

			//Check if we are secure
			if (isSecure)
			{
				//Create new cypher and add to local info
				localCryptoInfo.put("video", CryptoInfo.Generate());
		//Set property
		rtpMediaProperties.get("video").put("secure","1");
			}

			//Check if using ICE
			if (useICE)
			{
				//Create new ICE Info
				ICEInfo info = ICEInfo.Generate();
				//Set them
				client.SetLocalSTUNCredentials(confId, partId, MediaType.VIDEO, info.ufrag, info.pwd);
				//Add to local info
				localICEInfo.put("video", info);
			}
			//Get receiving ports
			recVideoPort = client.StartReceiving(confId, partId, MediaType.VIDEO, getRtpInMediaMap("video"));
		}
		} else if (recVideoPort>0) {
		//Stop it
		if (client.StopReceiving(confId, partId, MediaType.VIDEO))
			//Disable port
			recVideoPort = 0;
	}

		//If supported
		if (getTextSupported())
		{
		//If not already receiving
		if (recTextPort==0)
		{
			//Create rtp map for text
			createRTPMap("text");

			//Check if we are secure
			if (isSecure)
			{
				//Create new cypher and add to local info
				localCryptoInfo.put("text", CryptoInfo.Generate());
		//Set property
		rtpMediaProperties.get("text").put("secure","1");
			}

			//Check if using ICE
			if (useICE)
			{
				//Create new ICE Info
				ICEInfo info = ICEInfo.Generate();
				//Set them
				client.SetLocalSTUNCredentials(confId, partId, MediaType.TEXT, info.ufrag, info.pwd);
				//Add to local info
				localICEInfo.put("text", info);
			}
			//Get receiving ports
			recTextPort = client.StartReceiving(confId, partId, MediaType.TEXT, getRtpInMediaMap("text"));
		}
		} else if (recTextPort>0) {
		//Stop it
		if (client.StopReceiving(confId, partId, MediaType.TEXT))
			//Disable port
			recTextPort = 0;
	}

		//And ip
		setRecIp(conf.getRTPIp());
	}

   public void sendFPU() {
		//Get client
		XmlRpcMcuClient client = conf.getMCUClient();
		//Get id
		Integer confId = conf.getId();
		try {
			//Send fast pcture update
			client.SendFPU(confId, partId);
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	void requestFPU() {
		//If the other side supports INFO request
		if (useInfo)
		{
		//Send FPU
		String xml ="<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n<media_control>\r\n<vc_primitive>\r\n<to_encoder>\r\n<picture_fast_update></picture_fast_update>\r\n</to_encoder>\r\n</vc_primitive>\r\n</media_control>\r\n";
		try {
			 //Create ack
			SipServletRequest info = session.createRequest("INFO");
			//Set content
			info.setContent(xml, "application/media_control+xml");
			//Send it
			info.send();
		} catch (IOException ex) {
			//Log it
				logger.log(Level.SEVERE, "Error while requesting FPU for participant", ex);
		}
	}
	}

	private void onSDPNegotiationDone() throws XmlRpcException {
	//Log it
		logger.log(Level.INFO, "onSDPNegotiationDone videoBitrate:{0}", videoBitrate);
		//Get client
		XmlRpcMcuClient client = conf.getMCUClient();
		//Get conf id
		Integer confId = conf.getId();

		//If supported
		if (getAudioSupported())
		{

		//Check if DTLS enabled
		if (useDTLS)
		{
			//Get cryto info
		DTLSInfo info = remoteDTLSInfo.get("audio");
		//If present
		if (info!=null)
			//Set it
			client.SetRemoteCryptoDTLS(confId, partId,  MediaType.AUDIO, info.getSetup(), info.getHash(), info.getFingerprint());
		} else {
		//Get local crypto info
		CryptoInfo local = localCryptoInfo.get("audio");
		//If present
		if (local!=null)
			//Set it
			client.SetLocalCryptoSDES(confId, partId, MediaType.AUDIO, local.suite, local.key);

		//Get cryto info
		CryptoInfo remote = remoteCryptoInfo.get("audio");
			//If present
		if (remote!=null)
				//Set it
			   client.SetRemoteCryptoSDES(confId, partId, MediaType.AUDIO, remote.suite, remote.key);
		}

			//Get ice info
			ICEInfo ice = remoteICEInfo.get("audio");
			//If present
			if (ice!=null)
				//Set it
			   client.SetRemoteSTUNCredentials(confId, partId, MediaType.AUDIO, ice.ufrag, ice.pwd);
			//Set RTP properties
			client.SetRTPProperties(confId, partId, MediaType.AUDIO, rtpMediaProperties.get("audio"));
		}

		//If supported
		if (getVideoSupported())
		{
		//Check if DTLS enabled
		if (useDTLS)
		{
			//Get cryto info
		DTLSInfo info = remoteDTLSInfo.get("video");
		//If present
		if (info!=null)
			//Set it
			client.SetRemoteCryptoDTLS(confId, partId,  MediaType.VIDEO, info.getSetup(), info.getHash(), info.getFingerprint());
		} else {
		//Get local crypto info
		CryptoInfo local = localCryptoInfo.get("video");
		//If present
		if (local!=null)
			//Set it
			client.SetLocalCryptoSDES(confId, partId, MediaType.VIDEO, local.suite, local.key);

		//Get cryto info
		CryptoInfo remote = remoteCryptoInfo.get("video");
			//If present
		if (remote!=null)
				//Set it
			   client.SetRemoteCryptoSDES(confId, partId, MediaType.VIDEO, remote.suite, remote.key);
		}

						//Get ice info
			ICEInfo ice = remoteICEInfo.get("video");
			//If present
			if (ice!=null)
				//Set it
			   client.SetRemoteSTUNCredentials(confId, partId, MediaType.VIDEO, ice.ufrag, ice.pwd);
			//Set RTP properties
			client.SetRTPProperties(confId, partId, MediaType.VIDEO, rtpMediaProperties.get("video"));
		}

		//If supported
		if (getTextSupported())
		{
		//Check if DTLS enabled
		if (useDTLS)
		{
			//Get cryto info
		DTLSInfo info = remoteDTLSInfo.get("text");
		//If present
		if (info!=null)
			//Set it
			client.SetRemoteCryptoDTLS(confId, partId,  MediaType.TEXT, info.getSetup(), info.getHash(), info.getFingerprint());
		} else {
		//Get local crypto info
		CryptoInfo local = localCryptoInfo.get("text");
		//If present
		if (local!=null)
			//Set it
			client.SetLocalCryptoSDES(confId, partId, MediaType.TEXT, local.suite, local.key);

		//Get cryto info
		CryptoInfo remote = remoteCryptoInfo.get("text");
			//If present
		if (remote!=null)
				//Set it
			   client.SetRemoteCryptoSDES(confId, partId, MediaType.TEXT, remote.suite, remote.key);
		}
			//Get ice info
			ICEInfo ice = remoteICEInfo.get("text");
			//If present
			if (ice!=null)
				//Set it
			   client.SetRemoteSTUNCredentials(confId, partId, MediaType.TEXT, ice.ufrag, ice.pwd);
			//Set RTP properties
			client.SetRTPProperties(confId, partId, MediaType.TEXT, rtpMediaProperties.get("text"));
		}
	}
}
