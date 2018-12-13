/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.io.IOException;

public class FileIOException extends LocalizedException {

    public FileIOException(IOException cause, Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.FILE_IO, cause);
    }
}