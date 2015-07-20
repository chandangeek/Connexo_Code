package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.importers.impl.properties.DateFormatPropertySpec;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.device.data.importers.impl.properties.TimeZonePropertySpec;

import java.util.List;
import java.util.Optional;

public enum DeviceDataImporterProperty {
    DELIMITER("delimiter") {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return context.getPropertySpecService().stringPropertySpecWithValuesAndDefaultValue(getPropertyKey(),
                    true,
                    String.valueOf(AbstractDeviceDataFileImporterFactory.SEMICOLON),
                    String.valueOf(AbstractDeviceDataFileImporterFactory.SEMICOLON),
                    String.valueOf(AbstractDeviceDataFileImporterFactory.COMMA));
        }
    },
    DATE_FORMAT("dateFormat") {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return new DateFormatPropertySpec(getPropertyKey(), context.getThesaurus());
        }
    },
    TIME_ZONE("timeZone") {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return new TimeZonePropertySpec(getPropertyKey(), context.getThesaurus());
        }
    },
    NUMBER_FORMAT("numberFormat") {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return context.getPropertySpecService().stringReferencePropertySpec(getPropertyKey(), true,
                    new SupportedNumberFormat.SupportedNumberFormatFinder(), SupportedNumberFormat.valuesAsInfo());
        }

        @Override
        public void validateProperties(List<FileImporterProperty> properties, DeviceDataImporterContext context) {
            Optional<FileImporterProperty> delimiter = properties.stream().filter(p -> DELIMITER.isMatchKey(p.getName())).findFirst();
            Optional<FileImporterProperty> numberFormat = properties.stream().filter(p -> NUMBER_FORMAT.isMatchKey(p.getName())).findFirst();
            if (delimiter.isPresent() && numberFormat.isPresent()) {
                char delimiterValue = ((String) delimiter.get().getValue()).charAt(0);
                SupportedNumberFormat numberFormatValue = ((SupportedNumberFormat.SupportedNumberFormatInfo) numberFormat.get().getValue()).getFormat();
                if (delimiterValue == numberFormatValue.getDecimalSeparator() ||
                        (numberFormatValue.getGroupSeparator() != null && delimiterValue == numberFormatValue.getGroupSeparator().charValue())) {
                    throw new LocalizedFieldValidationException(MessageSeeds.NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER, "properties." + getPropertyKey());
                }
            }
        }
    },
    ;

    private String propertySuffix;

    DeviceDataImporterProperty(String propertySuffix) {
        this.propertySuffix = propertySuffix;
    }

    public String getPropertyKey() {
        return AbstractDeviceDataFileImporterFactory.IMPORTER_FACTORY_PROPERTY_PREFIX + "." + this.propertySuffix;
    }

    public boolean isMatchKey(String candidate){
        return candidate != null && getPropertyKey().equals(candidate);
    }

    public abstract PropertySpec getPropertySpec(DeviceDataImporterContext context);

    public void validateProperties(List<FileImporterProperty> properties, DeviceDataImporterContext context){
        // do nothing by default
    }
}
