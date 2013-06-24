package com.elster.jupiter.fileimport;

import java.io.IOException;

public class FileIOException extends RuntimeException {

    public FileIOException(IOException cause) {
        super(cause);
    }
}
