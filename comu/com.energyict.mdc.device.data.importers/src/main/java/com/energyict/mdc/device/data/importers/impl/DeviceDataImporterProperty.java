/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.users.PreferenceType;
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
    DELIMITER(TranslationKeys.DEVICE_DATA_IMPORTER_DELIMITER, TranslationKeys.DEVICE_DATA_IMPORTER_DELIMITER_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return context.getPropertySpecService()
                    .stringSpec()
                    .named(this.getPropertyKey(), this.getNameTranslationKey())
                    .describedAs(this.getDescriptionTranslationKey())
                    .fromThesaurus(context.getThesaurus())
                    .markRequired()
                    .markExhaustive()
                    .addValues(
                            String.valueOf(AbstractDeviceDataFileImporterFactory.SEMICOLON),
                            String.valueOf(AbstractDeviceDataFileImporterFactory.COMMA))
                    .setDefaultValue(String.valueOf(AbstractDeviceDataFileImporterFactory.SEMICOLON))
                    .finish();
        }
    },
    DATE_FORMAT(TranslationKeys.DEVICE_DATA_IMPORTER_DATE_FORMAT, TranslationKeys.DEVICE_DATA_IMPORTER_DATE_FORMAT_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return new DateFormatPropertySpec(this.getPropertyKey(), this.getNameTranslationKey(), context.getThesaurus());
        }
    },
    TIME_ZONE(TranslationKeys.DEVICE_DATA_IMPORTER_TIMEZONE, TranslationKeys.DEVICE_DATA_IMPORTER_TIMEZONE_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return new TimeZonePropertySpec(this.getPropertyKey(), this.getNameTranslationKey(), context.getThesaurus(), context.getClock());
        }
    },
    NUMBER_FORMAT(TranslationKeys.DEVICE_DATA_IMPORTER_NUMBER_FORMAT, TranslationKeys.DEVICE_DATA_IMPORTER_NUMBER_FORMAT_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            PropertySpecBuilder builder =
                context.getPropertySpecService()
                    .specForValuesOf(new SupportedNumberFormat.SupportedNumberFormatValueFactory())
                    .named(this.getPropertyKey(), this.getNameTranslationKey())
                    .describedAs(this.getDescriptionTranslationKey())
                    .fromThesaurus(context.getThesaurus())
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
                Optional<UserPreference> decimalSeparator = context.getUserService().getUserPreferencesService().getPreferenceByKey(user.get(), PreferenceType.DECIMAL_SEPARATOR);
                Optional<UserPreference> thousandsSeparator = context.getUserService().getUserPreferencesService().getPreferenceByKey(user.get(), PreferenceType.THOUSANDS_SEPARATOR);
                Stream<SupportedNumberFormat> stream = Arrays.asList(SupportedNumberFormat.values()).stream();
                if (decimalSeparator.isPresent()) {
                    stream = stream.filter(numberFormat -> numberFormat.getDecimalSeparator().toString().equals(decimalSeparator.get().getDisplayFormat()));
                }
                stream = stream.filter(numberFormat -> {
                    if (thousandsSeparator.isPresent()) {
                        return numberFormat.hasGroupSeparator() && numberFormat.getGroupSeparator().toString().equals(thousandsSeparator.get().getDisplayFormat());
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
                    throw new LocalizedFieldValidationException(MessageSeeds.NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER, "properties." + this.getPropertyKey());
                }
            }
        }
    };

    private final TranslationKeys nameTranslationKey;
    private final TranslationKeys descriptionTranslationKey;

    DeviceDataImporterProperty(TranslationKeys nameTranslationKey, TranslationKeys descriptionTranslationKey) {
        this.nameTranslationKey = nameTranslationKey;
        this.descriptionTranslationKey = descriptionTranslationKey;
    }

    public TranslationKeys getNameTranslationKey() {
        return nameTranslationKey;
    }

    public TranslationKeys getDescriptionTranslationKey() {
        return descriptionTranslationKey;
    }

    public String getPropertyKey() {
        return AbstractDeviceDataFileImporterFactory.IMPORTER_FACTORY_PROPERTY_PREFIX + "." + this.nameTranslationKey.getKey();
    }

    public boolean isMatchKey(String candidate) {
        return candidate != null && this.getPropertyKey().equals(candidate);
    }

    public abstract PropertySpec getPropertySpec(DeviceDataImporterContext context);

    public void validateProperties(List<FileImporterProperty> properties, DeviceDataImporterContext context) {
        // do nothing by default
    }

}