package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects;

import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * ComChannel that does not actually send any bytes.
 * It just remembers the last request that was sent.
 * Reading does nothing.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 14/08/2015 - 16:43
 */
public class LatchComChannel implements ComChannel {

    private byte[] lastRequest = new byte[0];
    private TypedProperties properties = TypedProperties.empty();

    public byte[] getLastRequest() {
        return lastRequest;
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
        lastRequest = new byte[0];
        return false;
    }

    /**
     * Concatenate byte b that is written to the lastRequest field.
     */
    @Override
    public int write(int b) {
        lastRequest = ProtocolTools.concatByteArrays(lastRequest, new byte[]{(byte) b});
        return 1;
    }

    /**
     * Concatenate the bytes that are written to the lastRequest field.
     */
    @Override
    public int write(byte[] bytes) {
        lastRequest = ProtocolTools.concatByteArrays(lastRequest, bytes);
        return bytes.length;
    }

    @Override
    public void close() {

    }

    @Override
    public void addProperties(TypedProperties typedProperties) {
        this.properties.setAllProperties(typedProperties);
    }

    @Override
    public TypedProperties getProperties() {
        return properties;
    }

    @Override
    public void prepareForDisConnect() {

    }

    @Override
    public void flush() throws IOException {

    }
}
