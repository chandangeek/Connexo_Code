package com.energyict.mdc.tasks;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.ip.datagrams.DatagramComChannel;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpSession;
import com.energyict.mdc.channels.ip.socket.SocketComChannel;
import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.nls.Thesaurus;
import com.energyict.mdc.channels.serial.SerialComChannel;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialPort;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.TypedProperties;

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
public abstract class ConnectionTypeImpl implements com.energyict.mdc.io.ConnectionType {

    private TypedProperties properties = TypedProperties.empty();

    public ConnectionTypeImpl() {
        super();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.properties = TypedProperties.copyOf(properties);
    }

    protected TypedProperties getAllProperties() {
        return this.properties;
    }

    protected Object getProperty(String propertyName) {
        return this.getAllProperties().getProperty(propertyName);
    }

    protected Object getProperty(String propertyName, Object defaultValue) {
        return this.getAllProperties().getProperty(propertyName, defaultValue);
    }

    protected void setProperty(String propertyName, Object value) {
        this.properties.setProperty(propertyName, value);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException{
        // Prepare the comChannel for disconnect
        comChannel.prepareForDisConnect();

        // Do the actual disconnect
        // Note: For most connectionTypes actual disconnect is not needed, so do nothing.
        // Should be overridden in those connectionTypes requiring a disconnect.
    }

    /**
     * Creates a new {@link com.energyict.mdc.protocol.ComChannel}
     * that uses Sockets as the actual connection mechanism.
     *
     * @param host The host name, or <code>null</code> for the loopback address.
     * @param port The port number
     * @param timeOut the timeOut in milliseconds to wait before throwing a ConnectionException
     * @return The ComChannel
     * @throws ConnectionException Indicates a failure in the actual connection mechanism
     */
    protected ComChannel newTcpIpConnection(String host, int port, int timeOut) throws ConnectionException {
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
    protected ComChannel newRxTxSerialConnection(final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
        ServerSerialPort serialPort = new RxTxSerialPort(serialPortConfiguration);
        serialPort.openAndInit();
        return new SerialComChannel(serialPort);
    }

    /**
     * Creates a new {@link com.energyict.mdc.protocol.ComChannel}
     * that uses a {@link SioSerialPort} as the interface with the physical ComPort
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws ConnectionException if an exception occurred during the creation or initialization of the ComPort
     */
    protected ComChannel newSioSerialConnection(final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
        ServerSerialPort serialPort = new SioSerialPort(serialPortConfiguration);
        serialPort.openAndInit();
        return new SerialComChannel(serialPort);
    }

    /**
     * Creates a new {@link com.energyict.mdc.protocol.ComChannel}
     * that uses UDP Datagrams as the actual connection mechanism
     *
     * @param bufferSize the bufferSize of the ByteArray which receives the UDP data
     * @param host the host to which to connect
     * @param port the portNumber to which we need to connect
     * @return the newly created DatagramComChannel
     * @throws ConnectionException if the connection setup did not work
     */
    protected ComChannel newUDPConnection(int bufferSize, String host, int port) throws ConnectionException {
        try {
            OutboundUdpSession udpSession = new OutboundUdpSession(bufferSize, host, port);
            return new DatagramComChannel(udpSession);
        } catch (IOException e) { // thrown when an unknown host occurs
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.NestedIOException, e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && this.getClass() == obj.getClass();
    }

    /**
     * Create a property that indicates the type of the ComChannel.
     * This is used by the protocols to determine the transport layer.
     */
    public static TypedProperties createTypeProperty(ComChannelType comChannelType) {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(ComChannelType.TYPE, comChannelType.getType());
        return typedProperties;
    }
}