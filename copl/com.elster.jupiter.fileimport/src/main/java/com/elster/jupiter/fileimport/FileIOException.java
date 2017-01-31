/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import com.elster.jupiter.fileimport.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.io.IOException;
import java.nio.file.Path;

/**
 * RuntimeException to wrap IOException that occur when reading/writing files.
 */
public class FileIOException extends LocalizedException {

    public FileIOException(Path file, IOException cause, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.FILE_IO, file.toAbsolutePath(), cause);
    }
}
