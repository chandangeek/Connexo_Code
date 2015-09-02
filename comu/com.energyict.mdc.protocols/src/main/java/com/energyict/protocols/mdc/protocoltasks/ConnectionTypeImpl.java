package com.energyict.protocols.mdc.protocoltasks;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.ServerSerialPort;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.properties.PropertySpec;

import java.io.IOException;

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

    protected TypedProperties getAllProperties() {
        return this.properties;
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.properties = TypedProperties.copyOf(properties);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return this.getPropertySpecs()
                .stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
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

    /**
     * Creates a new {@link ComChannel}
     * that uses Sockets as the actual connection mechanism.
     *
     * @param host The host name, or <code>null</code> for the loopback address.
     * @param port The port number
     * @param timeOut the timeOut in milliseconds to wait before throwing a ConnectionException
     * @return The ComChannel
     * @throws ConnectionException Indicates a failure in the actual connection mechanism
     */
    protected ComChannel newTcpIpConnection(SocketService socketService, String host, int port, int timeOut) throws ConnectionException {
        try {
            return socketService.newOutboundTcpIpConnection(host, port, timeOut);
        }
        catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    /**
     * Creates a new {@link ComChannel}
     * that uses UDP Datagrams as the actual connection mechanism
     *
     * @param bufferSize the bufferSize of the ByteArray which receives the UDP data
     * @param host the host to which to connect
     * @param port the portNumber to which we need to connect
     * @return the newly created DatagramComChannel
     * @throws ConnectionException if the connection setup did not work
     */
    protected ComChannel newUDPConnection(SocketService socketService, int bufferSize, String host, int port) throws ConnectionException {
        try {
            return socketService.newOutboundUDPConnection(bufferSize, host, port);
        }
        catch (IOException b) { // thrown when an unknown host occurs
            throw new ConnectionException(b);
        }
    }

    /**
     * Creates a new {@link ComChannel}
     * that uses an RxTxSerialPort as the interface with the physical ComPort
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws ConnectionException if an exception occurred during the creation or initialization of the ComPort
     */
    protected SerialComChannel newRxTxSerialConnection(SerialComponentService rxtxSerialComponentService, SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
        ServerSerialPort serialPort = rxtxSerialComponentService.newSerialPort(serialPortConfiguration);
        serialPort.openAndInit();
        return rxtxSerialComponentService.newSerialComChannel(serialPort);
    }

    /**
     * Creates a new {@link ComChannel}
     * that uses a {@link ServerSerialPort} as the interface with the physical ComPort.
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws ConnectionException if an exception occurred during the creation or initialization of the ComPort
     */
    protected SerialComChannel newSioSerialConnection(SerialComponentService serialComponentService, final SerialPortConfiguration serialPortConfiguration) throws ConnectionException {
        ServerSerialPort serialPort = serialComponentService.newSerialPort(serialPortConfiguration);
        serialPort.openAndInit();
        return serialComponentService.newSerialComChannel(serialPort);
    }

}