package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.protocol.ComChannel;

import com.energyict.protocolimpl.properties.TypedProperties;

import java.io.IOException;

/**
 * Provides an implementation for the {@link ComChannel} interface that basically does nothing.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/12
 * Time: 14:06
 */
public class DummyComChannel implements ComChannel {

    private TypedProperties typedProperties = TypedProperties.empty();

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

    }

    @Override
    public void addProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        this.setProperties(TypedProperties.copyOf(typedProperties));
    }

    private void setProperties(TypedProperties typedProperties) {
        this.typedProperties.setAllProperties(typedProperties);
    }

    @Override
    public TypedProperties getProperties() {
        return typedProperties;
    }

    @Override
    public void prepareForDisConnect() {

    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void setTimeout(long millis) {

    }

    @Override
    public boolean isVoid() {
        return false;
    }
}