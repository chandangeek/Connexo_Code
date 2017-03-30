/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Models the name of the local machine and provides opportunities
 * to set this from external resources such as simple properties files.
 * By default, it will be set to the hostname as determined by the InetAddress class.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:01)
 */
public final class HostName {

    private static final HostName SINGLETON = new HostName();
    private static final String FALLBACK = "localhost";
    private String name;

    /**
     * Overrule the default setting that was determined by the InetAddress class.
     *
     * @param hostName The host name
     */
    public static void setCurrent (String hostName) {
        SINGLETON.setName(hostName);
    }

    public static String getCurrent () {
        return SINGLETON.getName();
    }

    private HostName () {
        super();
        try {
            this.name = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            this.name = FALLBACK;
        }
    }

    private String getName () {
        return name;
    }

    private void setName (String name) {
        this.name = name;
    }

}