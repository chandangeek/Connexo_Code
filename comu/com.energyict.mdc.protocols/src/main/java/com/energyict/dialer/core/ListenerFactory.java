package com.energyict.dialer.core;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.UserEnvironment;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListenerFactory implements Serializable {

    /* Examples of usage:
      *** direct rs232 connection ***
      listener =ListenerFactory.getDirectListener().newListener();
      listener.init("COM1");
      listener.connect("",60000);
    */
    private static String ATLISTENER = "ATLISTENER";
    private static String IPLISTENER = "IPLISTENER";
    private static String UDPLISTENER = "UDPLISTENER";

    private static ListenerFactory[] all = {
            new ListenerFactory(ATLISTENER, "com.energyict.dialer.coreimpl.ATListener"),
            new ListenerFactory(IPLISTENER, "com.energyict.dialer.coreimpl.IPListener"),
            new ListenerFactory(UDPLISTENER, "com.energyict.dialer.coreimpl.UDPListener"),
    };

    private String name;
    private String listenerClassName;

    public ListenerFactory(String name, String listenerClassName) {
        this.name = name;
        this.listenerClassName = listenerClassName;
    }

    public String getListenerClassName() {
        return listenerClassName;
    }

    public String getName() {
        return name;
    }

    public String getLocalizedName() {
        return UserEnvironment.getDefault().getTranslation(getName());
    }

    public String toString() {
        return getLocalizedName();
    }

    public Listener newListener() {
        try {
            return (Listener) Class.forName(getListenerClassName()).newInstance();
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException ex) {
            throw new ApplicationException(ex);
        }
    }

    public static ListenerFactory getDefault() {
        return all[0];
    }

    public static ListenerFactory getIPListener() {
        return ListenerFactory.get(IPLISTENER);
    }

    public static ListenerFactory getUDPListener() {
        return ListenerFactory.get(UDPLISTENER);
    }

    public static ListenerFactory get(String name) {
        for (int i = 0; i < all.length; i++) {
            if (all[i].getName().equals(name)) {
                return all[i];
            }
        }
        throw new ApplicationException("Listener type " + name + " does not exist");
    }

    public static ListenerFactory get(int index) {
        if (index >= all.length) {
            return null;
        } else {
            return all[index];
        }
    }

    public static int nrOfListeners() {
        return all.length;
    }

    public static List getAll() {
        return Collections.unmodifiableList(Arrays.asList(all));
    }

}