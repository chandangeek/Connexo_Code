/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public static FileBlob from(Path path) {
        try {
            return from(Files.newInputStream(path));
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    public static FileBlob from(InputStream inputStream) {
        FileBlob blob = new FileBlob();
        blob.in = new BufferedInputStream(inputStream);
        return blob;
    }

    private FileBlob() {
        super();
    }

    public void setFile(Path path) {
        if (this.in != null) {
            throw new IllegalStateException("Either provide the path at construction time or use FileBlob.empty() in conjuction with this setter but you cannot have it both ways ;-)");
        }
        try {
            this.in = new BufferedInputStream(Files.newInputStream(path));
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
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
    public void clear() {
        throw new UnsupportedOperationException("FileBlob is intended to read file contents and push it into a BLOB field, not to update files from a BLOB field");
    }

    @Override
    public OutputStream setBinaryStream() {
        throw new UnsupportedOperationException("FileBlob is intended to read file contents and push it into a BLOB field, not to update files from a BLOB field");
    }

}