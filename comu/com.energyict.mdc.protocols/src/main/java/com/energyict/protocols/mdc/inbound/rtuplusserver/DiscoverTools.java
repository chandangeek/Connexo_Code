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
import com.energyict.mdc.protocol.api.dialer.core.Dialer;
import com.energyict.mdc.protocol.api.dialer.core.DialerFactory;
import com.energyict.mdc.protocol.api.dialer.core.LinkException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;

import java.io.IOException;
import java.util.Properties;

/**
 * @author kvds
 */
public class DiscoverTools {

    private int address;
    Dialer dialer = null;
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

    public void init() throws LinkException, IOException {
        dialer = DialerFactory.getDirectDialer().newDialer();
        dialer.init(port);
        if (commSettings == null) {
            dialer.getSerialCommunicationChannel().setParams(9600,
                    SerialCommunicationChannel.DATABITS_8,
                    SerialCommunicationChannel.PARITY_NONE,
                    SerialCommunicationChannel.STOPBITS_1);
        } else {
            int parity = 0;
            if (commSettings.getParity() == SerialCommunicationSettings.EVEN_PARITY) {
                parity = 2;
            } else if (commSettings.getParity() == SerialCommunicationSettings.ODD_PARITY) {
                parity = 1;
            }

            dialer.getSerialCommunicationChannel().setParams(commSettings.getSpeed(),
                    commSettings.getDataBits(),
                    parity,
                    commSettings.getStopBits());
        }

    }

    public void connect() throws LinkException, IOException {
        dialer.connect();

    }

    public void disconnect() throws LinkException, IOException {
        dialer.disConnect();
    }

    public Dialer getDialer() {
        return dialer;
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
