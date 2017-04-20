/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl.meteradvance;

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
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.NonOrBigDecimalValueFactory;
import com.elster.jupiter.properties.NonOrBigDecimalValueProperty;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TwoValuesAbsoluteDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.util.time.TemporalAmountComparator;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.impl.AbstractValidator;
import com.elster.jupiter.validators.impl.MessageSeeds;
import com.elster.jupiter.validators.impl.properties.ReadingTypeReference;
import com.elster.jupiter.validators.impl.properties.ReadingTypeValueFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

public class MeterAdvanceValidator extends AbstractValidator {

    private Logger logger = Logger.getLogger(MeterAdvanceValidator.class.getName());

    static final DateTimeFormatter dateAndTimeFormatter = DefaultDateTimeFormatters.mediumDate().withShortTime().build();

    static final String REFERENCE_READING_TYPE = "referenceReadingType";
    static final String MAX_ABSOLUTE_DIFFERENCE = "maxAbsoluteDifference";
    static final String REFERENCE_PERIOD = "referencePeriod";
    static final String MIN_THRESHOLD = "minThreshold";

    private final MeteringService meteringService;

    private Channel channel;
    private ZoneId channelZoneId;
    private TemporalAmount channelIntervalLength;
    private HasName targetObject;
    private ReadingType validatedReadingType;
    private Range<Instant> validatedInterval;

    private ValidationStrategy validationStrategy;

