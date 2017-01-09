package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.nls.Thesaurus;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimplv2.comchannels.WavenisStackUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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

    public WavenisGatewayConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        ComChannel comChannel = this.newWavenisConnection(this.hostPropertyValue(), this.portNumberPropertyValue(), this.connectionTimeOutPropertyValue());
        comChannel.addProperties(createTypeProperty(ComChannelType.WavenisGatewayComChannel));
        return comChannel;
    }

    private ComChannel newWavenisConnection(String host, int port, int timeOut) throws ConnectionException {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeOut);
            WavenisStack wavenisStack = WavenisStackUtils.start(socket.getInputStream(), socket.getOutputStream());
            return new WavenisGatewayComChannel(socket, wavenisStack);
        } catch (IOException e) {
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.NestedIOException, e);
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-12-18 09:25:29 +0100 (Fri, 18 Dec 2015)$";
    }
}