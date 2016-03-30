package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportDescription;
import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.BigDecimalParser;
import com.energyict.mdc.device.data.importers.impl.parsers.BooleanParser;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeviceInstallationImportDescription implements FileImportDescription<DeviceInstallationImportRecord> {

    private DateParser dateParser;
    private volatile DeviceDataImporterContext context;

    public DeviceInstallationImportDescription(String dateFormat, String timeZone, DeviceDataImporterContext context) {
        this.dateParser = new DateParser(dateFormat, timeZone);
        this.context = context;
    }

    @Override
    public DeviceInstallationImportRecord getFileImportRecord() {
        return new DeviceInstallationImportRecord();
    }

    public List<FileImportField<?>> getFields(DeviceInstallationImportRecord record) {
        List<FileImportField<?>> fields = new ArrayList<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        BigDecimalParser bigDecimalParser = new BigDecimalParser(SupportedNumberFormat.FORMAT3);
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setDeviceMRID)
                .markMandatory()
                .build());
        fields.add(CommonField.withParser(dateParser)
                .withSetter(record::setTransitionDate)
                .markMandatory()
                .build());
        IntStream.range(0, 2).forEach(cnt ->
                fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setGeoCoordinates)
                .build()));
        context.getMeteringService().getLocationTemplate().getRankings().entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .forEach(s-> {
                    if(context.getMeteringService().getLocationTemplate().getMandatoryFieldsNames().stream().anyMatch(m -> m.equals(s))){
                        fields.add(CommonField.withParser(stringParser)
                                .withSetter(record::addLocation)
                                .markMandatory()
                                .build());
                    }else{
                        fields.add(CommonField.withParser(stringParser)
                                .withSetter(record::addLocation)
                                .build());
                    }
                });
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setMasterDeviceMrid)
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setUsagePointMrid)
                .build());
        fields.add(CommonField.withParser(stringParser)
                .withSetter(record::setServiceCategory)
                .build());
        fields.add(CommonField.withParser(new BooleanParser())
                .withSetter(record::setInstallInactive)
                .build());
        fields.add(CommonField.withParser(dateParser)
                .withSetter(record::setStartValidationDate)
                .build());
        fields.add(CommonField.withParser(bigDecimalParser)
                .withSetter(record::setMultiplier)
                .build());
        return fields;
    }
}
