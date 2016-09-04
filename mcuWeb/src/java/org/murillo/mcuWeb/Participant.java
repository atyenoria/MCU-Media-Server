/*
 * Participant.java
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.MediaServer.Codecs.MediaType;
import org.murillo.MediaServer.XmlRpcMcuClient;

/**
 *
 * @author Sergio
 */
@XmlType()
@XmlAccessorType(XmlAccessType.NONE)
public abstract class Participant  implements Serializable {
    @XmlElement
    protected Integer id;
    @XmlElement(name="userId") //NOTE: This is so it does not get confued with the events.partId that maps to the id eleement
    protected Integer partId;
    @XmlElement
    protected String sessionId;
    @XmlElement
    protected Type type;
    @XmlElement
    protected String name;
    @XmlElement
    protected Profile profile;
    @XmlElement
    protected Boolean audioMuted;
    @XmlElement
    protected Boolean videoMuted;
    @XmlElement
    protected Boolean textMuted;
    @XmlElement
    protected Boolean audioSupported;
    @XmlElement
    protected Boolean videoSupported;
    @XmlElement
    protected Boolean textSupported;
    @XmlElement
    protected State state;
    @XmlElement
    protected Integer mosaicId;
    @XmlElement
    protected Integer sidebarId;
    @XmlElement
    protected Object data;
    @XmlElement
    protected Boolean isOwner;
    @XmlElement
    protected List<String> info;
    //NOT PUBLIC!!!
    protected String pin;

    protected HashSet<Listener> listeners = null;
    protected Conference conf = null;
    private boolean autoAccept;
    private String token;

    public interface Listener{
        public void onStateChanged(Participant part,State state,State prev);
	public void onMediaChanged(Participant part);
	public void onMediaMuted(Participant part,String media, boolean muted);
    };

    public enum State {CREATED,CONNECTING,WAITING_ACCEPT,CONNECTED,ERROR,TIMEOUT,BUSY,DECLINED,NOTFOUND,DISCONNECTED,DESTROYED}
    public static enum Type {
        SIP("SIP",  XmlRpcMcuClient.RTP),
        WEB("WEB",  XmlRpcMcuClient.RTMP);

        public final String name;
        public final Integer value;

        Type(String name,Integer value){
            this.name = name;
            this.value = value;
        }

        public Integer valueOf() {
            return value;
        }

        public String getName() {
                return name;
        }
    };

    Participant() {
        //Default constructor for Xml Serialization
    }

