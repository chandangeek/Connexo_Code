package com.energyict.mdc.channels.sms;

import com.energyict.mdc.channels.VoidComChannel;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import com.energyict.protocol.exceptions.ConnectionException;

import java.util.EnumSet;
import java.util.Set;

/**
 * Abstract implementation of the {@link com.energyict.mdc.io.ConnectionType} interface
 * specific for inbound SMS communication.
 *
 * @author sva
 * @since 26/06/13 - 17:10
 */
public abstract class AbstractInboundSmsConnectionType extends ConnectionTypeImpl {

    @Override
    public boolean allowsSimultaneousConnections() {
        return true;
    }

    @Override
    public boolean supportsComWindow() {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.SERVLET);
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        return new VoidComChannel();
    }

}
