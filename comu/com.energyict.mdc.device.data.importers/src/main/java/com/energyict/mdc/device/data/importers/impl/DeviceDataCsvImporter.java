package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.energyict.mdc.device.data.importers.impl.exceptions.ParserException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStreamReader;
import java.util.logging.Logger;

public class DeviceDataCsvImporter<T extends FileImportRecord> implements FileImporter {

    public static class Builder<T extends FileImportRecord>{
        private DeviceDataCsvImporter<T> importer;

        private Builder() {
            this.importer = new DeviceDataCsvImporter<>();
        }

        public Builder<T> withProcessor(FileImportProcessor<T> processor){
            this.importer.processor = processor;
            return this;
        }

        public Builder<T> withDelimiter(char delimiter){
            this.importer.csvDelimiter = delimiter;
            return this;
        }

        public DeviceDataCsvImporter<T> build(DeviceDataImporterContext context){
            this.importer.context = context;
            return this.importer;
        }
    }

    public static final char COMMENT_MARKER = '#';

    private DeviceDataImporterContext context;
    private char csvDelimiter;
    private FileImportParser<T> parser;
    private FileImportProcessor<T> processor;

    public static <T extends FileImportRecord> Builder<T> withParser(FileImportParser<T> parser){
        Builder<T> builder = new Builder<>();
        builder.importer.parser = parser;
        return builder;
    }

    private DeviceDataCsvImporter(){}

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
                } catch (ValueParserException e){

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
