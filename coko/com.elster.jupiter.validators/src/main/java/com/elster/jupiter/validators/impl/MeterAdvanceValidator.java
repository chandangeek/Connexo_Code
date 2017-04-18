/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeComparator;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.NonOrBigDecimalValueFactory;
import com.elster.jupiter.properties.NonOrBigDecimalValueProperty;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TwoValuesAbsoluteDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.TemporalAmountComparator;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.impl.properties.ReadingTypeReference;
import com.elster.jupiter.validators.impl.properties.ReadingTypeValueFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class MeterAdvanceValidator extends AbstractValidator {

    enum SkipValidationOption {
        DO_NOT_SKIP,
        MARK_ALL_VALID,
        MARK_ALL_NOT_VALIDATED
    }

    static class ValidationStrategy {

        private SkipValidationOption skipValidationOption = SkipValidationOption.DO_NOT_SKIP;
        private Range<Instant> validInterval;
        private Range<Instant> suspectInterval;

        private ValidationStrategy() {
        }

        static ValidationStrategy skipValidation(SkipValidationOption skipValidationOption) {
            ValidationStrategy validationStrategy = new ValidationStrategy();
            validationStrategy.skipValidationOption = skipValidationOption;
            return validationStrategy;
        }

        static ValidationStrategy markSuspect(Range<Instant> suspectInterval) {
            ValidationStrategy validationStrategy = new ValidationStrategy();
            validationStrategy.suspectInterval = suspectInterval;
            return validationStrategy;
        }

        static ValidationStrategy markValid(Range<Instant> validInterval) {
            ValidationStrategy validationStrategy = new ValidationStrategy();
            validationStrategy.validInterval = validInterval;
            return validationStrategy;
        }

        ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
            switch (skipValidationOption) {
                case MARK_ALL_VALID:
                    return ValidationResult.VALID;
                case MARK_ALL_NOT_VALIDATED:
                    return ValidationResult.NOT_VALIDATED;
                case DO_NOT_SKIP:
                    Instant timeStamp = intervalReadingRecord.getTimeStamp();
                    if (validInterval != null && validInterval.contains(timeStamp)) {
                        return ValidationResult.VALID;
                    } else if (suspectInterval != null && suspectInterval.contains(timeStamp)) {
                        return ValidationResult.SUSPECT;
                    } else {
                        return ValidationResult.NOT_VALIDATED;
                    }
                default:
                    throw new UnsupportedOperationException("Skip validation option is not supported: " + this.skipValidationOption.name());
            }
        }
    }

    //http://localhost:8080/api/mtr/readingtypes?filter=[{"property":"fullAliasName","value":"[15-minute]*"},{"property":"equidistant","value":true}]&start=0&limit=50

    private static Logger logger = Logger.getLogger(MeterAdvanceValidator.class.getName());

    static final String REFERENCE_READING_TYPE = "referenceReadingType";
    static final String MAX_ABSOLUTE_DIFFERENCE = "maxAbsoluteDifference";
    static final String REFERENCE_PERIOD = "referencePeriod";
    static final String MIN_THRESHOLD = "minThreshold";

    private enum TranslationKeys implements TranslationKey {

        REFERENCE_READING_TYPE(MeterAdvanceValidator.REFERENCE_READING_TYPE, "Reference reading type"),
        MAX_ABSOLUTE_DIFFERENCE(MeterAdvanceValidator.MAX_ABSOLUTE_DIFFERENCE, "Maximum absolute difference"),
        REFERENCE_PERIOD(MeterAdvanceValidator.REFERENCE_PERIOD, "Reference period"),
        MIN_THRESHOLD(MeterAdvanceValidator.MIN_THRESHOLD, "Minimum threshold");

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return MeterAdvanceValidator.class.getName() + "." + key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }
    }

    private final MeteringService meteringService;

    private ValidationStrategy validationStrategy;

    private Channel channel;
    private ZoneId channelZoneId;
    private TemporalAmount channelIntervalLength;


    MeterAdvanceValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService) {
        super(thesaurus, propertySpecService);
        this.meteringService = meteringService;
    }

    MeterAdvanceValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.meteringService = meteringService;
        checkRequiredProperties();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        try {
            validateThatChannelCanBeValidated(channel);
            this.channel = channel;
            this.channelIntervalLength = channel.getIntervalLength().get();

            ReadingType referenceReadingType = ((ReadingTypeReference) super.properties.get(REFERENCE_READING_TYPE)).getReadingType();
            validateReferenceReadingType(readingType, referenceReadingType);

            Channel referenceRegister = findReferenceRegisterOrThrowException(channel.getChannelsContainer(), referenceReadingType);
            Range<Instant> referenceInterval = computeReferenceInterval(channel, interval);
            NavigableMap<Instant, ReadingRecord> registerReadings = referenceRegister.getRegisterReadings(referenceReadingType, referenceInterval)
                    .stream().collect(Collectors.toMap(ReadingRecord::getTimeStamp, Function.identity(), (u1, u2) -> u1, TreeMap::new));

            // TODO validate that interval contains something

            ReadingRecord firstReferenceReading = findFirstReferenceReading(getFirstInterval(interval), registerReadings)
                    .orElseThrow(handleNoRegisterReadings(interval));
            ReadingRecord lastReferenceReading = findLastReferenceReading(getLastInterval(interval), registerReadings)
                    .orElseThrow(handleNoRegisterReadings(interval));

            if (firstReferenceReading.getTimeStamp().equals(lastReferenceReading.getTimeStamp())) {
                this.validationStrategy = ValidationStrategy.markValid(Range.atMost(firstReferenceReading.getTimeStamp()));
                return;
            }

            // compute register readings delta
            BigDecimal first = firstReferenceReading.getQuantity(referenceReadingType).getValue();
            BigDecimal last = lastReferenceReading.getQuantity(referenceReadingType).getValue();
            BigDecimal deltaOfRegisterReadings = last.subtract(first);

            NonOrBigDecimalValueProperty minimumThreshold = (NonOrBigDecimalValueProperty) super.properties.get(MIN_THRESHOLD);
            if (!minimumThreshold.isNone && deltaOfRegisterReadings.compareTo(minimumThreshold.value) < 0) {
                throw skipValidationException(SkipValidationOption.MARK_ALL_VALID, MessageSeeds.DIFFERENCE_BETWEEN_TWO_REGISTER_READINGS_LESS_THEN_MIN_THRESHOLD).get();
            }

            // compute channel readings sum
            Range<Instant> intervalToSummarize = Range.openClosed(firstReferenceReading.getTimeStamp(), lastReferenceReading.getTimeStamp());
            BigDecimal sumOfChannelReadings = channel.getIntervalReadings(intervalToSummarize)
                    .stream()
                    .map(IntervalReadingRecord::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .scaleByPowerOfTen(readingType.getMultiplier().getMultiplier())
                    .scaleByPowerOfTen(-referenceReadingType.getMultiplier().getMultiplier());


            BigDecimal absoluteDifference = sumOfChannelReadings.subtract(deltaOfRegisterReadings).abs();

            TwoValuesAbsoluteDifference maxDifference = (TwoValuesAbsoluteDifference) super.properties.get(MAX_ABSOLUTE_DIFFERENCE);
            switch (maxDifference.type) {
                case absolute:
                    if (absoluteDifference.compareTo(maxDifference.value) > 0) {
                        this.validationStrategy = ValidationStrategy.markSuspect(interval);
                    } else {
                        this.validationStrategy = ValidationStrategy.markValid(interval);
                    }
                    break;
                case percent:
                    BigDecimal relativeDifference = absoluteDifference.divide(deltaOfRegisterReadings, BigDecimal.ROUND_HALF_UP).abs().multiply(new BigDecimal(100));
                    if (relativeDifference.compareTo(maxDifference.value) > 0) {
                        this.validationStrategy = ValidationStrategy.markSuspect(interval);
                    } else {
                        this.validationStrategy = ValidationStrategy.markValid(interval);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported type of difference: " + maxDifference.type.name());
            }
        } catch (SkipValidationException e) {
            LoggingContext.get().severe(logger, e.getLocalizedMessage());
            this.validationStrategy = ValidationStrategy.skipValidation(e.getSkipValidationOption());
        }
    }

    private Range<Instant> getFirstInterval(Range<Instant> validatedInterval) {
        Instant startTime = validatedInterval.lowerEndpoint();
        Instant endTime = instantPlus(startTime, this.channelIntervalLength);
        return findSingleChannelInterval(Range.openClosed(startTime, endTime));
    }

    private Instant instantPlus(Instant instant, TemporalAmount interval) {
        return ZonedDateTime.ofInstant(instant, this.channelZoneId).plus(interval).toInstant();
    }

    private Range<Instant> getLastInterval(Range<Instant> validatedInterval) {
        Instant endTime = validatedInterval.upperEndpoint();
        Instant startTime = instantMinus(endTime, this.channelIntervalLength);
        return findSingleChannelInterval(Range.openClosed(startTime, endTime));
    }

    private Instant instantMinus(Instant instant, TemporalAmount interval) {
        return ZonedDateTime.ofInstant(instant, this.channelZoneId).minus(interval).toInstant();
    }

    private Range<Instant> findSingleChannelInterval(Range<Instant> searchInterval) {
        List<Instant> instants = this.channel.toList(searchInterval);
        if (instants.size() != 1) {
            throw new IllegalStateException("Exactly one timestamp is expected");
        }
        Instant endOfInterval = instants.get(0);
        return Range.openClosed(ZonedDateTime.ofInstant(endOfInterval, this.channelZoneId).minus(this.channelIntervalLength).toInstant(), endOfInterval);
    }

    private Optional<ReadingRecord> findFirstReferenceReading(Range<Instant> firstInterval, NavigableMap<Instant, ReadingRecord> registerReadings) {
        Instant firstIntervalStart = firstInterval.lowerEndpoint();
        Map.Entry<Instant, ReadingRecord> closestFromRightReading = registerReadings.ceilingEntry(firstIntervalStart);
        if (closestFromRightReading != null) {
            if (firstInterval.contains(closestFromRightReading.getKey())) {
                return Optional.of(closestFromRightReading.getValue());
            } else {
                Map.Entry<Instant, ReadingRecord> closestFromLeftReading = registerReadings.lowerEntry(firstIntervalStart);
                if (closestFromLeftReading != null) {
                    // we found closest from left but it may not be a single reading within an interval
                    return Optional.of(getRegisterReadingClosestToStartOfInterval(registerReadings, closestFromLeftReading.getKey()));
                } else {
                    return Optional.of(closestFromRightReading.getValue());
                }
            }
        }
        return Optional.empty();
    }

    private ReadingRecord getRegisterReadingClosestToStartOfInterval(NavigableMap<Instant, ReadingRecord> registerReadings, Instant time) {
        Instant start = instantMinus(time, this.channelIntervalLength);
        Range<Instant> singleChannelInterval = findSingleChannelInterval(Range.openClosed(start, time));
        return registerReadings.ceilingEntry(singleChannelInterval.upperEndpoint()).getValue();
    }

    private Supplier<SkipValidationException> handleNoRegisterReadings(Range<Instant> validatedInterval) {
        TimeDuration referencePeriodProp = (TimeDuration) super.properties.get(REFERENCE_PERIOD); // TODO handle none reference period
        if (referencePeriodProp != null) {
            long referencePeriodLength = referencePeriodProp.getMilliSeconds();
            long validatedPeriodLength = validatedInterval.upperEndpoint().toEpochMilli() - validatedInterval.lowerEndpoint().toEpochMilli();
            if (validatedPeriodLength > referencePeriodLength) {
                return skipValidationException(SkipValidationOption.MARK_ALL_VALID, MessageSeeds.REGISTER_READINGS_ARE_MISSING);
            }
        }
        return skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.REGISTER_READINGS_ARE_MISSING);
    }

    private Optional<ReadingRecord> findLastReferenceReading(Range<Instant> lastInterval, NavigableMap<Instant, ReadingRecord> registerReadings) {
        Instant endOfLastInterval = lastInterval.upperEndpoint();
        Map.Entry<Instant, ReadingRecord> closestFromRightReading = registerReadings.ceilingEntry(endOfLastInterval);
        if (closestFromRightReading != null) {
            return Optional.of(closestFromRightReading.getValue());
        }
        Map.Entry<Instant, ReadingRecord> closestFromLeftReading = registerReadings.lowerEntry(endOfLastInterval);
        if (closestFromLeftReading != null) {
            // we found closest from left but it may not be a single reading within an interval
            return Optional.of(getRegisterReadingClosestToStartOfInterval(registerReadings, closestFromLeftReading.getKey()));
        }
        return Optional.empty();
    }

    private void validateThatChannelCanBeValidated(Channel channel) {
        if (!channel.isRegular()) {
            throw skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.NOT_APPLICABLE_TO_IRREGULAR_CHANNEL).get();
        }
        channel.getIntervalLength().orElseThrow(skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.NOT_SUPPORTED_READINGTYPE));
    }

    private void validateReferenceReadingType(ReadingType readingType, ReadingType referenceReadingType) {
        if (areReadingTypesComparable(readingType, referenceReadingType)) {
            //Failed to validate period "Wed, 15 Feb 2017 00:00 until Thu, 16 Feb 2017 00:00" using method "Meter advance"
            // on <reading type> since the specified reference reading type doesn't match the <reading type> reading type on the <usage point name>/<device>
            //
            throw skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.REFERENCE_READINGTYPE_DOESNOT_MATCH_VALIDATED_ONE).get();
        }
    }

    private boolean areReadingTypesComparable(ReadingType validatedReadingType, ReadingType referenceReadingType) {
        return ReadingTypeComparator.ignoring(
                ReadingTypeComparator.Attribute.MacroPeriod,
                ReadingTypeComparator.Attribute.MeasuringPeriod,
                ReadingTypeComparator.Attribute.Multiplier
        ).compare(validatedReadingType, referenceReadingType) == 0;
    }

    private Channel findReferenceRegisterOrThrowException(ChannelsContainer channelsContainer, ReadingType referenceReadingType) {
        return channelsContainer.getChannel(referenceReadingType)
                .orElseGet(() -> findReferenceRegisterOnUsagePointContracts(channelsContainer, referenceReadingType)
                        .orElseThrow(skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.NO_REFERENCE_READINGTYPE)));
        //Failed to validate period "Wed, 15 Feb 2017 00:00 until Thu, 16 Feb 2017 00:00" using
        // method "Meter advance" on <reading type> since the specified reference reading type doesn't exist on the <usage point name>/<device>
    }

    private Optional<Channel> findReferenceRegisterOnUsagePointContracts(ChannelsContainer channelsContainer, ReadingType referenceReadingType) {
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            Instant startTime = channelsContainer.getStart();
            UsagePoint usagePoint = channelsContainer.getUsagePoint()
                    .orElseThrow(() -> new IllegalStateException("MetrologyContractChannelsContainer must refer to usage point!"));
            EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = usagePoint.getEffectiveMetrologyConfiguration(startTime)
                    .orElseThrow(() -> new IllegalStateException("Usage point must have an effective metrology configuration at start time of its channel container"));
            return effectiveMC.getMetrologyConfiguration().getContracts().stream()
                    .map(effectiveMC::getChannelsContainer)
                    .flatMap(Functions.asStream())
                    .map(container -> container.getChannel(referenceReadingType))
                    .flatMap(Functions.asStream())
                    .findAny();
        }
        return Optional.empty();
    }

    private Range<Instant> computeReferenceInterval(Channel channel, Range<Instant> originalInterval) {
        ZoneId zoneId = channel.getZoneId();
        ZonedDateTime originalStart = ZonedDateTime.ofInstant(originalInterval.lowerEndpoint(), zoneId);
        ZonedDateTime originalEnd = ZonedDateTime.ofInstant(originalInterval.upperEndpoint(), zoneId);

        TemporalAmount startOffset;
        if (new TemporalAmountComparator().compare(channel.getIntervalLength().get(), Period.ofDays(1)) <= 0) {
            startOffset = Period.ofMonths(2);
        } else {
            startOffset = Period.ofYears(5);
        }
        TemporalAmount endOffset = channel.getIntervalLength().get();

        Instant referenceIntervalStart = originalStart.minus(startOffset).toInstant();
        Instant referenceIntervalEnd = originalEnd.plus(endOffset).toInstant();

        Range<Instant> channelEffectiveness = channel.getChannelsContainer().getInterval().toOpenClosedRange();
        return channelEffectiveness.intersection(Range.closedOpen(referenceIntervalStart, referenceIntervalEnd));
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        return this.validationStrategy.validate(intervalReadingRecord);
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        // validator is not applicable to registers
        return ValidationResult.NOT_VALIDATED;
    }

    @Override
    public String getDefaultFormat() {
        return "Meter advance";
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    }

    @Override
    public List<TranslationKey> getExtraTranslationKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                buildReferenceReadingTypePropertySpec(),
                buildMaxAbsoluteDifferencePropertySpec(),
                buildReferencePeriodPropertySpec(),
                buildMinThresholdPropertySpec()
        );
    }

    @Override
    public List<PropertySpec> getPropertySpecs(ValidationPropertyDefinitionLevel level) {
        if (ValidationPropertyDefinitionLevel.TARGET_OBJECT == level) {
            return Collections.singletonList(buildMaxAbsoluteDifferencePropertySpec());
        } else {
            return getPropertySpecs();
        }
    }

    private PropertySpec buildReferenceReadingTypePropertySpec() {
        return getPropertySpecService()
                .specForValuesOf(new ReadingTypeValueFactory(meteringService, ReadingTypeValueFactory.Mode.ONLY_IRREGULAR))
                .named(REFERENCE_READING_TYPE, TranslationKeys.REFERENCE_READING_TYPE)
                .fromThesaurus(getThesaurus())
                .markRequired()
                .finish();
    }

    private PropertySpec buildMaxAbsoluteDifferencePropertySpec() {
        TwoValuesAbsoluteDifference defaultValue = new TwoValuesAbsoluteDifference();
        defaultValue.value = new BigDecimal(0);
        return getPropertySpecService()
                .specForValuesOf(new TwoValuesDifferenceValueFactory())
                .named(MAX_ABSOLUTE_DIFFERENCE, TranslationKeys.MAX_ABSOLUTE_DIFFERENCE)
                .fromThesaurus(getThesaurus())
                .markRequired()
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec buildReferencePeriodPropertySpec() {
        return getPropertySpecService()
                .timeDurationSpec()// TODO Non or TimeDuration
                .named(REFERENCE_PERIOD, TranslationKeys.REFERENCE_PERIOD)
                .fromThesaurus(getThesaurus())
                .markRequired()
                .finish();
    }

    private PropertySpec buildMinThresholdPropertySpec() {
        return getPropertySpecService()
                .specForValuesOf(new NonOrBigDecimalValueFactory())
                .named(MIN_THRESHOLD, TranslationKeys.MIN_THRESHOLD)
                .fromThesaurus(getThesaurus())
                .markRequired()
                .setDefaultValue(new NonOrBigDecimalValueProperty())
                .finish();
    }

    private Supplier<SkipValidationException> skipValidationException(SkipValidationOption skipValidationOption, MessageSeeds messageSeed, Object... args) {
        return () -> new SkipValidationException(getThesaurus(), skipValidationOption, messageSeed, args);
    }

    private static class SkipValidationException extends LocalizedException {

        private final SkipValidationOption skipValidationOption;

        private SkipValidationException(Thesaurus thesaurus, SkipValidationOption skipValidationOption, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
            this.skipValidationOption = skipValidationOption;
        }

        private SkipValidationOption getSkipValidationOption() {
            return skipValidationOption;
        }
    }
}
