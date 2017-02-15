package com.energyict.dialer.uplserialserviceprovider;

import com.energyict.mdc.upl.RuntimeEnvironment;
import com.energyict.mdc.upl.io.NestedIOException;

import java.io.IOException;

/**
 * Factory class for a {@link SerialPort}, using a specified library to provide the underlying interface. The property specified is called <b>serialPortServiceClass</b>
 */
public final class SerialPortServiceProvider {

    /**
     * The service class when using the SerialIO library (which is the default).
     */
    private static final String SERIALIO_CLASS = "com.energyict.dialer.serialserviceprovider.serialio.SerialPortImpl";

    /**
     * The name of the property in the environment (eiserver.properties).
     */
    private static final String ENVIRONMENT_PROPERTY_SERIAL_PORT_PROVIDER = "serialPortServiceClass";

    public static SerialPort getSerialPort(SerialConfig serialConfig, RuntimeEnvironment runtimeEnvironment) throws IOException {
        SerialPortServiceProvider o = new SerialPortServiceProvider();
        return o.getInstance(serialConfig, runtimeEnvironment);
    }

    @SuppressWarnings("unchecked")
    private SerialPort getInstance(SerialConfig serialConfig, RuntimeEnvironment runtimeEnvironment) throws IOException {
        try {
            String serviceClassName = runtimeEnvironment.getProperty(ENVIRONMENT_PROPERTY_SERIAL_PORT_PROVIDER, SERIALIO_CLASS);
            Class<SerialPort> c = (Class<SerialPort>) Class.forName(serviceClassName);
            SerialPort serialPort = c.newInstance();
            serialPort.init(serialConfig);
            return serialPort;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new NestedIOException(e);
        }
    }

}
