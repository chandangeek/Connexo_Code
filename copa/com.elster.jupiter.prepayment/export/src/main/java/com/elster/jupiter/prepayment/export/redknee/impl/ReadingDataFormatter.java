package com.elster.jupiter.prepayment.export.redknee.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.*;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.readings.*;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Formatting {@link Reading} as defined for Redknee prepayment solution.
 * Copyrights EnergyICT
 * Date: 5/10/2015
 * Time: 13:19
 */
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
    private final static String SEPARATOR = "\u007C";  // "|"
    private final static String DEFAULT_READING_TYPE = "0.0.2.1.1.12.0.0.0.0.0.0.0.0.072.0";
    private final static String DEFAULT_READING_QUALITY_TYPE =  ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALID, 0).getCode();

    private StructureMarker structureMarker;
    private ReadingContainer readingContainer;
    private Logger logger;
    private Instant latestTimeStamp;

    public ReadingDataFormatter(){
        super();
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
    }

    @Override
    public void endItem(ReadingTypeDataExportItem item) {}

    @Override
    public FormattedData processData(Stream<ExportData> exportDatas) {
        return exportDatas.map(this::format).reduce(SimpleFormattedData::merged).orElseGet(() -> SimpleFormattedData.of(Collections.emptyList()));
    }

    private SimpleFormattedData format(ExportData exportData){
        latestTimeStamp = null;
        structureMarker = exportData.getStructureMarker();

        MeterReading data = ((MeterReadingData) exportData).getMeterReading();
        return SimpleFormattedData.of(
                data.getIntervalBlocks().stream().map(Records::new).collect(Collectors.toList()),
                latestTimeStamp);
    }

    private class Records implements FormattedExportData{

        private IntervalBlock intervalBlock;
        private Meter meter;
        private UsagePoint usagePoint;
        private MeterActivation activation;

        Records(IntervalBlock intervalBlock){
            this.intervalBlock = intervalBlock;
        }

        @Override
        public String getAppendablePayload() {
            if (intervalBlock.getIntervals().isEmpty()) {
                logger.finest("Nothing to export");
                return "";
            }
            Instant start = intervalBlock.getIntervals().get(0).getTimeStamp();
            meter = readingContainer.getMeter(start).orElse(null);
            usagePoint = readingContainer.getUsagePoint(start).orElse(null);
            activation = null;
            if (meter != null) {
                activation = meter.getMeterActivation(start).orElse(null);
            }
            StringBuffer payload = new StringBuffer();

            intervalBlock.getIntervals().forEach( x-> append(payload, x));

            logger.finest("" + payload);

            return payload.toString();
        }

        @Override
        public StructureMarker getStructureMarker() {
            return ReadingDataFormatter.this.structureMarker;
        }

        private void append(StringBuffer payload, IntervalReading reading){
            String meterMRID = (meter == null ? "" :  meter.getMRID());
            String usagePointMRID = (usagePoint == null? "":  usagePoint.getMRID());
            String offset = (activation == null ? "" : activation.getZoneId().getRules().getOffset(reading.getTimeStamp()).getId());
            String readingTypeCode = intervalBlock.getReadingTypeCode();
            String readingTypeCodeOrDefault = ( readingTypeCode == null ? DEFAULT_READING_TYPE : readingTypeCode);
            List<? extends ReadingQuality> readingQualities = reading.getReadingQualities();
            String readingQuality = (reading.getReadingQualities().isEmpty() ? DEFAULT_READING_QUALITY_TYPE : readingQualities.get(0).getTypeCode());

            payload.append(RecordType.Detail.toString())
                   .append(SEPARATOR)
                   .append(meterMRID)
                   .append(SEPARATOR)
                   .append(usagePointMRID)
                   .append(SEPARATOR)
                   .append(readingTypeCodeOrDefault)
                   .append(SEPARATOR)
                   .append(readingQuality)
                   .append(SEPARATOR)
                   .append(reading.getTimeStamp().getEpochSecond())
                   .append(SEPARATOR)
                   .append(offset)
                   .append(SEPARATOR)
                   .append(reading.getValue())
                   .append(NEW_LINE);
            if (latestTimeStamp == null || reading.getTimeStamp().isAfter(latestTimeStamp)){
                latestTimeStamp = reading.getTimeStamp();
            }

        }
    }

}