    Participant(Integer id,Integer partId,String name,String token,Integer mosaicId,Integer sidebarId,Conference conf,Type type) {
        //Save values
        this.id = id;
        this.partId = partId;
        this.conf = conf;
        this.type = type;
        this.name = name;
	this.token = token;
        this.mosaicId = mosaicId;
        this.sidebarId = sidebarId;
        //Get initial profile
        this.profile = conf.getProfile();
        //Not muted
        this.audioMuted = false;
        this.videoMuted = false;
        this.textMuted = false;
        //Supported media
        this.audioSupported = true;
        this.videoSupported = true;
        this.textSupported = true;
	//No conference owner by default
	isOwner = false;
        //Autoaccept by default
        autoAccept = false;
	//Create participant info list
	info = new ArrayList<String>();
        //Create listeners
        listeners = new HashSet<Listener>();
        //Initial state
        state = State.CREATED;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

     public Conference getConference() {
        return conf;
    }

    public Integer getId() {
        return id;
    }

    public Integer getPartId() {
        return partId;
    }

    public Object getData() {
	return data;
    }

    public void setData(Object data) {
	this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
	return token;
    }

    public boolean isAutoAccept() {
        return autoAccept;
    }

    public void setAutoAccept(boolean autoAccept) {
        this.autoAccept = autoAccept;
    }
    
    public Profile getVideoProfile() {
        return profile;
    }

    protected void error(State state,String message)
    {
        //Set the state
        setState(state);
        //Check cdr
        //Teminate
        destroy();
    }

    protected void error(State state,String message,Integer code)
    {
        //Set the state
        setState(state);
        //Teminate
        destroy();
    }

    protected void setState(State state) {
        Logger.getLogger(Participant.class.getName()).log(Level.SEVERE, "Partipant {0} change state from {1} to {2}", new Object[]{getId(),this.state,state});
	//Store prev state
	State prev = this.state;
	//Change it
        this.state = state;
        //Call listeners
        for(Listener listener : listeners)
            //Call it
            listener.onStateChanged(this,state,prev);
    }

    protected void changedMedia() {
	Logger.getLogger(Participant.class.getName()).log(Level.SEVERE, "Partipant {0} changed media", new Object[]{getId()});
        //Call listeners
        for(Listener listener : listeners)
            //Call it
            listener.onMediaChanged(this);
    }

    protected void mutedMedia(String media,boolean muted) {
	Logger.getLogger(Participant.class.getName()).log(Level.FINE, "Partipant {0} muted {1} {2}", new Object[]{getId(),media,muted});
        //Call listeners
        for(Listener listener : listeners)
            //Call it
            listener.onMediaMuted(this,media,muted);
    }

    public void setName(String name) {
        this.name = name;
    }

    public State getState() {
        return state;
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

    public Boolean isSending(String string) {
	return true;
    }

    public void setAudioSupported(Boolean audioSupported) {
        this.audioSupported = audioSupported;
    }

    public void setTextSupported(Boolean textSupported) {
        this.textSupported = textSupported;
    }

    public void setVideoSupported(Boolean videoSupported) {
        this.videoSupported = videoSupported;
    }

    public void setListener(Listener listener) {
        listeners.add(listener);
    }

    public Type getType() {
        return type;
    }

    public void setAudioMuted(Boolean flag)
    {
        try {
            //Get client
            XmlRpcMcuClient client = conf.getMCUClient();
            //Delete participant
            client.SetMute(conf.getId(), partId, MediaType.AUDIO, flag);
            //Set audio muted
            audioMuted = flag;
	    //Launch event
	    mutedMedia("audio", flag);
        } catch (XmlRpcException ex) {
            Logger.getLogger(Participant.class.getName()).log(Level.SEVERE, "Failed to mute participant.", ex);
            }
        }
    public void setVideoMuted(Boolean flag)
    {
            try {
            //Get client
            XmlRpcMcuClient client = conf.getMCUClient();
            //Delete participant
            client.SetMute(conf.getId(), partId, MediaType.VIDEO, flag);
        //Set it
            videoMuted = flag;
	    //Launch event
	    mutedMedia("video", flag);
        } catch (XmlRpcException ex) {
            Logger.getLogger(Participant.class.getName()).log(Level.SEVERE, "Failed to mute participant.", ex);
        }
    }
    public void setTextMuted(Boolean flag)
        {
            try {
            //Get client
            XmlRpcMcuClient client = conf.getMCUClient();
            //Delete participant
            client.SetMute(conf.getId(), partId, MediaType.TEXT, flag);
            //Set it
            textMuted = flag;
	    //Launch event
	    mutedMedia("text", flag);
            } catch (XmlRpcException ex) {
            Logger.getLogger(Participant.class.getName()).log(Level.SEVERE, "Failed to mute participant.", ex);
            }
            }

    public Boolean getAudioMuted() {
        return audioMuted;
        }

    public Boolean getTextMuted() {
        return textMuted;
        }

    public Boolean getVideoMuted() {
        return videoMuted;
        }

    public Integer getMosaicId() {
        return mosaicId;
    }

    public Integer getSidebarId() {
        return sidebarId;
    }

    public void setMosaicId(Integer mosaicId) {
        this.mosaicId = mosaicId;
    }

    public void setSidebarId(Integer sidebarId) {
        this.sidebarId = sidebarId;
    }

    public Boolean getIsOwner() {
	return isOwner;
    }

    public void setIsOwner(Boolean isOwner) {
	this.isOwner = isOwner;
    }
    public String getPin() {
        return pin;
    }
    
    public String[] getInfo() {
	    return (String[])info.toArray();
    }
    
    /*** Must be overrriden by children */
    public boolean setVideoProfile(Profile profile)     { return false;}
    public boolean accept()                             { return false;}
    public boolean reject(Integer code,String reason)   { return false;}
    public void restart(Integer partId)                 { throw new RuntimeException("Not supported yet"); }
    public void end()                                   {}
    public void destroy()                               {}
    void requestFPU()                                   {}
}
