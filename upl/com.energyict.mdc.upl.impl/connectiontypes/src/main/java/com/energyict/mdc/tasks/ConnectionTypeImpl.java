package com.energyict.mdc.tasks;

import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.ip.datagrams.DatagramComChannel;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpSession;
import com.energyict.mdc.channels.ip.socket.SocketComChannel;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialPort;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.exceptions.SerialPortException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.ServerLoggableComChannel;

import javax.xml.bind.annotation.XmlElement;
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
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
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
     * @param host    The host name, or <code>null</code> for the loopback address.
     * @param port    The port number
     * @param timeOut the timeOut in milliseconds to wait before throwing a ConnectionException
     * @return The ComChannel
     * @throws ConnectionException Indicates a failure in the actual connection mechanism
     */
    protected ServerLoggableComChannel newTcpIpConnection(String host, int port, int timeOut) throws ConnectionException {
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
    protected ServerLoggableComChannel newRxTxSerialConnection(final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
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
    protected ServerLoggableComChannel newSioSerialConnection(final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
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
    protected ServerLoggableComChannel newUDPConnection(int bufferSize, String host, int port) throws ConnectionException {
        try {
            OutboundUdpSession udpSession = new OutboundUdpSession(bufferSize, host, port);
            return new DatagramComChannel(udpSession);
        } catch (IOException b) { // thrown when an unknown host occurs
            throw new ConnectionException(b);
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
    protected TypedProperties createTypeProperty(ComChannelType comChannelType) {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(ComChannelType.TYPE, comChannelType.getType());
        return typedProperties;
    }

    @Override
    public void injectConnectionTaskId(int connectionTaskId) {
        // Not interested in this kind of info, sub-classes who need this can override
    }
}