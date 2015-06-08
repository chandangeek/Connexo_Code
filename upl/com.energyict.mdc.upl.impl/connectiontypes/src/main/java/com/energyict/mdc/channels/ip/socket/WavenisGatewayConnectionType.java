package com.energyict.mdc.channels.ip.socket;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.*;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.protocolimplv2.comchannels.WavenisStackUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * Extension of the TCP/IP connection type that returns a WavenisComChannel.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 9:35
 * Author: khe
 */
@XmlRootElement
public class WavenisGatewayConnectionType extends OutboundTcpIpConnectionType {

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        for (ConnectionTaskProperty property : properties) {
            if (property.getValue() != null) {
                this.setProperty(property.getName(), property.getValue());
            }
        }
        ServerLoggableComChannel comChannel = this.newWavenisConnection(this.hostPropertyValue(), this.portNumberPropertyValue(), this.connectionTimeOutPropertyValue());
        comChannel.addProperties(createTypeProperty(ComChannelType.WavenisGatewayComChannel));
        comChannel.setComPort(comPort);
        return comChannel;
    }

    private ServerLoggableComChannel newWavenisConnection(String host, int port, int timeOut) throws ConnectionException {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeOut);
            WavenisStack wavenisStack = WavenisStackUtils.start(socket.getInputStream(), socket.getOutputStream());
            return new WavenisGatewayComChannel(socket, wavenisStack);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }
}