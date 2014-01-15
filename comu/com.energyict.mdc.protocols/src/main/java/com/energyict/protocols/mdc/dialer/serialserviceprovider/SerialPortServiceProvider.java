package com.energyict.protocols.mdc.dialer.serialserviceprovider;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.protocol.api.dialer.serialserviceprovider.SerialConfig;
import com.energyict.mdc.protocol.api.dialer.serialserviceprovider.SerialPort;

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

    /**
     * The selected service class.
     */
    private static final String SERVICE_CLASS = getStringProperty(ENVIRONMENT_PROPERTY_SERIAL_PORT_PROVIDER, SERIALIO_CLASS);

    public static SerialPort getSerialPort(SerialConfig serialConfig) throws IOException {
        SerialPortServiceProvider o = new SerialPortServiceProvider();
        return o.getInstance(serialConfig);
    }

    private SerialPort getInstance(SerialConfig serialConfig) throws IOException {
        try {
            @SuppressWarnings("unchecked") Class<SerialPort> c = (Class<SerialPort>) Class.forName(SERVICE_CLASS);
            SerialPort serialPort = c.newInstance();
            serialPort.init(serialConfig);
            return serialPort;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new NestedIOException(e);
        }
    }

    private static String getStringProperty(String key, String defaultValue) {
        String value = Environment.DEFAULT.get().getProperty(key);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

}
