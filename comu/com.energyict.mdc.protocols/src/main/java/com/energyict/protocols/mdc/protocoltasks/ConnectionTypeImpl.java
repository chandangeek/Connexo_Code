package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.protocols.mdc.channels.ip.datagrams.DatagramComChannel;
import com.energyict.protocols.mdc.channels.ip.datagrams.OutboundUdpSession;
import com.energyict.protocols.mdc.channels.ip.socket.SocketComChannel;
import com.energyict.protocols.mdc.channels.serial.SerialComChannel;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.protocols.mdc.channels.serial.SioSerialPort;
import com.energyict.protocols.mdc.channels.serial.direct.rxtx.RxTxSerialPort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Serves as the root for components that intend to implement
 * the {@link ConnectionType} interface.
 * Mostly provides code reuse opportunities for storing properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-24 (15:19)
 */
public abstract class ConnectionTypeImpl implements ServerConnectionType {

    private TypedProperties properties = TypedProperties.empty();
    private PropertySpecService propertySpecService;

    public ConnectionTypeImpl() {
        super();
    }

    protected TypedProperties getAllProperties() {
        return this.properties;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void copyProperties (TypedProperties properties) {
        this.properties = TypedProperties.copyOf(properties);
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs);
        return propertySpecs;
    }

    protected abstract void addPropertySpecs (List<PropertySpec> propertySpecs);

    protected Object getProperty(String propertyName) {
        return this.getAllProperties().getProperty(propertyName);
    }

    protected void setProperty(String propertyName, Object value) {
        this.properties.setProperty(propertyName, value);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // For most connectionTypes disconnect is not needed, so do nothing.
        // Should be overridden in those connectionTypes requiring a disconnect.
    }

    /**
     * Creates a new {@link ComChannel}
     * that uses Sockets as the actual connection mechanism.
     *
     * @param host    The host name, or <code>null</code> for the loopback address.
     * @param port    The port number
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
            throw new ConnectionException(e);
        }
    }

    /**
     * Creates a new {@link ComChannel}
     * that uses a {@link RxTxSerialPort} as the interface with the physical ComPort
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws ConnectionException if an exception occurred during the creation or initialization of the ComPort
     */
    protected ComChannel newRxTxSerialConnection(final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
        RxTxSerialPort serialPort = new RxTxSerialPort(serialPortConfiguration);
        serialPort.openAndInit();
        return new SerialComChannel(serialPort);
    }

    /**
     * Creates a new {@link ComChannel}
     * that uses a {@link SioSerialPort} as the interface with the physical ComPort
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws ConnectionException if an exception occurred during the creation or initialization of the ComPort
     */
    protected ComChannel newSioSerialConnection(final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
        SioSerialPort serialPort = new SioSerialPort(serialPortConfiguration);
        serialPort.openAndInit();
        return new SerialComChannel(serialPort);
    }

    /**
     * Creates a new {@link ComChannel}
     * that uses UDP Datagrams as the actual connection mechanism
     *
     * @param bufferSize the bufferSize of the ByteArray which receives the UDP data
     * @param host       the host to which to connect
     * @param port       the portNumber to which we need to connect
     * @return the newly created DatagramComChannel
     * @throws ConnectionException if the connection setup did not work
     */
    protected ComChannel newUDPConnection(int bufferSize, String host, int port) throws ConnectionException {
        try {
            OutboundUdpSession udpSession = new OutboundUdpSession(bufferSize, host, port);
            return new DatagramComChannel(udpSession);
        } catch (IOException b) { // thrown when an unknown host occurs
            throw new ConnectionException(b);
        }
    }

}