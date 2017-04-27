/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.NoneOrBigDecimal;
import com.elster.jupiter.properties.NoneOrBigDecimalValueFactory;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
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
import java.util.function.Function;
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
    protected NoneOrBigDecimal minThreshold;

    protected ReadingType readingType;
    // interval to log failed validation
    protected Range<Instant> failedValidatonInterval;

    protected UsagePoint validatingUsagePoint;
    protected String validatingUsagePointName;

    private Channel validatingChannel;
    protected MetrologyPurpose validatingPurpose;

    protected ValidationResult preparedValidationResult;

    protected Map<Instant, IntervalReadingRecord> checkReadingRecords;
    protected Map<Instant, ValidationResult> checkReadingRecordValidations;

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
        this.validatingChannel = channel;

        initValidatingPurpose();

        maxAbsoluteDifference = getMaxAbsoluteDiffProperty();
        minThreshold = getMinThresholdProperty();
        passIfNoRefData = getPassIfNoRefDataProperty();
        useValidatedData = getUseValidatedDataProperty();

    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {

        // verify predefined behaviour
        if (preparedValidationResult != null) {
            return preparedValidationResult;
        }

        IntervalReadingRecord checkIntervalReadingRecord = checkReadingRecords.get(intervalReadingRecord.getTimeStamp());

        return validate(intervalReadingRecord, checkIntervalReadingRecord);
    }

    private ValidationResult validate(IntervalReadingRecord mainReading, IntervalReadingRecord checkReading) {

        Instant timeStamp = mainReading.getTimeStamp();

        // [RULE CHECK] If no data is available on the check channel:
        if (checkReading == null) {
            // show log
            logFailure(InitCancelReason.REFERENCE_OUPUT_MISSING_OR_NOT_VALID.getProps().withReadingType(readingType));
            if (passIfNoRefData) {
                // [RULE ACTION] No further checks are done to the interval (marked as valid) and the rule moves to the next interval if Pass if no reference data is checked
                return ValidationResult.VALID;
            } else {
                // [RULE ACTION]  Stop the validation at the timestamp where the timestamp with the last reference data was found for the channel if Pass if no reference data is not checked
                prepareValidationResult(ValidationResult.NOT_VALIDATED, timeStamp);
                return ValidationResult.NOT_VALIDATED;
            }
        }

        // [RULE FLOW CHECK] Data is available on check output but not validated:
        ValidationResult checkReadingValidationResult = checkReadingRecordValidations.getOrDefault(checkReading
                .getTimeStamp(), ValidationResult.NOT_VALIDATED);
        if (checkReadingValidationResult != ValidationResult.VALID) {
            // show log
            if (useValidatedData) {
                logFailure(InitCancelReason.REFERENCE_OUPUT_MISSING_OR_NOT_VALID.getProps().withReadingType(readingType));
                // [RULE ACTION] Stop the validation at the timestamp where the timestamp with the last validated reference data was found for the channel if Use validated data is checked
                prepareValidationResult(ValidationResult.NOT_VALIDATED, timeStamp);
                return ValidationResult.NOT_VALIDATED;
            }   // else:
            // [RULE ACTION] Continue validation if Use validated data is unchecked
            // So, next checks will be applied

        }

        ComparingValues comparingValues = calculateComparingValues(mainReading, checkReading);

        if (!minThreshold.isNone()) {
            if (comparingValues.mainValue.compareTo(minThreshold.getValue()) <= 0 && comparingValues.checkValue.compareTo(minThreshold.getValue()) <= 0) {
                // [RULE FLOW ACTION] the check for the interval is marked valid and the validation moves to the next interval.
                return ValidationResult.VALID;
            }
        }

        BigDecimal differenceValue;


        if (TwoValuesDifference.Type.ABSOLUTE == maxAbsoluteDifference.getType()) {
            differenceValue = maxAbsoluteDifference.getValue();
        } else if (TwoValuesDifference.Type.RELATIVE == maxAbsoluteDifference.getType()) {
            differenceValue = comparingValues.mainValue.multiply(maxAbsoluteDifference.getValue()).multiply(new BigDecimal(0.01));
        } else {
            return ValidationResult.NOT_VALIDATED;
        }

        if (comparingValues.mainValue.subtract(comparingValues.checkValue).abs().compareTo(differenceValue) > 0) {
            return ValidationResult.SUSPECT;
        } else {
            return ValidationResult.VALID;
        }
    }

    private void prepareValidationResult(ValidationResult validationResult, Instant timeStamp) {
        preparedValidationResult = validationResult;

        failedValidatonInterval = Range.range(timeStamp, failedValidatonInterval.lowerBoundType(), failedValidatonInterval
                .upperEndpoint(), failedValidatonInterval.upperBoundType());

    }

    protected void initValidatingPurpose() {
        // find validating purpose
            if (validatingChannel.getChannelsContainer() instanceof MetrologyContractChannelsContainer){
                validatingPurpose = ((MetrologyContractChannelsContainer)validatingChannel.getChannelsContainer()).getMetrologyContract().getMetrologyPurpose();
            } else {
                throw new IllegalStateException("Validating channels container is not instance of MetrologyContractChannelsContainer");
            }
    }

    protected void initCheckData(UsagePoint referenceUsagePoint, ReadingType referenceReadingType) throws
            InitCancelException {

        List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMCList = referenceUsagePoint
                .getEffectiveMetrologyConfigurations(failedValidatonInterval);

        if (effectiveMCList.size() != 1) {
            LoggingContext.get()
                    .warning(getLogger(), getThesaurus().getFormat(MessageSeeds.VALIDATOR_MISC_NOT_ONE_EMC)
                            .format(rangeToString(failedValidatonInterval), getDisplayName(), referenceReadingType, effectiveMCList
                                    .size()));
            throw new InitCancelException(ValidationResult.NOT_VALIDATED);
        }

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = effectiveMCList.get(0);

        Optional<MetrologyContract> metrologyContract = effectiveMC.getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(contract -> contract.getMetrologyPurpose().equals(checkChannelPurpose))
                .findAny();

        // {RULE FLOW CHECK] specified purpose is not found on the usage point
        if (!metrologyContract.isPresent()) {
            // [RULE FLOW ACTION] Stop validation for the channel independently from Pass if no reference data field value (last check remains as before the validation), an error message appears in the log
            handleInitFail(InitCancelReason.NO_REFERENCE_PURPOSE_FOUND_ON_REFERENCE_USAGE_POINT
                    .getProps()
                    .withReadingType(referenceReadingType));
        }

        boolean checkOutputExistOnPurpose = false;

        Optional<ChannelsContainer> channelsContainerWithCheckChannel = effectiveMC.getChannelsContainer(metrologyContract
                .get());
        if (channelsContainerWithCheckChannel.isPresent()) {
            Optional<Channel> checkChannel = channelsContainerWithCheckChannel.get().getChannel(readingType);
            if (checkChannel.isPresent()) {
                checkOutputExistOnPurpose = true;
                // 3. prepare map of interval readings from check channel
                List<IntervalReadingRecord> checkChannelIntervalReadings = checkChannel.get()
                        .getIntervalReadings(failedValidatonInterval);
                checkReadingRecords = checkChannelIntervalReadings.stream()
                        .collect(Collectors.toMap(IntervalReadingRecord::getTimeStamp, Function.identity()));

                // 4. get validation statuses for check channel

                ValidationEvaluator evaluator = validationService.getEvaluator();
                checkReadingRecordValidations = evaluator.getValidationStatus(getSupportedQualityCodeSystems(), checkChannel
                        .get(), checkChannelIntervalReadings)
                        .stream()
                        .collect(Collectors.toMap(DataValidationStatus::getReadingTimestamp, DataValidationStatus::getValidationResult));
            }
        } else {
            // this means that purpose is not active on a usagepoint
            handleInitFail(InitCancelReason.REFERENCE_PURPOSE_HAS_NOT_BEEN_EVER_ACTIVATED
                    .getProps()
                    .withReadingType(referenceReadingType));
        }

        // {RULE FLOW CHECK] no 'check' output with matching reading type exists on the chosen purpose
        if (!checkOutputExistOnPurpose) {
            // [RULE FLOW ACTION] Stop validation for the channel independently from Pass if no reference data field value (last check remains as before the validation), an error message appears in the log
            handleInitFail(InitCancelReason.REFERENCE_OUTPUT_DOES_NOT_EXIST
                    .getProps()
                    .withReadingType(referenceReadingType));
        }
    }

    private void handleInitFail(InitCancelProps props) throws InitCancelException {
        logFailure(props);
        throw new InitCancelException(ValidationResult.NOT_VALIDATED);
    }

    protected void initUsagePointName(Channel channel) throws InitCancelException {
        Optional<UsagePoint> usagePoint = channel.getChannelsContainer()
                .getUsagePoint();

        if (!usagePoint.isPresent()) {
            LoggingContext.get()
                    .severe(getLogger(), getThesaurus().getFormat(MessageSeeds.VALIDATOR_INIT_MISC_NO_UP)
                            .format(rangeToString(failedValidatonInterval), getDisplayName(), readingType.getFullAliasName()));
            throw new InitCancelException(ValidationResult.NOT_VALIDATED);
        }

        validatingUsagePoint = usagePoint.get();
        validatingUsagePointName = validatingUsagePoint.getName();
    }


    protected String rangeToString(Range<Instant> range) {
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
                .setDefaultValue(new TwoValuesDifference(TwoValuesDifference.Type.ABSOLUTE, new BigDecimal(0)))
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
                .specForValuesOf(new NoneOrBigDecimalValueFactory())
                .named(MIN_THRESHOLD, MainCheckValidator.TranslationKeys.MIN_THRESHOLD)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(NoneOrBigDecimal.none())
                .finish();
    }

    NoneOrBigDecimal getMinThresholdProperty() {
        NoneOrBigDecimal value = (NoneOrBigDecimal) properties.get(MIN_THRESHOLD);
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

    MetrologyPurpose getCheckPurposeProperty(boolean required) {
        MetrologyPurpose value = (MetrologyPurpose) properties.get(CHECK_PURPOSE);
        if (value == null && required) {
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

    protected class InitCancelException extends Exception {
        ValidationResult validationResult;

        public InitCancelException(ValidationResult validationResult) {
            this.validationResult = validationResult;
        }

        public ValidationResult getValidationResult() {
            return validationResult;
        }
    }

    enum InitCancelReason {
        NO_REFERENCE_PURPOSE_FOUND_ON_REFERENCE_USAGE_POINT {
            @Override
            InitCancelProps getProps() {
                return new InitCancelProps(NO_REFERENCE_PURPOSE_FOUND_ON_REFERENCE_USAGE_POINT);
            }
        },
        REFERENCE_PURPOSE_HAS_NOT_BEEN_EVER_ACTIVATED {
            @Override
            InitCancelProps getProps() {
                return new InitCancelProps(REFERENCE_PURPOSE_HAS_NOT_BEEN_EVER_ACTIVATED);
            }
        },
        REFERENCE_OUTPUT_DOES_NOT_EXIST {
            @Override
            InitCancelProps getProps() {
                return new InitCancelProps(REFERENCE_OUTPUT_DOES_NOT_EXIST);
            }
        },
        REFERENCE_OUPUT_MISSING_OR_NOT_VALID{
            @Override
            InitCancelProps getProps() {
                return new InitCancelProps(REFERENCE_OUPUT_MISSING_OR_NOT_VALID);
            }
        };

        abstract InitCancelProps getProps();
    }

    static class InitCancelProps {
        InitCancelReason reason;
        ReadingType readingType;

        InitCancelProps(InitCancelReason reason) {
            this.reason = reason;
        }

        InitCancelProps withReadingType(ReadingType readingType) {
            this.readingType = readingType;
            return this;
        }

    }

    abstract void logFailure(InitCancelProps props);

    protected class ComparingValues {
        BigDecimal mainValue;
        BigDecimal checkValue;

        public ComparingValues(BigDecimal mainValue, BigDecimal checkValue) {
            this.mainValue = mainValue;
            this.checkValue = checkValue;
        }
    }

    protected ComparingValues calculateComparingValues(IntervalReadingRecord mainReading, IntervalReadingRecord checkReading){
        return new ComparingValues(mainReading.getValue(), checkReading.getValue());
    }
}
