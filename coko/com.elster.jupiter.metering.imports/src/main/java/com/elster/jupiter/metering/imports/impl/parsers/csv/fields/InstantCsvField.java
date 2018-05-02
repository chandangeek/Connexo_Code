/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.fields;

import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;
import com.elster.jupiter.metering.imports.impl.parsers.InstantParser;
import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvRecordWrapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperNotRecoverableException;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;

import java.time.Instant;

public class InstantCsvField extends CsvField<Instant> {
    private SupportedNumberFormat supportedNumberFormat;
    private String format;
    private String timeZone;
    private InstantParser parser;

    public InstantCsvField(String destinationFieldName, String csvFieldName, int position, InstantParser parser) {
        super(destinationFieldName, position, false, csvFieldName);
        this.parser = parser;
        this.format = format;
        this.timeZone = timeZone;
    }

    @Override
    public Instant getValue(CsvRecordWrapper record) throws ObjectMapperNotRecoverableException {
        try {
            return parser.parse(record.getValue(super.getFieldNames().get(0)));
        } catch (ValueParserException e) {
            throw new ObjectMapperNotRecoverableException(e);
        }
    }
}
