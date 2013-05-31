package com.energyict.mdc.tasks;

import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.channels.ip.datagrams.DatagramComChannel;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpSession;
import com.energyict.mdc.channels.ip.socket.SocketComChannel;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialPort;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.exceptions.SerialPortException;
import com.energyict.mdc.protocol.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Serves as the root for components that intend to implement
 * the {@link com.energyict.mdc.tasks.ConnectionType} interface.
 * Mostly provides code reuse opportunities for storing properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-24 (15:19)
 */
public abstract class ConnectionTypeImpl implements ConnectionType {

    private TypedProperties properties = TypedProperties.empty();

    public ConnectionTypeImpl() {
        super();
    }

    @Override
    public void addProperties(TypedProperties properties) {
        this.properties = TypedProperties.copyOf(properties);
    }

    protected TypedProperties getAllProperties() {
        return this.properties;
    }

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
     * Creates a new {@link com.energyict.mdc.protocol.ComChannel}
     * that uses Sockets as the actual connection mechanism.
     *
     * @param host    The host name, or <code>null</code> for the loopback address.
     * @param port    The port number
     * @param timeOut the timeOut in milliseconds to wait before throwing a ConnectionException
     * @return The ComChannel
     * @throws ConnectionException Indicates a failure in the actual connection mechanism
     */
    protected ServerComChannel newTcpIpConnection(String host, int port, int timeOut) throws ConnectionException {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeOut);
            return new SocketComChannel(socket);
        } catch (IOException e) {
            throw new ConnectionException(e);
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
    protected ServerComChannel newRxTxSerialConnection(final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
        try {
            SerialComponentFactory serialComponentFactory = ManagerFactory.getCurrent().getSerialComponentFactory();
            RxTxSerialPort serialPort = serialComponentFactory.newRxTxSerialPort(serialPortConfiguration);
            serialPort.openAndInit();
            return serialComponentFactory.newSerialComChannel(serialPort);
        } catch (SerialPortException e) {
            throw new ConnectionException(e);
        }
    }

    /**
     * Creates a new {@link com.energyict.mdc.protocol.ComChannel}
     * that uses a {@link SioSerialPort} as the interface with the physical ComPort
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws ConnectionException if an exception occurred during the creation or initialization of the ComPort
     */
    protected ServerComChannel newSioSerialConnection(final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
        try {
            SerialComponentFactory serialComponentFactory = ManagerFactory.getCurrent().getSerialComponentFactory();
            SioSerialPort serialPort = serialComponentFactory.newSioSerialPort(serialPortConfiguration);
            serialPort.openAndInit();
            return serialComponentFactory.newSerialComChannel(serialPort);
        } catch (SerialPortException | UnsatisfiedLinkError e) {
            throw new ConnectionException(e);
        }
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
    protected ServerComChannel newUDPConnection(int bufferSize, String host, int port) throws ConnectionException {
        try {
            OutboundUdpSession udpSession = new OutboundUdpSession(bufferSize, host, port);
            return new DatagramComChannel(udpSession);
        } catch (IOException b) { // thrown when an unknown host occurs
            throw new ConnectionException(b);
        }
    }

}