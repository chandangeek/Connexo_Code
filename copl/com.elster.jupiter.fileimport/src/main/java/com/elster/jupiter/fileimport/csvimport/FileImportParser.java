/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.csvimport;

import com.elster.jupiter.fileimport.csvimport.exceptions.FileImportParserException;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public interface FileImportParser<T extends FileImportRecord> {

    void init(CSVParser csvParser);

    T parse(CSVRecord csvRecord) throws FileImportParserException;

}
