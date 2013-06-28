package com.energyict.mdc.channels.sms;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract implementation of the {@link com.energyict.mdc.tasks.ConnectionType} interface specific for inbound SMS communication.
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
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<>();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>();
    }

}
