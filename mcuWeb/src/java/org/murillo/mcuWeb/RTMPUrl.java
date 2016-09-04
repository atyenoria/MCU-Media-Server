/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mcuWeb;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 *
 * @author Sergio
 */
@Embeddable
public class RTMPUrl implements Serializable {
    private String url;
    private String stream;

    public RTMPUrl() {
        
    }
    public RTMPUrl(String url, String stream) {
        this.url = url;
        this.stream = stream;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
