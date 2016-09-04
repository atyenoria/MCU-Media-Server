/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mcu.exceptions;

/**
 *
 * @author Sergio
 */
public class BroadcastNotFoundExcetpion  extends Exception {
    public final String UID;
    public BroadcastNotFoundExcetpion(String UID) {
        this.UID = UID;
    }
    @Override
    public String getMessage() {
        return "Broadcast uid:"+UID+" not found";
    }
}
