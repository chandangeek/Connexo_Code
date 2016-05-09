package com.elster.jupiter.orm;

import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides an implementation of the Blob interface that is backed by a File.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-09 (16:19)
 */
public final class FileBlob implements Blob {

    private BufferedInputStream in;

    public static FileBlob empty() {
        return new FileBlob();
    }

    public static FileBlob from(File file) throws FileNotFoundException {
        FileBlob blob = new FileBlob();
        blob.in = new BufferedInputStream(new FileInputStream(file));
        return blob;
    }

    private FileBlob() {
        super();
    }

    public void setFile(File file) throws FileNotFoundException {
        if (this.in != null) {
            throw new IllegalStateException("Either provide the file at construction time or use FileBlob.empty() in conjuction with this setter but you cannot have it both ways ;-)");
        }
        this.in = new BufferedInputStream(new FileInputStream(file));
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
        ByteStreams.copy(this.in, stream);
    }

    @Override
    public long length() {
        try {
            return this.in.available();
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    @Override
    public InputStream getBinaryStream() {
        return this.in;
    }

    @Override
    public InputStream getBinaryStream(long position, long length) {
        try {
            this.in.reset();
            if (this.in.available() < (position + length)) {
                throw new IllegalArgumentException("Not enough input available");
            }
            for (int i = 1; i < position; i++) {
                this.in.read();
            }
            this.in.mark(10000);
            return new LimitedBufferedInputStream(this.in, (int) length);
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("FileBlob is intended to read file contents and push it into a BLOB field, not to update files from a BLOB field");
    }

    @Override
    public OutputStream setBinaryStream() {
        throw new UnsupportedOperationException("FileBlob is intended to read file contents and push it into a BLOB field, not to update files from a BLOB field");
    }

    private class LimitedBufferedInputStream extends InputStream {
        private final BufferedInputStream actualStream;
        private int limit;

        private LimitedBufferedInputStream(BufferedInputStream actualStream, int limit) {
            this.actualStream = actualStream;
            this.limit = limit;
        }

        @Override
        public int available() throws IOException {
            return this.limit;
        }

        @Override
        public int read() throws IOException {
            if (this.limit > 0) {
                int read = this.actualStream.read();
                if (read != -1) {
                    this.limit--;
                    return read;
                } else {
                    return -1;
                }
            } else {
                return -1;
            }
        }
    }

}