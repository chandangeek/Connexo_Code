package com.elster.jupiter.prepayment.export.redknee.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.*;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.readings.*;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.NumberFormat;
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
