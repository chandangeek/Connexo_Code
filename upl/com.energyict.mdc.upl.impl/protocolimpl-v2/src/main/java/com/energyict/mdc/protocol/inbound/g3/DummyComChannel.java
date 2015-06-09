package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.ServerComChannel;
import com.energyict.mdc.tasks.ConnectionTask;

import java.io.IOException;

/**
 * Provides an implementation of a {@link ServerComChannel} that basically does nothing.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/12
 * Time: 14:06
 */
public class DummyComChannel implements ServerComChannel {

    private TypedProperties typedProperties = TypedProperties.empty();

    @Override
    public ComPort getComPort() {
        return null;
    }

    @Override
    public void setComPort(ComPort comPort) {

    }

    @Override
    public ConnectionTask getConnectionTask() {
        return null;
    }

    @Override
    public void setConnectionTask(ConnectionTask connectionTask) {

    }

    @Override
    public void sessionCountersStartWriting() {

    }

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
    public void addProperties(TypedProperties typedProperties) {
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
}