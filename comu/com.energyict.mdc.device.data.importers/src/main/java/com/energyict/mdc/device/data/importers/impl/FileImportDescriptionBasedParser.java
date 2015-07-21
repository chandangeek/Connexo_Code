package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import jdk.nashorn.internal.runtime.ParserException;
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
    public T parse(CSVRecord csvRecord) throws FileImportParserException {
        T record = this.descriptor.getFileImportRecord();
        record.setLineNumber(csvRecord.getRecordNumber());
        List<FileImportField<?>> fields = this.descriptor.getFields(record);
        List<String> rawValues = getRawValues(csvRecord);
        if (rawValues.size() < fields.size()){
            throw new FileImportParserException(MessageSeeds.FILE_FORMAT_ERROR, fields.size(), rawValues.size());
        }
        for (int i = 0; i < rawValues.size(); i++) {
            String rawValue = rawValues.get(i);
            int currentFieldIdx = i < fields.size() ? i : fields.size() - 1;
            FileImportField<?> currentField = fields.get(currentFieldIdx);
            if (currentField.isMandatory() && Checks.is(rawValue).emptyOrOnlyWhiteSpace()){
                throw new FileImportParserException(MessageSeeds.LINE_MISSING_VALUE_ERROR, csvRecord.getRecordNumber(), currentField.getTitle());
            }
            Consumer resultConsumer = currentField.getResultConsumer();
            try {
                resultConsumer.accept(currentField.getParser().parse(rawValue));
            } catch (ValueParserException ex){
                throw new FileImportParserException(MessageSeeds.LINE_FORMAT_ERROR,
                        csvRecord.getRecordNumber(), currentField.getTitle(), ex.getExpected());
            }
        }
        return record;
    }

    private List<String> getRawValues(CSVRecord csvRecord){
        List<String> rawValues = new ArrayList<>();
        csvRecord.forEach(rawValues::add);
        return rawValues;
    }
}
