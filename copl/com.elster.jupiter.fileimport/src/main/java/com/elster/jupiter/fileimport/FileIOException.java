package com.elster.jupiter.fileimport;

import com.elster.jupiter.fileimport.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.io.IOException;

/**
 * RuntimeException to wrap IOException that occur when reading/writing files.
 */
public class FileIOException extends LocalizedException {

    public FileIOException(IOException cause, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.FILE_IO, cause);
    }
}
