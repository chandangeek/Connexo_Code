package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;

import java.sql.SQLException;

public interface FileImportProcessor<T extends FileImportRecord> {

    void process(T data, FileImportLogger logger) throws ProcessorException;

    void complete(FileImportLogger logger);
}
