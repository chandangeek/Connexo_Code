package com.energyict.dialer.coreimpl;

import com.energyict.cpo.Environment;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * DataGramInputStream used for UDP sockets.
 * The bufferSize of the inputStream is configurable because the default size({@link #PIPE_SIZE}) is sometimes not enough.
 */
public class DatagramInputStream extends PipedInputStream {

    public static final String DataGramBufferInputStream = "DatagramInputStreamBufferSize";

    PipedOutputStream pos = null;

    public DatagramInputStream(PipedOutputStream pos) throws IOException {
        super(pos);
        this.pos = pos;
        try {
            String buffSize = Environment.getDefault().getProperty(DataGramBufferInputStream);
            if (buffSize != null) {
                this.buffer = new byte[Integer.valueOf(buffSize)];
            } else {
                this.buffer = new byte[PIPE_SIZE];
            }
        } catch (NumberFormatException e) {
            this.buffer = new byte[PIPE_SIZE];
        }
    }

    public void write(byte[] data, int off, int len) throws IOException {
        pos.write(data, off, len);
    }
}
