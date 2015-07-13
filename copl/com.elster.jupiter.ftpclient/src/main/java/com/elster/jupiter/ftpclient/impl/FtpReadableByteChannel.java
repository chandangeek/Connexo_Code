package com.elster.jupiter.ftpclient.impl;

import com.enterprisedt.net.ftp.pro.ProFTPClientInterface;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

class FtpReadableByteChannel implements SeekableByteChannel {

    private final FtpPath path;
    private final InputStream inputStream;
    private final ReadableByteChannel readableByteChannel;
    private long position = 0;
    private boolean open = true;

    FtpReadableByteChannel(ProFTPClientInterface ftpClient, FtpPath path) throws IOException {
        this.path = path;
        inputStream = path.getFileSystem().openInputStream(path);
        readableByteChannel = Channels.newChannel(inputStream);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int read = readableByteChannel.read(dst);
        if (read >= 0) {
            position += read;
        }
        return read;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new NonWritableChannelException();
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
        inputStream.close();
    }
}
