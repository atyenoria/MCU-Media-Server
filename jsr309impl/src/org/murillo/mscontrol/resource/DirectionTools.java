/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.resource;

import javax.media.mscontrol.join.Joinable.Direction;

/**
 *
 * @author Sergio
 */
public class DirectionTools {

    static public boolean isSending(Direction dir) {
         return !dir.equals(Direction.RECV);
    }

    static public boolean isReceving(Direction dir) {
         return !dir.equals(Direction.SEND);
    }

   static public Direction reverse(Direction direction) {
        switch(direction)
        {
            case SEND:
                return Direction.RECV;
            case RECV:
                return Direction.SEND;
        }
        return Direction.DUPLEX;
    }
}
