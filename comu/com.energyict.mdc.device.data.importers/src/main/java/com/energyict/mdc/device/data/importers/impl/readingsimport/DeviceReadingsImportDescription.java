package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.BigDecimalParser;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import java.util.ArrayList;
import java.util.List;

public class DeviceReadingsImportDescription implements FileImportDescription<DeviceReadingsImportRecord> {

    private final DateParser dateParser;
    private final BigDecimalParser bigDecimalParser;

    public DeviceReadingsImportDescription(String dateFormat, String timeZone, SupportedNumberFormat numberFormat) {
        this.dateParser = new DateParser(dateFormat, timeZone);
        this.bigDecimalParser = new BigDecimalParser(numberFormat);
    }

    @Override
    public DeviceReadingsImportRecord getFileImportRecord() {
        return new DeviceReadingsImportRecord();
    }

    @Override
    public List<FileImportField<?>> getFields(DeviceReadingsImportRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        //Device mRID
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::setDeviceMRID)
                .markMandatory()
                .build());
        //Reading date
        fields.add(CommonField.withParser(dateParser)
                .withConsumer(record::setReadingDateTime)
                .markMandatory()
                .build());
        //Reading type mRID
        fields.add(CommonField.withParser(stringParser)
                .withConsumer(record::addReadingType)
                .markMandatory()
                .markRepetitive()
                .build());
        //Reading value
        fields.add(CommonField.withParser(bigDecimalParser)
                .withConsumer(record::addReadingValue)
                .markMandatory()
                .markRepetitive()
                .build());
        return fields;
    }
}
