package com.elster.jupiter.fileimport;

import com.elster.jupiter.util.exception.BaseException;

import java.io.IOException;

/**
 * RuntimeException to wrap IOException that occur when reading/writing files.
 */
public class FileIOException extends BaseException {

    public FileIOException(IOException cause) {
        super(ExceptionTypes.FILE_IO, cause);
    }
}
