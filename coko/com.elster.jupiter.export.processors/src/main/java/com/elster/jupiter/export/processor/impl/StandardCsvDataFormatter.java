package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingDataFormatter;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.SimpleFormattedData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.export.TextLineExportData;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StandardCsvDataFormatter implements ReadingDataFormatter {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String VALID_STRING = "valid";
    public static final String INVALID_STRING = "suspect";
    public static final String SEMICOLON_VALUE = "semicolon";
    public static final String COMMA_VALUE = "comma";
    public static final String SEMICOLON_SEPARATOR = ";";
    public static final String COMMA_SEPARATOR = ",";
    public static final String DEFAULT_SEPARATOR = COMMA_SEPARATOR;
    private final ValidationService validationService;
    private final DataExportService dataExportService;

    private String fieldSeparator;
    private ReadingType readingType;
    private ReadingContainer readingContainer;
    private final Thesaurus thesaurus;
    private Meter meter;
    private String tag;
    private String updateTag;

    @Inject
    public StandardCsvDataFormatter(Thesaurus thesaurus, ValidationService validationService, DataExportService dataExportService) {
        this.thesaurus = thesaurus;
        this.validationService = validationService;
        this.dataExportService = dataExportService;
    }

    public StandardCsvDataFormatter(Map<String, Object> propertyMap, Thesaurus thesaurus, ValidationService validationService, DataExportService dataExportService) {
        this.validationService = validationService;
        this.thesaurus = thesaurus;
        this.dataExportService = dataExportService;

        if (propertyMap.containsKey(FormatterProperties.SEPARATOR.getKey())) {
            defineSeparator(propertyMap.get(FormatterProperties.SEPARATOR.getKey()).toString());
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
        readingContainer = item.getReadingContainer();
        readingType = item.getReadingType();
    }

    @Override
    public FormattedData processData(Stream<ExportData> exportDatas) {
        return exportDatas.map(this::processData)
                .map(pair -> SimpleFormattedData.of(pair.getLast(), pair.getFirst()))
                .reduce(SimpleFormattedData::merged)
                .orElseGet(() -> SimpleFormattedData.of(Collections.emptyList()));
    }

    private Pair<Instant, List<FormattedExportData>> processData(ExportData exportData) {
        StructureMarker main = dataExportService.forRoot(tag);
        StructureMarker update = dataExportService.forRoot(updateTag);
        MeterReading data = ((MeterReadingData) exportData).getMeterReading();
        List<Reading> readings = data.getReadings();
        List<IntervalBlock> intervalBlocks = data.getIntervalBlocks();
        Optional<Instant> latestProcessedTimestamp = readings.stream().map(Reading::getTimeStamp).max(Comparator.naturalOrder());
        setMeter(latestProcessedTimestamp);
        Map<Instant, DataValidationStatus> statuses = getDataValidationStatusMap(latestProcessedTimestamp, readings);
        Stream<FormattedExportData> readingStream = readings.stream()
                .map(reading -> writeReading(reading, statuses.get(reading.getTimeStamp())))
                .flatMap(Functions.asStream())
                .map(line -> TextLineExportData.of(createStructureMarker(exportData, main, update), line));

        Stream<FormattedExportData> intervalReadings = Stream.empty();
        if (!intervalBlocks.isEmpty()) {
            latestProcessedTimestamp = intervalBlocks.stream().flatMap(i -> i.getIntervals().stream()).map(IntervalReading::getTimeStamp).max(Comparator.naturalOrder());
            setMeter(latestProcessedTimestamp);
            Optional<Instant> latest = latestProcessedTimestamp;
            intervalReadings = intervalBlocks.stream()
                    .flatMap(block -> {
                        Map<Instant, DataValidationStatus> intervalStatuses = getDataValidationStatusMap(latest, block.getIntervals());
                        return block.getIntervals().stream()
                                .map(reading -> Pair.of(reading, intervalStatuses.get(reading.getTimeStamp())));
                    })
                    .map(pair -> writeReading(pair.getFirst(), pair.getLast()))
                    .flatMap(Functions.asStream())
                    .map(line -> TextLineExportData.of(createStructureMarker(exportData, main, update), line));
        }
        Instant lastExported = determineLastExported(readings, intervalBlocks);
        return Pair.of(lastExported, Stream.of(readingStream, intervalReadings).flatMap(Function.identity()).collect(Collectors.toList()));
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

    private Optional<String> writeReading(BaseReading reading, DataValidationStatus status) {

        if (reading.getValue() != null) {
            StringBuilder writer = new StringBuilder();
            // TODO correct ZoneId
            ZonedDateTime date = ZonedDateTime.ofInstant(reading.getTimeStamp(), ZoneId.systemDefault());
            writer.append(date.format(DATE_TIME_FORMATTER));
            writer.append(fieldSeparator);
            writer.append(meter.getMRID());
            writer.append(fieldSeparator);
            writer.append(readingType.getMRID());
            writer.append(fieldSeparator);
            writer.append(reading.getValue().toString());
            writer.append(fieldSeparator);
            if (status != null) {
                ValidationResult validationResult = status.getValidationResult();
                switch (validationResult) {
                    case VALID:
                        writer.append(VALID_STRING);
                        break;
                    case SUSPECT:
                        writer.append(INVALID_STRING);
                        break;
                }
            }
            writer.append(fieldSeparator);
            writer.append('\n');
            return Optional.of(writer.toString());
        }
        return Optional.empty();
    }

    public void endItem(ReadingTypeDataExportItem item) {
        if (!item.getReadingType().getMRID().equals(readingType.getMRID())) {
            throw new IllegalArgumentException("ReadingTypeDataExportItems passed to startItem() and EndItem() methods are different");
        }
        readingContainer = null;
        readingType = null;
        meter = null;
    }

    @Override
    public void endExport() {
    }

    private void defineSeparator(String separator) {
        switch (separator) {
            case SEMICOLON_VALUE:
                this.fieldSeparator = SEMICOLON_SEPARATOR;
                break;
            case COMMA_VALUE:
            default:
                this.fieldSeparator = COMMA_SEPARATOR;
                break;
        }
    }

    private Map<Instant, DataValidationStatus> getDataValidationStatusMap(Optional<Instant> instantRef, List<? extends BaseReading> readings) {
        Map<Instant, DataValidationStatus> statuses = new HashMap<>();
        if (instantRef.isPresent()) {
            List<DataValidationStatus> dataValidationStatuses = validationService.getEvaluator(meter, Interval.sinceEpoch().toOpenClosedRange())
                    .getValidationStatus(forValidation(instantRef.get(), meter), readings);
            dataValidationStatuses.stream().forEach(s -> statuses.put(s.getReadingTimestamp(), s));
        }
        return statuses;
    }

    private Channel forValidation(Instant latestProcessedTimestamp, Meter meter) {
        MeterActivation meterActivation = meter.getMeterActivation(latestProcessedTimestamp).orElseThrow(IllegalArgumentException::new);
        return findChannel(meterActivation).orElseThrow(IllegalArgumentException::new);
    }

    private void setMeter(Optional<Instant> latestProcessedTimestamp) {
        if (latestProcessedTimestamp.isPresent()) {
            Optional<Meter> meterOptional = readingContainer.getMeter(latestProcessedTimestamp.get());
            if (!meterOptional.isPresent()) {
                throw new DataExportException(new LocalizedException(thesaurus, MessageSeeds.INVALID_READING_CONTAINER, new IllegalArgumentException()) {
                });
            }
            meter = meterOptional.get();
        }
    }

    private Optional<Channel> findChannel(MeterActivation meterActivation) {
        for (Channel channel : meterActivation.getChannels()) {
            if (channel.getReadingTypes().contains(readingType)) {
                return Optional.of(channel);
            }
        }
        return Optional.empty();
    }
}
