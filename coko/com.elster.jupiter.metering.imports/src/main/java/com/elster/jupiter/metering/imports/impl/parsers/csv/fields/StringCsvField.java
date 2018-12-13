/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.fields;

import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvRecordWrapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperNotRecoverableException;

public class StringCsvField extends CsvField<String> {

    public StringCsvField(String destinationFieldName, String csvFieldName, int position) {
        super(destinationFieldName, position, false, csvFieldName);
    }

    @Override
    public String getValue(CsvRecordWrapper record) throws ObjectMapperNotRecoverableException {
        return record.getValue(super.getFieldNames().get(0));

    }
}
