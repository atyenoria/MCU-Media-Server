/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mcuWeb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.murillo.mcuWeb.Participant.State;

/**
 *
 * @author Sergio
 */
public class HTTPListener implements Conference.Listener {
    private final String callback;

    public HTTPListener(String callback) {
        //Store url
        this.callback = callback;
    }
    
    void request(String callback) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(callback);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(1000);
            conn.setConnectTimeout(5000);
            conn.connect();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while(rd.readLine()!= null) {
            }
            rd.close();
        } catch (Exception ex) {
            Logger.getLogger(HTTPListener.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            conn.disconnect();
        }
    }

    public void onConferenceInited(Conference conf) {
        //Call request
        request(callback+"?event=created&did="+conf.getDID()+"&uid="+conf.getUID());
    }

    public void onConferenceEnded(Conference conf) {
        //Call request
        request(callback+"?event=destroyed&did="+conf.getDID()+"&uid="+conf.getUID());
    }

    public void onParticipantCreated(String confId, Participant part) {
    }

    public void onParticipantStateChanged(String confId, Integer partId, State state, Object data, Participant part) {
    }

    public void onParticipantDestroyed(String confId, Integer partId) {
    }

    public void onParticipantMediaChanged(String confId, Integer partId, Participant part) {
    }

    public void onParticipantMediaMuted(String confId, Integer partId, Participant part, String media, boolean muted) {
    }

    public void onOwnerChanged(String confId, Integer partId, Object data, Participant owner) {
    }

	public void onConferenceRecordingStarted(Conference conf) {
	}

	public void onConferenceRecordingStopped(Conference conf) {
	}
}
