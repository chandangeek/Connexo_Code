package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.dlms.protocolimplv2.connection.TCPIPConnection;
import com.energyict.dlms.protocolimplv2.connection.UDPIPConnection;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimplv2.dlms.a2.A2DlmsSession;
import com.energyict.protocolimplv2.dlms.a2.A2HHUHDLCConnection;
import com.energyict.protocolimplv2.dlms.a2.A2HHUSignOn;

public class EI7DlmsSession extends A2DlmsSession {

    public EI7DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, A2HHUSignOn hhuSignOn, String deviceId) {
        super(comChannel, properties, hhuSignOn, deviceId);
    }

    @Override
    protected DlmsV2Connection defineTransportDLMSConnection() {
        if (ComChannelType.SerialComChannel.equals(getComChannel().getComChannelType()) || ComChannelType.OpticalComChannel.equals(getComChannel().getComChannelType())) {
            return new A2HHUHDLCConnection(getComChannel(), getProperties(), null);//the A2HHUHDLCConnection class in replacement of the HDLCConnection
        } else if (ComChannelType.SocketComChannel.equals(getComChannel().getComChannelType())) {
            return new TCPIPConnection(getComChannel(), getProperties());
        } else if (ComChannelType.DatagramComChannel.equals(getComChannel().getComChannelType())) {
            return new UDPIPConnection(getComChannel(), getProperties());
        } else {
            throw DeviceConfigurationException.unexpectedComChannel(ComChannelType.SerialComChannel.name() + ", " + ComChannelType.SocketComChannel.name(), getComChannel().getClass().getSimpleName());        }
    }
}
