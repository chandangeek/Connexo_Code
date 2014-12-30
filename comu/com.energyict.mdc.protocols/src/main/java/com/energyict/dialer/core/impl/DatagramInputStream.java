package com.energyict.dialer.core.impl;

import com.energyict.protocols.mdc.services.impl.EnvironmentPropertyService;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * DataGramInputStream used for UDP sockets.
 * The bufferSize of the inputStream is configurable because the default size({@link #PIPE_SIZE}) is sometimes not enough.
 */
public class DatagramInputStream extends PipedInputStream {

    private final PipedOutputStream pos;

    public DatagramInputStream(PipedOutputStream pos, EnvironmentPropertyService propertyService) throws IOException {
        super(pos);
        this.pos = pos;
        int bufferSize = propertyService.getDatagramInputStreamBufferSize();
        this.buffer = new byte[bufferSize];
    }

    public void write(byte[] data, int off, int len) throws IOException {
        pos.write(data, off, len);
    }

}