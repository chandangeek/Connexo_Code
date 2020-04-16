/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.exceptions.FileImportLineException;
import com.elster.jupiter.fileimport.csvimport.exceptions.FileImportParserException;
import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;
import com.elster.jupiter.fileimport.csvimport.fields.FieldSetter;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.elster.jupiter.util.Checks;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileImportDescriptionBasedParser<T extends FileImportRecord> implements FileImportParser<T> {

    private final FileImportDescription<T> descriptor;
    protected List<String> headers;

    public FileImportDescriptionBasedParser(FileImportDescription<T> descriptor) {
        this.descriptor = descriptor;
    }

    public void init(CSVParser csvParser) {
        initHeaders(csvParser);
    }

    @Override
    public T parse(CSVRecord csvRecord) throws FileImportParserException {
        checkRecordConsictency(csvRecord);
        T record = this.descriptor.getFileImportRecord();
        record.setLineNumber(csvRecord.getRecordNumber());
        List<FileImportField<?>> fields = new ArrayList<>(this.descriptor.getFields(record).values());
        List<String> rawValues = getRawValuesSkipTrailingNulls(csvRecord);
        if (rawValues.size() < getNumberOfMandatoryColumns(fields)) {
            throw new FileImportLineException(csvRecord.getRecordNumber(), MessageSeeds.FILE_FORMAT_ERROR, csvRecord.getRecordNumber(),
                    getNumberOfMandatoryColumns(fields), rawValues.size());
        }
        int repetitiveColumnCount = 0;
        for (int i = 0; i < rawValues.size() && (i < fields.size() || repetitiveColumnCount > 0); i++) {
            String rawValue = rawValues.get(i);
            int currentFieldIdx = i < fields.size() ? i : fields.size() - repetitiveColumnCount + (i - fields.size()) % repetitiveColumnCount;
            FileImportField<?> currentField = fields.get(currentFieldIdx);
            if (i < fields.size() && currentField.isRepetitive()) {
                repetitiveColumnCount++;
            }
            if (currentField.isMandatory() && Checks.is(rawValue).emptyOrOnlyWhiteSpace()) {
                throw new FileImportLineException(csvRecord.getRecordNumber(), MessageSeeds.LINE_MISSING_VALUE_ERROR, csvRecord.getRecordNumber(), getHeader(i));
            }
            try {
                FieldSetter fieldSetter = currentField.getSetter();
                FieldParser parser = currentField.getParser();
                fieldSetter.setFieldWithHeader(getHeader(i), parser.parse(rawValue));
            } catch (ValueParserException ex) {
                throw new FileImportParserException(MessageSeeds.LINE_FORMAT_ERROR, csvRecord.getRecordNumber(), getHeader(i), ex.getExpected());
            }
        }
        return record;
    }

    private void initHeaders(CSVParser parser) {
        headers = parser.getHeaderMap().entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null)
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        long numberOfMandatoryColumns = getNumberOfMandatoryColumns();
        if (headers.size() < numberOfMandatoryColumns) {
            throw new FileImportParserException(MessageSeeds.MISSING_TITLE_ERROR, numberOfMandatoryColumns, headers.size());
        }
    }

    protected void checkRecordConsictency(CSVRecord csvRecord){
        if (!csvRecord.isConsistent()) {
            throw new FileImportLineException(csvRecord.getRecordNumber(), MessageSeeds.WRONG_LINE_SIZE, csvRecord.getRecordNumber());
        }
    }

    private long getNumberOfMandatoryColumns() {
        return getNumberOfMandatoryColumns(this.descriptor.getFields(this.descriptor.getFileImportRecord()).values());
    }

    private long getNumberOfMandatoryColumns(Collection<FileImportField<?>> fields) {
        return fields
                .stream()
                .filter(FileImportField::isMandatory)
                .count();
    }

    private String getHeader(int position) {
        if (position >= 0 && position < this.headers.size()) {
            return this.headers.get(position);
        }
        return getColumnIdentifier(position);
    }

    private String getColumnIdentifier(int position) {
        return "#" + (position + 1);
    }

    private List<String> getRawValuesSkipTrailingNulls(CSVRecord csvRecord) {
        List<String> rawValues = new ArrayList<>();
        for (int i = csvRecord.size() - 1; i >= 0; i--) {//reverse bypass
            String value = csvRecord.get(i);
            if ((!Checks.is(value).empty() || !rawValues.isEmpty()) || (!this.descriptor.isSkipTrailingNulls() && i < headers.size())) {
                rawValues.add(value);
            }
        }
        Collections.reverse(rawValues);
        return rawValues;
    }
}
