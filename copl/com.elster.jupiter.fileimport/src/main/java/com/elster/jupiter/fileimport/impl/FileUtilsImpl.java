package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileIOException;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

class FileUtilsImpl implements FileUtils {

    private final Thesaurus thesaurus;

    @Inject
    FileUtilsImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public InputStream getInputStream(Path file) throws FileIOException {
        try {
            return Files.newInputStream(file);
        } catch (FileNotFoundException e) {
            throw new FileIOException(e, thesaurus);
        } catch (IOException e) {
            throw new FileIOException(e, thesaurus);
        }
    }

    @Override
    public Path move(Path source, Path target) {
        try {
            return Files.move(source, target);
        } catch (IOException e) {
            throw new FileIOException(e, thesaurus);
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path directory, String pathMatcher) {
        try {
            return Files.newDirectoryStream(directory, (pathMatcher!=null && !pathMatcher.isEmpty()) ? pathMatcher : "*");
        } catch (IOException e) {
            throw new FileIOException(e, thesaurus);
        }
    }

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }
}
