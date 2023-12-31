package com.energyict.protocolimplv2.comchannels;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.ProtocolException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Adapts a ComChannel so that it behaves as an InputStream.
 * This is only required for backwards compatibility with old-style
 * communication protocols such as {@link com.energyict.protocol.MeterProtocol}
 * and {@link com.energyict.protocol.SmartMeterProtocol}
 * that need an InputStream (and an OutputStream) at initialization time.
 * The ComServer statistics however, rely on the fact that all communication
 * goes through the ComChannel methods and no calls are made to
 * the underlying InputStream directly.
 * Because the old-style communication protocols are not aware of the
 * synchroneous nature of a ComChannel, it will invoke
 * startReading at the appropriate time.
 *
 * @author Rudi Vankeirsbilck (rvk)
 */
public class ComChannelInputStreamAdapter extends InputStream {

    private ComChannel comChannel;

    public ComChannelInputStreamAdapter (ComChannel comChannel) {
        super();
        this.comChannel = comChannel;
    }

    @Override
    public int available () throws IOException {
        this.ensureReading();
        return this.comChannel.available();
    }

    @Override
    public void close () throws IOException {
        this.comChannel.close();
    }

    @Override
    public void mark (int readlimit) {
        // Mark is not supported
    }

    @Override
    public boolean markSupported () {
        return false;
    }

    @Override
    public int read () throws IOException {
        this.ensureReading();
        return this.comChannel.read();
    }

    @Override
    public int read (byte[] b) throws IOException {
        this.ensureReading();
        return this.comChannel.read(b);
    }

    @Override
    public int read (byte[] b, int off, int len) throws IOException {
        this.ensureReading();
        return this.comChannel.read(b, off, len);
    }

    private void ensureReading () {
        this.comChannel.startReading();
    }

    @Override
    public void reset () throws IOException {
        throw new ProtocolException("Mark is not supported.");
    }

    @Override
    public long skip (long n) throws IOException {
        throw new ProtocolException("Skip is currently not supported.");
    }
}
