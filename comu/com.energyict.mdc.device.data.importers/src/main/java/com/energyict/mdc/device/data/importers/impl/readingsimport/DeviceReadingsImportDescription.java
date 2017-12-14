/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
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
        // Device mRID or name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceIdentifier)
                .markMandatory()
                .build());
        // Reading date
        fields.add(CommonField.withParser(dateParser)
                .withSetter(record::setReadingDateTime)
                .markMandatory()
                .build());
        // Reading type mRID or OBIS code or Channel/register name
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::addReadingType)
                .markMandatory()
                .markRepetitive()
                .build());
        // Reading value
        fields.add(CommonField.withParser(bigDecimalParser)
                .withSetter(record::addReadingValue)
                .markMandatory()
                .markRepetitive()
                .build());
        return fields;
    }
}
