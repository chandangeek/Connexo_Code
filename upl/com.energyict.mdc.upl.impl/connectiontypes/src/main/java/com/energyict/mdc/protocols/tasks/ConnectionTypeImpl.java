package com.energyict.mdc.protocols.tasks;

import com.energyict.mdc.channel.ip.datagrams.DatagramComChannel;
import com.energyict.mdc.channel.ip.datagrams.OutboundUdpSession;
import com.energyict.mdc.channel.ip.socket.SocketComChannel;
import com.energyict.mdc.channel.serial.OpticalDriver;
import com.energyict.mdc.channel.serial.SerialComChannelImpl;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.channel.serial.direct.rxtx.RxTxSerialPort;
import com.energyict.mdc.channel.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.mdc.protocols.tasks.AbstractConnectionTypeImpl;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Serves as the root for components that intend to implement
 * the ConnectionType interface.
 * Mostly provides code reuse opportunities for storing properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-24 (15:19)
 */
@XmlRootElement
public abstract class ConnectionTypeImpl extends AbstractConnectionTypeImpl {

    public ConnectionTypeImpl() {
        super();
    }

    /**
     * Creates a new {@link com.energyict.mdc.protocol.ComChannel}
     * that uses Sockets as the actual connection mechanism.
     *
     * @param host    The host name, or <code>null</code> for the loopback address.
     * @param port    The port number
     * @param timeOut the timeOut in milliseconds to wait before throwing a ConnectionException
     * @return The ComChannel
     * @throws ConnectionException Indicates a failure in the actual connection mechanism
     */
    public ComChannel newTcpIpConnection(String host, int port, int timeOut) throws ConnectionException {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeOut);
            return new SocketComChannel(socket);
        } catch (IOException e) {
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.NestedIOException, e);
        }
    }

    /**
     * Creates a new {@link com.energyict.mdc.protocol.ComChannel}
     * that uses a {@link RxTxSerialPort} as the interface with the physical ComPort
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws ConnectionException if an exception occurred during the creation or initialization of the ComPort
     */
    public SerialPortComChannel newRxTxSerialConnection(final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
        ServerSerialPort serialPort = new RxTxSerialPort(serialPortConfiguration);
        serialPort.openAndInit();
        ComChannelType comChannelType = this instanceof OpticalDriver ? ComChannelType.OpticalComChannel : ComChannelType.SerialComChannel;
        return new SerialComChannelImpl(serialPort, comChannelType);
    }

    /**
     * Creates a new {@link com.energyict.mdc.protocol.ComChannel}
     * that uses a {@link SioSerialPort} as the interface with the physical ComPort
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws ConnectionException if an exception occurred during the creation or initialization of the ComPort
     */
    public SerialPortComChannel newSioSerialConnection(final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
        ServerSerialPort serialPort = new SioSerialPort(serialPortConfiguration);
        serialPort.openAndInit();
        ComChannelType comChannelType = this instanceof OpticalDriver ? ComChannelType.OpticalComChannel : ComChannelType.SerialComChannel;
        return new SerialComChannelImpl(serialPort, comChannelType);
    }

    /**
     * Creates a new {@link com.energyict.mdc.protocol.ComChannel}
     * that uses UDP Datagrams as the actual connection mechanism
     *
     * @param bufferSize the bufferSize of the ByteArray which receives the UDP data
     * @param host       the host to which to connect
     * @param port       the portNumber to which we need to connect
     * @return the newly created DatagramComChannel
     * @throws ConnectionException if the connection setup did not work
     */
    public ComChannel newUDPConnection(int bufferSize, String host, int port) throws ConnectionException {
        try {
            OutboundUdpSession udpSession = new OutboundUdpSession(bufferSize, host, port);
            return new DatagramComChannel(udpSession);
        } catch (IOException e) { // thrown when an unknown host occurs
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.NestedIOException, e);
        }
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
    }
}