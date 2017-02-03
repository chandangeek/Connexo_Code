/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.imports.impl.exceptions.FileImportLineException;
import com.elster.jupiter.metering.imports.impl.exceptions.FileImportParserException;
import com.elster.jupiter.metering.imports.impl.exceptions.ValueParserException;
import com.elster.jupiter.metering.imports.impl.fields.FieldSetter;
import com.elster.jupiter.metering.imports.impl.fields.FileImportField;
import com.elster.jupiter.metering.imports.impl.parsers.InstantParser;
import com.elster.jupiter.metering.imports.impl.usagepoint.MeterRoleWithMeterAndActivationDate;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileImportDescriptionBasedParser<T extends FileImportRecord> implements FileImportParser<T> {
    private static final String CUSTOM_PROPERTY_FIELD = "customPropertySetValue";
    private static final String CUSTOM_PROPERTY_TIME_FIELD = "customPropertySetTime";

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
            throw new FileImportLineException(csvRecord.getRecordNumber(), MessageSeeds.FILE_FORMAT_ERROR, csvRecord.getRecordNumber(),
                    getNumberOfMandatoryColumns(fields), rawValues.size());
        }

        for (Map.Entry<String, FileImportField<?>> field : fields.entrySet().stream()
                .filter(f -> !f.getKey().equals(CUSTOM_PROPERTY_FIELD) && csvRecord.isMapped(f.getKey()))
                .collect(Collectors.toList())) {
            if (field.getValue().isMandatory() && (csvRecord.toMap().entrySet().stream()
                    .allMatch(e -> !e.getKey().equalsIgnoreCase(field.getKey()) || Checks.is(csvRecord.get(e.getKey()))
                            .emptyOrOnlyWhiteSpace()))) {
                throw new FileImportLineException(csvRecord.getRecordNumber(), MessageSeeds.LINE_MISSING_VALUE_ERROR, csvRecord.getRecordNumber(), field.getKey());
            }
        }

        Map<String, String> locations = new HashMap<>();
        for (Map.Entry<String, String> entry : csvRecord.toMap().entrySet()) {
            fields.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst().ifPresent(field -> {
                if (field.isMandatory() && csvRecord.isMapped(entry.getKey()) && Checks.is(csvRecord.get(entry.getKey()))
                        .emptyOrOnlyWhiteSpace()) {
                    throw new FileImportLineException(csvRecord.getRecordNumber(), MessageSeeds.LINE_MISSING_VALUE_ERROR, csvRecord.getRecordNumber(), field.getFieldName());
                }
                if (context.getMeteringService().getLocationTemplate().getTemplateMembers().stream()
                        .map(LocationTemplate.TemplateField::getName)
                        .anyMatch(s -> s.equals(entry.getKey()))) {
                    locations.put(entry.getKey(), entry.getValue());
                } else {
                    try {
                        FieldSetter fieldSetter = field.getSetter();
                        FieldParser parser = field.getParser();
                        fieldSetter.setFieldWithHeader(field.getFieldName(), parser.parse(csvRecord.get(entry.getKey())));
                    } catch (ValueParserException ex) {
                        throw new FileImportParserException(MessageSeeds.LINE_FORMAT_ERROR, csvRecord.getRecordNumber(), field.getFieldName(), ex.getExpected());
                    }
                }
            });
        }

        context.getMeteringService().getLocationTemplate().getTemplateMembers().stream()
                .sorted((t1, t2) -> Integer.compare(t1.getRanking(), t2.getRanking()))
                .map(LocationTemplate.TemplateField::getName)
                .forEach(s -> {
                    fields.entrySet().stream()
                            .filter(e -> e.getKey().equalsIgnoreCase(s))
                            .map(Map.Entry::getValue)
                            .findFirst().ifPresent(field -> {
                        try {
                            FieldSetter fieldSetter = field.getSetter();
                            fieldSetter.setFieldWithHeader(field.getFieldName(), locations.get(s));
                        } catch (ValueParserException ex) {
                            throw new FileImportParserException(MessageSeeds.LINE_FORMAT_ERROR, csvRecord.getRecordNumber(), field.getFieldName(), ex.getExpected());
                        }

                    });
                });

        addMeterRolesWithMetersAndActivationDates(fields, csvRecord);
        addTransitionsField(fields, csvRecord);

        return parseCustomProperties(record, csvRecord, fields);
    }

    private T parseCustomProperties(T record, CSVRecord csvRecord, Map<String, FileImportField<?>> fields) {
        Map<String, String> csvRecordMap = csvRecord.toMap();
        FieldSetter fieldSetter = fields.get(CUSTOM_PROPERTY_FIELD).getSetter();
        FieldParser dateParser = fields.get(CUSTOM_PROPERTY_TIME_FIELD).getParser();
        Map<String, CustomPropertySetRecord> customPropertySetValues = new HashMap<>();
        for (RegisteredCustomPropertySet rset : context.getCustomPropertySetService()
                .findActiveCustomPropertySets(UsagePoint.class)) {
            CustomPropertySet set = rset.getCustomPropertySet();
            CustomPropertySetRecord customPropertySetRecord = new CustomPropertySetRecord();
            CustomPropertySetValues values = CustomPropertySetValues.empty();
            try {
                if (dateParser instanceof InstantParser) {
                    csvRecordMap.entrySet()
                            .stream()
                            .filter(r -> r.getKey().equalsIgnoreCase(set.getId() + ".versionId"))
                            .findFirst()
                            .ifPresent(r -> customPropertySetRecord.setVersionId(((InstantParser) dateParser).parse(r.getValue())));
                    csvRecordMap.entrySet()
                            .stream()
                            .filter(r -> r.getKey().equalsIgnoreCase(set.getId() + ".startTime"))
                            .findFirst()
                            .ifPresent(r -> customPropertySetRecord.setStartTime(((InstantParser) dateParser).parse(r.getValue())));
                    csvRecordMap.entrySet()
                            .stream()
                            .filter(r -> r.getKey().equalsIgnoreCase(set.getId() + ".endTime"))
                            .findFirst()
                            .ifPresent(r -> customPropertySetRecord.setEndTime(((InstantParser) dateParser).parse(r.getValue())));
                }
            } catch (ValueParserException ex) {
                throw new FileImportParserException(MessageSeeds.LINE_FORMAT_ERROR, csvRecord.getRecordNumber(),
                        set.getId(), ex.getExpected());
            }


            Map<Class, FieldParser> parsers = descriptor.getParsers();
            for (Object spec : set.getPropertySpecs()) {
                try {
                    if (spec instanceof PropertySpec) {
                        Optional<String> propertyValue = csvRecordMap.entrySet()
                                .stream()
                                .filter(r -> r.getKey()
                                        .equalsIgnoreCase(set.getId() + "." + ((PropertySpec) spec).getName()))
                                .map(Map.Entry::getValue)
                                .findFirst();
                        if (propertyValue.isPresent()) {
                            FieldParser parser = parsers.get(((PropertySpec) spec).getValueFactory().getValueType());
                            if (parser != null) {
                                values.setProperty(((PropertySpec) spec).getName(), parser.parse(propertyValue.get()));
                            } else {
                                values.setProperty(((PropertySpec) spec).getName(), ((PropertySpec) spec).getValueFactory()
                                        .fromStringValue(propertyValue.get()));
                            }
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
                customPropertySetValues.put(set.getId(), customPropertySetRecord);
            }
        }
        fieldSetter.setFieldWithHeader(CUSTOM_PROPERTY_FIELD, customPropertySetValues);

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

    private List<MeterRoleWithMeterAndActivationDate> getMeterRoles(CSVRecord csvRecord) {
        Map<Integer, MeterRoleWithMeterAndActivationDate> roles = new HashMap<>();
        Map<String, String> recordMap = csvRecord.toMap();
        recordMap.keySet().stream().forEach(key -> {
            String header = key.toLowerCase();
            String value = recordMap.get(key);
            if (header.startsWith("meterrole")) {
                int index = header.charAt(header.length() - 1);
                if (roles.containsKey(index)) {
                    roles.get(index).setMeterRole(value);
                } else {
                    MeterRoleWithMeterAndActivationDate meterRoleWithMeterAndActivationDate = new MeterRoleWithMeterAndActivationDate();
                    meterRoleWithMeterAndActivationDate.setMeterRole(value);
                    roles.put(index, meterRoleWithMeterAndActivationDate);
                }
            } else if (header.startsWith("meter") && !header.startsWith("meterrole")) {
                int index = header.charAt(header.length() - 1);
                if (roles.containsKey(index)) {
                    roles.get(index).setMeter(value);
                } else {
                    MeterRoleWithMeterAndActivationDate meterRoleWithMeterAndActivationDate = new MeterRoleWithMeterAndActivationDate();
                    meterRoleWithMeterAndActivationDate.setMeter(value);
                    roles.put(index, meterRoleWithMeterAndActivationDate);
                }
            } else if (header.startsWith("activationdate")) {
                int index = header.charAt(header.length() - 1);
                if (roles.containsKey(index)) {
                    roles.get(index).setActivationDate(value);
                } else {
                    MeterRoleWithMeterAndActivationDate meterRoleWithMeterAndActivationDate = new MeterRoleWithMeterAndActivationDate();
                    meterRoleWithMeterAndActivationDate.setActivationDate(value);
                    roles.put(index, meterRoleWithMeterAndActivationDate);
                }
            }
        });

        return new ArrayList<>(roles.values());
    }

    private void addMeterRolesWithMetersAndActivationDates(Map<String, FileImportField<?>> fields, CSVRecord csvRecord) {
        Optional<String> meterRolesField = fields.keySet()
                .stream()
                .filter(key -> key.equalsIgnoreCase("meterRoles"))
                .findFirst();
        FieldSetter<List<MeterRoleWithMeterAndActivationDate>> setter = (FieldSetter<List<MeterRoleWithMeterAndActivationDate>>) fields
                .get(meterRolesField.get())
                .getSetter();
        setter.setField(getMeterRoles(csvRecord));
    }

    private void addTransitionsField(Map<String, FileImportField<?>> fields, CSVRecord csvRecord) {
        Optional<String> transitionsField = fields.keySet()
                .stream()
                .filter(key -> key.equalsIgnoreCase("transitionAttributes"))
                .findFirst();
        FieldSetter<Map<String, String>> setter = (FieldSetter<Map<String, String>>) fields
                .get(transitionsField.get())
                .getSetter();
        setter.setField(getTransitionAttributes(csvRecord));
    }

    private Map<String, String> getTransitionAttributes(CSVRecord csvRecord) {
        Map<String, String> recordMap = csvRecord.toMap();
        Map<String, String> transitions = new HashMap<>();
        recordMap.keySet().stream().forEach(key -> {
            String header = key.toLowerCase();
            if (header.startsWith("transition")
                    && !header.equalsIgnoreCase("transition")
                    && !header.equalsIgnoreCase("transitionDate")) {
                String transitionName = header.substring(10);
                transitions.put(transitionName, recordMap.get(key));
            }
        });

        return transitions;
    }
}