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
import com.energyict.protocol.exceptions.ProtocolRuntimeException;

import java.util.logging.Logger;

public class A2DlmsSession extends DlmsSession {

    private boolean opticalConnection = false;

    private A2 protocol;
    private ComChannel comChannel;

    public A2DlmsSession(ComChannel comChannel, DlmsSessionProperties properties) {
        super(comChannel, properties);
    }

    public A2DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, Logger logger) {
        super(comChannel, properties, logger);
    }

    public A2DlmsSession(ComChannel comChannel, DlmsSessionProperties properties, A2HHUSignOn hhuSignOn, String deviceId,
                         A2 protocol) {
        super(comChannel, properties, hhuSignOn, deviceId);

        this.protocol = protocol;
        this.comChannel = comChannel;

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
            try {
                if (this.protocol.useCachedFrameCounter()) {
                    // intentionally set retries to 0 to save up traffic
                    this.dlmsConnection.setRetries(0);
                }
                super.createAssociation();
            } catch (ProtocolRuntimeException ex) {
                this.protocol.journal("Association with cached frame counter failed: " + ex.getMessage());

                // set back the original value for retries
                this.dlmsConnection.setRetries(getProperties().getRetries());

                // fallback to reading the frame counter with the public client
                this.protocol.setupSession(comChannel, A2.FRAME_COUNTER_MANAGEMENT_ONLINE);
                super.createAssociation();
            } finally {
                // set back the original value for retries
                this.dlmsConnection.setRetries(getProperties().getRetries());
            }
        }
    }
}