package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
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
import com.elster.jupiter.metering.MeteringService;
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
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;

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
import java.util.Set;
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
    private final ValidationService validationService;
    private final DataExportService dataExportService;
    private final MeteringService meteringService;

    private String fieldSeparator;
    private ReadingType readingType;
    private ReadingContainer readingContainer;
    private final Thesaurus thesaurus;
    private Meter meter;
    private String tag;
    private String updateTag;

    @Inject
    StandardCsvDataFormatter(Thesaurus thesaurus, ValidationService validationService, DataExportService dataExportService, MeteringService meteringService) {
        this.thesaurus = thesaurus;
        this.validationService = validationService;
        this.dataExportService = dataExportService;
        this.meteringService = meteringService;
    }

    StandardCsvDataFormatter(Map<String, Object> propertyMap, Thesaurus thesaurus, ValidationService validationService, DataExportService dataExportService, MeteringService meteringService) {
        this.validationService = validationService;
        this.thesaurus = thesaurus;
        this.dataExportService = dataExportService;
        this.meteringService = meteringService;

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
        readingContainer = item.getReadingContainer();
        readingType = item.getReadingType();
    }

    @Override
    public FormattedData processData(Stream<ExportData> exportData) {
        // TODO: update this when export is allowed from MDM; empty set means qualities from all systems taken into account
        return exportData.map(data -> processData(Collections.emptySet(), data))
                .map(pair -> SimpleFormattedData.of(pair.getLast(), pair.getFirst()))
                .reduce(SimpleFormattedData::merged)
                .orElseGet(() -> SimpleFormattedData.of(Collections.emptyList()));
    }

    private Pair<Instant, List<FormattedExportData>> processData(Set<QualityCodeSystem> qualityCodeSystems, ExportData exportData) {
        StructureMarker main = dataExportService.forRoot(tag).withPeriodOf(exportData.getStructureMarker());
        StructureMarker update = dataExportService.forRoot(updateTag).withPeriodOf(exportData.getStructureMarker());
        MeterReading data = ((MeterReadingData) exportData).getMeterReading();
        List<Reading> readings = data.getReadings();
        List<IntervalBlock> intervalBlocks = data.getIntervalBlocks();
        Map<Instant, DataValidationStatus> statuses = readings.stream()
                .map(Reading::getTimeStamp)
                .max(Comparator.naturalOrder())
                .map(latestProcessedTimestamp -> {
                    setMeter(latestProcessedTimestamp);
                    return getDataValidationStatusMap(qualityCodeSystems, latestProcessedTimestamp, readings);
                })
                .orElseGet(Collections::emptyMap);
        Stream<FormattedExportData> readingStream = readings.stream()
                .map(reading -> writeNonCumulativeReading(reading, statuses.get(reading.getTimeStamp())))
                .flatMap(Functions.asStream())
                .map(line -> TextLineExportData.of(createStructureMarker(exportData, main, update), line));

        Stream<FormattedExportData> intervalReadings = Stream.empty();
        if (!intervalBlocks.isEmpty()) {
            Optional<Instant> latestProcessedTimestampOptional = intervalBlocks.stream()
                    .map(IntervalBlock::getIntervals)
                    .flatMap(Collection::stream)
                    .map(IntervalReading::getTimeStamp)
                    .max(Comparator.naturalOrder());
            latestProcessedTimestampOptional.ifPresent(this::setMeter);
            intervalReadings = intervalBlocks.stream()
                    .flatMap(block -> {
                        ReadingType readingType = meteringService.getReadingType(block.getReadingTypeCode()).get();
                        Map<Instant, DataValidationStatus> intervalStatuses = latestProcessedTimestampOptional
                                .map(latestProcessedTimestamp -> getDataValidationStatusMap(qualityCodeSystems, latestProcessedTimestamp, block.getIntervals()))
                                .orElseGet(Collections::emptyMap);
                        return block.getIntervals().stream()
                                .map(reading -> {
                                    DataValidationStatus status = intervalStatuses.get(reading.getTimeStamp());
                                    ValidationResult validationResult = status == null ?
                                            ValidationResult.NOT_VALIDATED :
                                            readingType.isCumulative() ?
                                                    status.getBulkValidationResult() :
                                                    status.getValidationResult();
                                    return Pair.of(reading, validationResult);
                                });
                    })
                    .map(pair -> writeReading(pair.getFirst(), pair.getLast()))
                    .flatMap(Functions.asStream())
                    .map(line -> TextLineExportData.of(createStructureMarker(exportData, main, update), line));
        }
        Instant lastExported = determineLastExported(readings, intervalBlocks);
        return Pair.of(lastExported, Stream.concat(readingStream, intervalReadings).collect(Collectors.toList()));
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

    private Optional<String> writeNonCumulativeReading(BaseReading reading, DataValidationStatus status) {
        return writeReading(reading, status == null ? ValidationResult.NOT_VALIDATED : status.getValidationResult());
    }

    private Optional<String> writeReading(BaseReading reading, ValidationResult validationResult) {
        if (reading.getValue() != null) {
            ZonedDateTime date = ZonedDateTime.ofInstant(reading.getTimeStamp(), ZoneId.systemDefault());
            StringJoiner joiner = new StringJoiner(fieldSeparator, "", "\n")
                    .add(DEFAULT_DATE_TIME_FORMAT.format(date))
                    .add(meter.getMRID())
                    .add(meter.getName())
                    .add(readingType.getMRID())
                    .add(reading.getValue().toString());
            switch (validationResult) {
                case VALID:
                    joiner.add(VALID_STRING);
                    break;
                case SUSPECT:
                    joiner.add(INVALID_STRING);
                    break;
                default:
                    joiner.add("");
            }
            return Optional.of(joiner.toString());
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

    private void defineSeparator(TranslatablePropertyValueInfo separator) {
        if (separator.getId().equals(FormatterProperties.SEPARATOR_SEMICOLON.getKey())) {
            this.fieldSeparator = SEMICOLON_SEPARATOR;
        } else {
            this.fieldSeparator = COMMA_SEPARATOR;
        }
    }

    private Map<Instant, DataValidationStatus> getDataValidationStatusMap(Set<QualityCodeSystem> qualityCodeSystems,
                                                                          Instant instant,
                                                                          List<? extends BaseReading> readings) {
        return validationService.getEvaluator(meter)
                .getValidationStatus(qualityCodeSystems, forValidation(instant, meter), readings)
                .stream()
                .collect(Collectors.toMap(DataValidationStatus::getReadingTimestamp, Function.identity(), (v1, v2) -> v2));
    }

    private Channel forValidation(Instant latestProcessedTimestamp, Meter meter) {
        MeterActivation meterActivation = meter.getMeterActivation(latestProcessedTimestamp).orElseThrow(IllegalArgumentException::new);
        return findChannel(meterActivation).orElseThrow(IllegalArgumentException::new);
    }

    private void setMeter(Instant latestProcessedTimestamp) {
        meter = readingContainer.getMeter(latestProcessedTimestamp)
                .orElseThrow(() -> new DataExportException(new LocalizedException(thesaurus, MessageSeeds.INVALID_READING_CONTAINER, new IllegalArgumentException()) {
                }));
    }

    private Optional<Channel> findChannel(MeterActivation meterActivation) {
        return meterActivation.getChannelsContainer().getChannels().stream()
                .filter(channel -> channel.getReadingTypes().contains(readingType))
                .findFirst();
    }
}
