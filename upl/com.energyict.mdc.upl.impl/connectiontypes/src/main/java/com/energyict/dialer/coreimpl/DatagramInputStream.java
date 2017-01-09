package com.energyict.dialer.coreimpl;

import com.energyict.mdc.upl.RuntimeEnvironment;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Optional;

/**
 * DataGramInputStream used for UDP sockets.
 * The bufferSize of the inputStream is configurable because the default size({@link #PIPE_SIZE}) is sometimes not enough.
 */
public class DatagramInputStream extends PipedInputStream {

    public static final String DataGramBufferInputStream = "DatagramInputStreamBufferSize";

    private final RuntimeEnvironment environment;
    private final PipedOutputStream pos;

    public DatagramInputStream(RuntimeEnvironment environment, PipedOutputStream pos) throws IOException {
        super(pos);
        this.environment = environment;
        this.pos = pos;
        this.buffer = new byte[this.getIntProperty(DataGramBufferInputStream, PIPE_SIZE)];
    }

    protected int getIntProperty(String key, int defaultValue) {
        Optional<String> propertyValue = this.environment.getProperty(key);
        if (propertyValue.isPresent()) {
            try {
                return Integer.parseInt(propertyValue.get());
            } catch (NumberFormatException ex) {
                // silently ignore
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public void write(byte[] data, int off, int len) throws IOException {
        pos.write(data, off, len);
    }
}
