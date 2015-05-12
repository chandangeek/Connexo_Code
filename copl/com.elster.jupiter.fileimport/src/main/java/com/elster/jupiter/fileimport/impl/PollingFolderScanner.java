package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileIOException;
import com.elster.jupiter.nls.Thesaurus;
import javax.inject.Inject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * FolderScanner that simply lists the files in the Folder.
 */
class PollingFolderScanner implements FolderScanner {

    private final Path directory;
    private final Predicate<Path> filter;
    private final FileSystem fileSystem;
    private final Thesaurus thesaurus;
    private final String pathMatcher;

    @Inject
    public PollingFolderScanner(Predicate<Path> filter, FileSystem fileSystem, Path directory, String pathMatcher,  Thesaurus thesaurus) {
        this.filter = filter;
        this.fileSystem = fileSystem;
        this.directory = directory;
        this.thesaurus = thesaurus;
        this.pathMatcher = pathMatcher;
    }

    @Override
    public Stream<File> getFiles() {
    	try {
    		return directoryContent().filter(filter).map(path -> path.toFile());
    	} catch (IOException e) {
    		throw new FileIOException(e, thesaurus);
    	}
    }

    private Stream<Path> directoryContent() throws IOException {
        return StreamSupport.stream(fileSystem.newDirectoryStream(directory, pathMatcher).spliterator(),false);
    }

}
