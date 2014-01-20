package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileIOException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Predicates;
import com.elster.jupiter.util.To;
import com.google.common.collect.FluentIterable;

import javax.inject.Inject;
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
    private final Thesaurus thesaurus;

    @Inject
    public PollingFolderScanner(Predicates predicates, FileSystem fileSystem, Path directory, Thesaurus thesaurus) {
        this.predicates = predicates;
        this.fileSystem = fileSystem;
        this.directory = directory;
        this.thesaurus = thesaurus;
    }

    @Override
    public Iterator<File> getFiles() {
        try {
            return directoryContent().filter(predicates.onlyFiles()).transform(To.FILE).iterator();
        } catch (IOException e) {
            throw new FileIOException(e, thesaurus);
        }
    }

    private FluentIterable<Path> directoryContent() throws IOException {
        return FluentIterable.from(fileSystem.newDirectoryStream(directory));
    }

}
