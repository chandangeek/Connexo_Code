package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import org.apache.commons.csv.CSVRecord;

public interface FileImportParser<T extends FileImportRecord>{

    T parse(CSVRecord csvRecord) throws FileImportParserException;

}
