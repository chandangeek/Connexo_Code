/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;

public interface FileImportProcessor<T extends FileImportRecord> {

    void process(T data, FileImportLogger logger) throws ProcessorException;

    void complete(FileImportLogger logger);
}
