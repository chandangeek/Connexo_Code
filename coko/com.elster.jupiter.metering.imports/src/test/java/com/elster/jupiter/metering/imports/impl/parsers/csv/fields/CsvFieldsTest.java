/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv.fields;

import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperException;

import org.junit.Before;
import org.junit.Test;

public class CsvFieldsTest {

    public static final String destintionField1 = "f1";
    public static final String fieldName1 = "c1";
    public static final String destinationField2 = "f2";
    public static final String fieldName2 = "c2";
    public static final String destinationField3 = "f3";
    public static final String fieldName3 = "c3";
    public static final int positionZero = 0;
    public static final int positionOne = 1;
    public static final int positionTwo = 2;

    private CsvFields csvFields;

    @Before
    public void setUp() {
        csvFields = new CsvFields();
    }

    @Test
    public void duplicateFieldPositionIsAllowed() throws ObjectMapperException {
        csvFields.add(new StringCsvField(destintionField1, fieldName1, positionZero));
        csvFields.add(new StringCsvField(destinationField2, fieldName2, positionZero));
    }

    @Test(expected = ObjectMapperException.class)
    public void repetableNotLast() throws ObjectMapperException {
        csvFields.add(new StringCsvField(destintionField1, fieldName1, positionZero));
        csvFields.add(new StringRepetableCsvField(destinationField2, positionOne));
        csvFields.add(new StringCsvField(destinationField3, fieldName3, positionTwo));
    }

    @Test
    public void noRepeatable() throws ObjectMapperException {
        csvFields.add(new StringCsvField(destintionField1, fieldName1, positionZero));
        csvFields.add(new StringCsvField(destinationField2, fieldName2, positionOne));
        csvFields.add(new StringCsvField(destinationField3, fieldName3, positionTwo));
    }

    @Test(expected = ObjectMapperException.class)
    public void duplicateDestinationFields() throws ObjectMapperException {
        csvFields.add(new StringCsvField(destintionField1, fieldName1, positionZero));
        csvFields.add(new StringCsvField(destinationField2, fieldName2, positionOne));
        csvFields.add(new StringCsvField(destinationField2, fieldName3, positionTwo));
    }

    @Test(expected = ObjectMapperException.class)
    public void duplicateSourceFieldName() throws ObjectMapperException {
        csvFields.add(new StringCsvField(destintionField1, fieldName2, positionZero));
        csvFields.add(new StringCsvField(destinationField2, fieldName2, positionOne));
    }
}
