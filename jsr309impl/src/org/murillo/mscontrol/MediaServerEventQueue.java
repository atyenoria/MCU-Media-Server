/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.murillo.MediaServer.XmlRpcEventManager;

/**
 *
 * @author Sergio
 */
public class MediaServerEventQueue implements XmlRpcEventManager.Listener, Runnable{
    public interface Listener {
        public abstract void onPlayerEndOfStream(URI sessUri,URI playerUri);
    }
    
    private final XmlRpcEventManager em;
    private Listener listener;
    private Thread thread;
    private final String url;

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
        //Stop thread
        thread.stop();
    }
    

    MediaServerEventQueue(String url) {
        //Store url
        this.url = url;
        //Create event manager
        em = new XmlRpcEventManager();
        //Create thread;
        thread = new Thread(this);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    @Override
    public void onConnect() {
        //
    }

    @Override
    public void onError() {
        //
    }

    @Override
    public void onDisconnect() {
        //
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
        
        //Depending on the type
        switch(type)
        {
            case 1:
                try {
                    String sessTag = (String) arr[1];
                    String playerTag = (String) arr[2];
                    //PlayerEndOfFile
                    listener.onPlayerEndOfStream(new URI(sessTag), new URI(playerTag.replace("/player","")));
                } catch (URISyntaxException ex) {
                    Logger.getLogger(MediaServerEventQueue.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
    }
}
