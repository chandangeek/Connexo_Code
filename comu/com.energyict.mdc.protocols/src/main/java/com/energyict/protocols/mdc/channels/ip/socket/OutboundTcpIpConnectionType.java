package com.energyict.protocols.mdc.channels.ip.socket;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComPortType;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.ConnectionType;
import com.energyict.mdc.protocol.ServerComChannel;
import com.energyict.mdc.protocol.dynamic.ConnectionProperty;
import com.energyict.protocols.mdc.channels.ip.OutboundIpConnectionType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link ConnectionType} interface for TCP/IP.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-24 (14:59)
 */
public class OutboundTcpIpConnectionType extends OutboundIpConnectionType {

    public OutboundTcpIpConnectionType() {
        super();
    }

    @Override
    public boolean allowsSimultaneousConnections () {
        return true;
    }

    @Override
    public boolean supportsComWindow () {
        return true;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes () {
        return EnumSet.of(ComPortType.TCP);
    }

    @Override
    public String getVersion () {
        return "$Date: 2013-04-12 15:03:44 +0200 (vr, 12 apr 2013) $";
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        for (ConnectionProperty property : properties) {
            if (property.getValue() != null) {
                this.setProperty(property.getName(), property.getValue());
            }
        }
        return this.newTcpIpConnection(this.hostPropertyValue(), this.portNumberPropertyValue(), this.connectionTimeOutPropertyValue());
    }

}