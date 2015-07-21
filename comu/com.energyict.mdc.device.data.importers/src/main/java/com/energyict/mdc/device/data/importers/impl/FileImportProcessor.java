package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;

public interface FileImportProcessor<T extends FileImportRecord> {

    void process(T data, FileImportRecordContext context) throws ProcessorException;

}
