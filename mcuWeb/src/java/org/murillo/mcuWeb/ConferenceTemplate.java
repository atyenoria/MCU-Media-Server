/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mcuWeb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Sergio
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class ConferenceTemplate {

    @XmlElement
    private String uid;
    @XmlElement
    private String name;
    @XmlElement
    private String did;
    @XmlElement
    private MediaMixer mixer;
    @XmlElement
    private Integer size;
    @XmlElement
    private Integer compType;
    @XmlElement
    private Profile profile;
    @XmlElement
    private String audioCodecs;
    @XmlElement
    private String videoCodecs;
    @XmlElement
    private String textCodecs;
    @XmlElement
    private Integer vad;
    @XmlElement
    private Boolean autoAccept;
    @XmlElement
    private HashMap<String,String> properties;

    public ConferenceTemplate()  {
        //Create property map
        this.properties = new HashMap<String, String>();
    }

    ConferenceTemplate(String name, String did, MediaMixer mixer, Integer size, Integer compType, Integer vad, Profile profile, Boolean autoAccept, String audioCodecs,String videoCodecs,String textCodecs) {
       //Set values
        this.uid = did;
        this.name = name;
        this.did = did;
        this.mixer = mixer;
        this.size = size;
        this.compType = compType;
        this.profile = profile;
        this.vad = vad;
        this.autoAccept = autoAccept;
        this.audioCodecs = audioCodecs;
        this.videoCodecs = videoCodecs;
        this.textCodecs = textCodecs;
        //Create property map
        this.properties = new HashMap<String, String>();
    }

    public String getUID() {
        return uid;
    }

     public Integer getCompType() {
        return compType;
    }

    public String getDID() {
        return did;
    }

    public MediaMixer getMixer() {
        return mixer;
    }

    public String getName() {
        return name;
    }

    public Profile getProfile() {
        return profile;
    }

    public Integer getSize() {
        return size;
    }

    public Boolean isDIDMatched(String did) {
        //Check did
        if (did==null)
            //not matched
            return false;
        //Check for default one
        if (this.did.equals("*"))
            //Matched
            return true;
        //Get length
        int len = did.length();
        //First check length
        if (this.did.length()!=len)
            //not matched
            return false;
        //Compare each caracter
        for(int i=0;i<len;i++)
            //They have to be the same or the pattern an X
            if (this.did.charAt(i)!='X' && this.did.charAt(i)!=did.charAt(i))
                //Not matched
                return false;
        //Matched!
        return true;
    }

    public String getAudioCodecs() {
        return audioCodecs;
}

    public String getVideoCodecs() {
        return videoCodecs;
    }

    public String getTextCodecs() {
        return textCodecs;
    }

    public Integer getVADMode() {
        return vad;
}

    public Boolean isAutoAccept() {
        return autoAccept;
    }

    public HashMap<String,String> getProperties() {
        return properties;
    }

    public void addProperty(String key,String value) {
        //Add property
        properties.put(key, value);
    }

    public void addProperties(HashMap<String,String> props) {
        //Add all
        properties.putAll(props);
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
}
    public String getProperty(String key) {
        return properties.get(key);
    }

    public Integer getIntProperty(String key,Integer defaultValue) {
	//Define default value
	Integer value = defaultValue;
	//Try to conver ti
	try { value = Integer.parseInt(getProperty(key)); } catch (Exception e) {}
	//return converted or default
        return value;
    }

    public boolean addProperties(String properties) {
        try {
            //Create template properties
            Properties props = new Properties();
            //Parse them
            props.load(new ByteArrayInputStream(properties.getBytes()));
            //For each one
            for (Entry entry : props.entrySet()) {
                //Add them
                addProperty(entry.getKey().toString(), entry.getValue().toString());
            }
        } catch (IOException ex) {
            Logger.getLogger(ConferenceTemplate.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public boolean setProperties(String properties) {
        try {
            //Create template properties
            Properties props = new Properties();
            //Parse them
            props.load(new ByteArrayInputStream(properties.getBytes()));
            //Clear properties
            this.properties.clear();
            //For each one
            for (Entry entry : props.entrySet()) {
                //Add them
                addProperty(entry.getKey().toString(), entry.getValue().toString());
            }
        } catch (IOException ex) {
            Logger.getLogger(ConferenceTemplate.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }


    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    public void setAudioCodecs(String audioCodecs) {
	this.audioCodecs = audioCodecs;
    }

    public void setAutoAccept(Boolean autoAccept) {
	this.autoAccept = autoAccept;
    }

    public void setCompType(Integer compType) {
	this.compType = compType;
    }

    public void setDid(String did) {
	this.did = did;
    }

    public void setMixer(MediaMixer mixer) {
	this.mixer = mixer;
    }

    public void setName(String name) {
	this.name = name;
    }

    public void setProfile(Profile profile) {
	this.profile = profile;
    }

    public void setSize(Integer size) {
	this.size = size;
    }

    public void setTextCodecs(String textCodecs) {
	this.textCodecs = textCodecs;
    }

    public void setVad(Integer vad) {
	this.vad = vad;
    }

    public void setVideoCodecs(String videoCodecs) {
	this.videoCodecs = videoCodecs;
    }

}
