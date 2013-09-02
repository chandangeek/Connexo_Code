package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileIOException;
import com.elster.jupiter.util.To;
import com.google.common.collect.FluentIterable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

public class PollingFolderScanner implements FolderScanner {

    private final Path directory;

    public PollingFolderScanner(Path directory) {
        this.directory = directory;
    }

    @Override
    public Iterator<File> getFiles() {
        try {
            return directoryContent().filter(Bus.getPredicates().onlyFiles()).transform(To.FILE).iterator();
        } catch (IOException e) {
            throw new FileIOException(e);
        }
    }

    private FluentIterable<Path> directoryContent() throws IOException {
        return FluentIterable.from(Bus.getFileSystem().newDirectoryStream(directory));
    }

}
