package com.elster.jupiter.fileimport;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

import java.io.IOException;

/**
 * RuntimeException to wrap IOException that occur when reading/writing files.
 */
public class FileIOException extends BaseException {

    public FileIOException(IOException cause, Thesaurus thesaurus) {
        super(ExceptionTypes.FILE_IO, buildMessage(thesaurus), cause);
    }

    private static String buildMessage(Thesaurus thesaurus) {
        return thesaurus.getFormat(MessageSeeds.FILE_IO).format();
    }
}
