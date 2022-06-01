package com.energyict.protocolimplv2.dlms.a2;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.dlms.protocolimplv2.connection.SecureConnection;
import com.energyict.dlms.protocolimplv2.connection.TCPIPConnection;
import com.energyict.dlms.protocolimplv2.connection.UDPIPConnection;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.protocol.exception.DeviceConfigurationException;

import java.util.logging.Logger;

public class A2DlmsSession extends DlmsSession {

    private boolean opticalConnection = false;

    public A2DlmsSession(ComChannel comChannel, DlmsSessionProperties properties) {
        super(comChannel, properties);
    }

    public A2DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, Logger logger) {
        super(comChannel, properties, logger);
    }

    public A2DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, A2HHUSignOn hhuSignOn, String deviceId) {
        super(comChannel, properties, hhuSignOn, deviceId);
        if (hhuSignOn != null) {
            opticalConnection = true;
            A2RequestFrameBuilder frameBuilder = new A2RequestFrameBuilder(properties);
            // the A2HHUHDLCConnection and A2HHUSignOn should share the same instance of A2RequestFrameBuilder otherwise the frameCounter will be out of sync
            ((A2HHUHDLCConnection) ((SecureConnection) getDlmsV2Connection()).getTransportConnection()).setFrameBuilder(frameBuilder);
            hhuSignOn.setFrameBuilder(frameBuilder);
        }
    }

    @Override
    protected DlmsV2Connection defineTransportDLMSConnection() {
        if (ComChannelType.SerialComChannel.equals(getComChannel().getComChannelType()) || ComChannelType.OpticalComChannel.equals(getComChannel().getComChannelType())) {
            return new A2HHUHDLCConnection(getComChannel(), getProperties(), null);//the A2HHUHDLCConnection class in replacement of the HDLCConnection
        } else if (ComChannelType.SocketComChannel.equals(getComChannel().getComChannelType())) {
            return new TCPIPConnection(getComChannel(), getProperties());
        }  else if (ComChannelType.DatagramComChannel.equals(getComChannel().getComChannelType())) {
            return new UDPIPConnection(getComChannel(), getProperties());
        } else {
            throw DeviceConfigurationException.unexpectedComChannel(ComChannelType.SerialComChannel.name() + ", " + ComChannelType.SocketComChannel.name(), getComChannel().getClass().getSimpleName());
        }
    }

    @Override
    public void createAssociation() {
        if (opticalConnection) {
            ((A2HHUHDLCConnection) ((SecureConnection) getDlmsV2Connection()).getTransportConnection()).createAssociation();
        } else {
            super.createAssociation();
        }
    }
}