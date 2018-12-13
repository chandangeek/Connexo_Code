/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv;

import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperNotRecoverableException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperRecovarableException;

import org.apache.commons.csv.CSVRecord;

public class CsvRecordWrapper {

    public final CSVRecord record;

    public CsvRecordWrapper(CSVRecord record) {
        super();
        this.record = record;
    }

    public String getValue(String fieldName) throws ObjectMapperNotRecoverableException {
        try {
            return record.get(fieldName);
        } catch (RuntimeException e) {
            throw new ObjectMapperNotRecoverableException("Requesting field from csv that does not exists");
        }
    }

    public int getSize() {
        return record.size();
    }

    public String getValue(int i) throws ObjectMapperRecovarableException {
        try {
            return record.get(i);
        } catch (RuntimeException e) {
            throw new ObjectMapperRecovarableException("Requesting csv field position that does not exist");
        }
    }

    public long getLineNumber() {
        return record.getRecordNumber();
    }

}
