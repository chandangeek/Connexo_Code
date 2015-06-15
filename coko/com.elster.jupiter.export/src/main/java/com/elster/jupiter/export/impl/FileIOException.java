package com.elster.jupiter.export.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.io.IOException;
import java.nio.file.Path;

/**
 * RuntimeException to wrap IOException that occur when reading/writing files.
 */
public class FileIOException extends LocalizedException {

    public FileIOException(Thesaurus thesaurus, Path path, IOException cause) {
        super(thesaurus, MessageSeeds.FILE_IO, cause, path.toAbsolutePath().toString());
    }
}
