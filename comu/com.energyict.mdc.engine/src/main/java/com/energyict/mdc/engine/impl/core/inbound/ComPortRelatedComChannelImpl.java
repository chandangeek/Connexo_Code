package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ComChannel;

import java.io.IOException;

/**
 * Provides an implementation for the {@link ComPortRelatedComChannel} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (13:44)
 */
public class ComPortRelatedComChannelImpl  implements ComPortRelatedComChannel {

    private ComChannel comChannel;
    private ComPort comPort;

    public ComPortRelatedComChannelImpl(ComChannel comChannel) {
        super();
        this.comChannel = comChannel;
    }

    @Override
    public ComPort getComPort() {
        return comPort;
    }

    @Override
    public void setComPort(ComPort comPort) {
        this.comPort = comPort;
    }

    @Override
    public ComChannel getDelegatingComChannel() {
        return comChannel;
    }

    @Override
    public boolean startReading() {
        return comChannel.startReading();
    }

    @Override
    public int read() {
        return comChannel.read();
    }

    @Override
    public int read(byte[] buffer) {
        return comChannel.read(buffer);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        return comChannel.read(buffer, offset, length);
    }

    @Override
    public int available() {
        return comChannel.available();
    }

    @Override
    public boolean startWriting() {
        return comChannel.startWriting();
    }

    @Override
    public int write(int b) {
        return comChannel.write(b);
    }

    @Override
    public int write(byte[] bytes) {
        return comChannel.write(bytes);
    }

    @Override
    public void close() {
        comChannel.close();
    }

    @Override
    public void addProperties(TypedProperties typedProperties) {
        comChannel.addProperties(typedProperties);
    }

    @Override
    public TypedProperties getProperties() {
        return comChannel.getProperties();
    }

    @Override
    public void flush() throws IOException {
        comChannel.flush();
    }

}