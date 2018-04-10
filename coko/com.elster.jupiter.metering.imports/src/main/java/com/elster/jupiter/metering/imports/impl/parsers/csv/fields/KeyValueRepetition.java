/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.fields;

import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvRecordWrapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperException;

import java.util.HashMap;
import java.util.Map;

public class KeyValueRepetition extends CsvField<Map<String, String>> {

    public KeyValueRepetition(String destinationFieldName, int position) {
        super(destinationFieldName, position, true);
    }

    @Override
    public Map<String, String> getValue(CsvRecordWrapper record) {
        Map<String, String> localMap = new HashMap<>();
        for (int i = super.getPosition(); i < record.getSize() - 1; i += 2) {
            try {
                localMap.put(validateKey(record.getValue(i)), validateValue(record.getValue(i + 1)));
            } catch (ObjectMapperException e) {
                // nothing to do while key or value is invalid ... just log it
            }
        }
        return localMap;
    }

    protected String validateKey(String key) throws ObjectMapperException {
        return key;
    }

    protected String validateValue(String value) throws ObjectMapperException {
        return value;
    }
}
