/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.mocks;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

/**
 * Provides a mock implementation for the {@link TCPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-04 (08:43)
 */
public class MockTCPInboundComPort extends MockComPort implements TCPBasedInboundComPort {

    private static final int NOT_CHANGED = -1;

    private int portNumber;
    private int numberOfSimultaneousConnections;
    private int changedNumberOfSimultaneousConnections = NOT_CHANGED;

    protected MockTCPInboundComPort (ComServer comServer, String name) {
        super(comServer, name);
    }

    protected MockTCPInboundComPort (ComServer comServer, long id, String name) {
        super(comServer, id, name);
    }

    @Override
    public boolean isInbound () {
        return true;
    }

    @Override
    public int getPortNumber () {
        return portNumber;
    }

    public void setPortNumber (int portNumber) {
        this.portNumber = portNumber;
    }

    @Override
    public int getNumberOfSimultaneousConnections () {
        if (this.changedNumberOfSimultaneousConnections == NOT_CHANGED) {
            return numberOfSimultaneousConnections;
        }
        else {
            return this.changedNumberOfSimultaneousConnections;
        }
    }

    public void setNumberOfSimultaneousConnections (int numberOfSimultaneousConnections) {
        this.changedNumberOfSimultaneousConnections = numberOfSimultaneousConnections;
        this.becomeDirty();
    }

    @Override
    public ComPortType getComPortType() {
        return ComPortType.TCP;
    }

    @Override
    public InboundComPortPool getComPortPool() {
        return null;
    }

    @Override
    public void setComPortPool(InboundComPortPool comPortPool) {

    }

    /**
     * Indicate that this InboundComPort is TCP-based
     *
     * @return true if this port is an instance of TCPBasedInboundComPort, false otherwise
     */
    @Override
    public boolean isTCPBased() {
        return true;
    }

    /**
     * Indicate that this InboundComPort is UDP-based
     *
     * @return true if this port is an instance of {@link com.energyict.mdc.engine.config.UDPBasedInboundComPort}, false otherwise
     */
    @Override
    public boolean isUDPBased() {
        return false;
    }

    /**
     * Indicate that this InboundComPort is Modem-based
     *
     * @return true if this port is an instance of ModemBasedInboundComPort, false otherwise
     */
    @Override
    public boolean isModemBased() {
        return false;
    }

    @Override
    public boolean isServletBased () {
        return false;
    }

}