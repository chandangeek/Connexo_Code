/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;


import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.metering.imports.impl.properties.DateFormatPropertySpec;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;
import com.elster.jupiter.metering.imports.impl.properties.TimeZonePropertySpec;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.users.PreferenceType;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public enum DataImporterProperty {
    DELIMITER(TranslationKeys.Labels.DATA_IMPORTER_DELIMITER, TranslationKeys.Labels.DATA_IMPORTER_DELIMITER_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(MeteringDataImporterContext context) {
            return context.getPropertySpecService()
                    .stringSpec()
                    .named(this.getPropertyKey(), this.getNameTranslationKey())
                    .describedAs(this.getDescriptionTranslationKey())
                    .fromThesaurus(context.getThesaurus())
                    .markRequired()
                    .markExhaustive()
                    .addValues(
                            String.valueOf(AbstractFileImporterFactory.SEMICOLON),
                            String.valueOf(AbstractFileImporterFactory.COMMA))
                    .setDefaultValue(String.valueOf(AbstractFileImporterFactory.SEMICOLON))
                    .finish();
        }
    },
    DATE_FORMAT(TranslationKeys.Labels.DATA_IMPORTER_DATE_FORMAT, TranslationKeys.Labels.DATA_IMPORTER_DATE_FORMAT_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(MeteringDataImporterContext context) {
            return new DateFormatPropertySpec(this.getPropertyKey(), this.getNameTranslationKey(), context.getThesaurus());
        }
    },
    TIME_ZONE(TranslationKeys.Labels.DATA_IMPORTER_TIMEZONE, TranslationKeys.Labels.DATA_IMPORTER_TIMEZONE_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(MeteringDataImporterContext context) {
            return new TimeZonePropertySpec(this.getPropertyKey(), this.getNameTranslationKey(), context.getThesaurus(), context
                    .getClock());
        }
    },
    NUMBER_FORMAT(TranslationKeys.Labels.DATA_IMPORTER_NUMBER_FORMAT, TranslationKeys.Labels.DATA_IMPORTER_NUMBER_FORMAT_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(MeteringDataImporterContext context) {
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

        private SupportedNumberFormat getDefaultNumberFormat(MeteringDataImporterContext context) {
            Optional<User> user = Optional.ofNullable(context.getThreadPrincipalService().getPrincipal())
                    .map(Principal::getName)
                    .flatMap(context.getUserService()::findUser);
            if (user.isPresent()) {
                Optional<UserPreference> decimalSeparator = context.getUserService()
                        .getUserPreferencesService()
                        .getPreferenceByKey(user.get(), PreferenceType.DECIMAL_SEPARATOR);
                Optional<UserPreference> thousandsSeparator = context.getUserService()
                        .getUserPreferencesService()
                        .getPreferenceByKey(user.get(), PreferenceType.THOUSANDS_SEPARATOR);
                Stream<SupportedNumberFormat> stream = Arrays.stream(SupportedNumberFormat.values());
                if (decimalSeparator.isPresent()) {
                    stream = stream.filter(numberFormat -> numberFormat.getDecimalSeparator()
                            .toString()
                            .equals(decimalSeparator.get().getDisplayFormat()));
                }
                stream = stream.filter(numberFormat -> thousandsSeparator
                        .map(userPreference -> numberFormat.hasGroupSeparator() && numberFormat.getGroupSeparator().toString().equals(userPreference.getDisplayFormat()))
                        .orElseGet(() -> !numberFormat.hasGroupSeparator()));
                return stream.findFirst().orElse(null);
            }
            return null;
        }

        @Override
        public void validateProperties(List<FileImporterProperty> properties, MeteringDataImporterContext context) {
            Optional<FileImporterProperty> delimiter = properties.stream()
                    .filter(p -> DELIMITER.isMatchKey(p.getName()))
                    .findFirst();
            Optional<FileImporterProperty> numberFormat = properties.stream()
                    .filter(p -> NUMBER_FORMAT.isMatchKey(p.getName()))
                    .findFirst();
            if (delimiter.isPresent() && numberFormat.isPresent()) {
                char delimiterValue = ((String) delimiter.get().getValue()).charAt(0);
                SupportedNumberFormat numberFormatValue = ((SupportedNumberFormat.SupportedNumberFormatInfo) numberFormat
                        .get()
                        .getValue()).getFormat();
                if (delimiterValue == numberFormatValue.getDecimalSeparator() ||
                        (numberFormatValue.getGroupSeparator() != null && delimiterValue == numberFormatValue.getGroupSeparator())) {
                    throw new LocalizedFieldValidationException(MessageSeeds.NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER, "properties." + this
                            .getPropertyKey());
                }
            }
        }
    };

    private final TranslationKey nameTranslationKey;
    private final TranslationKey descriptionTranslationKey;

    DataImporterProperty(TranslationKey nameTranslationKey, TranslationKey descriptionTranslationKey) {
        this.nameTranslationKey = nameTranslationKey;
        this.descriptionTranslationKey = descriptionTranslationKey;
    }

    public TranslationKey getNameTranslationKey() {
        return nameTranslationKey;
    }

    public TranslationKey getDescriptionTranslationKey() {
        return descriptionTranslationKey;
    }

    public String getPropertyKey() {
        return AbstractFileImporterFactory.IMPORTER_FACTORY_PROPERTY_PREFIX + "." + this.nameTranslationKey.getKey();
    }

    public boolean isMatchKey(String candidate) {
        return candidate != null && this.getPropertyKey().equals(candidate);
    }

    public abstract PropertySpec getPropertySpec(MeteringDataImporterContext context);

    public void validateProperties(List<FileImporterProperty> properties, MeteringDataImporterContext context) {
        // do nothing by default
    }

}
