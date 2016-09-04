/*
 * MediaMixer.java
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
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.MediaServer.XmlRpcBroadcasterClient;
import org.murillo.MediaServer.XmlRpcMcuClient;
import org.murillo.MediaServer.XmlRpcMcuClient.ConferenceInfo;
import org.murillo.util.SubNetInfo;
/**
 *
 * @author Sergio Garcia Murillo
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class MediaMixer implements Serializable,MediaMixerMCUEventQueue.Listener {
    private String uid;
    @XmlElement
    private String name;
    @XmlElement
    private String url;
    @XmlElement
    private String ip;
    @XmlElement
    private String publicIp;
    @XmlElement
    private SubNetInfo localNet;
    @XmlElement
    private Integer sys;
    @XmlElement
    private Integer user;
    @XmlElement
    private Integer load;
    @XmlElement
    private Integer cpus;
    @XmlElement
    private Integer loadAverage;

    private MediaMixerMCUEventQueue eventQueue;
    private String state;
    private HashSet<XmlRpcMcuClient> mcuClients;
    private Thread reconnectThread;
    private XmlRpcMcuClient client;
    private Listener listener;

    public interface Listener  {
        public void onMediaMixerReconnected(MediaMixer mediaMixer, Map<Integer, ConferenceInfo> conferences);
        public void onConferenceParticipantRequestFPU(MediaMixer mixer,Integer confId,String tag, Integer partId);
    }

    public MediaMixer() {

    }

    /** Creates a new instance of MediaMixer */
    public MediaMixer(String id,String name,String url,String ip,String publicIp,String localNet) throws MalformedURLException {
        ///Create uuid
        this.uid = id;
        //Save Values
        this.name = name;
        //Check if it ends with "/"
        if (url.endsWith("/"))
            //Remove it
            this.url = url.substring(0,url.length()-2);
        else
            //Copy all
            this.url = url;
        this.ip = ip;
        this.publicIp = publicIp;
        //Create default client
        client = new XmlRpcMcuClient(url + "/mcu");
        //Create client list
        mcuClients = new HashSet<XmlRpcMcuClient>();
        //Set local net
        try {
            //parse it
            this.localNet = new SubNetInfo(localNet);
        } catch (UnknownHostException ex) {
            //Log
            Logger.getLogger(MediaMixer.class.getName()).log(Level.SEVERE, "Wrong format for LocalNet expecified", ex);
            //Create empty one
            this.localNet = new SubNetInfo(new byte[]{0,0,0,0},0);
        }
        //NO event queue
        eventQueue = null;
        //NO state
        state = "";
	//No stats
	user = 0;
	sys = 0;
	load = 0;
	cpus = 0;
	loadAverage = -1;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public void setUrl(String url) {
        //Check if it ends with "/"
        if (url.endsWith("/"))
            //Remove it
            this.url = url.substring(0,url.length()-2);
        else
            //Copy all
            this.url = url;
    }

    public void setLocalNet(String localNet) throws UnknownHostException {
        this.localNet = new SubNetInfo(localNet);
    }

    public void setListener(Listener listener) {
        //Set it
        this.listener = listener;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
    
    public String getIp() {
        return ip;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public SubNetInfo getLocalNet() {
        return localNet;
    }

    public boolean isNated(String ip){
        try {
            //Check if it is a private network address  and not in local address
            if (SubNetInfo.isPrivate(ip) && !localNet.contains(ip))
                //It is nated
                return true;
        } catch (UnknownHostException ex) {
            //Log
            Logger.getLogger(MediaMixer.class.getName()).log(Level.WARNING, "Wrong IP address, doing NAT {0}", ip);
            //Do nat
            return true;
        }

        //Not nat
        return false;
    }

    @XmlElement(name="id")
    public String getUID() {
        return uid;
    }
    
    public Integer getEventQueueId() {
        return eventQueue!=null?eventQueue.getId():0;
    }

    public Boolean isConnected() {
	return eventQueue!=null?eventQueue.isConnected():false;
    }

    public XmlRpcBroadcasterClient createBroadcastClient() {
        XmlRpcBroadcasterClient client = null;
        try {
            client = new XmlRpcBroadcasterClient(url + "/broadcaster");
        } catch (MalformedURLException ex) {
            Logger.getLogger(MediaMixer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return client;
    }

    public XmlRpcMcuClient createMcuClient() {
        XmlRpcMcuClient mcuClient = null;
        try {
            //Create client
            mcuClient = new XmlRpcMcuClient(url + "/mcu");
            //Append to set
            mcuClients.add(mcuClient);
            //Start event listener
            startEventListener();
        } catch (XmlRpcException ex) {
            Logger.getLogger(MediaMixer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(MediaMixer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mcuClient;
    }
    
    private void startEventListener() throws MalformedURLException, XmlRpcException {
        //If we have  an event queue
        if (eventQueue!=null)
            //Started already
            return;
        //Create event queue
        Integer queueId = client.EventQueueCreate();
        //Attach
        eventQueue = new MediaMixerMCUEventQueue(queueId,url+"/events/mcu/"+Integer.toString(queueId));
        //Set listener
        eventQueue.setListener(this);
        //Start listening for events
        eventQueue.start();
        //Log
        Logger.getLogger(MediaMixer.class.getName()).log(Level.FINEST, "Started event listener queueId:{0}",queueId);
    }

    private void stopEventListener() {
        //Check
         if (eventQueue==null)
             //Do nothing
             return;
        //Log
        Logger.getLogger(MediaMixer.class.getName()).log(Level.FINEST, "Stop event listener");
        //Stop waiting for events
        eventQueue.stop();
        try{
            //Delete event queue
            client.EventQueueDelete(eventQueue.getId());
            //Log
        } catch (XmlRpcException ex) {
            Logger.getLogger(MediaMixer.class.getName()).log(Level.SEVERE, "Exception deleting event queue {0}", ex.getMessage());
        }
        //Clean
        eventQueue = null;
    }

    protected void startRetryConnect() {
        //Check if thread already running
        if (reconnectThread!=null && reconnectThread.isAlive())
            //Exit
            return;
        //We
        final MediaMixer mediaMixer = this;
        //Start reconnecting thread
        reconnectThread = new Thread(new Runnable()
        {
            public void run()
            {
                //Initial time to sleep
                int sleep = 1;
                //Log
                Logger.getLogger(MediaMixer.class.getName()).log(Level.FINEST, "MediaMixer reconnecting attempt in {0} sec", sleep);
                //Try until interrupted
                while(reconnectThread!=null)
                {
                    try {
                        //Lock
                        synchronized(mediaMixer) {
                            //Wait
                            mediaMixer.wait(sleep*1000);
                        }
                    } catch (InterruptedException ex) {
                        //Log
                        Logger.getLogger(MediaMixer.class.getName()).log(Level.FINEST, "MediaMixer reconnect interrupted");
                        //Exit
                        return;
                    }

                    try {
                        //Stop event listener
                        stopEventListener();
                        //Try to connect
                        startEventListener();
                        //Get conferences
                        Map<Integer, ConferenceInfo> conferences = client.getConferences();
                        //Check if we have listeners
                        if (listener!=null)
                            //Send event
                            listener.onMediaMixerReconnected(mediaMixer,conferences);
                        //done
                        return;
                    } catch (XmlRpcException ex) {
                        //Check if lower than maximium retry time
                        if (sleep<64)
                            //Duplicate time
                            sleep*=2;
                        //Log
                        Logger.getLogger(MediaMixer.class.getName()).log(Level.FINEST, "MediaMixer reconnect failed retrying in {0} sec: {1}", new Object[]{sleep,ex.getMessage()});
                    } catch (MalformedURLException ex2) {
                        //Log
                        Logger.getLogger(MediaMixer.class.getName()).log(Level.FINEST, "MediaMixer reconnect failed", ex2);
                    }
                }
            }
        });

        //Start thread
        reconnectThread.start();
    }

    private void stopRetryConnect() {
        //Lock
        synchronized(this) {
            //Signal
            this.notifyAll();
            //remove thread
            reconnectThread = null;
        }
    }

    public static HashMap<Integer,String> getSizes() {
        //The map
        HashMap<Integer,String> sizes = new HashMap<Integer,String>();

	//Set values
	sizes.put(XmlRpcMcuClient.QCIF,	    "QCIF\t176x144:1,22");
	sizes.put(XmlRpcMcuClient.CIF,	    "CIF\t352x288:1,22");
	sizes.put(XmlRpcMcuClient.VGA,	    "VGA\t640x480:1,33");
	sizes.put(XmlRpcMcuClient.PAL,	    "PAL\t768x576:1,33");
	sizes.put(XmlRpcMcuClient.HVGA,	    "HVGA\t480x320:1,50");
	sizes.put(XmlRpcMcuClient.QVGA,	    "QVGA\t320x240:1,33");
	sizes.put(XmlRpcMcuClient.HD720P,   "HD720P\t1280x720:1,78");
	sizes.put(XmlRpcMcuClient.WQVGA,    "WQVGA\t400x240:1,67");
	sizes.put(XmlRpcMcuClient.W448P,    "W448P\t768x448:1,71");
	sizes.put(XmlRpcMcuClient.SD448P,   "SD448P\t576x448:1,29");
	sizes.put(XmlRpcMcuClient.W288P,    "W288P\t512x288:1,78");
	sizes.put(XmlRpcMcuClient.W576,	    "W576\t1024x576:1,78");
	sizes.put(XmlRpcMcuClient.FOURCIF,  "FOURCIF\t704x576:1,22");
	sizes.put(XmlRpcMcuClient.FOURSIF,  "FOURSIF\t704x480:1,47");
	sizes.put(XmlRpcMcuClient.XGA,	    "XGA\t1024x768:1,33");
	sizes.put(XmlRpcMcuClient.WVGA,	    "WVGA\t800x480:1,67");
	sizes.put(XmlRpcMcuClient.DCIF,	    "DCIF\t528x384:1,38");
	sizes.put(XmlRpcMcuClient.SIF,	    "SIF\t352x240:1,47");
	sizes.put(XmlRpcMcuClient.QSIF,	    "QSIF\t176x120:1,47");
	sizes.put(XmlRpcMcuClient.SD480P,   "SD480P\t480x360:1,33");
	sizes.put(XmlRpcMcuClient.SQCIF,    "SQCIF\t128x96:1,33");
	sizes.put(XmlRpcMcuClient.SCIF,	    "SCIF\t256x192:1,33");

        //Return map
        return sizes;
    }
    
    public static HashMap<Integer,String> getVADModes() {
        //The map
        HashMap<Integer,String> modes = new HashMap<Integer,String>();
        //Add values
        modes.put(XmlRpcMcuClient.VADNONE,"None");
        modes.put(XmlRpcMcuClient.VADFULL,"Full");
        //Return map
        return modes;
    }

    public static HashMap<Integer,String> getMosaics() {
        //The map
        HashMap<Integer,String> mosaics = new HashMap<Integer,String>();
        //Add values
        mosaics.put(XmlRpcMcuClient.MOSAIC1x1	,"MOSAIC1x1");
        mosaics.put(XmlRpcMcuClient.MOSAIC2x2	,"MOSAIC2x2");
        mosaics.put(XmlRpcMcuClient.MOSAIC3x3	,"MOSAIC3x3");
	mosaics.put(XmlRpcMcuClient.MOSAIC4x4	,"MOSAIC4x4");
        mosaics.put(XmlRpcMcuClient.MOSAIC4x5A	,"MOSAIC4x5 Anamorphic");
        mosaics.put(XmlRpcMcuClient.MOSAIC5x5	,"MOSAIC5x5");
        mosaics.put(XmlRpcMcuClient.MOSAIC1p1	,"MOSAIC1+1");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p1A	,"MOSAIC1+1 Anamorphic");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p2	,"MOSAIC1+2");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p2A	,"MOSAIC1+2 Anamorphic");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p3A	,"MOSAIC1+3 Anamorphic");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p2x2A,"MOSAIC1+2x2 Anamorphic");
        mosaics.put(XmlRpcMcuClient.MOSAIC1p4A	,"MOSAIC1+4 Anamorphic");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p5	,"MOSAIC1+5");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p6A	,"MOSAIC1+6 Anamorphic");
        mosaics.put(XmlRpcMcuClient.MOSAIC1p7	,"MOSAIC1+7");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p12	,"MOSAIC1+12");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p1p2x4A,"MOSAIC1+1+2x4 Anamorphic");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p2x6A,"MOSAIC1+2x6 Anamorphic");
	mosaics.put(XmlRpcMcuClient.MOSAIC1p16A	,"MOSAIC1+16 Anamorphic");
	mosaics.put(XmlRpcMcuClient.MOSAIC3p4	,"MOSAIC3+4");
        mosaics.put(XmlRpcMcuClient.MOSAICPIP1	,"MOSAICPIP1");
        mosaics.put(XmlRpcMcuClient.MOSAICPIP3	,"MOSAICPIP3");

        //Return map
        return mosaics;
    }

    public void onMCUEventQueueConnected() {
        //Stop any pending reconnect, just in case
        stopRetryConnect();
        //Set state
        state = "Connected";
        //Log
        Logger.getLogger(MediaMixer.class.getName()).log(Level.INFO, "MediaMixer mcu event queue connected [id:{0}]",getUID());
    }

    public void onMCUEventQueueDisconnected() {
        //Set state
        state = "Disconnected";
        //Log
        Logger.getLogger(MediaMixer.class.getName()).log(Level.SEVERE, "MediaMixer mcu event queue disconnected [id:{0},queueId:{1}]", new Object[]{getUID(),getEventQueueId()});
    }

    public void onMCUEventQueueError() {
        //Set errror
        state = "Error";
        //Log
        Logger.getLogger(MediaMixer.class.getName()).log(Level.SEVERE, "MediaMixer mcu error [id:{0},queueId:{1}]", new Object[]{getUID(),getEventQueueId()});
        //Start reconnecting
        startRetryConnect();
    }

    public String getState() {
        return state;
    }

    void releaseMcuClient(XmlRpcMcuClient client) {
        //Release client
        mcuClients.remove(client);
        //Check number of clients
        if (mcuClients.isEmpty())
            //Stop event listener
            stopEventListener();
    }

    public void onConferenceParticipantRequestFPU(Integer confId,String tag, Integer partId) {
        //Check listener
        if (listener!=null)
            //Fire request
            listener.onConferenceParticipantRequestFPU(this, partId, tag, partId);
}

    public void onCPULoadInfo(Integer user, Integer sys, Integer load, Integer cpus) {
	//Store values
	this.user = user;
	this.sys  = sys;
	this.load = load;
	this.cpus = cpus;
	//Calculate load average
	if (loadAverage==-1)
	    //First
	    loadAverage = load;
	else
	    //median
	    loadAverage = (int)(0.9*loadAverage + 0.1*load);
    }

    public Integer getCpus() {
	return cpus;
    }

    public Integer getLoad() {
	return load;
    }

    public Integer getLoadAverage() {
	return loadAverage;
    }

    public Integer getSystemLoad() {
	return sys;
    }

    public Integer getUserLoad() {
	return user;
    }

    @XmlElement
    int getScore() {
	return (100 - load) * cpus;
    }
}
