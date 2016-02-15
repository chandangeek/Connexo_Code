package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.FileImportParserException;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public interface FileImportParser<T extends FileImportRecord> {

    void init(CSVParser csvParser);

    T parse(CSVRecord csvRecord) throws FileImportParserException;

}
