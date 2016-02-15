package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.FileImportParserException;
import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ValueParserException;
import com.elster.jupiter.metering.imports.impl.usagepoint.fields.FieldSetter;
import com.elster.jupiter.metering.imports.impl.usagepoint.fields.FileImportField;
import com.elster.jupiter.metering.imports.impl.usagepoint.parsers.FieldParser;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.*;
import java.util.stream.Collectors;

public class FileImportDescriptionBasedParser<T extends FileImportRecord> implements FileImportParser<T> {

    private final FileImportDescription<T> descriptor;
    private final MeteringDataImporterContext context;
    private List<String> headers;

    public FileImportDescriptionBasedParser(FileImportDescription<T> descriptor, MeteringDataImporterContext context) {
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
            throw new FileImportParserException(MessageSeeds.FILE_FORMAT_ERROR, csvRecord.getRecordNumber(),
                    getNumberOfMandatoryColumns(fields), rawValues.size());
        }

        for (Map.Entry<String, FileImportField<?>> field : fields.entrySet().stream().filter(f -> !f.getKey().equals("customPropertySetValue")).collect(Collectors.toList())) {
            try {
                FieldSetter fieldSetter = field.getValue().getSetter();
                FieldParser parser = field.getValue().getParser();
                fieldSetter.setFieldWithHeader(field.getKey(), parser.parse(csvRecord.get(field.getKey())));
            } catch (ValueParserException ex) {
                throw new FileImportParserException(MessageSeeds.LINE_FORMAT_ERROR, csvRecord.getRecordNumber(), field.getKey(), ex.getExpected());
            }
        }

        FieldSetter fieldSetter = fields.get("customPropertySetValue").getSetter();
        Map<CustomPropertySet, CustomPropertySetValues> customPropertySetValues = new HashMap<>();
        for (RegisteredCustomPropertySet rset : context.getCustomPropertySetService().findActiveCustomPropertySets(UsagePoint.class)) {
            CustomPropertySet set = rset.getCustomPropertySet();
            CustomPropertySetValues values = CustomPropertySetValues.empty();
            for (Object spec : set.getPropertySpecs()) {
                if(spec instanceof PropertySpec && csvRecord.isMapped(set.getId()+"."+((PropertySpec) spec).getName())){
                    //values.setProperty(((PropertySpec) spec).getName(),csvRecord.get(set.getId()+"."+((PropertySpec) spec).getName()));
                    values.setProperty(((PropertySpec) spec).getName(),((PropertySpec) spec).getValueFactory().fromStringValue(csvRecord.get(set.getId()+"."+((PropertySpec) spec).getName())));
                }
            }
            if (!values.isEmpty()){
                customPropertySetValues.put(set,values);
            }
        }
        fieldSetter.setFieldWithHeader("customPropertySetValue", customPropertySetValues);

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
        return fields
                .values()
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
