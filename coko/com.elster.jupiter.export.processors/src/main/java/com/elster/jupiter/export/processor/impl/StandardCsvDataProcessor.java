package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.*;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.readings.*;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StandardCsvDataProcessor implements DataProcessor {

    private final DataExportService dataExportService;
    private final AppService appService;

    private Path tempDirectory;
    private String fileSeparator;
    private String filePrefix;
    private String fileExtension;
    private String path;
    private boolean writeMainFile;
    private boolean updatedDataSeparateFile;
    private String updatedDataFilePrefix;
    private String updatedDataFileExtension;
    private Path tempFile;
    private BufferedWriter writer;
    private Path tempUpdatedFile;
    private BufferedWriter updatedWriter;
    private String readingType;
    private Instant fileNameTimestamp;
    private ReadingContainer readingContainer;
    private final Thesaurus thesaurus;
    private FileSystem fileSystem;

    @Inject
    public StandardCsvDataProcessor(DataExportService dataExportService, AppService appService, Thesaurus thesaurus) {
        this.dataExportService = dataExportService;
        this.appService = appService;
        this.thesaurus = thesaurus;
    }

    public StandardCsvDataProcessor(DataExportService dataExportService, AppService appService, List<DataExportProperty> properties, Thesaurus thesaurus, FileSystem fileSystem, Path tempDirectory) {
        this.dataExportService = dataExportService;
        this.appService = appService;
        this.thesaurus = thesaurus;
        this.fileSystem = fileSystem;
        this.tempDirectory = tempDirectory;
        Map<String, Object> propertyMap = getPropertiesMap(properties);
        if (propertyMap.containsKey(FormatterProperties.SEPARATOR.getKey())) {
            defineSeparator(propertyMap.get(FormatterProperties.SEPARATOR.getKey()).toString());
        } else {
            this.fileSeparator = ",";
        }

        this.filePrefix = getStringProperty(propertyMap, FormatterProperties.FILENAME_PREFIX.getKey(), "");
        this.fileExtension = getStringProperty(propertyMap, FormatterProperties.FILE_EXTENSION.getKey(), "csv");
        this.path = getStringProperty(propertyMap, FormatterProperties.FILE_PATH.getKey(), "");

        if (propertyMap.containsKey(FormatterProperties.UPDATE_IN_SEPARATE_FILE.getKey())) {
            this.updatedDataSeparateFile = Boolean.valueOf(propertyMap.get(FormatterProperties.UPDATE_IN_SEPARATE_FILE.getKey()).toString());
        }
        if (this.updatedDataSeparateFile) {
            this.updatedDataFilePrefix = getStringProperty(propertyMap, FormatterProperties.UPDATE_FILE_PREFIX.getKey(), "");
            this.updatedDataFileExtension = (getStringProperty(propertyMap, FormatterProperties.UPDATE_FILE_EXTENSION.getKey(), "csv"));
        }
    }

    private String getStringProperty(Map<String, Object> propertyMap, String key, String defaultValue) {
        return propertyMap.containsKey(key) ? propertyMap.get(key).toString() : defaultValue;
    }

    @Override
    public void startExport(DataExportOccurrence dataExportOccurrence, Logger logger) {
        fileNameTimestamp = dataExportOccurrence.getTriggerTime();
        try {
            tempFile = Files.createTempFile(tempDirectory, "tempfile", null);
            writer = Files.newBufferedWriter(tempFile);
        } catch (IOException ex) {
            throw new FatalDataExportException(new FileIOException(ex, thesaurus));
        }
        if (updatedDataSeparateFile) {
            try {
                tempUpdatedFile = Files.createTempFile(tempDirectory, "tempfileUpdate", updatedDataFileExtension);
                updatedWriter = Files.newBufferedWriter(tempUpdatedFile);
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
            if (reading.getValue() != null) {
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
            }
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
            Path file = ensuringDirectoryExists(createFile(filePrefix, fileExtension));
            try {
                writer.close();
                Files.copy(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new FatalDataExportException(new FileIOException(ex, thesaurus));
            }
        }
        if (updatedDataSeparateFile) {
            Path updatedFile = ensuringDirectoryExists(createFile(updatedDataFilePrefix, updatedDataFileExtension));
            try {
                updatedWriter.close();
                Files.copy(tempUpdatedFile, updatedFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new FatalDataExportException(new FileIOException(ex, thesaurus));
            }
        }
    }

    private Path getDefaultExportDir() {
        AppServer appServer = appService.getAppServer().orElseThrow(IllegalStateException::new);
        return dataExportService.getExportDirectory(appServer).orElseGet(() -> fileSystem.getPath("").toAbsolutePath());
    }

    private Path createFile(String prefix, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        // TODO correct ZoneId
        ZonedDateTime date = ZonedDateTime.ofInstant(fileNameTimestamp, ZoneId.systemDefault());
        StringBuilder fileNameUpdated = new StringBuilder(prefix);
        if (!fileNameUpdated.toString().isEmpty()) {
            fileNameUpdated.append('_');
        }
        fileNameUpdated.append( date.format(formatter)).append('.').append(extension);

        Path path = fileSystem.getPath(this.path);
        if (path.isAbsolute()) {
            return path.resolve(fileNameUpdated.toString());
        }
        return getDefaultExportDir().resolve(path).resolve(fileNameUpdated.toString());
    }

    private Path ensuringDirectoryExists(Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new FatalDataExportException(new FileIOException(e, thesaurus));
        }
        return path;
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
