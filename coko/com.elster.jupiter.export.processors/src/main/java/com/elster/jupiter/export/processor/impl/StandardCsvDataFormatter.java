/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.MeterReadingValidationData;
import com.elster.jupiter.export.ReadingDataFormatter;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.SimpleFormattedData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.export.TextLineExportData;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class StandardCsvDataFormatter implements ReadingDataFormatter, StandardFormatter {

    public static final String VALID_STRING = "valid";
    public static final String INVALID_STRING = "suspect";
    public static final String SEMICOLON_SEPARATOR = ";";
    public static final String COMMA_SEPARATOR = ",";
    public static final String DEFAULT_SEPARATOR = COMMA_SEPARATOR;

    private final DataExportService dataExportService;

    private String fieldSeparator;
    private ReadingType readingType;
    private IdentifiedObject domainObject;
    private String tag;
    private String updateTag;

    @Inject
    StandardCsvDataFormatter(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    StandardCsvDataFormatter(Map<String, Object> propertyMap, DataExportService dataExportService) {
        this.dataExportService = dataExportService;
        if (propertyMap.containsKey(FormatterProperties.SEPARATOR.getKey())) {
            defineSeparator((TranslatablePropertyValueInfo) propertyMap.get(FormatterProperties.SEPARATOR.getKey()));
        } else {
            this.fieldSeparator = DEFAULT_SEPARATOR;
        }
        tag = getStringProperty(propertyMap, FormatterProperties.TAG.getKey(), "export");
        updateTag = getStringProperty(propertyMap, FormatterProperties.UPDATE_TAG.getKey(), "update");
    }

    private String getStringProperty(Map<String, Object> propertyMap, String key, String defaultValue) {
        return propertyMap.get(key) != null ? propertyMap.get(key).toString() : defaultValue;
    }

    @Override
    public void startExport(DataExportOccurrence dataExportOccurrence, Logger logger) {
    }

    public void startItem(ReadingTypeDataExportItem item) {
        domainObject = item.getDomainObject();
        readingType = item.getReadingType();
    }

    @Override
    public FormattedData processData(Stream<ExportData> exportData) {
        return exportData.map(this::processData)
                .map(pair -> SimpleFormattedData.of(pair.getLast(), pair.getFirst()))
                .reduce(SimpleFormattedData::merged)
                .orElseGet(() -> SimpleFormattedData.of(Collections.emptyList()));
    }

    private Pair<Instant, List<FormattedExportData>> processData(ExportData exportData) {
        StructureMarker main = dataExportService.forRoot(tag).withPeriodOf(exportData.getStructureMarker());
        StructureMarker update = dataExportService.forRoot(updateTag).withPeriodOf(exportData.getStructureMarker());
        MeterReadingData meterReadingData = ((MeterReadingData) exportData);
        MeterReading data = meterReadingData.getMeterReading();
        MeterReadingValidationData validationData = meterReadingData.getValidationData();
        List<Reading> readings = data.getReadings();
        List<IntervalBlock> intervalBlocks = data.getIntervalBlocks();
        ReadingType readingType = meterReadingData.getItem().getReadingType();

        List<FormattedExportData> formattedExportData = Stream.concat(
                readings.stream()
                        .map(reading ->
                                writeReading(reading, asValidationResult(validationData.getValidationStatus(reading.getTimeStamp()), readingType))),
                intervalBlocks.stream()
                        .map(IntervalBlock::getIntervals)
                        .flatMap(Collection::stream)
                        .map(reading ->
                                writeReading(reading, asValidationResult(validationData.getValidationStatus(reading.getTimeStamp()), readingType))))
                .flatMap(Functions.asStream())
                .map(line -> TextLineExportData.of(createStructureMarker(exportData, main, update), line))
                .collect(Collectors.toList());

        Instant lastExported = determineLastExported(readings, intervalBlocks);
        return Pair.of(lastExported, formattedExportData);
    }

    private ValidationResult asValidationResult(DataValidationStatus validationStatus, ReadingType readingType) {
        if (validationStatus == null) {
            return ValidationResult.NOT_VALIDATED;
        }
        if (readingType.isRegular() && readingType.isCumulative()) {
            validationStatus.getBulkValidationResult();
        }
        return validationStatus.getValidationResult();
    }

    private StructureMarker createStructureMarker(ExportData exportData, StructureMarker main, StructureMarker update) {
        StructureMarker structureMarker = exportData.getStructureMarker();
        if (structureMarker.endsWith("update")) {
            return update.adopt(structureMarker);
        }
        return main.adopt(structureMarker);
    }

    private Instant determineLastExported(List<Reading> readings, List<IntervalBlock> intervalBlocks) {
        return Stream.of(intervalBlocks.stream()
                .map(IntervalBlock::getIntervals)
                .flatMap(Collection::stream), readings.stream())
                .flatMap(Function.identity())
                .map(BaseReading::getTimeStamp)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private Optional<String> writeReading(BaseReading reading, ValidationResult validationResult) {
        if (reading.getValue() != null) {
            ZonedDateTime date = ZonedDateTime.ofInstant(reading.getTimeStamp(), ZoneId.systemDefault());
            StringJoiner joiner = new StringJoiner(fieldSeparator, "", "\n")
                    .add(DEFAULT_DATE_TIME_FORMAT.format(date))
                    .add(domainObject.getMRID())
                    .add(domainObject.getName())
                    .add(readingType.getMRID())
                    .add(reading.getValue().toString())
                    .add(asString(validationResult));
            return Optional.of(joiner.toString());
        }
        return Optional.empty();
    }

    private String asString(ValidationResult validationResult) {
        switch (validationResult) {
            case VALID:
                return VALID_STRING;
            case SUSPECT:
                return INVALID_STRING;
            default:
                return "";
        }
    }

    public void endItem(ReadingTypeDataExportItem item) {
        if (!item.getReadingType().getMRID().equals(readingType.getMRID())) {
            throw new IllegalArgumentException("ReadingTypeDataExportItems passed to startItem() and endItem() methods are different");
        }
        readingType = null;
        domainObject = null;
    }

    @Override
    public void endExport() {
    }

    private void defineSeparator(TranslatablePropertyValueInfo separator) {
        if (separator.getId().equals(FormatterProperties.SEPARATOR_SEMICOLON.getKey())) {
            this.fieldSeparator = SEMICOLON_SEPARATOR;
        } else {
            this.fieldSeparator = COMMA_SEPARATOR;
        }
    }
}
