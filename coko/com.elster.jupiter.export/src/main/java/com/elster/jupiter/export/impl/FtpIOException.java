package com.elster.jupiter.export.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.io.IOException;

/**
 * RuntimeException to wrap IOException that occur when reading/writing files.
 */
public class FtpIOException extends LocalizedException {

    public FtpIOException(Thesaurus thesaurus, String server, int port, IOException cause) {
        super(thesaurus, MessageSeeds.FILE_IO, cause, server, port);
    }
}
