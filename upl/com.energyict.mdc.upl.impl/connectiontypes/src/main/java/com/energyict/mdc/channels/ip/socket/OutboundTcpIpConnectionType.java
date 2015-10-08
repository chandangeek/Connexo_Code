package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.ip.OutboundIpConnectionType;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link com.energyict.mdc.tasks.ConnectionType} interface for TCP/IP.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-24 (14:59)
 */
@XmlRootElement
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
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        for (ConnectionTaskProperty property : properties) {
            if(property.getValue() != null){
                this.setProperty(property.getName(), property.getValue());
            }
        }
        ComChannel comChannel = this.newTcpIpConnection(this.hostPropertyValue(), this.portNumberPropertyValue(), this.connectionTimeOutPropertyValue());
        comChannel.addProperties(createTypeProperty(ComChannelType.SocketComChannel));
        return comChannel;
    }

}