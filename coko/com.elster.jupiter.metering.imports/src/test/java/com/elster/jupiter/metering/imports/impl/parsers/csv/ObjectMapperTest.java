/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.parsers.csv;

import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.FakeCSVField;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.KeyValueRepetition;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.StringCsvField;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ObjectMapperTest {

    @Mock
    private CsvRecordWrapper csvRecord;

    @Test(expected = ObjectMapperException.class)
    public void testAddNullCsvField() throws ObjectMapperException {
        ObjectMapper<?> objMapper = new ObjectMapper<>(TestObject::new);
        objMapper.add(null);
    }

    @Test(expected = ObjectMapperException.class)
    public void testAddWrongField() throws ObjectMapperException {
        ObjectMapper<TestObject> objMapper = new ObjectMapper<>(TestObject::new);
        String csvFieldName = "mda";
        objMapper.add(new StringCsvField("nonExistingField", csvFieldName, 0));
    }

    @Test
    public void testSingleStringObjectMapper() throws ObjectMapperException {
        ObjectMapper<TestObject> objMapper = new ObjectMapper<>(TestObject::new);
        String csvFieldName = "csvFieldName";
        objMapper.add(new StringCsvField("usagePointName", csvFieldName, 0));
        String expected = "usp";
        when(csvRecord.getValue(csvFieldName)).thenReturn(expected);
        TestObject object = objMapper.getObject(csvRecord);
        assertEquals(expected, object.getUsagePointName());
    }

    @Test
    public void testComplexObjectMapper() throws ObjectMapperException {
        ObjectMapper<TestObject> objMapper = new ObjectMapper<>(TestObject::new);
        String csvFieldName1 = "usagePointName";
        String csvFieldName2 = "date";
        String csvFieldName3 = "purpose";

        objMapper.add(new StringCsvField(csvFieldName1, csvFieldName1, 0));
        objMapper.add(new StringCsvField(csvFieldName2, csvFieldName2, 1));
        objMapper.add(new StringCsvField(csvFieldName3, csvFieldName3, 2));
        objMapper.add(new FakeCSVField("lineNumber"));
        objMapper.add(new KeyValueRepetition("mapOfReadingTypeAndValue", 3));

        String expectedUsagePointName = "un usage point";
        String expectedUsagePointDate = "12.12.2019";
        String expectedUsagePointPurpose = "billing";
        String expectedUsagePointReadingType = "10.10.10";
        String expectedUsagePointReadingTypeValue = "20.20.20.20";
        long expectedLineNumber = 1L;

        when(csvRecord.getValue(csvFieldName1)).thenReturn(expectedUsagePointName);
        when(csvRecord.getValue(csvFieldName2)).thenReturn(expectedUsagePointDate);
        when(csvRecord.getValue(csvFieldName3)).thenReturn(expectedUsagePointPurpose);
        when(csvRecord.getValue(3)).thenReturn(expectedUsagePointReadingType);
        when(csvRecord.getValue(4)).thenReturn(expectedUsagePointReadingTypeValue);
        when(csvRecord.getSize()).thenReturn(5);
        when(csvRecord.getLineNumber()).thenReturn(expectedLineNumber);

        TestObject object = objMapper.getObject(csvRecord);
        assertEquals(expectedUsagePointName, object.getUsagePointName());
        assertEquals(expectedUsagePointDate, object.getDate());
        assertEquals(expectedUsagePointPurpose, object.getPurpose());
        assertEquals(expectedLineNumber, object.getLineNumber());
        assertTrue(object.mapOfReadingTypeAndValue.containsKey(expectedUsagePointReadingType));
        assertTrue(object.mapOfReadingTypeAndValue.containsValue(expectedUsagePointReadingTypeValue));

    }

    private class TestObject {
        private String usagePointName;
        private String date;
        private String purpose;
        private long lineNumber;
        private Map<String, String> mapOfReadingTypeAndValue;

        public String getUsagePointName() {
            return usagePointName;
        }

        public void setUsagePointName(String usagePointName) {
            this.usagePointName = usagePointName;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public long getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(long lineNumber) {
            this.lineNumber = lineNumber;
        }

        public Map<String, String> getMapOfReadingTypeAndValue() {
            return mapOfReadingTypeAndValue;
        }

        public void setMapOfReadingTypeAndValue(Map<String, String> mapOfReadingTypeAndValue) {
            this.mapOfReadingTypeAndValue = mapOfReadingTypeAndValue;
        }
    }
}
