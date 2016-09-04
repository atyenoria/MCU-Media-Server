/*
 * Conference.java
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

import javax.servlet.sip.ServletParseException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipURI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.MediaServer.Codecs;
import org.murillo.MediaServer.XmlRpcMcuClient;
import org.murillo.MediaServer.XmlRpcMcuClient.MediaStatistics;
import org.murillo.mcu.exceptions.ParticipantNotFoundException;
import org.murillo.mcuWeb.Participant.State;

/**
 *
 * @author esergar
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class Conference implements Participant.Listener,Serializable {

	public void onMediaChanged(Participant part) {
	}

	public void onMediaMuted(Participant part, String media, boolean muted) {
	}

	public interface Listener {
		public void onConferenceInited(Conference conf);
		public void onConferenceEnded(Conference conf);
		public void onConferenceRecordingStarted(Conference conf);
		public void onConferenceRecordingStopped(Conference conf);
		public void onParticipantCreated(String confId, Participant part);
		public void onParticipantStateChanged(String confId, Integer partId, State state, Object data, Participant part);
		public void onParticipantMediaChanged(String confId, Integer partId, Participant part);
		public void onParticipantMediaMuted(String confId,  Integer partId, Participant part,String media, boolean muted);
		public void onParticipantDestroyed(String confId, Integer partId);
		public void onOwnerChanged(String confId, Integer partId, Object data, Participant owner);
	}

	protected Integer id;
	protected String UID;
	@XmlElement
	protected Date timestamp;
	@XmlElement
	protected String name;
	@XmlElement
	protected String did;
	protected SipURI uri;
	protected XmlRpcMcuClient client;
	@XmlElement
	protected MediaMixer mixer;
	protected ConcurrentHashMap<Integer,Participant> participants;
	@XmlElement
	protected int numActParticipants;
	@XmlElement
	protected Integer vad;
	@XmlElement
	protected boolean autoAccept;
	@XmlElement
	protected Boolean isAdHoc;
	protected Profile profile;
	protected SipFactory sf;
	@XmlElement
	protected boolean addToDefaultMosaic;
	protected boolean isDestroying;
	protected HashSet<Listener> listeners;
	protected HashMap<String,List<Integer>> supportedCodecs = null;
	protected HashMap<String,String> properties = null;
	@XmlElement
	private SipURI defaultProxyUri;
	@XmlElement
	private Boolean defatultRtcpFeedBack;
	@XmlElement
	private boolean defaultUseIce;
	@XmlElement
	private Boolean defaultIsSecure;
	@XmlElement
	private boolean defaultUseDTLS;
	@XmlElement
	private boolean broadcasting;

	private Integer numSlots;
	private Integer compType;
	private Integer size;
	private Integer slots[];

	//Participant id counter
	private final AtomicInteger count = new AtomicInteger(XmlRpcMcuClient.AppMixerId);
	private final AtomicBoolean inited = new AtomicBoolean(false);

	private static final Logger logger =  Logger.getLogger(Conference.class.getName());

	public Conference(){
		//Empty constructor for XML serializator
	}

	/** Creates a new instance of Conference */
	public Conference(SipFactory sf,String name,String did,SipURI uri,MediaMixer mixer,Integer size,Integer compType,Integer vad, Profile profile,Boolean isAdHoc) throws XmlRpcException {
		//Create the client
		this.client = mixer.createMcuClient();
		//Generate a uuid and apped it to the did to get an unique id not overlapping with the did
		UID = did+"-"+UUID.randomUUID().toString();
		//Create conference
		this.id = client.CreateConference(UID,mixer.getEventQueueId());
		//Get timestamp
		this.timestamp = new Date();
		//Save values
		this.mixer = mixer;
		this.name = name;
		this.did = did;
		this.uri = uri;
		this.profile = profile;
		this.isAdHoc = isAdHoc;
		this.vad = vad;
		//Create listeners
		listeners = new HashSet<Listener>();
		//Default composition and size
		this.compType = compType;
		this.size = size;
		this.numSlots = XmlRpcMcuClient.getMosaicNumSlots(compType);
		//Num of active participants
		this.numActParticipants = 0;
		//Store sip factory
		this.sf = sf;
		//Create conference slots
		this.slots = new Integer[numSlots];
		//Empty
		for (int i=0;i<numSlots;i++)
			//All free
			slots[i] = 0;
		//Create supported codec map
		supportedCodecs = new HashMap<String, List<Integer>>();
		//Create properties map
		properties = new HashMap<String,String>();
		//Enable all audio codecs
		addSupportedCodec("audio", Codecs.OPUS);
		addSupportedCodec("audio", Codecs.SPEEX16);
		addSupportedCodec("audio", Codecs.GSM);
		addSupportedCodec("audio", Codecs.G722);
		addSupportedCodec("audio", Codecs.PCMU);
		addSupportedCodec("audio", Codecs.PCMA);
		//Enable all video codecs
		addSupportedCodec("video", Codecs.VP8);
		addSupportedCodec("video", Codecs.H264);
		addSupportedCodec("video", Codecs.H263_1998);
		addSupportedCodec("video", Codecs.MPEG4);
		addSupportedCodec("video", Codecs.H263_1996);
		addSupportedCodec("video", Codecs.RED);
		addSupportedCodec("video", Codecs.ULPFEC);
		addSupportedCodec("video", Codecs.RTX);
		//Enable all text codecs
		addSupportedCodec("text", Codecs.T140RED);
		addSupportedCodec("text", Codecs.T140);
		//Create the participant map
		participants = new ConcurrentHashMap<Integer,Participant>();
		//Set composition type
		client.SetCompositionType(id,XmlRpcMcuClient.DefaultMosaic,compType, size);
		//If it is a 1P type set fist slot for VAD
		if (vad!=XmlRpcMcuClient.VADNONE && (compType==XmlRpcMcuClient.MOSAIC1p7 || compType==XmlRpcMcuClient.MOSAIC1p5))
			//Vad controlled
			setMosaicSlot(0,XmlRpcMcuClient.SLOTVAD);
		//Set properties for conference init
		properties.put("vad-mode"								,Integer.toString(vad));
		properties.put("audio.mixer.rate"						,profile.getAudioRate().toString());
		//By default add to default mosaic
		addToDefaultMosaic = true;
		//By default do autoAccept
		autoAccept = true;
		//RTP defaults
		defatultRtcpFeedBack = false;
		defaultUseIce = false;
		defaultUseDTLS = false;
		defaultIsSecure = false;
		//Not isDestroying
		isDestroying = false;
		//Start broadcast
		broadcasting = client.StartBroadcaster(id);
		//Start recording
		startRecordingBroadcaster(applyVariables("/var/recordings/${DID}-${TS}.flv"));
	}

	public void init()  throws XmlRpcException  {
		synchronized(inited) {
			//Init conference
			client.InitConference(id,properties);
			//We are inited
			fireOnConferenceInited();
			//We are inited
			inited.set(true);
			//Notify all
			inited.notifyAll();
		}
	}

	public void destroy() {
		//Check if already was destroying
		if (isDestroying)
			//Do nothing
			return;
		//We are isDestroying
		isDestroying = true;
		//For each participant
		for (Participant part : participants.values())
		{
			try {
				//Disconnect
				part.end();
			} catch (Exception ex) {
				 logger.log(Level.SEVERE, "Error ending or destroying participant on conference destroy", ex);
			}
		}

		try {
			//Check if broadcasting
			if (broadcasting)
				//Stop broadcast
				client.StopBroadcaster(id);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Error StopBroadcaster on conference destroy", ex);
		}

		try {
			//Remove conference
			client.DeleteConference(id);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Error deleting conference on destroy", ex);
		}
		//launch event
		fireOnConferenceEnded();
		//Remove client
		mixer.releaseMcuClient(client);
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Integer getCompType() {
		return compType;
	}

	public boolean isAutoAccept() {
		return autoAccept;
	}

	public void setAutoAccept(boolean autoAccept) {
		this.autoAccept = autoAccept;
	}

	public Integer getVADMode() {
		return vad;
	}

	private void setCompType(Integer compType) {
		//Save mosaic info
		this.compType = compType;

		//Get new number of slots
		Integer n = XmlRpcMcuClient.getMosaicNumSlots(compType);
		//Create new slot array
		Integer[] s = new Integer[n];

		//Copy the old ones into the new one
		for (int i=0; i<n && i<numSlots; i++)
			//Copy
			s[i] = slots[i];

		//The rest are empty
		for (int i=numSlots;i<n;i++)
			//Free slots
			s[i] = 0;

		//Save new slots
		numSlots = n;
		slots = s;
	}

	public Integer getSize() {
		return size;
	}

	public Integer getNumSlots() {
		return numSlots;
	}

	public Collection<Participant> getParticipants() {
		return participants.values();
	}

	public Integer getNumParcitipants() {
		return numActParticipants;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public void setDefatultRtcpFeedBack(Boolean defatultRtcpFeedBack) {
		this.defatultRtcpFeedBack = defatultRtcpFeedBack;
	}

	public void setDefaultIsSecure(Boolean defaultIsSecure) {
		this.defaultIsSecure = defaultIsSecure;
	}

	public void setDefaultUseIce(boolean defaultUseIce) {
		this.defaultUseIce = defaultUseIce;
	}

	public Participant getParticipant(Integer partId) throws ParticipantNotFoundException  {
		//Get participant
		Participant part = participants.get(partId);
		//If not found
		if (part==null)
			//Throw new exception
			throw new ParticipantNotFoundException(partId);
		//return participant
		return part;
	}

	 public Participant getParticipantByPartId(Integer partId) throws ParticipantNotFoundException  {
		 //For each participant
		for (Participant part : participants.values() )
			//Checlk if it has same partiticpant Id
			if (partId.equals(part.getPartId()))
				//return participant
				return part;
		//Throw new exception
		throw new ParticipantNotFoundException(partId);
		
	}
	 
	public boolean getAutoAccept() {
		return autoAccept;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@XmlElement(name="id")
	public String getUID() {
		return UID;
	}

	@XmlElement(name="defaultProxy")
	public String getDefaultProxy() {
		return defaultProxyUri!=null?defaultProxyUri.toString():null;
	}

	public String getDID() {
		return did;
	}

	public MediaMixer getMixer() {
		return mixer;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public final void setMosaicSlot(Integer num, Integer partId) {
		//Set mosaic slot for default mosaic
		setMosaicSlot(XmlRpcMcuClient.DefaultMosaic, num, partId);
	}

	public void setMosaicSlot(Integer mosaicId, Integer num, Integer partId) {
		//Set composition type
		try {
			//Set mosaic
			client.SetMosaicSlot(id, mosaicId, num, partId);
			//Set slot
			slots[num] = partId;
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}

	public void addMosaicParticipant(Integer mosaicId,Integer partId) {
		try {
			//Add participant to mosaic
			client.AddMosaicParticipant(id, mosaicId, partId);
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}

	public void removeMosaicParticipant(Integer mosaicId,Integer partId) {
		try {
			//Remove participant from mosaic
			client.RemoveMosaicParticipant(id, mosaicId, partId);
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}
	public Integer[] getMosaicSlots() {
		//Return a clone
		return slots.clone();
	}

	public Boolean isAdHoc() {
		return isAdHoc;
	}

	public Participant createParticipant(Participant.Type type, String name,int mosaicId, int sidebarId) {
		Participant part = null;
		//Block until we are inited
		synchronized(inited) {
			//Check if we are inited
			if (!inited.get()) 
			{
				try {
					//Wait until we are inited
					inited.wait(30000);
					//Check if we are inited again
					if (!inited.get()) {
						//Double check 
						logger.log(Level.SEVERE, "Timeout waiting for init");
						//Exit
						return null;
					}
				} catch (InterruptedException ex) {
					logger.log(Level.SEVERE, "Interrupted while waiting for conference init", ex);
					//Exit
					return null;
				}
			}
		}
		try {
			//Check name
			if (name==null)
				//Empte name
				name = "";
			//Create unique auth token
			String token = UUID.randomUUID().toString();
			//Create participant in mixer conference
			Integer partId = client.CreateParticipant(id,name.replace('.','_'),token,type.valueOf(),mosaicId,sidebarId);
			//Check type
			if (type==Participant.Type.SIP)
			{
				//Create the participant
				part = new RTPParticipant(count.incrementAndGet(),partId,name,token,mosaicId,sidebarId,this);
				//Set defaults
				((RTPParticipant)part).setIsSecure(defaultIsSecure);
				((RTPParticipant)part).setUseICE(defaultUseIce);
				((RTPParticipant)part).setUseDTLS(defaultUseDTLS);
				((RTPParticipant)part).setRtcpFeedBack(defatultRtcpFeedBack);
				//For each supported coed
				for (Entry<String,List<Integer>> media : supportedCodecs.entrySet())
					//for each codec
					for (Integer codec : media.getValue())
						//Add media codec
						((RTPParticipant)part).addSupportedCodec(media.getKey(), codec);
			}
			//Set autoAccept
			part.setAutoAccept(autoAccept);
			//Append to list
			participants.put(partId, part);
			//Fire event
			fireOnParticipantCreated(part);
			//Set listener
			part.setListener(this);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to create participant", ex);
		}
		return part;
	}

	public int createMosaic(Integer compType, Integer size) {
		try {
			//Create new mosaic
			return client.CreateMosaic(id, compType, size);
		} catch (XmlRpcException ex) {
			//Log
			logger.log(Level.SEVERE, null, ex);
			//Error
			return -1;
		}
	}

	public boolean setMosaicOverlayImage(Integer mosaicId, String fileName) {
		try {
			// create new mosaic
			return client.SetMosaicOverlayImage(id, mosaicId, fileName);
		} catch (XmlRpcException ex) {
			//Log
			logger.log(Level.SEVERE, "failed to send MCU setMosaicOverlayImage", ex);
			//Exit
			return false;
		}
	}

	public boolean resetMosaicOverlay(Integer mosaicId) {
		try {
			return client.ResetMosaicOverlay(id, mosaicId);
		} catch (XmlRpcException ex) {
			//Log
			logger.log(Level.SEVERE, "failed to send MCU ResetMosaicOverlay", ex);
			//exit
			return false;
		}
	}

	public boolean deleteMosaic(Integer mosaicId) {
		try {
			return client.DeleteMosaic(id, mosaicId);
		} catch (XmlRpcException ex) {
			//Log
			logger.log(Level.SEVERE, "failed to send MCU DeleteMosaic", ex);
			//Error
			return false;
		}
	}

	public void joinParticipant(Participant part) throws XmlRpcException {
		//If auto add participant to default mosaic
		if (addToDefaultMosaic)
		{
			//Check if video is supported
			if (part.getVideoSupported() && part.isSending("video"))
				//Add it to the default mosiac
				client.AddMosaicParticipant(id, XmlRpcMcuClient.DefaultMosaic, part.getPartId());
			//Check if audio is supported
			if (part.getAudioSupported() && part.isSending("audio"))
				//Add it to the default sidebar
				client.AddSidebarParticipant(id, XmlRpcMcuClient.DefaultSidebar, part.getPartId());
		}
	}

	boolean acceptParticipant(Integer partId, Integer mosaicId, Integer sidebarId) throws XmlRpcException, ParticipantNotFoundException {
		//Get the participant
		Participant part = getParticipant(partId);

		//Set mosaic for participant
		client.SetParticipantMosaic(id,part.getPartId(),mosaicId);
		//Store value
		part.setMosaicId(mosaicId);
		//Set sidebar for participant
		client.SetParticipantSidebar(id,part.getPartId(),sidebarId);
		//Set it
		part.setSidebarId(sidebarId);

		//Accept participant
		if (!part.accept())
			//Exit
			return false;
		//ok
		return true;
	}

	boolean rejectParticipant(Integer partId) throws ParticipantNotFoundException {
		//Get the participant
		Participant part = getParticipant(partId);
		//Reject it
		return part.reject(486, "Rejected");
	}

	public boolean changeParticipantProfile(Integer partId, Profile profile) throws ParticipantNotFoundException {
		//Get the participant
		Participant part = getParticipant(partId);
		//Set new video profile
		return part.setVideoProfile(profile);
	}

	public void setParticipantAudioMute(Integer partId, Boolean flag) throws ParticipantNotFoundException {
		//Get the participant
		Participant part = getParticipant(partId);
		//Set new video profile
		part.setAudioMuted(flag);
	}

	public void setParticipantVideoMute(Integer partId, Boolean flag) throws ParticipantNotFoundException {
		//Get the participant
		Participant part = getParticipant(partId);
		//Set new video profile
		part.setVideoMuted(flag);
	}

	public void setCompositionType(Integer compType, Integer size) {
		//Set it with default mosaic
		setCompositionType(XmlRpcMcuClient.DefaultMosaic,compType, size);
	}

	public void setCompositionType(Integer mosaicId, Integer compType, Integer size) {
		try {
			//Set composition
			client.SetCompositionType(id, mosaicId, compType, size);
			//Set composition size
			setCompType(compType);
			//Set mosaic size
			setSize(size);
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}

	public RTMPUrl addConferenceToken() {
		//Generate uid token
		String token = UUID.randomUUID().toString();
		try {
			//Add token
			client.AddConferencetToken(id, token);
		 } catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		//Add token playback url
		return new RTMPUrl("rtmp://"+mixer.getPublicIp()+"/mcu/"+id,"watcher/"+token);
	}

	public Participant callParticipant(String dest) {
		return callParticipant(dest,null);
	}

	public Participant callParticipant(String dest,String proxy) {
		//Get ip address
		String domain = System.getProperty("SipBindAddress");
		//If it is not set
		if (domain==null)
			//Set dummy domain
			domain = "mcuWeb";
		//Call
		return callParticipant(dest,"sip:"+did+"@"+domain,proxy,XmlRpcMcuClient.DefaultMosaic,XmlRpcMcuClient.DefaultSidebar);
	}

	public Participant callParticipant(String dest,String orig,String proxy,int mosaicId,int sidebarId) {
		//Log
		logger.log(Level.INFO, "Calling {0} via {1} proxy or {2} default proxy", new Object[]{dest,proxy,defaultProxyUri});
		try {
			SipURI proxyUri = null;
			//Create addresses
			Address to = sf.createAddress(dest);
			Address from = sf.createAddress(orig);
			//Creaete proxy
			if (proxy!= null && !proxy.isEmpty())
				//Create uri
				proxyUri = (SipURI) sf.createURI(proxy);
			else
				//Use default proxy
				proxyUri = defaultProxyUri;
			//Get name
			String partName = to.getDisplayName();
			//If empty
			if (partName==null || partName.isEmpty() || partName.equalsIgnoreCase("anonymous"))
				//Set to user
				partName = ((SipURI)to.getURI()).getUser();
			//Create participant
			RTPParticipant part = (RTPParticipant) createParticipant(Participant.Type.SIP,partName,mosaicId,sidebarId);
			//Make call
			part.doInvite(sf,from,to,proxyUri);
			//Return participant
			return part;
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "ERROR calling participant", ex);
		}
		//Return participant
		return null;
	}

	public void removeParticipant(Integer partId) throws ParticipantNotFoundException {
		//Get participant
		Participant part = getParticipant(partId);
		//Log
		logger.log(Level.SEVERE, "removing participant partId={0} partName={1}", new Object[]{partId, part.getName()});
		//End it
		part.end();
	}

	Map<String, MediaStatistics> getParticipantStats(Integer partId) {
		try {
			//Get them
			return client.getParticipantStatistics(id, partId);
		} catch (XmlRpcException ex) {
			logger.log(Level.SEVERE, null, ex);
			return null;
		}
	}

	@Override
	public void onStateChanged(Participant part, State state,State prev) {
		//If we are destroying
		if (isDestroying)
			//Exit
			return;
		//Launch event
		fireOnParticipantStateChanged(part,state,part.getData());
		//Check previous state
		if (prev.equals(State.CREATED))
			//Increase the number of active participanst
			numActParticipants++;

		//Check new state
		if (state.equals(State.DESTROYED))
		{
			//launch ended
			fireOnParticipantDestroyed(part);
			//Reduce number of active participants
			numActParticipants--;
			//Delete
			participants.remove(part.getId());
			//Check if it is an add hoc conference and there are not any other participant
			if(numActParticipants<1 && !isDestroying)
				//We are finished
				destroy();
		}
	}

	 public final void clearSupportedCodec(String media) {
		 //Check if we have the media
		 if (supportedCodecs.containsKey(media))
			//clear it
			supportedCodecs.get(media).clear();
	}

	 public final void addSupportedCodec(String media,Integer codec) {
		 //Check if we have the media
		 if (!supportedCodecs.containsKey(media))
			 //Create it
			 supportedCodecs.put(media, new Vector<Integer>());
		 //Add codec to media
		 supportedCodecs.get(media).add(codec);
	 }

	 protected final XmlRpcMcuClient getMCUClient() {
		 return client;
}

	 protected final String getRTPIp() {
		 return mixer.getIp();
	 }

	 protected final String getRTMPIp() {
		 return mixer.getPublicIp();
	 }

	 public void addListener(Listener listener) {
		 //Add it
		 listeners.add(listener);
	 }

	 public void removeListener(Listener listener) {
		 //Remove from set
		 listeners.remove(listener);
	 }

	 private void fireOnConferenceInited() {
		 //Log
		logger.log(Level.FINE, "conference inited confId={0}", new Object[]{getId()});
		//For each listener in set
		for (Listener listener : listeners)
			//Send it
			listener.onConferenceInited(this);
	 }

	 private void fireOnConferenceEnded() {
		//Log
		logger.log(Level.FINE, "conference ended confId={0}", new Object[]{getId()});
		//For each listener in set
		for (Listener listener : listeners)
			//Send it
			listener.onConferenceEnded(this);
	}

	private void fireOnParticipantCreated(Participant part) {
		//For each listener in set
		for (Listener listener : listeners)
			//Send it
			listener.onParticipantCreated(getUID(),part);
	}

	private void fireOnParticipantDestroyed(Participant part) {
		//For each listener in set
		for (Listener listener : listeners)
			//Send it
			listener.onParticipantDestroyed(getUID(),part.getId());
	}

	private void fireOnParticipantStateChanged(Participant participant,State state, Object data) {
		//For each listener in set
		for (Listener listener : listeners)
			//Send it
			listener.onParticipantStateChanged(getUID(), participant.getId(), state, data, participant);
	}

	void requestFPU(Integer partId) throws ParticipantNotFoundException {
		//Get participant
		Participant part = getParticipantByPartId(partId);
		//Send request
		part.requestFPU();
	}

	public void addProperty(String key,String value) {
		//Add property
		properties.put(key, value);
	}

	public void addProperties(HashMap<String,String> props) {
		//Add all
		properties.putAll(props);
	}

	public boolean containsProperty(String key) {
		//Check
		return properties.containsKey(key);
	}

	public String getProperty(String key) {
		//Check
		return properties.get(key);
	}

	public boolean isBroadcasting() {
		return broadcasting;
	}
	
	void setDefaultSipProxy(String proxy) throws ServletParseException {
		//Create uri
		defaultProxyUri = (SipURI) sf.createURI(proxy);
	}

	public boolean startRecordingBroadcaster(String filename)
	{
		//Check if broadcasting
		if (!broadcasting)
			//Error
			return false;
		try {
			//Start recoriding
			client.StartRecordingBroadcaster(id, filename);
		} catch (XmlRpcException ex) {
			//Log
			logger.log(Level.SEVERE, "startRecordingBroadcaster", ex);
			//Exit
			return false;
		}
		//oK
		return true;
	}

	public boolean stopRecordingBroadcater()
	{
		//Check if broadcasting
		if (!broadcasting)
			//Error
			return false;
		try {
			//Start recoriding
			client.StopRecordingBroadcaster(id);
		} catch (XmlRpcException ex) {
			//Log
			logger.log(Level.SEVERE, "stopRecordingBroadcater", ex);
			//Exit
			return false;
		}
		//oK
		return true;
	}
	String applyVariables(String stream) {
		//Set formater
		SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		//Set UTC time zone
		utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		//Get time
		String date = utcDateFormat.format(getTimestamp());
		return stream.replace("${DID}", getDID())
				.replace("${UID}", getUID())
				.replace("${TS}", Long.toString(getTimestamp().getTime()))
				.replace("${DATE}", date)
				.replace("${NAME}", getName())
				.replace("${ID}", getId().toString());
	}
	SipURI getSipURI() {
		return uri;
	}

	@XmlElement(name="uri")
	String getUri() {
		return uri.toString();
	}
}
