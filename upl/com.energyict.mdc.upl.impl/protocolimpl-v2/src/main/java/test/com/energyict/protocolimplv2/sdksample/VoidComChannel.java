package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.io.IOException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-22 (08:39)
 */
public class VoidComChannel implements ComChannel {

    @Override
    public boolean startReading() {
        //nothing to do
        return true;
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
        //nothing to do
        return true;
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
        // nothing to close
    }

    @Override
    public void flush() throws IOException {
        // nothing to do
    }

    @Override
    public void addProperties(TypedProperties typedProperties) {

    }

    @Override
    public TypedProperties getProperties() {
        return com.energyict.protocolimpl.properties.TypedProperties.empty();
    }

    @Override
    public void prepareForDisConnect() {

    }

    @Override
    public void setTimeout(long millis) {

    }

    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.Invalid;
    }
}