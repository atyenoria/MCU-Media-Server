/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mcu.exceptions;

/**
 *
 * @author Sergio
 */
public class ConferenceNotFoundExcetpion  extends Exception {
    public final String UID;
    public ConferenceNotFoundExcetpion(String UID) {
        this.UID = UID;
    }
    @Override
    public String getMessage() {
        return "Conference uid:"+UID+" not found";
    }
}
