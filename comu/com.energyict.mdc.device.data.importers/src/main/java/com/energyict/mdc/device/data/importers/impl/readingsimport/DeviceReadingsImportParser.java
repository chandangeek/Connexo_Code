package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.energyict.mdc.device.data.importers.impl.FileImportParser;
import com.energyict.mdc.device.data.importers.impl.exceptions.ParserException;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;

public class DeviceReadingsImportParser implements FileImportParser<DeviceReadingsImportRecord> {

    private DateParser dateParser;
    private SupportedNumberFormat numberFormat;

    public DeviceReadingsImportParser(String dateFormat, String timeZone, SupportedNumberFormat numberFormat) {
        this.dateParser = new DateParser(dateFormat, timeZone);
        this.numberFormat = numberFormat;
    }

    @Override
    public DeviceReadingsImportRecord parse(CSVRecord csvRecord) throws ParserException {
        DeviceReadingsImportRecord importRecord = new DeviceReadingsImportRecord(csvRecord.getRecordNumber());

        Iterator<String> columns = csvRecord.iterator();
        if(!columns.hasNext()) {
            //throw falat parset exception Format error for line X: missing device MRID
        }
        importRecord.setDeviceMRID(columns.next());
        if (!columns.hasNext()) {
            //throw exception Format error for line X: missing column Y.
        }
        importRecord.setReadingDateTime(dateParser.parse(columns.next()));
        while(columns.hasNext()) {
            String readingType = columns.next();
            if (!columns.hasNext()) {
                //throw exception
            }
        }
        return importRecord;
    }
}
