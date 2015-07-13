package com.elster.jupiter.ftpclient.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

class FtpWritableByteChannel implements SeekableByteChannel {

    private final FtpPath path;
    private final OutputStream outputStream;
    private final WritableByteChannel writableByteChannel;
    private long position = 0;
    private boolean open = true;

    private FtpWritableByteChannel(FtpPath path, boolean append) throws IOException {
        this.path = path;
        outputStream = append ? path.getFileSystem().openOutputStreamToAppend(path) : path.getFileSystem().openOutputStream(path);
        writableByteChannel = Channels.newChannel(outputStream);
    }

    static FtpWritableByteChannel toAppend(FtpPath path) throws IOException {
        return new FtpWritableByteChannel(path, true);
    }

    static FtpWritableByteChannel toOverwrite(FtpPath path) throws IOException {
        return new FtpWritableByteChannel(path, false);
    }
    @Override
    public int read(ByteBuffer dst) throws IOException {
        throw new NonReadableChannelException();
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int write = writableByteChannel.write(src);
        if (write >= 0) {
            position += write;
        }
        return write;
    }

    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long size() throws IOException {
        return path.size();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new NonWritableChannelException();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        open = false;
        outputStream.close();
    }
}
