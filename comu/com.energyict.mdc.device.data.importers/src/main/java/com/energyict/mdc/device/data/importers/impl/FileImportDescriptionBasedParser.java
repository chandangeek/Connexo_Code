package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FileImportDescriptionBasedParser<T extends FileImportRecord> implements FileImportParser<T> {

    private final FileImportDescription<T> descriptor;

    public FileImportDescriptionBasedParser(FileImportDescription<T> descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public T parse(CSVRecord csvRecord, FileImportRecordContext recordContext) throws FileImportParserException {
        T record = this.descriptor.getFileImportRecord();
        record.setLineNumber(csvRecord.getRecordNumber());
        List<FileImportField<?>> fields = this.descriptor.getFields(record);
        List<String> rawValues = getRawValues(csvRecord);
        if (rawValues.size() < fields.size()){
            throw new FileImportParserException(MessageSeeds.FILE_FORMAT_ERROR, csvRecord.getRecordNumber(), fields.size(), rawValues.size());
        }
        int repetitiveColumnCount = 0;
        for (int i = 0; i < rawValues.size() && (i < fields.size() || repetitiveColumnCount > 0); i++) {
            String rawValue = rawValues.get(i);
            // note: here we use division without remnant
            int currentFieldIdx = i < fields.size() ? i : i - (i-1) / repetitiveColumnCount * repetitiveColumnCount;
            FileImportField<?> currentField = fields.get(currentFieldIdx);
            if (i < fields.size() && currentField.isRepetitive()) {
                repetitiveColumnCount++;
            }
            if (currentField.isMandatory() && Checks.is(rawValue).emptyOrOnlyWhiteSpace()) {
                throw new FileImportParserException(MessageSeeds.LINE_MISSING_VALUE_ERROR, csvRecord.getRecordNumber(), recordContext.getHeaderColumn(i));
            }
            Consumer resultConsumer = currentField.getResultConsumer();
            try {
                resultConsumer.accept(currentField.getParser().parse(rawValue));
            } catch (ValueParserException ex) {
                throw new FileImportParserException(MessageSeeds.LINE_FORMAT_ERROR,
                        csvRecord.getRecordNumber(), recordContext.getHeaderColumn(i), ex.getExpected());
            }
        }
        return record;
    }

    @Override
    public List<String> parseHeaders(CSVParser parser) {
        List<String> headers = FileImportParser.super.parseHeaders(parser);
        long numberOfMandatoryColumns = getNumberOfMandatoryColumns();
        if (headers.size() < numberOfMandatoryColumns){
            throw new FileImportParserException(MessageSeeds.MISSING_TITLE_ERROR, numberOfMandatoryColumns, headers.size());
        }
        return headers;
    }

    public long getNumberOfMandatoryColumns() {
        return this.descriptor.getFields(this.descriptor.getFileImportRecord())
                .stream()
                .filter(FileImportField::isMandatory)
                .count();
    }

    private List<String> getRawValues(CSVRecord csvRecord){
        List<String> rawValues = new ArrayList<>();
        csvRecord.forEach(rawValues::add);
        return rawValues;
    }
}
