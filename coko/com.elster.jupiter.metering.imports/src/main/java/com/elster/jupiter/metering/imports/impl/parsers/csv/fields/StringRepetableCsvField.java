/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.fields;

import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvRecordWrapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperRecovarableException;

import java.util.ArrayList;
import java.util.List;

public class StringRepetableCsvField extends CsvField<List<String>> {

    public StringRepetableCsvField(String destinationFieldName, int position) {
        super(destinationFieldName, position, true, "generatedFieldName");
    }

    @Override
    public List<String> getValue(CsvRecordWrapper record) throws ObjectMapperRecovarableException {
        List<String> values = new ArrayList<>();
        for (int i = super.getPosition(); i <= record.getSize(); i++) {
            values.add(record.getValue(i));
        }
        return values;
    }
}
