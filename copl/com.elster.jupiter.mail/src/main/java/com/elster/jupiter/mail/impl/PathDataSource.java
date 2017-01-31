/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathDataSource implements DataSource {
    private Path path;
    private FileTypeMap typeMap;

    public PathDataSource(Path file) {
        this.path = null;
        this.typeMap = null;
        this.path = file;
    }

    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(path);
    }

    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(path);
    }

    public String getContentType() {
        return this.typeMap == null ? FileTypeMap.getDefaultFileTypeMap().getContentType(this.path.toString()) : this.typeMap.getContentType(this.path.toString());
    }

    public String getName() {
        return this.path.getFileName().toString();
    }

    public void setFileTypeMap(FileTypeMap map) {
        this.typeMap = map;
    }
}
