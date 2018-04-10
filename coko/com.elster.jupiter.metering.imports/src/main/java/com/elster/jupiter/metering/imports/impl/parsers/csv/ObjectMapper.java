/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv;

import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperInitException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperNotRecoverableException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperRecovarableException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.CsvField;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.CsvFields;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class ObjectMapper<T> {
    private final Supplier<T> supplier;
    private final Class<T> persistentClass;
    private final CsvFields csvFields = new CsvFields();

    @SuppressWarnings("unchecked")
    public ObjectMapper(Supplier<T> supplier) {
        this.supplier = supplier;
        this.persistentClass = (Class<T>) supplier.get().getClass();
    }

    public ObjectMapper<T> add(CsvField<?> csvField) throws ObjectMapperInitException {
        if (csvField == null) {
            throw new ObjectMapperInitException("Refusing to add null csv field");
        }
        checkFieldExistence(csvField.getDestinationFieldName());
        csvFields.add(csvField);
        return this;
    }


    private void checkFieldExistence(String destinationFieldName) throws ObjectMapperInitException {
        try {
            persistentClass.getDeclaredField(destinationFieldName);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new ObjectMapperInitException(e);
        }
    }


    public T getObject(CsvRecordWrapper csvRecord) throws ObjectMapperNotRecoverableException, ObjectMapperRecovarableException, ObjectMapperInitException {
        T obj = getInstance();
        try {
            for (CsvField<?> csvField : csvFields.getFields()) {

                Field field = obj.getClass().getDeclaredField(csvField.getDestinationFieldName());
                field.setAccessible(true);
                field.set(obj, csvField.getValue(csvRecord));
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new ObjectMapperNotRecoverableException(e);
        }


        return obj;
    }


    private T getInstance() {
        return supplier.get();
    }

}
