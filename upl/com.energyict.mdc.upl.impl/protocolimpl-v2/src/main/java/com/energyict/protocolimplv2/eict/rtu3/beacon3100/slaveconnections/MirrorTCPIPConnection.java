package com.energyict.protocolimplv2.eict.rtu3.beacon3100.slaveconnections;

import com.energyict.mdc.protocol.ComChannel;

import com.energyict.dlms.protocolimplv2.CommunicationSessionProperties;
import com.energyict.dlms.protocolimplv2.connection.TCPIPConnection;

/**
 * Serves as a TCPIPConnection when the slave devices communication in 'mirror-mode' with the Beacon
 * All connection-related exceptions are actually connection related exceptions and the framework will handle
 * them as such.
 * <p>
 * Basically I just serve as a facade for the Mirror scenario, all logic in the TCPIPConnection should result in correct behavior
 * <p>
 * Copyrights EnergyICT
 * Date: 07.04.16
 * Time: 15:15
 */
public class MirrorTCPIPConnection extends TCPIPConnection {

    public MirrorTCPIPConnection(ComChannel comChannel, CommunicationSessionProperties properties) {
        super(comChannel, properties);
    }
}
