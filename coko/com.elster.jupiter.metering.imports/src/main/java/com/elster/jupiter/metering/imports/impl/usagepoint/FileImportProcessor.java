package com.elster.jupiter.metering.imports.impl.usagepoint;


import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ProcessorException;

public interface FileImportProcessor<T extends FileImportRecord> {

    void process(T data, FileImportLogger logger) throws ProcessorException;

    void complete(FileImportLogger logger);
}
