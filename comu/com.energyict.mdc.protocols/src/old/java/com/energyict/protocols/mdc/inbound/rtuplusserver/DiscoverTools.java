/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DiscoverTools.java
 *
 * Created on 19 oktober 2007, 16:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocols.mdc.inbound.rtuplusserver;

import com.energyict.mdc.protocol.api.SerialCommunicationSettings;

import java.util.Properties;

/**
 * @author kvds
 */
public class DiscoverTools {

    private int address;
    String port;
    SerialCommunicationSettings commSettings;
    private Properties properties;

    /**
     * Creates a new instance of DiscoverTools
     */
    public DiscoverTools(String port) {
        this(port, null);
    }

    public DiscoverTools(String port, SerialCommunicationSettings commSettings) {
        this.port = port;
        this.commSettings = commSettings;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

}