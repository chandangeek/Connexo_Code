/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.fields;

import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperInitException;

import java.util.ArrayList;
import java.util.List;

public class CsvFields {
    private final List<CsvField<?>> csvFields = new ArrayList<>();


    public void add(CsvField<?> csvField) throws ObjectMapperInitException {
        for (CsvField<?> declaredField : csvFields) {
            if (declaredField.getDestinationFieldName().equals(csvField.getDestinationFieldName()) ||
                    declaredField.getFieldNames().equals(csvField.getFieldNames())) {
                throw new ObjectMapperInitException("Duplicate csv field declaration");
            }
        }
        if (!csvFields.isEmpty()) {
            if (csvFields.get(csvFields.size() -1).isRepeatable()) {
                throw new ObjectMapperInitException("Repteable should be only on last position");
            }
        }
        this.csvFields.add(csvField);
    }

    public List<CsvField<?>> getFields() throws ObjectMapperInitException {
        if (csvFields.isEmpty()) {
            throw new ObjectMapperInitException("Requesting empty list fields");
        }
        return csvFields;
    }

}
