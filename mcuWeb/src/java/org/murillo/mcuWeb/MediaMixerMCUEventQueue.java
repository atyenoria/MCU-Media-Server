/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mcuWeb;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.murillo.MediaServer.XmlRpcEventManager;

/**
 *
 * @author Sergio
 */
public class MediaMixerMCUEventQueue implements XmlRpcEventManager.Listener, Runnable{

    public interface Listener {
        public abstract void onMCUEventQueueConnected();
        public abstract void onMCUEventQueueDisconnected();
        public abstract void onMCUEventQueueError();
        public abstract void onConferenceParticipantRequestFPU(Integer confId,String tag, Integer partId);
	public abstract void onCPULoadInfo(Integer user,Integer sys, Integer load, Integer cpus);
    }

    private final XmlRpcEventManager em;
    private Listener listener;
    private Thread thread;
    private final String url;
    private final Integer id;

    @Override
    public void run() {
        try{
            //Connect
            em.Connect(url, this);
        } catch(Exception e) {
            //Send error
            onError();
        }
    }

    public void start(){
        //Create thread and start
        thread.start();
    }

    public void stop() {
        //Cancel event manager
        em.Cancel();
    }

    public Boolean isConnected() {
	return em.isConnected();
    }

    MediaMixerMCUEventQueue(Integer id,String url) {
        //Store url and id
        this.id = id;
        this.url = url;
        //Create event manager
        em = new XmlRpcEventManager();
        //Create thread;
        thread = new Thread(this);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public void onConnect() {
        //Check listener
        if (listener!=null)
            //call it
            listener.onMCUEventQueueConnected();
    }

    @Override
    public void onError() {
        //Check listener
        if (listener!=null)
            //call it
            listener.onMCUEventQueueError();
    }

    @Override
    public void onDisconnect() {
        //Check listener
        if (listener!=null)
            //call it
            listener.onMCUEventQueueDisconnected();
    }

    @Override
    public void onEvent(Object result) {
        //Check listener
        if (listener==null)
            //Exit
            return;

        //Convert to array
        Object[] arr = (Object[]) result;
        //Get type
        Integer type = (Integer) arr[0];

        Logger.getLogger(this.getClass().getName()).log(Level.FINEST, "Got event of type {0}", type);
        
        //Depending on the type
        switch(type)
        {
            case 1:
                //Get parameters
                Integer confId  = (Integer) arr[1];
                String tag  = (String) arr[2];
                Integer partId = (Integer) arr[3];
                //Send onConferenceParticipantRequestFPU
                listener.onConferenceParticipantRequestFPU(confId,tag,partId);
                break;
	    case 2:
		//Get parameters
		Integer user = (Integer) arr[1];
		Integer sys  = (Integer) arr[2];
		Integer load = (Integer) arr[3];
		Integer cpus = (Integer) arr[4];
		//Call listener
		listener.onCPULoadInfo(user,sys,load,cpus);
		break;
        }
    }
}
