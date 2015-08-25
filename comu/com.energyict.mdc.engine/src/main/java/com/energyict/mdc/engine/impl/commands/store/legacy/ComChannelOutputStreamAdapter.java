package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Adapts a ComChannel so that it behaves as an OutputStream.
 * This is only required for backwards compatibility with old-style
 * communication protocols such as {@link MeterProtocol}
 * and {@link SmartMeterProtocol}
 * that need an OutputStream (and an OutputStream) at initialization time.
 * The ComServer statistics however, rely on the fact that all communication
 * goes through the ComChannel methods and no calls are made to
 * the underlying OutputStream directly.
 * Because the old-style communication protocols are not aware of the
 * synhroneous nature of a ComChannel, it will invoke
 * startWriting at the appropriate time.
 *
 * @author Rudi Vankeirsbilck (rvk)
 */
class ComChannelOutputStreamAdapter extends OutputStream {

    private ComChannel comChannel;

    public ComChannelOutputStreamAdapter (ComChannel comChannel) {
        super();
        this.comChannel = comChannel;
    }

    @Override
    public void close () throws IOException {
        this.comChannel.close();
    }

    @Override
    public void flush () throws IOException {
        comChannel.flush();
    }

    @Override
    public void write (byte[] b) throws IOException {
        this.comChannel.startWriting();
        this.comChannel.write(b);
    }

    @Override
    public void write (int b) throws IOException {
        this.comChannel.startWriting();
        this.comChannel.write(b);
    }

    @Override
    public void write (byte[] b, int off, int len) throws IOException {
        this.comChannel.startWriting();
        this.comChannel.write(b);
    }

}
