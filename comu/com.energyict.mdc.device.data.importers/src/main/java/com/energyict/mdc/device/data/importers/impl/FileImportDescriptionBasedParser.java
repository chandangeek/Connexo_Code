package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.importers.impl.exceptions.ParserException;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
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
    public T parse(CSVRecord csvRecord) throws ParserException {
        T record = this.descriptor.getFileImportRecord();
        record.setLineNumber(csvRecord.getRecordNumber());
        List<FileImportField<?>> fields = this.descriptor.getFields(record);
        List<String> rawValues = getRawValues(csvRecord);
        if (rawValues.size() < fields.size()){
            // TODO throw: File format error: wrong number of title columns in the first line. Importer service expects X but was Y.
        }
        for (int i = 0; i < rawValues.size(); i++) {
            String rawValue = rawValues.get(i);
            int currentFieldIdx = i < fields.size() ? i : fields.size() - 1;
            FileImportField<?> currentField = fields.get(currentFieldIdx);
            if (currentField.isMandatory() && Checks.is(rawValue).emptyOrOnlyWhiteSpace()){
                // TODO throw: Format error for line X: missing column Y.
            }
            Consumer resultConsumer = currentField.getResultConsumer();
            resultConsumer.accept(currentField.getParser().parse(rawValue));
        }
        return record;
    }

    private List<String> getRawValues(CSVRecord csvRecord){
        List<String> rawValues = new ArrayList<>();
        csvRecord.forEach(rawValues::add);
        return rawValues;
    }
}
