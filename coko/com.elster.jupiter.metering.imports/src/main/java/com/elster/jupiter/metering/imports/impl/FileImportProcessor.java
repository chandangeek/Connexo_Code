package com.elster.jupiter.metering.imports.impl;


import com.elster.jupiter.metering.imports.impl.exceptions.ProcessorException;

public interface FileImportProcessor<T extends FileImportRecord> {

    void process(T data, FileImportLogger logger) throws ProcessorException;
}
