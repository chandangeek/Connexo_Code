/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.metering.imports.impl.exceptions.FileImportParserException;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public interface FileImportParser<T extends FileImportRecord> {

    void init(CSVParser csvParser);

    T parse(CSVRecord csvRecord) throws FileImportParserException;

}
