package com.energyict.mdc.engine.impl.core.mocks;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.ports.ComPortType;

import java.util.Collections;
import java.util.List;

/**
 * Provides a mock implementation for the {@link OutboundComPort} interface
 * for demo purposes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-04 (09:16)
 */
public class MockOutboundComPort extends MockComPort implements OutboundComPort {

    private ComPortType type;
    private int numberOfSimultaneousConnections;

    public MockOutboundComPort (ComServer comServer, String name) {
        super(comServer, name);
    }

    public MockOutboundComPort (ComServer comServer, long id, String name) {
        super(comServer, id, name);
    }

    public ComPortType getComPortType () {
        return type;
    }

    public void setComPortType (ComPortType type) {
        this.type = type;
    }

    @Override
    public int getNumberOfSimultaneousConnections () {
        return numberOfSimultaneousConnections;
    }

    public void setNumberOfSimultaneousConnections (int numberOfSimultaneousConnections) {
        this.numberOfSimultaneousConnections = numberOfSimultaneousConnections;
        this.becomeDirty();
    }

    @Override
    public boolean isInbound () {
        return false;
    }

    @Override
    public String getDescription () {
        return getName();
    }

    public List<ComJob> findExecutableComTasks () {
        return Collections.emptyList();
    }



}