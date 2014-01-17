/*
 * ListenerMarker.java
 *
 * Created on 27 mei 2005, 15:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dialer.core;

import com.energyict.mdc.protocol.api.dialer.core.IPListen;
import com.energyict.mdc.protocol.api.dialer.core.Link;

/**
 * @author Koen
 */
public class ListenerMarker {

    /**
     * Creates a new instance of ListenerMarker
     */
    public ListenerMarker() {
    }

    public static boolean hasIPListenMarker(Link link) {
        Class[] interfaces = link.getClass().getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].isAssignableFrom(IPListen.class)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasIPListenMarker(Listener listener) {
        Class[] interfaces = listener.getClass().getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].isAssignableFrom(IPListen.class)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasUDPListenMarker(Link link) {
        Class[] interfaces = link.getClass().getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].isAssignableFrom(UDPListen.class)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasUDPListenMarker(Listener listener) {
        Class[] interfaces = listener.getClass().getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].isAssignableFrom(UDPListen.class)) {
                return true;
            }
        }
        return false;
    }

}
