/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mcuWeb;

/**
 *
 * @author Sergio
 */
public class Broadcast {

    private Integer id;
    private String name;
    private String tag;
    private MediaMixer mixer;


    /** Creates a new instance of Conference */
    protected Broadcast(Integer id,String name,String tag,MediaMixer mixer) {
        //Save values
        this.id = id;
        this.mixer = mixer;
        this.name = name;
        this.tag = tag;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUID() {
        return id+"@"+mixer.getName();
    }

    public String getTag() {
        return tag;
    }

    public MediaMixer getMixer() {
        return mixer;
    }
}