    public MeterAdvanceValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService) {
        super(thesaurus, propertySpecService);
        this.meteringService = meteringService;
    }

    public MeterAdvanceValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.meteringService = meteringService;
        checkRequiredProperties();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        try {
            validateThatReadingTypeCanBeValidated(readingType);
            this.channel = channel;
            this.channelZoneId = channel.getZoneId();
            this.channelIntervalLength = channel.getIntervalLength().get();
            this.targetObject = fetchTargetObject(channel.getChannelsContainer());
            this.validatedReadingType = readingType;
            this.validatedInterval = interval;

            ReadingType referenceReadingType = getReferenceReadingTypeProperty().getReadingType();
            validateReferenceReadingType(referenceReadingType);

            Channel referenceRegister = findReferenceRegisterOrThrowException(referenceReadingType);
            Range<Instant> referenceInterval = computeReferenceInterval(interval);
            NavigableMap<Instant, ReadingRecord> registerReadings = referenceRegister.getRegisterReadings(referenceReadingType, referenceInterval)
                    .stream().collect(Collectors.toMap(ReadingRecord::getTimeStamp, Function.identity(), (u1, u2) -> u1, TreeMap::new));

            // TODO validate that interval contains something

            Range<Instant> firstChannelInterval = getFirstInterval(interval);
            ReadingRecord firstReferenceReading = findFirstReferenceReading(firstChannelInterval, registerReadings)
                    .orElseThrow(handleNoRegisterReadings(interval));
            Range<Instant> lastChannelInterval = getLastInterval(interval);
            ReadingRecord lastReferenceReading = findLastReferenceReading(lastChannelInterval, registerReadings)
                    .orElseThrow(handleNoRegisterReadings(interval));
            if (firstReferenceReading.getTimeStamp().equals(lastReferenceReading.getTimeStamp())) {
                this.validationStrategy = ValidationStrategy.markValid(Range.atMost(firstReferenceReading.getTimeStamp()));
                return;
            }

            // compute register readings delta
            BigDecimal first = firstReferenceReading.getValue();
            BigDecimal last = lastReferenceReading.getValue();
            BigDecimal deltaOfRegisterReadings = last.subtract(first);

            NonOrBigDecimalValueProperty minimumThreshold = getMinimumThresholdProperty();
            if (!minimumThreshold.isNone && deltaOfRegisterReadings.compareTo(minimumThreshold.value) < 0) {
                throw skipValidationException(SkipValidationOption.MARK_ALL_VALID,
                        MessageSeeds.DIFFERENCE_BETWEEN_TWO_REGISTER_READINGS_LESS_THAN_MIN_THRESHOLD, getDefaultMessageSeedArgs()).get();
            }

            // compute channel readings sum
            Range<Instant> intervalToSummarize = Range.openClosed(firstReferenceReading.getTimeStamp(), lastReferenceReading.getTimeStamp());
            BigDecimal sumOfChannelReadings = channel.getIntervalReadings(readingType, intervalToSummarize)
                    .stream()
                    .map(IntervalReadingRecord::getValue)
                    .filter(value -> value != null)
                    .reduce(BigDecimal::add).orElse(BigDecimal.ZERO)
                    .scaleByPowerOfTen(readingType.getMultiplier().getMultiplier())
                    .scaleByPowerOfTen(-referenceReadingType.getMultiplier().getMultiplier());

            TwoValuesAbsoluteDifference maxDifference = getMaximumDifferenceProperty();
            boolean differenceValid = isDifferenceValid(deltaOfRegisterReadings, sumOfChannelReadings, maxDifference);
            if (firstReferenceReading.getTimeStamp().isAfter(firstChannelInterval.upperEndpoint()) && !differenceValid) {
                this.validationStrategy = ValidationStrategy.markValidAndSuspect(Range.openClosed(interval.lowerEndpoint(), firstReferenceReading.getTimeStamp()), intervalToSummarize);
            } else if (differenceValid) {
                this.validationStrategy = ValidationStrategy.markValid(intervalToSummarize);
            } else {
                this.validationStrategy = ValidationStrategy.markSuspect(intervalToSummarize);
            }
        } catch (SkipValidationException e) {
            this.logger.log(e.getMessageSeed().getLevel(), e.getLocalizedMessage());
            this.validationStrategy = ValidationStrategy.skipValidation(e.getSkipValidationOption());
        }
    }

    private void validateThatReadingTypeCanBeValidated(ReadingType readingType) {
        if (!readingType.isRegular()) {
            throw skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.UNSUPPORTED_IRREGULAR_CHANNEL, getDisplayName()).get();
        }
        readingType.getIntervalLength().orElseThrow(skipValidationException(
                SkipValidationOption.MARK_ALL_NOT_VALIDATED,
                MessageSeeds.UNSUPPORTED_READINGTYPE, readingType.getMRID(), getDisplayName()));
    }

    private HasName fetchTargetObject(ChannelsContainer channelsContainer) {
        return channelsContainer.getMeter().map(HasName.class::cast)
                .orElseGet(() -> channelsContainer.getUsagePoint()
                        .orElseThrow(() -> new IllegalArgumentException("Channels container must refer either to meter or usage point")));
    }

    private ReadingTypeReference getReferenceReadingTypeProperty() {
        return (ReadingTypeReference) super.properties.get(REFERENCE_READING_TYPE);
    }

    private NonOrBigDecimalValueProperty getMinimumThresholdProperty() {
        return (NonOrBigDecimalValueProperty) super.properties.get(MIN_THRESHOLD);
    }

    private TwoValuesAbsoluteDifference getMaximumDifferenceProperty() {
        return (TwoValuesAbsoluteDifference) super.properties.get(MAX_ABSOLUTE_DIFFERENCE);
    }

    private void validateReferenceReadingType(ReadingType referenceReadingType) {
        if (!areReadingTypesComparable(this.validatedReadingType, referenceReadingType)) {
            throw skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED,
                    MessageSeeds.REFERENCE_READINGTYPE_DOES_NOT_MATCH_VALIDATED_ONE, getDefaultMessageSeedArgs()).get();
        }
    }

    private Object[] getDefaultMessageSeedArgs() {
        return new Object[]{
                instantToString(this.validatedInterval.lowerEndpoint()),
                instantToString(this.validatedInterval.upperEndpoint()),
                getDisplayName(),
                this.validatedReadingType.getMRID(),
                this.targetObject.getName()};
    }

    String instantToString(Instant instant) {
        return dateAndTimeFormatter.format(ZonedDateTime.ofInstant(instant, this.channelZoneId));
    }

    private boolean areReadingTypesComparable(ReadingType validatedReadingType, ReadingType referenceReadingType) {
        return ReadingTypeComparator.ignoring(
                ReadingTypeComparator.Attribute.MacroPeriod,
                ReadingTypeComparator.Attribute.MeasuringPeriod,
                ReadingTypeComparator.Attribute.Accumulation,
                ReadingTypeComparator.Attribute.Multiplier
        ).compare(validatedReadingType, referenceReadingType) == 0;
    }

    private Channel findReferenceRegisterOrThrowException(ReadingType referenceReadingType) {
        ChannelsContainer channelsContainer = this.channel.getChannelsContainer();
        return channelsContainer.getChannel(referenceReadingType)
                .orElseGet(() -> findReferenceRegisterOnUsagePointContracts(channelsContainer, referenceReadingType)
                        .orElseThrow(skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.NO_REFERENCE_READINGTYPE, getDefaultMessageSeedArgs())));
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

    private Range<Instant> computeReferenceInterval(Range<Instant> originalInterval) {
        ZonedDateTime originalStart = ZonedDateTime.ofInstant(originalInterval.lowerEndpoint(), this.channelZoneId);
        ZonedDateTime originalEnd = ZonedDateTime.ofInstant(originalInterval.upperEndpoint(), this.channelZoneId);

        TemporalAmount startOffset;
        if (new TemporalAmountComparator().compare(this.channelIntervalLength, Period.ofDays(1)) <= 0) {
            startOffset = Period.ofMonths(2);
        } else {
            startOffset = Period.ofYears(5);
        }
        TemporalAmount endOffset = this.channelIntervalLength;

        Instant referenceIntervalStart = originalStart.minus(startOffset).toInstant();
        Instant referenceIntervalEnd = originalEnd.plus(endOffset).toInstant();

        Range<Instant> channelEffectiveness = this.channel.getChannelsContainer().getInterval().toOpenClosedRange();
        return channelEffectiveness.intersection(Range.closedOpen(referenceIntervalStart, referenceIntervalEnd));// yes, closedOpen!
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
                return skipValidationException(SkipValidationOption.MARK_ALL_VALID, MessageSeeds.REGISTER_READINGS_ARE_MISSING, getDefaultMessageSeedArgs());
            }
        }
        return skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.REGISTER_READINGS_ARE_MISSING, getDefaultMessageSeedArgs());
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

    private boolean isDifferenceValid(BigDecimal deltaOfRegisterReadings, BigDecimal sumOfChannelReadings, TwoValuesAbsoluteDifference maxDifference) {
        BigDecimal absoluteDifference = deltaOfRegisterReadings.subtract(sumOfChannelReadings).abs();
        BigDecimal difference;
        switch (maxDifference.type) {
            case absolute:
                difference = absoluteDifference;
                break;
            case percent:
                difference = absoluteDifference.divide(deltaOfRegisterReadings, BigDecimal.ROUND_HALF_UP).abs().multiply(new BigDecimal(100));
                break;
            default:
                throw new UnsupportedOperationException("Unsupported type of difference: " + maxDifference.type.name());
        }
        return difference.compareTo(maxDifference.value) <= 0;
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        return this.validationStrategy.validate(intervalReadingRecord);
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return this.validationStrategy.validate(readingRecord);
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

    ValidationStrategy getValidationStrategy() {
        return validationStrategy;
    }
}
