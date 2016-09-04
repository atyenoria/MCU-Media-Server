/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mcu.exceptions;

/**
 *
 * @author Sergio
 */
public class ParticipantNotFoundException extends Exception {
    public final Integer partId;
    public ParticipantNotFoundException(Integer partId) {
        this.partId = partId;
    }
    @Override public String getMessage() {
        return "Participant id:"+partId+" not found";
    }
}
