package com.elster.jupiter.fileimport.impl;

import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;


/**
 * Abstraction of the file system.
 */
interface FileSystem {

    /**
     * Opens an InputStream on the contents of the given File
     * @param file
     * @return an InputStream
     */
    InputStream getInputStream(Path file);

    /**
     * Moves the file at source to the directory at target.
     * @param source
     * @param target
     * @return
     */
    Path move(Path source, Path target);

    /**
     * @param directory
     * @return a new DirectoryStream on the given directory
     */
    DirectoryStream<Path> newDirectoryStream(Path directory, String pathMatcher);

    /**
     * @param path
     * @return true if the path exists, false otherwise
     */
    boolean exists(Path path);
}
