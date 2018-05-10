/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv;

import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperNotRecoverableException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CsvParserWrapper {

    private CSVParser csvParser;

    public CsvParserWrapper(InputStream inputStream, String delimiter, char commentMarker) throws ObjectMapperNotRecoverableException {
        try {
            this.csvParser = init(inputStream, delimiter, commentMarker);
        } catch (IOException e) {
            throw new ObjectMapperNotRecoverableException(e);
        }
    }

    private CSVParser init(InputStream inputStream, String delimiter, char commentMarker) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader()
                .withAllowMissingColumnNames(true)
                .withIgnoreSurroundingSpaces(true)
                .withDelimiter(delimiter.charAt(0))
                .withCommentMarker(commentMarker);
        return new CSVParser(new InputStreamReader(inputStream), csvFormat);
    }

    public CSVParser getCsvParser() {
        return csvParser;
    }
}
