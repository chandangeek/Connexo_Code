/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.fields;

import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvRecordWrapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperNotRecoverableException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperRecovarableException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CsvField<T> {

    private final String destinationFieldName;
    private final List<String> fieldName;
    private final Integer position;
    private final boolean isRepeatble;

    /**
     * Mapping between multiple fields from CSV to single field in final Object
     *
     * @param csvFieldName
     * @param position
     * @param isRepeatble
     */
    public CsvField(String destinationFieldName, Integer position, boolean isRepeatble, String... csvFieldName) {
        super();
        this.destinationFieldName = destinationFieldName;
        this.fieldName = Arrays.asList(csvFieldName);
        this.position = position;
        this.isRepeatble = isRepeatble;
    }

    public CsvField(String destinationFieldName, int position, boolean isRepetable) {
        fieldName = new ArrayList<>();
        for (int i = 1; i <= position; i++) {
            fieldName.add("generatedFieldName" + i);
        }
        this.destinationFieldName = destinationFieldName;
        this.position = position;
        this.isRepeatble = isRepetable;
    }

    public abstract T getValue(CsvRecordWrapper record) throws ObjectMapperRecovarableException, ObjectMapperNotRecoverableException;

    public boolean isRepeatable() {
        return isRepeatble;
    }

    protected List<String> getFieldNames(){
        return this.fieldName;
    }

    public String getDestinationFieldName() {
        return this.destinationFieldName;
    }

    public Integer getPosition() {
        return position;
    }
}
