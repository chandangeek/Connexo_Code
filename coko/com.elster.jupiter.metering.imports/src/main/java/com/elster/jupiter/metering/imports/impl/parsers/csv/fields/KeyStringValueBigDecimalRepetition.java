/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.fields;

import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;
import com.elster.jupiter.metering.imports.impl.parsers.BigDecimalParser;
import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvRecordWrapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperNotRecoverableException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class KeyStringValueBigDecimalRepetition extends CsvField<Map<String, BigDecimal>> {

    private final BigDecimalParser bigDecimalParser;

    public KeyStringValueBigDecimalRepetition(String destinationFieldName, int position, BigDecimalParser bigDecimalParser) {
        super(destinationFieldName, position, true);
        this.bigDecimalParser = bigDecimalParser;
    }


    @Override
    public Map<String, BigDecimal> getValue(CsvRecordWrapper record) {
        Map<String, BigDecimal> localMap = new HashMap<>();
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

    protected BigDecimal validateValue(String value) throws ObjectMapperException {
        try {
            return bigDecimalParser.parse(value);
        } catch (ValueParserException e) {
            throw new ObjectMapperNotRecoverableException(e);
        }
    }
}
