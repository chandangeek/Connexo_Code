/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;


import com.elster.jupiter.slp.importers.impl.exceptions.ProcessorException;

public interface FileImportProcessor<T extends FileImportRecord> {

    void process(T data, FileImportLogger logger) throws ProcessorException;

    void complete(FileImportLogger logger);
}
