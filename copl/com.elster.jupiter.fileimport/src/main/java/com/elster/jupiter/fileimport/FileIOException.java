package com.elster.jupiter.fileimport;

import com.elster.jupiter.util.exception.BaseException;

import java.io.IOException;

public class FileIOException extends BaseException {

    public FileIOException(IOException cause) {
        super(ExceptionTypes.FILE_IO, cause);
    }
}
