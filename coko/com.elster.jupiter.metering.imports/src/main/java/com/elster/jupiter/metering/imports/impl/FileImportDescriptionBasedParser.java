package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.imports.impl.exceptions.FileImportParserException;
import com.elster.jupiter.metering.imports.impl.exceptions.ValueParserException;
import com.elster.jupiter.metering.imports.impl.fields.FieldSetter;
import com.elster.jupiter.metering.imports.impl.fields.FileImportField;
import com.elster.jupiter.metering.imports.impl.parsers.DateParser;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        for (Map.Entry<String, FileImportField<?>> field : fields.entrySet()
                .stream()
                .filter(f -> !f.getKey().equals("customPropertySetValue") && csvRecord.isMapped(f.getKey()))
                .collect(Collectors.toList())) {
            try {
                FieldSetter fieldSetter = field.getValue().getSetter();
                FieldParser parser = field.getValue().getParser();
                fieldSetter.setFieldWithHeader(field.getKey(), parser.parse(csvRecord.get(field.getKey())));
            } catch (ValueParserException ex) {
                throw new FileImportParserException(MessageSeeds.LINE_FORMAT_ERROR, csvRecord.getRecordNumber(), field.getKey(), ex
                        .getExpected());
            }
        }

        FieldSetter fieldSetter = fields.get("customPropertySetValue").getSetter();
        FieldParser dateParser = fields.get("customPropertySetTime").getParser();
        Map<CustomPropertySet, CustomPropertySetRecord> customPropertySetValues = new HashMap<>();
        for (RegisteredCustomPropertySet rset : context.getCustomPropertySetService()
                .findActiveCustomPropertySets(UsagePoint.class)) {
            CustomPropertySet set = rset.getCustomPropertySet();
            CustomPropertySetRecord customPropertySetRecord = new CustomPropertySetRecord();
            CustomPropertySetValues values = CustomPropertySetValues.empty();

            if (dateParser instanceof DateParser) {
                if (csvRecord.isMapped(set.getId() + ".versionId")) {
                    customPropertySetRecord.setVersionId(((DateParser) dateParser).parse(csvRecord.get(set.getId() + ".versionId")));
                }
                if (csvRecord.isMapped(set.getId() + ".startTime")) {
                    customPropertySetRecord.setStartTime(((DateParser) dateParser).parse(csvRecord.get(set.getId() + ".startTime")));
                }
                if (csvRecord.isMapped(set.getId() + ".endTime")) {
                    customPropertySetRecord.setEndTime(((DateParser) dateParser).parse(csvRecord.get(set.getId() + ".endTime")));
                }
            }

            Map<Class, FieldParser> parsers = descriptor.getParsers();
            for (Object spec : set.getPropertySpecs()) {
                try {
                    if (spec instanceof PropertySpec && csvRecord.isMapped(set.getId() + "." + ((PropertySpec) spec).getName())) {
                        FieldParser parser = parsers.get(((PropertySpec) spec).getValueFactory().getValueType());
                        if (parser != null) {
                            values.setProperty(((PropertySpec) spec).getName(), parser.parse(csvRecord.get(set.getId() + "." + ((PropertySpec) spec)
                                    .getName())));
                        } else {
                            values.setProperty(((PropertySpec) spec).getName(), ((PropertySpec) spec).getValueFactory()
                                    .fromStringValue(csvRecord.get(set.getId() + "." + ((PropertySpec) spec).getName())));
                        }
                    }
                } catch (ValueParserException ex) {
                    throw new FileImportParserException(MessageSeeds.LINE_FORMAT_ERROR, csvRecord.getRecordNumber(), ((PropertySpec) spec)
                            .getDisplayName(), ex
                            .getExpected());
                }
            }
            customPropertySetRecord.setCustomPropertySetValues(values);
            customPropertySetRecord.setLineNumber(record.getLineNumber());

            if (!customPropertySetRecord.isEmpty()) {
                customPropertySetValues.put(set, customPropertySetRecord);
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
