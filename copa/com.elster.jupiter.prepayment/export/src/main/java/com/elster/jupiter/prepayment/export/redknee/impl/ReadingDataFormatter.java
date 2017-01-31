/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.export.redknee.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.SimpleFormattedData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.export.TextLineExportData;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadingDataFormatter implements com.elster.jupiter.export.ReadingDataFormatter{

    private enum RecordType{
        Detail("D");

        private String id;

        RecordType(String id){
            this.id = id;
        }

        public String toString(){
            return id;
        }
    }

    private final static String NEW_LINE = "\n";
    private final static String DEFAULT_READING_TYPE = "0.0.2.1.1.12.0.0.0.0.0.0.0.0.072.0";
    private final static String DEFAULT_READING_QUALITY_TYPE =  ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALID, 0).getCode();

    private final DataExportService dataExportService;
    private Thesaurus thesaurus;
    private String fieldSeparator;
    private String tag;
    private String updateTag;
    private StructureMarker structureMarker;
    private StructureMarker mainStructureMarker;
    private StructureMarker updateStructureMarker;
    private ReadingContainer readingContainer;
    private Logger logger;
    private Instant latestTimeStamp;

    @Inject
    public ReadingDataFormatter(DataExportService dataExportService){
        super();
        this.dataExportService = dataExportService;
        this.fieldSeparator = ReadingDataFormatterFactory.FieldSeparator.PIPE.getSymbol();
    }

    public ReadingDataFormatter(DataExportService dataExportService, Thesaurus thesaurus, Map<String, Object> propertyMap)  {
        this.dataExportService = dataExportService;
        this.thesaurus = thesaurus;
        this.fieldSeparator = ReadingDataFormatterFactory.FieldSeparator.separatorForName(propertyMap.get(FormatterProperties.SEPARATOR.getKey()).toString());
        this.tag = propertyMap.get(FormatterProperties.TAG.getKey()) != null ? propertyMap.get(FormatterProperties.TAG.getKey()).toString() :  "export";
        this.updateTag =  propertyMap.get(FormatterProperties.UPDATE_TAG.getKey()) != null ? propertyMap.get(FormatterProperties.UPDATE_TAG.getKey()).toString() :  "update";
    }

    @Override
    public void startExport(DataExportOccurrence occurrence, Logger logger) {
        this.logger = logger;
    }

    @Override
    public void endExport() {}

    @Override
    public void startItem(ReadingTypeDataExportItem item) {
        readingContainer = item.getReadingContainer();
        latestTimeStamp =  item.getLastExportedDate().orElse(null);
        this.logger.finest("readingContainer:" + readingContainer.getMeter(latestTimeStamp).get().getMRID());
    }

    @Override
    public void endItem(ReadingTypeDataExportItem item) {}

    @Override
    public FormattedData processData(Stream<ExportData> exportDatas) {
        return exportDatas.map(this::format)
                .reduce(SimpleFormattedData::merged)
                .orElseGet(() -> SimpleFormattedData.of(Collections.emptyList()));
    }

    private SimpleFormattedData format(ExportData exportData){
        structureMarker = exportData.getStructureMarker();
        mainStructureMarker = dataExportService.forRoot(tag).withPeriodOf(structureMarker);
        updateStructureMarker = dataExportService.forRoot(updateTag).withPeriodOf(structureMarker);

        List<IntervalBlock> intervalBlocks = ((MeterReadingData) exportData).getMeterReading().getIntervalBlocks();

        return SimpleFormattedData.of(
                intervalBlocks.stream().map(Records::new).flatMap(Records::formatReadings).collect(Collectors.toList()),
                latestTimeStamp);
    }

    private StructureMarker createStructureMarker() {
        if (structureMarker.endsWith("update")) {
            return updateStructureMarker.adopt(structureMarker);
        }
        return mainStructureMarker.adopt(structureMarker);
    }

    private class Records {

        private IntervalBlock intervalBlock;
        private Meter meter;
        private UsagePoint usagePoint;
        private MeterActivation activation;

        Records(IntervalBlock intervalBlock){
            this.intervalBlock = intervalBlock;
            Instant start = intervalBlock.getIntervals().get(0).getTimeStamp();
            meter = readingContainer.getMeter(start).orElse(null);
            usagePoint = readingContainer.getUsagePoint(start).orElse(null);
            activation = null;
            if (meter != null) {
                activation = meter.getMeterActivation(start).orElse(null);
            }
        }

        public Stream<FormattedExportData> formatReadings(){
            List<IntervalReading> readings = intervalBlock.getIntervals();
            if (readings.isEmpty()) {
                logger.finest(String.format("Nothing to export for meter %s", meter == null ? "" :  meter.getMRID()));
                return Stream.empty();
            }
            return readings.stream().map(reading -> TextLineExportData.of(createStructureMarker(), payload(reading)));
        }

        private String payload(IntervalReading intervalReading){
            return payloadElements(intervalReading).collect(Collectors.joining(fieldSeparator, "", NEW_LINE));
        }

        private Stream<String> payloadElements(IntervalReading reading){
            String offset = (activation == null ? "" : activation.getZoneId().getRules().getOffset(reading.getTimeStamp()).getId());
            List<? extends ReadingQuality> readingQualities = reading.getReadingQualities();
            String readingQuality = (readingQualities.isEmpty() ? DEFAULT_READING_QUALITY_TYPE : readingQualities.get(0).getTypeCode());
            BigDecimal readingValue = reading.getValue();

            String[] field = new String[8];
            field[0] = RecordType.Detail.toString();                     // RecordType
            field[1] = (meter == null ? "" :  meter.getMRID());          // Meter mRID
            field[2] = (usagePoint == null ? "":  usagePoint.getMRID()); // UsagePoint mRID
            field[3] = (intervalBlock.getReadingTypeCode() == null ? DEFAULT_READING_TYPE : intervalBlock.getReadingTypeCode());  // ReadingType
            field[4] = readingQuality;                                   // Reading quality
            field[5] = ""+reading.getTimeStamp().getEpochSecond();          // Timestamp
            field[6] = offset;                                           // Timestamp offset
            field[7] = (readingValue == null ? "" : readingValue.toString());                               // Value

            return Arrays.stream(field);
        }
    }

}
