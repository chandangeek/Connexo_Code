package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.*;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.readings.*;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StandardCsvDataProcessor implements DataProcessor {
    private String fileSeparator;
    private String filePrefix;
    private String fileExtension;
    private boolean writeMainFile;
    private boolean updatedDataSeparateFile;
    private String updatedDataFilePrefix;
    private String updatedDataFileExtension;
    private File tempFile;
    private BufferedWriter writer;
    private File tempUpdatedFile;
    private BufferedWriter updatedWriter;
    private String deviceMRID;
    private String readingType;
    private Instant fileNameTimestamp;
    private Thesaurus thesaurus;

    @Inject
    public StandardCsvDataProcessor(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public StandardCsvDataProcessor(List<DataExportProperty> properties) {
        Map<String, Object> propertyMap = getPropertiesMap(properties);
        if (propertyMap.containsKey(FormatterProperties.SEPARATOR.getKey())) {
            defineSeparator(propertyMap.get(FormatterProperties.SEPARATOR.getKey()).toString());
        } else {
            this.fileSeparator = ",";
        }
        // TODO define proper way to check if main file is needed
        if (propertyMap.containsKey(FormatterProperties.FILE_EXTENSION.getKey())) {
            this.writeMainFile = true;
            this.filePrefix = propertyMap.containsKey(FormatterProperties.FILENAME_PREFIX.getKey()) ?
                    propertyMap.get(FormatterProperties.FILENAME_PREFIX.getKey()).toString() : "";
            this.fileExtension = /*(propertyMap.containsKey(FormatterProperties.FILE_EXTENSION.getKey()) ?*/
                    propertyMap.get(FormatterProperties.FILE_EXTENSION.getKey()).toString()/* : "csv")*/;
        }
        if (propertyMap.containsKey(FormatterProperties.UPDATE_IN_SEPARATE_FILE.getKey())) {
            this.updatedDataSeparateFile = Boolean.valueOf(propertyMap.get(FormatterProperties.UPDATE_IN_SEPARATE_FILE.getKey()).toString());
        }
        if (this.updatedDataSeparateFile) {
            this.updatedDataFilePrefix = propertyMap.containsKey(FormatterProperties.UPDATE_FILE_PREFIX.getKey()) ?
                    propertyMap.get(FormatterProperties.UPDATE_FILE_PREFIX.getKey()).toString() : "";
            this.updatedDataFileExtension = (propertyMap.containsKey(FormatterProperties.UPDATE_FILE_EXTENSION.getKey()) ?
                    propertyMap.get(FormatterProperties.UPDATE_FILE_EXTENSION.getKey()).toString() : "csv");
        }
    }

    @Override
    public void startExport(DataExportOccurrence dataExportOccurrence, Logger logger) {
        fileNameTimestamp = dataExportOccurrence.getTriggerTime();
        try {
            if (writeMainFile) {
                tempFile = File.createTempFile("tempfile", fileExtension);
                writer = new BufferedWriter(new FileWriter(tempFile));
            }
            if (updatedDataSeparateFile) {
                tempUpdatedFile = File.createTempFile("tempfileUpdate", updatedDataFileExtension);
                updatedWriter = new BufferedWriter(new FileWriter(tempUpdatedFile));
            }
        } catch (IOException ex) {
            throw new FatalDataExportException(ex);
        }
    }

    @Override
    public void startItem(ReadingTypeDataExportItem item) {
        if (!(item.getReadingContainer() instanceof Meter)) {
            throw new DataExportException(new LocalizedException(thesaurus, MessageSeeds.INVALID_READING_CONTAINER, new IllegalArgumentException()) {
            });
        }
        Meter meter = Meter.class.cast(item.getReadingContainer());
        deviceMRID = meter.getMRID();
        readingType = item.getReadingType().getMRID();
    }

    @Override
    public Optional<Instant> processData(MeterReading data) {
        Instant latestProcessedTimestamp = null;
        if (!writeMainFile) {
            return Optional.ofNullable(latestProcessedTimestamp);
        }
        List<Reading> readings = data.getReadings();
        List<IntervalBlock> intervalBlocks = data.getIntervalBlocks();
        if (!readings.isEmpty()) {
            latestProcessedTimestamp = writeReadings(readings);
        } else if (!intervalBlocks.isEmpty()) {
            for (IntervalBlock block : intervalBlocks) {
                latestProcessedTimestamp = writeReadings(block.getIntervals());
            }
        }
        return Optional.ofNullable(latestProcessedTimestamp);
    }

    private Instant writeReadings(List<? extends BaseReading> readings) {
        Instant latestProcessedTimestamp = null;
        for (BaseReading reading : readings) {
            latestProcessedTimestamp = writeReading(reading);
        }
        return latestProcessedTimestamp;
    }

    private Instant writeReading(BaseReading reading) {
        try {
            Long timestamp = reading.getTimeStamp().toEpochMilli();
            writer.write(timestamp.toString());
            writer.write(fileSeparator);
            writer.write(deviceMRID);
            writer.write(fileSeparator);
            writer.write(readingType);
            writer.write(fileSeparator);
            writer.write(reading.getValue().toString());
            writer.write(fileSeparator);
            List<? extends ReadingQuality> readingQualities = reading.getReadingQualities();
            //TODO handle readingQualities properly
            for (ReadingQuality readingQuality : readingQualities) {
                //ReadingQualityRecord record = ReadingQualityRecord.class.cast(readingQuality);
                writer.write(readingQuality.getTypeCode());
                writer.write("-");
            }
            writer.write(fileSeparator);
            writer.newLine();
        } catch (IOException ex) {
            throw new FatalDataExportException(ex);
        }
        return reading.getTimeStamp();
    }

    @Override
    public Optional<Instant> processUpdatedData(MeterReading updatedData) {
        return Optional.<Instant>empty();
    }

    @Override
    public void endItem(ReadingTypeDataExportItem item) {
        if (!(item.getReadingContainer() instanceof Meter)) {
            throw new DataExportException(new LocalizedException(thesaurus, MessageSeeds.INVALID_READING_CONTAINER, new IllegalArgumentException()) {
            });
        }
        Meter meter = Meter.class.cast(item.getReadingContainer());
        if (!meter.getMRID().equals(deviceMRID) || !item.getReadingType().getMRID().equals(readingType)) {
            throw new IllegalArgumentException("ReadingTypeDataExportItems passed to startItem() and EndItem() methods are different");
        }
        deviceMRID = null;
        readingType = null;
    }

    @Override
    public void endExport() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        // TODO what if file with the same name already exists?
        Date date = new Date(fileNameTimestamp.toEpochMilli());
        // TODO defile file location dir
        if (writeMainFile) {
            StringBuilder fileName = new StringBuilder(filePrefix);
            if (!fileName.toString().isEmpty()) {
                fileName.append("_");
            }
            fileName.append(formatter.format(date)).append(".").append(fileExtension);
            File file = new File(fileName.toString());
            try {
                writer.close();
                Files.copy(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new FatalDataExportException(ex);
            }
        }
        if (updatedDataSeparateFile) {
            StringBuilder fileNameUpdated = new StringBuilder(updatedDataFilePrefix);
            if (!fileNameUpdated.toString().isEmpty()) {
                fileNameUpdated.append("_");
            }
            fileNameUpdated.append(formatter.format(date)).append(".").append(updatedDataFileExtension);
            File updatedFile = new File(fileNameUpdated.toString());
            try {
                updatedWriter.close();
                Files.copy(tempUpdatedFile.toPath(), updatedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new FatalDataExportException(ex);
            }
        }
    }

    private Map<String, Object> getPropertiesMap(List<DataExportProperty> properties) {
        return properties.stream()
                .collect(Collectors.toMap(DataExportProperty::getName, DataExportProperty::getValue));
    }

    private void defineSeparator(String separator) {
        switch (separator) {
            case "comma":
                this.fileSeparator = ",";
                break;
            case "semicolon":
                this.fileSeparator = ";";
                break;
            default:
                this.fileSeparator = ",";
                break;
        }
    }
}
