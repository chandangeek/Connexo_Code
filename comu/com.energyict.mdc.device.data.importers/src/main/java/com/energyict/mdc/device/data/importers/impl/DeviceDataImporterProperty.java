/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.users.PreferenceType;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.energyict.mdc.device.data.importers.impl.devices.topology.DeviceTopologyImporterFactory;
import com.energyict.mdc.device.data.importers.impl.properties.DateFormatPropertySpec;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.device.data.importers.impl.properties.TimeZonePropertySpec;

import org.json.JSONObject;

import java.security.Principal;
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
    SECURITY_ACCESSOR_MAPPING(TranslationKeys.DEVICE_CERTIFICATES_IMPORTER_SECURITY_ACCESSOR_MAPPING, TranslationKeys.DEVICE_CERTIFICATES_IMPORTER_SECURITY_ACCESSOR_MAPPING_DESCRIPTION) {

        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return context.getPropertySpecService()
                    .stringSpec()
                    .named(this.getPropertyKey(), this.getNameTranslationKey())
                    .describedAs(this.getDescriptionTranslationKey())
                    .fromThesaurus(context.getThesaurus())
                    .finish();
        }

        @Override
        public void validateProperties(List<FileImporterProperty> properties, DeviceDataImporterContext context) {
            Optional<FileImporterProperty> securityAccesorMapping = properties.stream().filter(p -> SECURITY_ACCESSOR_MAPPING.isMatchKey(p.getName())).findFirst();
            if(securityAccesorMapping.isPresent() && !((String)securityAccesorMapping.get().getValue()).isEmpty()){
                try{
                    new JSONObject((String) securityAccesorMapping.get().getValue());
                } catch (Exception ex){
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_JSON, securityAccesorMapping.get().getName()).fromSubField("properties");
                }
            }

        }

    },
    SYSTEM_TITLE_PROPERTY_NAME(TranslationKeys.DEVICE_CERTIFICATES_IMPORTER_SYSTEM_TILE_PROPERTY_NAME, TranslationKeys.DEVICE_CERTIFICATES_IMPORTER_SYSTEM_TILE_PROPERTY_NAME_DESCRIPTION) {

        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return context.getPropertySpecService()
                    .stringSpec()
                    .named(this.getPropertyKey(), this.getNameTranslationKey())
                    .describedAs(this.getDescriptionTranslationKey())
                    .fromThesaurus(context.getThesaurus())
                    .setDefaultValue("DeviceSystemTitle")
                    .finish();
        }
    },
    NUMBER_FORMAT(TranslationKeys.DEVICE_DATA_IMPORTER_NUMBER_FORMAT, TranslationKeys.DEVICE_DATA_IMPORTER_NUMBER_FORMAT_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            PropertySpecBuilder<HasIdAndName> builder =
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
            Optional<User> user = Optional.ofNullable(context.getThreadPrincipalService().getPrincipal())
                    .map(Principal::getName)
                    .flatMap(context.getUserService()::findUser);
            if (user.isPresent()) {
                Optional<UserPreference> decimalSeparator = context.getUserService().getUserPreferencesService().getPreferenceByKey(user.get(), PreferenceType.DECIMAL_SEPARATOR);
                Optional<UserPreference> thousandsSeparator = context.getUserService().getUserPreferencesService().getPreferenceByKey(user.get(), PreferenceType.THOUSANDS_SEPARATOR);
                Stream<SupportedNumberFormat> stream = Arrays.stream(SupportedNumberFormat.values());
                if (decimalSeparator.isPresent()) {
                    stream = stream.filter(numberFormat -> numberFormat.getDecimalSeparator().toString().equals(decimalSeparator.get().getDisplayFormat()));
                }
                stream = stream.filter(numberFormat -> thousandsSeparator
                        .map(userPreference ->numberFormat.hasGroupSeparator() && numberFormat.getGroupSeparator().toString().equals(userPreference.getDisplayFormat()))
                        .orElseGet(() -> !numberFormat.hasGroupSeparator()));
                return stream.findFirst().orElse(null);
            }
            return null;
        }

        @Override
        public void validateProperties(List<FileImporterProperty> properties, DeviceDataImporterContext context) {
            Optional<FileImporterProperty> delimiter = properties.stream().filter(p -> DELIMITER.isMatchKey(p.getName())).findFirst();
            if(delimiter.isPresent() && ((String)delimiter.get().getValue()).isEmpty()){
                throw new LocalizedFieldValidationException(MessageSeeds.CAN_NOT_BE_SPACE_OR_EMPTY,"properties." + delimiter.get().getName());
            }
            Optional<FileImporterProperty> numberFormat = properties.stream().filter(p -> NUMBER_FORMAT.isMatchKey(p.getName())).findFirst();
            if (delimiter.isPresent() && !"".equals(delimiter.get().getValue()) && numberFormat.isPresent()) {
                char delimiterValue = ((String) delimiter.get().getValue()).charAt(0);
                SupportedNumberFormat numberFormatValue = ((SupportedNumberFormat.SupportedNumberFormatInfo) numberFormat.get().getValue()).getFormat();
                if (delimiterValue == numberFormatValue.getDecimalSeparator() ||
                        (numberFormatValue.getGroupSeparator() != null && delimiterValue == numberFormatValue.getGroupSeparator())) {
                    throw new LocalizedFieldValidationException(MessageSeeds.NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER, "properties." + this.getPropertyKey());
                }
            }
        }
    },

    DEVICE_IDENTIFIER(TranslationKeys.DEVICE_TOPOLOGY_IMPORTER_DEVICE_IDENTIFIER, TranslationKeys.DEVICE_TOPOLOGY_IMPORTER_DEVICE_IDENTIFIER_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return context.getPropertySpecService()
                    .stringSpec()
                    .named(this.getPropertyKey(), this.getNameTranslationKey())
                    .describedAs(this.getDescriptionTranslationKey())
                    .fromThesaurus(context.getThesaurus())
                    .markRequired()
                    .markExhaustive(PropertySelectionMode.COMBOBOX)
                    .addValues( DeviceTopologyImporterFactory.DEVICE_IDENTIFIER_SERIAL, DeviceTopologyImporterFactory.DEVICE_IDENTIFIER_NAME)
                    .setDefaultValue(DeviceTopologyImporterFactory.DEVICE_IDENTIFIER_SERIAL)
                    .finish();
        }
    },

    ALLOW_REASSIGNING(TranslationKeys.DEVICE_TOPOLOGY_IMPORTER_ALLOW_REASSIGNING, TranslationKeys.DEVICE_TOPOLOGY_IMPORTER_ALLOW_REASSIGNING_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(DeviceDataImporterContext context) {
            return context.getPropertySpecService()
                    .booleanSpec()
                    .named(this.getPropertyKey(), this.getNameTranslationKey())
                    .describedAs(this.getDescriptionTranslationKey())
                    .fromThesaurus(context.getThesaurus())
                    .setDefaultValue(Boolean.FALSE)
                    .finish();
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
