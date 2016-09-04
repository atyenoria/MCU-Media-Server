/*
 * Profile.java
 * 
 * Created on 03-oct-2007, 12:46:56
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mcuWeb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
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
 * @author esergar
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class Profile implements Serializable{

    @XmlElement
    private String uid;
    @XmlElement
    private String name;
    @XmlElement
    private Integer videoBitrate;
    @XmlElement
    private Integer videoFPS;
    @XmlElement
    private Integer videoSize;
    @XmlElement
    private Integer intraPeriod;
    @XmlElement
    private Integer maxVideoBitrate;
    @XmlElement
    private Integer audioRate;
    @XmlElement
    private HashMap<String,String> properties;

    public Profile() {
        //Empty properties
        this.properties = new HashMap<String, String>();
    }

    public Profile(String uid, String name, Integer videoSize, Integer videoBitrate, Integer videoFPS, Integer intraPeriod, Integer maxVideoBitrate) {
        //Set values
        this.uid = uid;
        this.name = name;
        this.videoBitrate = videoBitrate;
        this.videoSize = videoSize;
        this.videoFPS = videoFPS;
        this.intraPeriod = intraPeriod;
        this.maxVideoBitrate = maxVideoBitrate;
        this.audioRate = 8000;
        //Empty properties
        this.properties = new HashMap<String, String>();
    }
    
    public Profile(String uid, String name, Integer videoSize, Integer videoBitrate, Integer videoFPS, Integer intraPeriod, Integer maxVideoBitrate, Integer audioRate) {
        //Set values
        this.uid = uid;
        this.name = name;
        this.videoBitrate = videoBitrate;
        this.videoSize = videoSize;
        this.videoFPS = videoFPS;
        this.intraPeriod = intraPeriod;
        this.maxVideoBitrate = maxVideoBitrate;
        this.audioRate = audioRate;
        //Empty properties
        this.properties = new HashMap<String, String>();
    }

    public String getUID() {
        return uid;
    }
    
    public String getName() {
        return name;
    }

    public Integer getVideoBitrate() {
        return videoBitrate;
    }

    public Integer getVideoFPS() {
        return videoFPS;
    }

    public Integer getVideoSize() {
        return videoSize;
    }

    public Integer getIntraPeriod() {
        return intraPeriod;
}

    public Integer getMaxVideoBitrate() {
        return maxVideoBitrate;
}

    public Integer getAudioRate() {
        return audioRate;
    }
    public HashMap<String,String> getProperties() {
        return properties;
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

    public void addProperty(String key,String value) {
        //Add property
        properties.put(key, value);
    }

    public void addProperties(HashMap<String,String> props) {
        //Add all
        properties.putAll(props);
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
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public void setAudioRate(Integer audioRate) {
        this.audioRate = audioRate;
    }

    public void setIntraPeriod(Integer intraPeriod) {
        this.intraPeriod = intraPeriod;
    }

    public void setMaxVideoBitrate(Integer maxVideoBitrate) {
        this.maxVideoBitrate = maxVideoBitrate;
    }

    public void setName(String name) {
        this.name = name;
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
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }


    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    public void setVideoBitrate(Integer videoBitrate) {
        this.videoBitrate = videoBitrate;
    }

    public void setVideoFPS(Integer videoFPS) {
        this.videoFPS = videoFPS;
    }

    public void setVideoSize(Integer videoSize) {
        this.videoSize = videoSize;
    }
}
