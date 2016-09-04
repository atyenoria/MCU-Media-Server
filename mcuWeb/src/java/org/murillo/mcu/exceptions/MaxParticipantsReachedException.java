/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mcu.exceptions;

/**
 *
 * @author Sergio
 */
public class MaxParticipantsReachedException extends Exception {

    @Override public String getMessage() {
        return "Maximum number of participants for confenrece reached";
    }
}
