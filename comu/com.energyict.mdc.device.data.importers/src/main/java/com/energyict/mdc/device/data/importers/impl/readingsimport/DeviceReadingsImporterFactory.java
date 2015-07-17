package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.properties.DateFormatPropertySpec;
import com.energyict.mdc.device.data.importers.impl.DeviceDataCsvImporter;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.device.data.importers.impl.properties.TimeZonePropertySpec;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.device.data.importers.impl.DeviceReadingsImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)
public class DeviceReadingsImporterFactory extends AbstractDeviceDataFileImporterFactory {

    public static final String NAME = "DeviceReadingsImporterFactory";

    public static final String DELIMITER = NAME + ".delimiter";
    public static final String DATE_FORMAT = NAME + ".dateFormat";
    public static final String TIME_ZONE = NAME + ".timeZone";
    public static final String NUMBER_FORMAT = NAME + ".numberFormat";

    public DeviceReadingsImporterFactory() {
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        String delimiter = (String) properties.get(DELIMITER);
        String dateFormat = (String) properties.get(DATE_FORMAT);
        String timeZone = (String) properties.get(TIME_ZONE);
        SupportedNumberFormat numberFormat = ((SupportedNumberFormat.SupportedNumberFormatInfo) properties.get(NUMBER_FORMAT)).getFormat();

        DeviceReadingsImportParser parser = new DeviceReadingsImportParser(dateFormat, timeZone, numberFormat);
        DeviceReadingsImportProcessor processor = new DeviceReadingsImportProcessor();
        return new DeviceDataCsvImporter<DeviceReadingsImportRecord>(delimiter.charAt(0), parser, processor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.DEVICE_READINGS_IMPORTER.getDefaultFormat();
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {
        Optional<FileImporterProperty> delimiter = properties.stream().filter(p -> DELIMITER.equals(p.getName())).findFirst();
        Optional<FileImporterProperty> numberFormat = properties.stream().filter(p -> NUMBER_FORMAT.equals(p.getName())).findFirst();
        if (delimiter.isPresent() && numberFormat.isPresent()) {
            String delimiterValue = (String) delimiter.get().getValue();
            SupportedNumberFormat numberFormatValue = ((SupportedNumberFormat.SupportedNumberFormatInfo) numberFormat.get().getValue()).getFormat();
            if (COMMA.equals(delimiterValue) && (COMMA.equals(numberFormatValue.getDecimalSeparator()) ||
                    (numberFormatValue.getGroupSeparator() != null && COMMA.equals(numberFormatValue.getGroupSeparator())))) {
                throw new LocalizedFieldValidationException(MessageSeeds.NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER, "properties." + NUMBER_FORMAT);
            }
        }
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
            case DELIMITER:
                return TranslationKeys.DEVICE_READINGS_IMPORTER_DELIMITER.getKey();
            case DATE_FORMAT:
                return TranslationKeys.DEVICE_READINGS_IMPORTER_DATEFORMAT.getDefaultFormat();
            case TIME_ZONE:
                return TranslationKeys.DEVICE_READINGS_IMPORTER_TIMEZONE.getDefaultFormat();
            case NUMBER_FORMAT:
                return TranslationKeys.DEVICE_READINGS_IMPORTER_NUMBERFORMAT.getDefaultFormat();
            default:
                return "";
        }
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(DELIMITER, DATE_FORMAT, TIME_ZONE, NUMBER_FORMAT);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue(DELIMITER, true, SEMICOLON, SEMICOLON, COMMA));
        builder.add(new DateFormatPropertySpec(DATE_FORMAT, thesaurus));//TODO set default value depending on application server timezone
        builder.add(new TimeZonePropertySpec(TIME_ZONE, thesaurus));
        builder.add(propertySpecService.stringReferencePropertySpec(NUMBER_FORMAT, true,
                new SupportedNumberFormat.SupportedNumberFormatFinder(), SupportedNumberFormat.valuesAsInfo()));//TODO set default value depending on user preferences
        return builder.build();
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
        super.setThesaurus(nlsService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }
}
