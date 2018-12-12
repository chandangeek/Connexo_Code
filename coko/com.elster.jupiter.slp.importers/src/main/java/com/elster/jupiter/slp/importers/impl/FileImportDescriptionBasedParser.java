/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;

import com.elster.jupiter.fileimport.csvimport.FileImportDescription;
import com.elster.jupiter.fileimport.csvimport.FileImportParser;
import com.elster.jupiter.fileimport.csvimport.FileImportRecord;
import com.elster.jupiter.fileimport.csvimport.exceptions.FileImportLineException;
import com.elster.jupiter.fileimport.csvimport.exceptions.FileImportParserException;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.elster.jupiter.util.Checks;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileImportDescriptionBasedParser<T extends FileImportRecord> implements FileImportParser<T> {

    private final FileImportDescription<T> descriptor;
    private final SyntheticLoadProfileDataImporterContext context;
    private List<String> headers;

    public FileImportDescriptionBasedParser(FileImportDescription<T> descriptor, SyntheticLoadProfileDataImporterContext context) {
        this.descriptor = descriptor;
        this.context = context;
    }

    public void init(CSVParser csvParser) {
        initHeaders(csvParser);
    }

    @Override
    public T parse(CSVRecord csvRecord) throws FileImportParserException {
        T record = this.descriptor.getFileImportRecord();
        record.setLineNumber(csvRecord.getRecordNumber());
        Map<String, FileImportField<?>> fields = this.descriptor.getFields(record);
        List<String> rawValues = getRawValuesSkipTrailingNulls(csvRecord);
        if (rawValues.size() < getNumberOfMandatoryColumns(fields)) {
            throw new FileImportLineException(csvRecord.getRecordNumber(), MessageSeeds.FILE_FORMAT_ERROR, csvRecord.getRecordNumber(),
                    getNumberOfMandatoryColumns(fields), rawValues.size());
        }

        return record;
    }

    private void initHeaders(CSVParser parser) {
        headers = parser.getHeaderMap().entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null)
                .sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        long numberOfMandatoryColumns = getNumberOfMandatoryColumns();
        if (headers.size() < numberOfMandatoryColumns) {
            throw new FileImportParserException(MessageSeeds.MISSING_TITLE_ERROR, numberOfMandatoryColumns, headers.size());
        }
    }

    private long getNumberOfMandatoryColumns() {
        return getNumberOfMandatoryColumns(this.descriptor.getFields(this.descriptor.getFileImportRecord()));
    }

    private long getNumberOfMandatoryColumns(Map<String, FileImportField<?>> fields) {
        return fields.values().stream().filter(FileImportField::isMandatory).count();
    }

    private List<String> getRawValuesSkipTrailingNulls(CSVRecord csvRecord) {
        List<String> rawValues = new ArrayList<>();
        for (int i = csvRecord.size() - 1; i >= 0; i--) {//reverse bypass
            String value = csvRecord.get(i);
            if (!Checks.is(value).empty() || !rawValues.isEmpty()) {
                rawValues.add(value);
            }
        }
        Collections.reverse(rawValues);
        return rawValues;
    }

}