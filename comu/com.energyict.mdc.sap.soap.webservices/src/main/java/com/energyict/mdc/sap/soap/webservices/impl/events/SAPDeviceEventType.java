/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.events;

import com.elster.jupiter.util.streams.Predicates;

import java.util.NoSuchElementException;
import java.util.Optional;

public class SAPDeviceEventType {
    enum CsvField {
        FORWARDED_TO_SAP,
        EVENT_CODE,
        DEVICE_EVENT_CODE,
        FOLLOW_UP_ACTION_OWNER,
        CATEGORY_CODE,
        CATEGORY_CODE_DESCRIPTION,
        TYPE_CODE,
        TYPE_CODE_DESCRIPTION,
        SEVERITY_CODE,
        SEVERITY_CODE_DESCRIPTION,
        ORIGIN_TYPE_CODE,
        ORIGIN_TYPE_CODE_DESCRIPTION,
        SAP_PROCESS_WORKFLOW_TRIGGER,
        REMARKS;

        int position() {
            return ordinal() + 1;
        }
    }
    static final String NO_EVENT_CODE = "0.0.0.0";
    private final String eventCode;
    private final String deviceEventCode;
    private final int categoryCode;
    private final int typeCode;
    private final int severityCode;
    private final int originTypeCode;
    private final boolean forwardedToSap;

    private SAPDeviceEventType(String csvEntry, String separator) {
        String[] values = csvEntry.split(separator, CsvField.values().length);
        eventCode = parseString(values, CsvField.EVENT_CODE).filter(Predicates.not(NO_EVENT_CODE::equals)).orElse(null);
        deviceEventCode = parseString(values, CsvField.DEVICE_EVENT_CODE).orElse(null);
        forwardedToSap = parseBoolean(values, CsvField.FORWARDED_TO_SAP);
        if (forwardedToSap) {
            if (eventCode == null && deviceEventCode == null) {
                throw missingRequiredPairException(CsvField.EVENT_CODE, CsvField.DEVICE_EVENT_CODE);
            }
            if (eventCode != null && deviceEventCode != null) {
                throw bothEventCodeAndDeviceEventCodePresentException();
            }
        }
        categoryCode = parseMandatoryInt(values, CsvField.CATEGORY_CODE);
        typeCode = parseMandatoryInt(values, CsvField.TYPE_CODE);
        severityCode = parseMandatoryInt(values, CsvField.SEVERITY_CODE);
        originTypeCode = parseMandatoryInt(values, CsvField.ORIGIN_TYPE_CODE);
    }

    static SAPDeviceEventType parseFromCsvEntry(String csvEntry, String separator) {
        return new SAPDeviceEventType(csvEntry, separator);
    }

    private static Optional<String> parseString(String[] values, CsvField field) {
        if (values.length <= field.ordinal()) {
            return Optional.empty();
        }
        return Optional.ofNullable(values[field.ordinal()])
                .map(String::trim)
                .filter(Predicates.not(String::isEmpty));
    }

    private static Optional<Integer> parseInt(String[] values, CsvField field) {
        return parseString(values, field)
                .map(string -> toInt(string, field));
    }

    private static Integer toInt(String value, CsvField field) {
        try {
            return value == null ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect integer value '" + value + "' for " + field.name() + " on position " + field.position() + '.', e);
        }
    }

    private static boolean parseBoolean(String[] values, CsvField field) {
        return parseString(values, field)
                .map(string -> {
                    if ("true".equalsIgnoreCase(string)) {
                        return true;
                    }
                    if ("false".equalsIgnoreCase(string)) {
                        return false;
                    }
                    throw new IllegalArgumentException("Incorrect boolean value '" + string + "' for " + field.name() + " on position " + field.position() + '.');
                })
                .orElse(false);
    }

    private static int parseMandatoryInt(String[] values, CsvField field) {
        return parseInt(values, field)
                .orElseThrow(() -> new NoSuchElementException("Missing required element " + field.name() + " on position " + field.position() + '.'));
    }

    private static IllegalArgumentException missingRequiredPairException(CsvField field1, CsvField field2) {
        return new IllegalArgumentException("Missing both elements " + field1.name() + " & " + field2.name() +
                " on respective positions " + field1.position() + " & " + field2.position() + ". At least one of them is required.");
    }

    private static IllegalArgumentException bothEventCodeAndDeviceEventCodePresentException() {
        return new IllegalArgumentException("Both elements " + CsvField.EVENT_CODE.name() + " & " + CsvField.DEVICE_EVENT_CODE.name() +
                " on respective positions " + CsvField.EVENT_CODE.position() + " & " + CsvField.DEVICE_EVENT_CODE.position() + " have values, though " +
                CsvField.DEVICE_EVENT_CODE.name() + " can be used only in case " + CsvField.EVENT_CODE.name() + " is '0.0.0.0'.");
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public int getSeverityCode() {
        return severityCode;
    }

    public int getOriginTypeCode() {
        return originTypeCode;
    }

    public boolean isForwardedToSap() {
        return forwardedToSap;
    }

    public Optional<String> getEventCode() {
        return Optional.ofNullable(eventCode);
    }

    public Optional<String> getDeviceEventCode() {
        return Optional.ofNullable(deviceEventCode);
    }
}
