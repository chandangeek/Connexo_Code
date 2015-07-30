package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public interface FileImportParser<T extends FileImportRecord> {

    void init(CSVParser csvParser);

    T parse(CSVRecord csvRecord) throws FileImportParserException;

}
