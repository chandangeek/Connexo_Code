package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.energyict.mdc.device.data.importers.impl.FileImportParser;
import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import com.energyict.mdc.device.data.importers.impl.parsers.BigDecimalParser;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import org.apache.commons.csv.CSVRecord;

import java.math.BigDecimal;
import java.util.Iterator;

public class DeviceReadingsImportParser implements FileImportParser<DeviceReadingsImportRecord> {

    private DateParser dateParser;
    private BigDecimalParser bigDecimalParser;

    public DeviceReadingsImportParser(String dateFormat, String timeZone, SupportedNumberFormat numberFormat) {
        this.dateParser = new DateParser(dateFormat, timeZone);
        this.bigDecimalParser = new BigDecimalParser(numberFormat);
    }

    @Override
    public DeviceReadingsImportRecord parse(CSVRecord csvRecord) throws FileImportParserException {
        DeviceReadingsImportRecord importRecord = new DeviceReadingsImportRecord(csvRecord.getRecordNumber());

        Iterator<String> fields = csvRecord.iterator();
        if(!fields.hasNext()) {
//            throw new ParserException()
            //throw parset exception Format error for line X: missing device MRID
        }
        importRecord.setDeviceMRID(fields.next());
        if (!fields.hasNext()) {
            //throw exception Format error for line X: missing column Y.
        }
        importRecord.setReadingDateTime(dateParser.parse(fields.next()));
        while(fields.hasNext()) {
            String readingType = fields.next();
            if (!fields.hasNext()) {
                //throw exception
            }
            BigDecimal readingValue = bigDecimalParser.parse(fields.next());
            importRecord.addReading(readingType, readingValue);
        }
        return importRecord;
    }
}
