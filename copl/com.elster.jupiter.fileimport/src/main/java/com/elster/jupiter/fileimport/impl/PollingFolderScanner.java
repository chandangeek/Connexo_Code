/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileIOException;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * FolderScanner that simply lists the files in the Folder.
 */
class PollingFolderScanner implements FolderScanner {

    private final Path directory;
    private final Predicate<Path> filter;
    private final FileUtils fileSystem;
    private final Thesaurus thesaurus;
    private final String pathMatcher;

    @Inject
    public PollingFolderScanner(Predicate<Path> filter, FileUtils fileSystem, Path directory, String pathMatcher,  Thesaurus thesaurus) {
        this.filter = filter;
        this.fileSystem = fileSystem;
        this.directory = directory;
        this.thesaurus = thesaurus;
        this.pathMatcher = pathMatcher;
    }

    @Override
    public Stream<Path> getFiles() {
    	try {
    		return directoryContent().filter(filter);
    	} catch (IOException e) {
    		throw new FileIOException(directory, e, thesaurus);
    	}
    }

    private Stream<Path> directoryContent() throws IOException {
        try(DirectoryStream<Path> stream = fileSystem.newDirectoryStream(directory, pathMatcher)) {
            Stream.Builder<Path> streamBuilder = Stream.builder();
            stream.forEach(streamBuilder::add);
            return streamBuilder.build();
        }
    }
}
