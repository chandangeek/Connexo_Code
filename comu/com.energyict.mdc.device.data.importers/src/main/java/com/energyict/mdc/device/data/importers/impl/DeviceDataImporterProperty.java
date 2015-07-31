package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.StringReferenceFactory;
import com.elster.jupiter.users.FormatKey;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.energyict.mdc.device.data.importers.impl.properties.DateFormatPropertySpec;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.device.data.importers.impl.properties.TimeZonePropertySpec;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
            PropertySpecBuilder builder = context.getPropertySpecService()
                    .newPropertySpecBuilder(new StringReferenceFactory(new SupportedNumberFormat.SupportedNumberFormatFinder()));
            builder.name(getPropertyKey())
                    .markRequired()
                    .addValues(SupportedNumberFormat.valuesAsInfo())
                    .markExhaustive();
            SupportedNumberFormat defaultNumberFormat = getDefaultNumberFormat(context);
            if (defaultNumberFormat != null) {
                builder.setDefaultValue(new SupportedNumberFormat.SupportedNumberFormatInfo(defaultNumberFormat));
            }
            return builder.finish();
        }

        private SupportedNumberFormat getDefaultNumberFormat(DeviceDataImporterContext context) {
            Optional<User> user = context.getUserService().findUser(context.getThreadPrincipalService().getPrincipal().getName());
            if (user.isPresent()) {
                Optional<UserPreference> decimalSeparator = context.getUserService().getUserPreferencesService().getPreferenceByKey(user.get(), FormatKey.DECIMAL_SEPARATOR);
                Optional<UserPreference> thousandsSeparator = context.getUserService().getUserPreferencesService().getPreferenceByKey(user.get(), FormatKey.THOUSANDS_SEPARATOR);
                Stream<SupportedNumberFormat> stream = Arrays.asList(SupportedNumberFormat.values()).stream();
                if (decimalSeparator.isPresent()) {
                    stream = stream.filter(numberFormat -> numberFormat.getDecimalSeparator().toString().equals(decimalSeparator.get().getFormatFE()));
                }
                stream = stream.filter(numberFormat -> {
                    if (thousandsSeparator.isPresent()) {
                        return numberFormat.hasGroupSeparator() && numberFormat.getGroupSeparator().toString().equals(thousandsSeparator.get().getFormatFE());
                    } else {
                        return !numberFormat.hasGroupSeparator();
                    }
                });
                return stream.findFirst().orElse(null);
            }
            return null;
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
    },;

    private String propertySuffix;

    DeviceDataImporterProperty(String propertySuffix) {
        this.propertySuffix = propertySuffix;
    }

    public String getPropertyKey() {
        return AbstractDeviceDataFileImporterFactory.IMPORTER_FACTORY_PROPERTY_PREFIX + "." + this.propertySuffix;
    }

    public boolean isMatchKey(String candidate) {
        return candidate != null && getPropertyKey().equals(candidate);
    }

    public abstract PropertySpec getPropertySpec(DeviceDataImporterContext context);

    public void validateProperties(List<FileImporterProperty> properties, DeviceDataImporterContext context) {
        // do nothing by default
    }
}
