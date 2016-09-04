/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.murillo.MediaServer.XmlRPCJSR309Client;

/**
 *
 * @author Sergio
 */
public class MediaServer extends XmlRPCJSR309Client {
    private String name;
    private String url;
    private String ip;
    private SubNetInfo localNet;

    /** Creates a new instance of MediaMixer */
    public MediaServer(String name,String url,String ip,String localNet) throws MalformedURLException
    {
        //call parent
        super(url.endsWith("/") ? url+"jsr309" : url+"/jsr309");
        //Save Values
        this.name = name;
        this.url = url;
        this.ip = ip;
        try {
                //parse it
                this.localNet = new SubNetInfo(localNet);
            } catch (UnknownHostException ex) {
                //Log
                Logger.getLogger(MediaServer.class.getName()).log(Level.SEVERE, null, ex);
                //Create empty one
                this.localNet = new SubNetInfo(new byte[]{0,0,0,0},0);
            }
    }

    public MediaServerEventQueue getEventQueue(int id) {
        return new MediaServerEventQueue(url+"/events/jsr309/"+Integer.toString(id));
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

    public boolean isNated(String ip){
        try {
            //Check if it is a private network address  and not in local address
            if (SubNetInfo.isPrivate(ip) && !localNet.contains(ip))
                //It is nated
                return true;
        } catch (UnknownHostException ex) {
            //Log
            Logger.getLogger(MediaServer.class.getName()).log(Level.WARNING, "Wrong IP address, doing NAT {0}", ip);
            //Do nat
            return true;
        }

        //Not nat
        return false;
    }

    public SubNetInfo getLocalNet() {
        return localNet;
    }

    public String getUID() {
        return name+"@"+url;
    }
}
