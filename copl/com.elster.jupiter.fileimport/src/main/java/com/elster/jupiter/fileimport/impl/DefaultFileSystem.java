package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileIOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultFileSystem implements FileSystem {

    @Override
    public InputStream getInputStream(File file) throws FileIOException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new FileIOException(e);
        }
    }

    @Override
    public Path move(Path source, Path target) {
        try {
            return Files.move(source, target);
        } catch (IOException e) {
            throw new FileIOException(e);
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path directory) {
        try {
            return Files.newDirectoryStream(directory);
        } catch (IOException e) {
            throw new FileIOException(e);
        }
    }

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }
}
