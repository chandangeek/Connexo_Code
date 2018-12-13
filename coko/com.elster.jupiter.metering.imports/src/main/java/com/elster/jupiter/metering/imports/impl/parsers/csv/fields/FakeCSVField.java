/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.fields;

import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvRecordWrapper;

public class FakeCSVField extends CsvField<Long> {
    public FakeCSVField(String destinationFieldName) {
        super(destinationFieldName, 0, false);
    }

    @Override
    public Long getValue(CsvRecordWrapper record) {
        return record.getLineNumber();
    }
}
