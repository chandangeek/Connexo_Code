/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.NonOrBigDecimalValueFactory;
import com.elster.jupiter.properties.NonOrBigDecimalValueProperty;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TwoValuesAbsoluteDifference;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validators.MissingRequiredProperty;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by dantonov on 23.04.2017.
 */
public abstract class MainCheckAbstractValidator extends AbstractValidator {

    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDM);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DefaultDateTimeFormatters.mediumDate()
            .withShortTime()
            .build()
            .withZone(ZoneId
                    .systemDefault())
            .withLocale(Locale.ENGLISH);

    static final String CHECK_PURPOSE = "checkPurpose";
    static final String MAX_ABSOLUTE_DIFF = "maximumAbsoluteDifference";
    static final String MIN_THRESHOLD = "minThreshold";
    static final String PASS_IF_NO_REF_DATA = "passIfNoRefData";
    static final String USE_VALIDATED_DATA = "useValidatedData";

    protected MetrologyConfigurationService metrologyConfigurationService;
    protected ValidationService validationService;

    protected MetrologyPurpose checkChannelPurpose;
    protected TwoValuesDifference maxAbsoluteDifference;
    protected Boolean passIfNoRefData;
    protected Boolean useValidatedData;
    protected NonOrBigDecimalValueProperty minThreshold;

    protected ReadingType readingType;
    // interval to log failed validation
    protected Range<Instant> failedValidatonInterval;

    protected UsagePoint validatedUsagePoint;
    protected String validatedUsagePointName;

    protected ValidationResult preparedValidationResult;

    private Logger logger;

    public MainCheckAbstractValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService) {
        super(thesaurus, propertySpecService);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
    }

    public MainCheckAbstractValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService, ValidationService validationService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.validationService = validationService;
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        this.readingType = readingType;
        this.failedValidatonInterval = interval;

        //LoggingContext.get().info(getLogger(), "init main check");

        // 1. parse validator parameters
        checkChannelPurpose = getCheckPurposeProperty();
        maxAbsoluteDifference = getMaxAbsoluteDiffProperty();
        minThreshold = getMinThresholdProperty();
        passIfNoRefData = getPassIfNoRefDataProperty();
        useValidatedData = getUseValidatedDataProperty();
    }

    protected void initUsagePointName(Channel channel) throws InitCancelException{
        Optional<UsagePoint> usagePoint = channel.getChannelsContainer()
                .getUsagePoint();

        if (!usagePoint.isPresent()) {
            LoggingContext.get()
                    .severe(getLogger(), getThesaurus().getFormat(MessageSeeds.VALIDATOR_INIT_MISC_NO_UP)
                            .format(rangeToString(failedValidatonInterval), getDisplayName(), readingType.getFullAliasName()));
            preparedValidationResult = ValidationResult.NOT_VALIDATED;
            throw new InitCancelException();
        }

        validatedUsagePoint = usagePoint.get();
        validatedUsagePointName = validatedUsagePoint.getName();
    }



    private String rangeToString(Range<Instant> range) {
        Instant lowerBound = null;
        if (range.hasLowerBound()) {
            lowerBound = range.lowerEndpoint();
        }

        Instant upperBound = null;
        if (range.hasUpperBound()) {
            upperBound = range.upperEndpoint();
        }

        String lower = lowerBound != null ? DATE_TIME_FORMATTER.format(lowerBound) : "-\"\\u221E\\t\"";
        String upper = upperBound != null ? DATE_TIME_FORMATTER.format(upperBound) : "+\"\\u221E\\t\"";
        return "\"" + lower + " until " + upper + "\"";
    }

    PropertySpec buildMaxAbsoluteDiffPropertySpec() {
        return getPropertySpecService()
                .specForValuesOf(new TwoValuesDifferenceValueFactory())
                .named(MAX_ABSOLUTE_DIFF, MainCheckValidator.TranslationKeys.MAX_ABSOLUTE_DIFF)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(new TwoValuesAbsoluteDifference() {{
                    value = new BigDecimal(0);
                }})
                .finish();
    }

    TwoValuesDifference getMaxAbsoluteDiffProperty() {
        TwoValuesDifference value = (TwoValuesDifference) properties.get(MAX_ABSOLUTE_DIFF);
        if (value == null) {
            throw new MissingRequiredProperty(getThesaurus(), MAX_ABSOLUTE_DIFF);
        }
        return value;
    }

    PropertySpec buildMinThresholdPropertySpec() {
        return getPropertySpecService()
                .specForValuesOf(new NonOrBigDecimalValueFactory())
                .named(MIN_THRESHOLD, MainCheckValidator.TranslationKeys.MIN_THRESHOLD)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(new NonOrBigDecimalValueProperty())
                .finish();
    }

    NonOrBigDecimalValueProperty getMinThresholdProperty() {
        NonOrBigDecimalValueProperty value = (NonOrBigDecimalValueProperty) properties.get(MIN_THRESHOLD);
        if (value == null) {
            throw new MissingRequiredProperty(getThesaurus(), MIN_THRESHOLD);
        }
        return value;
    }

    PropertySpec buildPassIfNoRefDataPropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(PASS_IF_NO_REF_DATA, MainCheckValidator.TranslationKeys.PASS_IF_NO_REF_DATA)
                .fromThesaurus(this.getThesaurus())
                .finish();
    }

    boolean getPassIfNoRefDataProperty() {
        Boolean value = (Boolean) properties.get(PASS_IF_NO_REF_DATA);
        if (value == null) {
            throw new MissingRequiredProperty(getThesaurus(), PASS_IF_NO_REF_DATA);
        }
        return value;
    }

    PropertySpec buildUseValidatedDataPropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(USE_VALIDATED_DATA, MainCheckValidator.TranslationKeys.USE_VALIDATED_DATA)
                .fromThesaurus(this.getThesaurus())
                .finish();
    }

    boolean getUseValidatedDataProperty() {
        Boolean value = (Boolean) properties.get(USE_VALIDATED_DATA);
        if (value == null) {
            throw new MissingRequiredProperty(getThesaurus(), USE_VALIDATED_DATA);
        }
        return value;
    }

    PropertySpec buildCheckPurposePropertySpec() {
        List<MetrologyPurpose> metrologyPurposes = metrologyConfigurationService.getMetrologyPurposes();
        return getPropertySpecService()
                .referenceSpec(MetrologyPurpose.class)
                .named(CHECK_PURPOSE, MainCheckValidator.TranslationKeys.CHECK_PURPOSE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(metrologyPurposes.get(0))
                .addValues(metrologyPurposes)
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
    }

    MetrologyPurpose getCheckPurposeProperty() {
        MetrologyPurpose value = (MetrologyPurpose) properties.get(CHECK_PURPOSE);
        if (value == null) {
            throw new MissingRequiredProperty(getThesaurus(), CHECK_PURPOSE);
        }
        return value;
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    @Override
    public List<TranslationKey> getExtraTranslationKeys() {
        return Stream.concat(Arrays.stream(TranslationKeys.values()), Arrays.stream(getAdditionalExtraTranslationKeys()))
                .map(c -> new MainCheckTranslationKey(c, getClassName()))
                .collect(Collectors.toList());
    }

    TranslationKey[] getAdditionalExtraTranslationKeys() {
        return new TranslationKey[0];
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        // this validator is not planned to be applied for registers
        // So, this method has no logic
        return ValidationResult.NOT_VALIDATED;
    }

    abstract String getClassName();


    Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    enum TranslationKeys implements TranslationKey {
        CHECK_PURPOSE(MainCheckAbstractValidator.CHECK_PURPOSE, "Check purpose"),
        MAX_ABSOLUTE_DIFF(MainCheckAbstractValidator.MAX_ABSOLUTE_DIFF, "Maximum absolute difference"),
        PASS_IF_NO_REF_DATA(MainCheckAbstractValidator.PASS_IF_NO_REF_DATA, "Pass if no reference data"),
        USE_VALIDATED_DATA(MainCheckAbstractValidator.USE_VALIDATED_DATA, "Use validated data"),
        MIN_THRESHOLD(MainCheckAbstractValidator.MIN_THRESHOLD, "Minimum threshold");

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }
    }

    protected class MainCheckTranslationKey implements TranslationKey {

        String className;
        TranslationKey translationKeys;

        MainCheckTranslationKey(TranslationKey translationKeys, String className) {
            this.className = className;
            this.translationKeys = translationKeys;
        }

        @Override
        public String getKey() {
            return className + "." + translationKeys.getKey();
        }

        @Override
        public String getDefaultFormat() {
            return this.translationKeys.getDefaultFormat();
        }
    }

    protected class InitCancelException extends Exception{

    }
}
