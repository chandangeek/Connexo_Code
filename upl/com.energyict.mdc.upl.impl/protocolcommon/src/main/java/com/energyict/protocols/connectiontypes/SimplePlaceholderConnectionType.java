package com.energyict.protocols.connectiontypes;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionType;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Serves as a simple Test ConnectionType. It will not connect to anything physically, but you can use
 * it in SDK protocols
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 14:57
 */
public class SimplePlaceholderConnectionType implements ConnectionType {

    @Override
    public boolean allowsSimultaneousConnections() {
        return false;
    }

    @Override
    public boolean supportsComWindow() {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.allOf(ComPortType.class);
    }

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        return new ComChannel() {
            @Override
            public boolean startReading() {
                return false;
            }

            @Override
            public int read() {
                return 0;
            }

            @Override
            public int read(byte[] buffer) {
                return 0;
            }

            @Override
            public int read(byte[] buffer, int offset, int length) {
                return 0;
            }

            @Override
            public int available() {
                return 0;
            }

            @Override
            public boolean startWriting() {
                return false;
            }

            @Override
            public int write(int b) {
                return 0;
            }

            @Override
            public int write(byte[] bytes) {
                return 0;
            }

            @Override
            public void close() {
                // nothing to do
            }

            @Override
            public void flush() throws IOException {
                // nothing to do
            }
        };
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // nothing to disconnect
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return null;
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return false;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // really nothing to do
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }
}
