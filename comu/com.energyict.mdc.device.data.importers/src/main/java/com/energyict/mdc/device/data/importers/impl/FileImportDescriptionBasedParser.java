package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;
import com.energyict.mdc.device.data.importers.impl.fields.FieldSetter;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.FieldParser;

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
    private List<String> headers;

    public FileImportDescriptionBasedParser(FileImportDescription<T> descriptor) {
        this.descriptor = descriptor;
    }

    public void init(CSVParser csvParser) {
        initHeaders(csvParser);
    }

    @Override
    public T parse(CSVRecord csvRecord) throws FileImportParserException {
        T record = this.descriptor.getFileImportRecord();
        record.setLineNumber(csvRecord.getRecordNumber());
        List<FileImportField<?>> fields = this.descriptor.getFields(record);
        List<String> rawValues = getRawValuesSkipTrailingNulls(csvRecord);
        if (rawValues.size() < getNumberOfMandatoryColumns(fields)) {
            throw new FileImportParserException(MessageSeeds.FILE_FORMAT_ERROR, csvRecord.getRecordNumber(),
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
                throw new FileImportParserException(MessageSeeds.LINE_MISSING_VALUE_ERROR, csvRecord.getRecordNumber(), getHeaderColumn(i));
            }
            try {
                FieldSetter fieldSetter = currentField.getSetter();
                FieldParser parser = currentField.getParser();
                fieldSetter.setFieldWithHeader(getHeaderColumn(i), parser.parse(rawValue));
            } catch (ValueParserException ex) {
                throw new FileImportParserException(MessageSeeds.LINE_FORMAT_ERROR, csvRecord.getRecordNumber(), getHeaderColumn(i), ex.getExpected());
            }
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

    private long getNumberOfMandatoryColumns(List<FileImportField<?>> fields) {
        return fields
                .stream()
                .filter(FileImportField::isMandatory)
                .count();
    }

    private String getHeaderColumn(int position) {
        if (position >= 0 && position < this.headers.size()) {
            return this.headers.get(position);
        }
        return "#" + (position + 1);
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
