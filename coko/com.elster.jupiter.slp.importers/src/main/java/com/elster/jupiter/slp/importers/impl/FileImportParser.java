/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;

import com.elster.jupiter.slp.importers.impl.exceptions.FileImportParserException;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public interface FileImportParser<T extends FileImportRecord> {

    void init(CSVParser csvParser);

    T parse(CSVRecord csvRecord) throws FileImportParserException;

}
