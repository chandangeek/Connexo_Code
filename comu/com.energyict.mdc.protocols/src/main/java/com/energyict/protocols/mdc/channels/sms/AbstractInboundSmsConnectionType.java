package com.energyict.protocols.mdc.channels.sms;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComPortType;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.ConnectionType;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.protocol.dynamic.ConnectionProperty;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract implementation of the {@link ConnectionType} interface specific for inbound SMS communication.
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
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

}