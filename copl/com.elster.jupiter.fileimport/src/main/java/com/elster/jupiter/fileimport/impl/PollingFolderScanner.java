package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileIOException;
import com.elster.jupiter.util.Predicates;
import com.elster.jupiter.util.To;
import com.google.common.collect.FluentIterable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * FolderScanner that simply lists the files in the Folder.
 */
class PollingFolderScanner implements FolderScanner {

    private final Path directory;
    private final Predicates predicates;
    private final FileSystem fileSystem;

    public PollingFolderScanner(Predicates predicates, FileSystem fileSystem, Path directory) {
        this.predicates = predicates;
        this.fileSystem = fileSystem;
        this.directory = directory;
    }

    @Override
    public Iterator<File> getFiles() {
        try {
            return directoryContent().filter(predicates.onlyFiles()).transform(To.FILE).iterator();
        } catch (IOException e) {
            throw new FileIOException(e);
        }
    }

    private FluentIterable<Path> directoryContent() throws IOException {
        return FluentIterable.from(fileSystem.newDirectoryStream(directory));
    }

}
