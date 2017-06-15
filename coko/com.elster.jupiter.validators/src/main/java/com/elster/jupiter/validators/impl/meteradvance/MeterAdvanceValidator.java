/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl.meteradvance;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeComparator;
import com.elster.jupiter.metering.ReadingTypeValueFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.NoneOrBigDecimal;
import com.elster.jupiter.properties.NoneOrBigDecimalValueFactory;
import com.elster.jupiter.properties.NoneOrTimeDurationValue;
import com.elster.jupiter.properties.NoneOrTimeDurationValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.util.time.TemporalAmountComparator;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.impl.AbstractValidator;
import com.elster.jupiter.validators.impl.MessageSeeds;

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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MeterAdvanceValidator extends AbstractValidator {

    enum Conversion {

        MEASUREMENTS_PER_HOUR {

            private static final double MINUTES_PER_HOUR = 60.0;

            @Override
            boolean isApplicable(ReadingType from, ReadingType to) {
                if (super.isApplicable(from, to) && !from.getMacroPeriod().isApplicable()) {
                    if (MeasurementKind.DEMAND == from.getMeasurementKind() && ReadingTypeUnit.WATT == from.getUnit()) {
                        return MeasurementKind.ENERGY == to.getMeasurementKind() && ReadingTypeUnit.WATTHOUR == to.getUnit();
                    }
                    if (ReadingTypeUnit.VOLT == from.getUnit()) {
                        return ReadingTypeUnit.VOLTHOUR == to.getUnit();
                    }
                }
                return false;
            }

            @Override
            Function<BigDecimal, BigDecimal> converter(ReadingType from, ReadingType to) {
                Function<BigDecimal, BigDecimal> multiplierConversion = MULTIPLIER_ONLY.converter(from, to);
                Function<BigDecimal, BigDecimal> timeConversion = value -> value.multiply(BigDecimal.valueOf(from.getMeasuringPeriod().getMinutes() / MINUTES_PER_HOUR));
                return multiplierConversion.andThen(timeConversion);
            }
        },
        MULTIPLIER_ONLY {
            @Override
            boolean isApplicable(ReadingType from, ReadingType to) {
                return super.isApplicable(from, to) && from.getUnit() == to.getUnit() && from.getMeasurementKind() == to.getMeasurementKind();
            }

            @Override
            Function<BigDecimal, BigDecimal> converter(ReadingType from, ReadingType to) {
                return value -> value.scaleByPowerOfTen(from.getMultiplier().getMultiplier() - to.getMultiplier().getMultiplier());
            }
        };

        abstract Function<BigDecimal, BigDecimal> converter(ReadingType from, ReadingType to);

        boolean isApplicable(ReadingType from, ReadingType to) {
            return ReadingTypeComparator.ignoring(
                    ReadingTypeComparator.Attribute.MacroPeriod,
                    ReadingTypeComparator.Attribute.MeasuringPeriod,
                    ReadingTypeComparator.Attribute.MeasurementKind,
                    ReadingTypeComparator.Attribute.Accumulation,
                    ReadingTypeComparator.Attribute.Multiplier,
                    ReadingTypeComparator.Attribute.Unit
            ).compare(from, to) == 0;
        }
    }

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
    public void init(Channel channel, ReadingType validatedReadingType, Range<Instant> interval) {
        try {
            validateThatReadingTypeCanBeValidated(validatedReadingType);
            initFields(channel, validatedReadingType, interval);

            ReadingType referenceReadingType = getReferenceReadingTypeProperty().getReadingType();
            Conversion conversion = findAvailableConversionOrThrowException(validatedReadingType, referenceReadingType);

            validateInterval(interval);

            Range<Instant> firstChannelInterval = getFirstInterval(interval);
            Range<Instant> lastChannelInterval = getLastInterval(interval);

            // find first and last reference readings
            Channel referenceChannel = findReferenceChannelOrThrowException(referenceReadingType);
            Range<Instant> referenceInterval = computeReferenceInterval(firstChannelInterval, lastChannelInterval);
            NavigableMap<Instant, BaseReadingRecord> referenceReadings = referenceChannel.getReadings(referenceReadingType, referenceInterval)
                    .stream().collect(Collectors.toMap(BaseReadingRecord::getTimeStamp, Function.identity(), (u1, u2) -> u1, TreeMap::new));
            ReferenceReading firstReferenceReading = Optional.ofNullable(findFirstReferenceReading(firstChannelInterval, referenceReadings))
                    .orElseThrow(() -> skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.REFERENCE_READINGS_ARE_MISSING, getDefaultMessageSeedArgs()));
            ReferenceReading lastReferenceReading = Optional.ofNullable(findLastReferenceReading(lastChannelInterval, referenceReadings))
                    .orElseThrow(() -> skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.REFERENCE_READINGS_ARE_MISSING, getDefaultMessageSeedArgs()));
            if (firstReferenceReading.getTimeStamp().equals(lastReferenceReading.getTimeStamp())) {
                this.validationStrategy = ValidationStrategy.markValid(Range.atMost(firstReferenceReading.getTimeStamp()));
                return;
            }

            // compute reference readings delta
            BigDecimal deltaOfReferenceReadings = lastReferenceReading.getValue().subtract(firstReferenceReading.getValue());
            validateDeltaOfReferenceReadings(deltaOfReferenceReadings);

            // compute channel readings sum
            Range<Instant> intervalToSummarize = Range.openClosed(firstReferenceReading.getClosestIntervalStart(), lastReferenceReading.getClosestIntervalEnd());
            BigDecimal sumOfChannelReadings = getSumOfIntervalReadings(intervalToSummarize, conversion.converter(this.validatedReadingType, referenceReadingType));

            this.validationStrategy = computeValidationStrategy(deltaOfReferenceReadings, sumOfChannelReadings, firstChannelInterval, intervalToSummarize);
        } catch (SkipValidationException e) {
            this.getLogger().log(e.getMessageSeed().getLevel(), e.getLocalizedMessage());
            this.validationStrategy = ValidationStrategy.skipValidation(e.getSkipValidationOption());
        }
    }

    private void validateThatReadingTypeCanBeValidated(ReadingType readingType) {
        if (!readingType.isRegular()) {
            throw skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.UNSUPPORTED_IRREGULAR_CHANNEL, getDisplayName());
        }
        boolean isValid = readingType.getIntervalLength().isPresent()
                && Accumulation.DELTADELTA == readingType.getAccumulation()
                && EnumSet.of(MeasurementKind.ENERGY, MeasurementKind.DEMAND, MeasurementKind.VOLTAGE).contains(readingType.getMeasurementKind());
        if (!isValid) {
            throw skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED, MessageSeeds.UNSUPPORTED_READINGTYPE, readingType.getMRID(), getDisplayName());
        }
    }

    private void initFields(Channel channel, ReadingType readingType, Range<Instant> interval) {
        this.channel = channel;
        this.channelZoneId = channel.getZoneId();
        this.channelIntervalLength = channel.getIntervalLength().get();
        this.targetObject = fetchTargetObject(channel.getChannelsContainer());
        this.validatedReadingType = readingType;
        this.validatedInterval = interval;
    }

    private HasName fetchTargetObject(ChannelsContainer channelsContainer) {
        return channelsContainer.getMeter().map(HasName.class::cast)
                .orElseGet(() -> channelsContainer.getUsagePoint()
                        .orElseThrow(() -> new IllegalArgumentException("Channels container must refer either to meter or usage point")));
    }

    private ReadingTypeValueFactory.ReadingTypeReference getReferenceReadingTypeProperty() {
        return (ReadingTypeValueFactory.ReadingTypeReference) super.properties.get(REFERENCE_READING_TYPE);
    }

    private TwoValuesDifference getMaximumDifferenceProperty() {
        return (TwoValuesDifference) super.properties.get(MAX_ABSOLUTE_DIFFERENCE);
    }

    private NoneOrTimeDurationValue getReferencePeriodProperty() {
        return (NoneOrTimeDurationValue) super.properties.get(REFERENCE_PERIOD);
    }

    private NoneOrBigDecimal getMinimumThresholdProperty() {
        return (NoneOrBigDecimal) super.properties.get(MIN_THRESHOLD);
    }

    private Conversion findAvailableConversionOrThrowException(ReadingType validatedReadingType, ReadingType referenceReadingType) {
        return Arrays.asList(Conversion.values()).stream()
                .filter(conversion -> conversion.isApplicable(validatedReadingType, referenceReadingType))
                .findFirst()
                .orElseThrow(() -> skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED,
                        MessageSeeds.REFERENCE_READINGTYPE_DOES_NOT_MATCH_VALIDATED_ONE, getDefaultMessageSeedArgs()));
    }

    private void validateInterval(Range<Instant> validatedInterval) {
        NoneOrTimeDurationValue referencePeriodProperty = getReferencePeriodProperty();
        if (!referencePeriodProperty.isNone()) {
            long referencePeriodLength = referencePeriodProperty.getValue().getMilliSeconds();
            long validatedPeriodLength = validatedInterval.upperEndpoint().toEpochMilli() - validatedInterval.lowerEndpoint().toEpochMilli();
            if (validatedPeriodLength > referencePeriodLength) {
                throw skipValidationException(SkipValidationOption.MARK_ALL_VALID,
                        MessageSeeds.VALIDATED_PERIOD_IS_GREATER_THAN_REFERENCE_PERIOD, getDefaultMessageSeedArgs());
            }
        }
    }

    private Object[] getDefaultMessageSeedArgs() {
        return new Object[]{
                instantToString(this.validatedInterval.lowerEndpoint()),
                instantToString(this.validatedInterval.upperEndpoint()),
                getDisplayName(),
                this.validatedReadingType.getFullAliasName(),
                this.targetObject.getName()};
    }

    String instantToString(Instant instant) {
        return dateAndTimeFormatter.format(ZonedDateTime.ofInstant(instant, this.channelZoneId));
    }

    private Channel findReferenceChannelOrThrowException(ReadingType referenceReadingType) {
        ChannelsContainer channelsContainer = this.channel.getChannelsContainer();
        return channelsContainer.getChannel(referenceReadingType)
                .orElseGet(() -> findReferenceChannelOnUsagePointContracts(channelsContainer, referenceReadingType)
                        .orElseThrow(() -> skipValidationException(SkipValidationOption.MARK_ALL_NOT_VALIDATED,
                                MessageSeeds.NO_REFERENCE_READINGTYPE, getDefaultMessageSeedArgs())));
    }

    private Optional<Channel> findReferenceChannelOnUsagePointContracts(ChannelsContainer channelsContainer, ReadingType referenceReadingType) {
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

    private Range<Instant> computeReferenceInterval(Range<Instant> firstInterval, Range<Instant> lastInterval) {
        TemporalAmount startOffset;
        if (new TemporalAmountComparator().compare(this.channelIntervalLength, Period.ofDays(1)) <= 0) {
            startOffset = Period.ofMonths(2);
        } else {
            startOffset = Period.ofYears(5);
        }
        TemporalAmount endOffset = this.channelIntervalLength;

        Instant referenceIntervalStart = instantMinus(firstInterval.lowerEndpoint(), startOffset);
        Instant referenceIntervalEnd = instantPlus(lastInterval.upperEndpoint(), endOffset);

        Range<Instant> channelEffectiveness = this.channel.getChannelsContainer().getInterval().toOpenClosedRange();
        return channelEffectiveness.intersection(Range.closedOpen(referenceIntervalStart, referenceIntervalEnd));// yes, closedOpen!
    }

    private Range<Instant> getFirstInterval(Range<Instant> validatedInterval) {
        Instant startTime = validatedInterval.lowerEndpoint();
        Instant endTime = instantPlusChannelInterval(startTime);
        return findSingleChannelInterval(Range.openClosed(startTime, endTime));
    }

    private Range<Instant> getLastInterval(Range<Instant> validatedInterval) {
        Instant endTime = validatedInterval.upperEndpoint();
        Instant startTime = instantMinusChannelInterval(endTime);
        return findSingleChannelInterval(Range.openClosed(startTime, endTime));
    }

    private Instant instantPlus(Instant instant, TemporalAmount interval) {
        return ZonedDateTime.ofInstant(instant, this.channelZoneId).plus(interval).toInstant();
    }

    private Instant instantPlusChannelInterval(Instant instant) {
        return instantPlus(instant, this.channelIntervalLength);
    }

    private Instant instantMinus(Instant instant, TemporalAmount interval) {
        return ZonedDateTime.ofInstant(instant, this.channelZoneId).minus(interval).toInstant();
    }

    private Instant instantMinusChannelInterval(Instant instant) {
        return instantMinus(instant, this.channelIntervalLength);
    }

    private Range<Instant> findEnclosingChannelInterval(Instant time) {
        Instant end = instantPlusChannelInterval(time);
        return findSingleChannelInterval(Range.openClosed(time, end));
    }

    private Range<Instant> findPreviousChannelInterval(Instant time) {
        Instant start = instantMinusChannelInterval(time);
        return findSingleChannelInterval(Range.openClosed(start, time));
    }

    private Range<Instant> findSingleChannelInterval(Range<Instant> searchInterval) {
        List<Instant> instants = this.channel.toList(searchInterval);
        if (instants.size() != 1) {
            throw new IllegalStateException("Exactly one timestamp is expected");
        }
        Instant endOfInterval = instants.get(0);
        return Range.openClosed(ZonedDateTime.ofInstant(endOfInterval, this.channelZoneId).minus(this.channelIntervalLength).toInstant(), endOfInterval);
    }

    private ReferenceReading findFirstReferenceReading(Range<Instant> firstInterval, NavigableMap<Instant, BaseReadingRecord> referenceReadings) {
        Instant startOfFirstInterval = firstInterval.lowerEndpoint();
        Map.Entry<Instant, BaseReadingRecord> closestFromRightReading = referenceReadings.ceilingEntry(startOfFirstInterval);
        if (closestFromRightReading != null) {
            if (Ranges.copy(firstInterval).asClosedOpen().contains(closestFromRightReading.getKey())) {// excluding upper end-point
                return new ReferenceReading(closestFromRightReading.getValue(), firstInterval);
            } else {
                Map.Entry<Instant, BaseReadingRecord> closestFromLeftReading = referenceReadings.lowerEntry(startOfFirstInterval);
                if (closestFromLeftReading != null) {
                    // we found closest from left but it may not be a single reading within an interval
                    Range<Instant> enclosingInterval = findEnclosingChannelInterval(closestFromLeftReading.getKey());
                    BaseReadingRecord closestToStartReading = referenceReadings.ceilingEntry(enclosingInterval.lowerEndpoint()).getValue();
                    return new ReferenceReading(closestToStartReading, enclosingInterval);
                } else {
                    Range<Instant> enclosingInterval = findEnclosingChannelInterval(closestFromRightReading.getKey());
                    return new ReferenceReading(closestFromRightReading.getValue(), enclosingInterval);
                }
            }
        }
        return null;
    }

    private ReferenceReading findLastReferenceReading(Range<Instant> lastInterval, NavigableMap<Instant, BaseReadingRecord> referenceReadings) {
        Instant endOfLastInterval = lastInterval.upperEndpoint();
        Map.Entry<Instant, BaseReadingRecord> closestFromRightReading = referenceReadings.ceilingEntry(endOfLastInterval);
        if (closestFromRightReading != null) {
            Range<Instant> previousInterval = findPreviousChannelInterval(closestFromRightReading.getKey());
            return new ReferenceReading(closestFromRightReading.getValue(), previousInterval);
        }
        Map.Entry<Instant, BaseReadingRecord> closestFromLeftReading = referenceReadings.lowerEntry(endOfLastInterval);
        if (closestFromLeftReading != null) {
            // we found closest from left but it may not be a single reading within an interval
            Range<Instant> previousInterval = findPreviousChannelInterval(closestFromLeftReading.getKey());
            BaseReadingRecord closestToStartReading = referenceReadings.ceilingEntry(previousInterval.upperEndpoint()).getValue();
            return new ReferenceReading(closestToStartReading, previousInterval);
        }
        return null;
    }

    private void validateDeltaOfReferenceReadings(BigDecimal deltaOfReferenceReadings) {
        NoneOrBigDecimal minimumThreshold = getMinimumThresholdProperty();
        if (!minimumThreshold.isNone() && deltaOfReferenceReadings.compareTo(minimumThreshold.getValue()) < 0) {
            throw skipValidationException(SkipValidationOption.MARK_ALL_VALID,
                    MessageSeeds.DIFFERENCE_BETWEEN_TWO_REFERENCE_READINGS_LESS_THAN_MIN_THRESHOLD, getDefaultMessageSeedArgs());
        }
    }

    private BigDecimal getSumOfIntervalReadings(Range<Instant> intervalToSummarize, Function<BigDecimal, BigDecimal> conversion) {
        return this.channel.getIntervalReadings(this.validatedReadingType, intervalToSummarize)
                .stream()
                .map(IntervalReadingRecord::getValue)
                .filter(value -> value != null)
                .reduce(BigDecimal::add)
                .map(conversion::apply)
                .orElse(BigDecimal.ZERO);
    }

    private ValidationStrategy computeValidationStrategy(BigDecimal deltaOfReferenceReadings, BigDecimal sumOfChannelReadings,
                                                         Range<Instant> firstChannelInterval, Range<Instant> intervalToSummarize) {
        TwoValuesDifference maxDifference = getMaximumDifferenceProperty();
        boolean isDifferenceValid = isDifferenceValid(deltaOfReferenceReadings, sumOfChannelReadings, maxDifference);
        if (firstChannelInterval.upperEndpoint().compareTo(intervalToSummarize.lowerEndpoint()) <= 0) {
            return isDifferenceValid ?
                    ValidationStrategy.markValid(Range.openClosed(firstChannelInterval.lowerEndpoint(), intervalToSummarize.upperEndpoint())) :
                    ValidationStrategy.markValidAndSuspect(Range.openClosed(firstChannelInterval.lowerEndpoint(), intervalToSummarize.lowerEndpoint()), intervalToSummarize);
        } else {
            return isDifferenceValid ?
                    ValidationStrategy.markValid(intervalToSummarize) :
                    ValidationStrategy.markSuspect(intervalToSummarize);
        }
    }

    private boolean isDifferenceValid(BigDecimal deltaOfReferenceReadings, BigDecimal sumOfChannelReadings, TwoValuesDifference maxDifference) {
        BigDecimal maxDifferenceValue;
        switch (maxDifference.getType()) {
            case ABSOLUTE:
                maxDifferenceValue = maxDifference.getValue().abs();
                break;
            case RELATIVE:
                maxDifferenceValue = maxDifference.getValue().divide(new BigDecimal(100), 3, BigDecimal.ROUND_HALF_UP).multiply(deltaOfReferenceReadings);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported type of difference: " + maxDifference.getType().name());
        }
        BigDecimal differenceValue = deltaOfReferenceReadings.subtract(sumOfChannelReadings).abs();
        return maxDifferenceValue.compareTo(differenceValue) >= 0;
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
        return "Meter advance [STD]";
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
                .specForValuesOf(new ReadingTypeValueFactory(meteringService, ReadingTypeValueFactory.Mode.ALL))
                .named(REFERENCE_READING_TYPE, TranslationKeys.REFERENCE_READING_TYPE)
                .fromThesaurus(getThesaurus())
                .markRequired()
                .finish();
    }

    private PropertySpec buildMaxAbsoluteDifferencePropertySpec() {
        TwoValuesDifference defaultValue = new TwoValuesDifference(TwoValuesDifference.Type.ABSOLUTE, BigDecimal.ZERO);
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
                .specForValuesOf(new NoneOrTimeDurationValueFactory())
                .named(REFERENCE_PERIOD, TranslationKeys.REFERENCE_PERIOD)
                .fromThesaurus(getThesaurus())
                .markRequired()
                .setDefaultValue(NoneOrTimeDurationValue.none())
                .finish();
    }

    private PropertySpec buildMinThresholdPropertySpec() {
        return getPropertySpecService()
                .specForValuesOf(new NoneOrBigDecimalValueFactory())
                .named(MIN_THRESHOLD, TranslationKeys.MIN_THRESHOLD)
                .fromThesaurus(getThesaurus())
                .markRequired()
                .setDefaultValue(NoneOrBigDecimal.none())
                .finish();
    }

    @Override
    public void validateProperties(Map<String, Object> properties) {
        if (properties.containsKey(REFERENCE_READING_TYPE)) {
            ReadingTypeValueFactory.ReadingTypeReference readingTypeReference = (ReadingTypeValueFactory.ReadingTypeReference) properties.get(REFERENCE_READING_TYPE);
            ReadingType readingType = readingTypeReference.getReadingType();
            if (readingType.getMeasuringPeriod().isApplicable()) {
                throw new LocalizedFieldValidationException(MessageSeeds.REFERENCE_READINGTYPE_INCORRECT, "properties." + REFERENCE_READING_TYPE);
            }
        }
    }

    private SkipValidationException skipValidationException(SkipValidationOption skipValidationOption, MessageSeeds messageSeed, Object... args) {
        return new SkipValidationException(getThesaurus(), skipValidationOption, messageSeed, args);
    }

    ValidationStrategy getValidationStrategy() {
        return validationStrategy;
    }

    private static class ReferenceReading {

        private final BaseReadingRecord readingRecord;
        private final Range<Instant> interval;

        ReferenceReading(BaseReadingRecord readingRecord, Range<Instant> channelIntervalToSummarize) {
            this.readingRecord = readingRecord;
            this.interval = channelIntervalToSummarize;
        }

        Instant getClosestIntervalStart() {
            return interval.lowerEndpoint();
        }

        Instant getClosestIntervalEnd() {
            return interval.upperEndpoint();
        }

        BigDecimal getValue() {
            return readingRecord.getValue();
        }

        Instant getTimeStamp() {
            return readingRecord.getTimeStamp();
        }
    }
}
