package com.elster.jupiter.prepayment.export.redknee.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.*;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.readings.*;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Formatting {@link Reading} as defined for Redknee prepayment solution.</p>
 * <p>
 * <ul>Format
 * <li>1 reading per line</li>
 * <li>Separator: &#7C</li>
 * <li>Order of readings: chronologically sorted</li>
 * <li>Each meter read file will have a header line starting with # ans specifies the version number</li>
 * <li>All columns always available</li>
 * <li>Intervals exported Closed-Open principle</li>
 * <li>Data of 1 device is one file</li>
 * </ul>
 * </p>
 * <p>
 * <table>
 *     <tr>
 *          <th>Order</th>
 *          <th>Column</th>
 *          <th>Format</th>
 *          <th>Note</th>
 *          <th>Example</th>
 *     </tr>
 *     <tr>
 *         <td>1</td>
 *         <td>Record type</td>
 *         <td>Char(1)</td>
 *         <td>'D' = Detail</td>
 *         <td>D</td>
 *     </tr>
 *     <tr>
 *         <td>2</td>
 *         <td>Meter ID</td>
 *         <td>String</td>
 *         <td>MRID of the device</td>
 *         <td>SPE100000100001</td>
 *     </tr>
 *     <tr>
 *         <td>3</td>
 *         <td>Service Point ID</td>
 *         <td>String</td>
 *         <td>MRID of the usage point</td>
 *         <td>UP100011</td>
 *     </tr>
 *     <tr>
 *         <td>4</td>
 *         <td>Reading type</td>
 *         <td>String</td>
 *         <td>Reading type with 18 digits</td>
 *         <td>0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0</td>
 *     </tr>
 *     <tr>
 *         <td>5</td>
 *         <td>Reading quality</td>
 *         <td>String</td>
 *         <td>Reading quality of a reading in Connexo</td>
 *         <td>3.0.0</td>
 *     </tr>
 *     <tr>
 *         <td>6</td>
 *         <td>Reading time</td>
 *         <td>Number</td>
 *         <td>in seconds since epoch (januari 1970)</td>
 *         <td>1441803897</td>
 *     </tr>
 *     <tr>
 *         <td>7</td>
 *         <td>Offset to UTC</td>
 *         <td>+/-dd:dd</td>
 *         <td></td>
 *         <td>+02:00</td>
 *     </tr>
 *     <tr>
 *         <td>8</td>
 *         <td>Reading value</td>
 *         <td>Number</td>
 *         <td></td>
 *         <td>136</td>
 *     </tr>
 * </table>
 * </p>
 *
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
    private final static String DEFAULT_READING_TYPE = "0.0.2.1.1.12.0.0.0.0.0.0.0.0.072.0";
    private final static String DEFAULT_READING_QUALITY_TYPE =  ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALID, 0).getCode();

    private final DataExportService dataExportService;
    private Thesaurus thesaurus;
    private String fieldSeparator;
    private String tag;
    private String updateTag;
    private StructureMarker structureMarker;
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
        this.updateTag =  propertyMap.get(FormatterProperties.UPDATE_TAG.getKey()) != null ? propertyMap.get(FormatterProperties.TAG.getKey()).toString() :  "update";
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
        this.logger.info("readingContainer:" + readingContainer.getMeter(Instant.now()).get().getMRID());
    }

    @Override
    public void endItem(ReadingTypeDataExportItem item) {}

    @Override
    public FormattedData processData(Stream<ExportData> exportDatas) {
        return exportDatas.map(this::format).reduce(SimpleFormattedData::merged).orElseGet(() -> SimpleFormattedData.of(Collections.emptyList()));
    }

    private SimpleFormattedData format(ExportData exportData){
        latestTimeStamp = null;
        structureMarker = createStructureMarker( exportData,
                                                 dataExportService.forRoot(tag).withPeriodOf(exportData.getStructureMarker()),
                                                 dataExportService.forRoot(updateTag).withPeriodOf(exportData.getStructureMarker()));

        MeterReading data = ((MeterReadingData) exportData).getMeterReading();
        List<IntervalBlock> intervalBlocks = data.getIntervalBlocks();

        latestTimeStamp = intervalBlocks.stream().flatMap(i -> i.getIntervals().stream()).map(IntervalReading::getTimeStamp).max(Comparator.naturalOrder()).orElse(null);

        return SimpleFormattedData.of(
                data.getIntervalBlocks().stream().map(Records::new).collect(Collectors.toList()),
                latestTimeStamp);
    }

    private StructureMarker createStructureMarker(ExportData exportData, StructureMarker main, StructureMarker update) {
        StructureMarker structureMarker = exportData.getStructureMarker();
        if (structureMarker.endsWith("update")) {
            return update.adopt(structureMarker);
        }
        return main.adopt(structureMarker);
    }

    private class Records implements FormattedExportData{

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

        @Override
        public String getAppendablePayload() {
            if (intervalBlock.getIntervals().isEmpty()) {
                logger.finest(String.format("Nothing to export for meter %s", meter == null ? "" :  meter.getMRID()));
                return "";
            }
//            Instant start = intervalBlock.getIntervals().get(0).getTimeStamp();
//            meter = readingContainer.getMeter(start).orElse(null);
//            usagePoint = readingContainer.getUsagePoint(start).orElse(null);
//            activation = null;
//            if (meter != null) {
//                activation = meter.getMeterActivation(start).orElse(null);
//            }
            StringBuffer payload = new StringBuffer();

            intervalBlock.getIntervals().forEach( x-> append(payload, x));

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
                   .append(fieldSeparator)
                   .append(meterMRID)
                   .append(fieldSeparator)
                   .append(usagePointMRID)
                   .append(fieldSeparator)
                   .append(readingTypeCodeOrDefault)
                   .append(fieldSeparator)
                   .append(readingQuality)
                   .append(fieldSeparator)
                   .append(reading.getTimeStamp().getEpochSecond())
                   .append(fieldSeparator)
                   .append(offset)
                   .append(fieldSeparator)
                   .append(reading.getValue())
                   .append(NEW_LINE);
            if (latestTimeStamp == null || reading.getTimeStamp().isAfter(latestTimeStamp)){
                latestTimeStamp = reading.getTimeStamp();
            }

        }
    }

}
