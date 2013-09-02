package com.elster.jupiter.fileimport.impl;

import java.io.File;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

public interface FileSystem {

    InputStream getInputStream(File file);

    Path move(Path source, Path target);

    DirectoryStream<Path> newDirectoryStream(Path directory);

    boolean exists(Path path);
}
