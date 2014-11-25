package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.*;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.readings.*;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private String readingType;
    private Instant fileNameTimestamp;
    private ReadingContainer readingContainer;
    private final Thesaurus thesaurus;

    @Inject
    public StandardCsvDataProcessor(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public StandardCsvDataProcessor(List<DataExportProperty> properties, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        Map<String, Object> propertyMap = getPropertiesMap(properties);
        if (propertyMap.containsKey(FormatterProperties.SEPARATOR.getKey())) {
            defineSeparator(propertyMap.get(FormatterProperties.SEPARATOR.getKey()).toString());
        } else {
            this.fileSeparator = ",";
        }

        this.filePrefix = propertyMap.containsKey(FormatterProperties.FILENAME_PREFIX.getKey()) ?
                propertyMap.get(FormatterProperties.FILENAME_PREFIX.getKey()).toString() : "";
        this.fileExtension = (propertyMap.containsKey(FormatterProperties.FILE_EXTENSION.getKey()) ?
                propertyMap.get(FormatterProperties.FILE_EXTENSION.getKey()).toString() : "csv");

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
            tempFile = File.createTempFile("tempfile", fileExtension);
            writer = new BufferedWriter(new FileWriter(tempFile));
        } catch (IOException ex) {
            throw new FatalDataExportException(new FileIOException(ex, thesaurus));
        }
        if (updatedDataSeparateFile) {
            try {
                tempUpdatedFile = File.createTempFile("tempfileUpdate", updatedDataFileExtension);
                updatedWriter = new BufferedWriter(new FileWriter(tempUpdatedFile));
            } catch (IOException ex) {
                throw new FatalDataExportException(new FileIOException(ex, thesaurus));
            }
        }
    }

    @Override
    public void startItem(ReadingTypeDataExportItem item) {
        readingContainer = item.getReadingContainer();
        readingType = item.getReadingType().getMRID();
    }

    @Override
    public Optional<Instant> processData(MeterReading data) {
        List<Reading> readings = data.getReadings();
        Optional<Instant> latestProcessedTimestamp = readings.stream().map(Reading::getTimeStamp).max(Comparator.naturalOrder());
        readings.stream().forEach(this::writeReading);
        List<IntervalBlock> intervalBlocks = data.getIntervalBlocks();
        if (!intervalBlocks.isEmpty()) {
            latestProcessedTimestamp = intervalBlocks.stream().flatMap(i -> i.getIntervals().stream()).map(IntervalReading::getTimeStamp).max(Comparator.naturalOrder());
            intervalBlocks.stream().forEach(block -> block.getIntervals().stream().forEach(this::writeReading));
        }
        writeMainFile |= latestProcessedTimestamp.isPresent();
        return latestProcessedTimestamp;
    }

    private void writeReading(BaseReading reading) {
        Optional<Meter> meterOptional = readingContainer.getMeter(reading.getTimeStamp());
        if (!meterOptional.isPresent()) {
            throw new DataExportException(new LocalizedException(thesaurus, MessageSeeds.INVALID_READING_CONTAINER, new IllegalArgumentException()) {});
        }
        try {
            Long timestamp = reading.getTimeStamp().toEpochMilli();
            writer.write(timestamp.toString());
            writer.write(fileSeparator);
            writer.write(meterOptional.get().getMRID());
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
            throw new FatalDataExportException(new FileIOException(ex, thesaurus));
        }
    }

    @Override
    public Optional<Instant> processUpdatedData(MeterReading updatedData) {
        return Optional.empty();
    }

    @Override
    public void endItem(ReadingTypeDataExportItem item) {
        if (!item.getReadingType().getMRID().equals(readingType)) {
            throw new IllegalArgumentException("ReadingTypeDataExportItems passed to startItem() and EndItem() methods are different");
        }
        readingContainer = null;
        readingType = null;
    }

    @Override
    public void endExport() {
        if (writeMainFile) {
            File file = createFile(filePrefix, fileExtension);
            try {
                writer.close();
                Files.copy(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new FatalDataExportException(new FileIOException(ex, thesaurus));
            }
        }
        if (updatedDataSeparateFile) {
            File updatedFile = createFile(updatedDataFilePrefix, updatedDataFileExtension);
            try {
                updatedWriter.close();
                Files.copy(tempUpdatedFile.toPath(), updatedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new FatalDataExportException(new FileIOException(ex, thesaurus));
            }
        }
    }

    private File createFile(String prefix, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        // TODO correct ZoneId
        ZonedDateTime date = ZonedDateTime.ofInstant(fileNameTimestamp, ZoneId.systemDefault());
        StringBuilder fileNameUpdated = new StringBuilder(prefix);
        if (!fileNameUpdated.toString().isEmpty()) {
            fileNameUpdated.append('_');
        }
        fileNameUpdated.append( date.format(formatter)).append('.').append(extension);
        return new File(fileNameUpdated.toString());
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
