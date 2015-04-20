package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.util.Properties;

/**
 * US variant of the MaxSys protocol
 */
public class MaxSys extends com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys {

    final static String PD_NODE_PREFIX = "F";   // Standalone (this allows us in the US to interrogate standalone meters)

    @Override
    protected String getpNodePrefix(Properties p) {
        return p.getProperty(PK_NODE_PREFIX, PD_NODE_PREFIX);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }
}