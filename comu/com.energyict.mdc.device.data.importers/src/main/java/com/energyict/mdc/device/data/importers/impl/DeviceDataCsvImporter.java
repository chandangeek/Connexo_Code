package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.energyict.mdc.device.data.importers.impl.exceptions.ParserException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStreamReader;
import java.util.logging.Logger;

public class DeviceDataCsvImporter<T extends FileImportRecord> implements FileImporter {

    public static final char COMMENT_MARKER = '#';

    private char csvDelimiter;
    private FileImportParser<T> parser;
    private FileImportProcessor<T> processor;

    public DeviceDataCsvImporter(char csvDelimiter, FileImportParser<T> parser, FileImportProcessor<T> processor) {
        this.csvDelimiter = csvDelimiter;
        this.parser = parser;
        this.processor = processor;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        Logger logger = fileImportOccurrence.getLogger();

        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces(true).withDelimiter(csvDelimiter).withCommentMarker(COMMENT_MARKER);

        try (CSVParser csvParser = new CSVParser(new InputStreamReader(fileImportOccurrence.getContents()), csvFormat)) {
            for (CSVRecord csvRecord : csvParser) {
                try {
                    T data = parser.parse(csvRecord);
                    processor.process(data);
                } catch (ParserException e) {
                    e.printStackTrace();
                } catch(ProcessorException e) {
                    e.printStackTrace();
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
