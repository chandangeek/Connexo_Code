/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.AdvanceReadingsSettings;
import com.elster.jupiter.estimation.AdvanceReadingsSettingsFactory;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.math.RoundingMode.HALF_UP;

/**
 * Created by igh on 25/03/2015.
 */
class AverageWithSamplesEstimator extends AbstractEstimator {

    /**
     * Contains {@link TranslationKey}s for all the
     * {@link PropertySpec}s of this estimator.
     *
     * @author Rudi Vankeirsbilck (rudi)
     * @since 2015-12-07 (14:03)
     */
    public enum TranslationKeys implements TranslationKey {
        ESTIMATOR_NAME(AverageWithSamplesEstimator.class.getName(), "Average with samples"),
        MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS("averagewithsamples.maxNumberOfConsecutiveSuspects", "Max number of consecutive suspects"),
        MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DESCRIPTION("averagewithsamples.maxNumberOfConsecutiveSuspects.description",
                "The maximum number of consecutive suspects that is allowed. If this amount is exceeded data is not estimated, but can be manually edited or estimated."),
        MIN_NUMBER_OF_SAMPLES("averagewithsamples.minNumberOfSamples", "Minimum samples"),
        MIN_NUMBER_OF_SAMPLES_DESCRIPTION("averagewithsamples.minNumberOfSamples.description", "The minimum amount of sample needed for estimation."),
        MAX_NUMBER_OF_SAMPLES("averagewithsamples.maxNumberOfSamples", "Maximum samples"),
        MAX_NUMBER_OF_SAMPLES_DESCRIPTION("averagewithsamples.maxNumberOfSamples.description", "The maximum amount of sample needed for estimation."),
        ALLOW_NEGATIVE_VALUES("averagewithsamples.allowNegativeValues", "Allow negative values"),
        ALLOW_NEGATIVE_VALUES_DESCRIPTION("averagewithsamples.allowNegativeValues.description", "Indicates that negative values can be used to estimate data."),
        RELATIVE_PERIOD("averagewithsamples.relativePeriod", "Relative period"),
        RELATIVE_PERIOD_DESCRIPTION("averagewithsamples.relativePeriod.description", "The number of samples you will find and be able to use for estimation."),
        ADVANCE_READINGS_SETTINGS("averagewithsamples.advanceReadingsSettings", "Use advance readings"),
        ADVANCE_READINGS_SETTINGS_DESCRIPTION("averagewithsamples.advanceReadingsSettings.description",
                "Use other data than the channel’s own delta values to estimate suspect data.");

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }
    }

    private static class SamplesComparator implements Comparator<BaseReadingRecord> {

        private ZonedDateTime estimatableTime;

        SamplesComparator(Instant estimatableTime, ZoneId zoneId) {
            this.estimatableTime = ZonedDateTime.ofInstant(estimatableTime, zoneId);
        }

        private ZoneId getZone() {
            return estimatableTime.getZone();
        }

        @Override
        public int compare(BaseReadingRecord a, BaseReadingRecord b) {
            ZonedDateTime timeA = ZonedDateTime.ofInstant(a.getTimeStamp(), getZone());
            ZonedDateTime timeB = ZonedDateTime.ofInstant(b.getTimeStamp(), getZone());

            // use distance in days to avoid DST shenanigans

            long distanceA = abs(ChronoUnit.DAYS.between(timeA, estimatableTime));
            long distanceB = abs(ChronoUnit.DAYS.between(timeB, estimatableTime));

            return Long.compare(distanceA, distanceB);
        }
    }

    static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = TranslationKeys.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS.getKey();
    static final String MIN_NUMBER_OF_SAMPLES = TranslationKeys.MIN_NUMBER_OF_SAMPLES.getKey();
    static final String MAX_NUMBER_OF_SAMPLES = TranslationKeys.MAX_NUMBER_OF_SAMPLES.getKey();
    static final String ALLOW_NEGATIVE_VALUES = TranslationKeys.ALLOW_NEGATIVE_VALUES.getKey();
    static final String RELATIVE_PERIOD = TranslationKeys.RELATIVE_PERIOD.getKey();
    static final String ADVANCE_READINGS_SETTINGS = TranslationKeys.ADVANCE_READINGS_SETTINGS.getKey();

    private static final Long MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = 10L;
    private static final Long MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE = 1L;
    private static final Long MAX_NUMBER_OF_SAMPLES_DEFAULT_VALUE = 10L;
    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);

    private final ValidationService validationService;
    private final MeteringService meteringService;
    private final TimeService timeService;

    private Long numberOfConsecutiveSuspects;
    private Long minNumberOfSamples;
    private Long maxNumberOfSamples;
    private boolean allowNegativeValues = false;
    private RelativePeriod relativePeriod;
    private AdvanceReadingsSettings advanceReadingsSettings;

    AverageWithSamplesEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService,
                                ValidationService validationService, MeteringService meteringService,
                                TimeService timeService) {
        super(thesaurus, propertySpecService);
        this.validationService = validationService;
        this.meteringService = meteringService;
        this.timeService = timeService;
    }

    AverageWithSamplesEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService,
                                ValidationService validationService, MeteringService meteringService,
                                TimeService timeService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.validationService = validationService;
        this.meteringService = meteringService;
        this.timeService = timeService;
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system) {
        List<EstimationBlock> remain = new ArrayList<>();
        List<EstimationBlock> estimated = new ArrayList<>();
        Set<QualityCodeSystem> systems = Estimator.qualityCodeSystemsToTakeIntoAccount(system);
        for (EstimationBlock block : estimationBlocks) {
            try (LoggingContext contexts = initLoggingContext(block)) {
                if (!isEstimable(block)) {
                    remain.add(block);
                } else {
                    if (estimate(block, systems)) {
                        estimated.add(block);
                    } else {
                        remain.add(block);
                    }
                }
            }
        }
        return SimpleEstimationResult.of(remain, estimated);
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.ESTIMATOR_NAME.getDefaultFormat();
    }

    @Override
    public void validateProperties(Map<String, Object> estimatorProperties) {
        Long maxSamples = null;
        Long minSamples = null;
        if (estimatorProperties == null) {
            throw new IllegalArgumentException("Estimator properties should be provided");
        }
        for (Map.Entry<String, Object> property : estimatorProperties.entrySet()) {
            if (property.getKey().equals(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS)) {
                Long value = (Long) property.getValue();
                if (value.intValue() < 1) {
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS_SHOULD_BE_INTEGER_VALUE,
                            MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
                }
            } else if (property.getKey().equals(ADVANCE_READINGS_SETTINGS)) {
                Object settings = property.getValue();
                if (settings instanceof ReadingTypeAdvanceReadingsSettings) {
                    ReadingType readingType = ((ReadingTypeAdvanceReadingsSettings) settings).getReadingType();
                    if (readingType == null) {
                        throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ADVANCE_READINGTYPE_NONE_NOT_ALLOWED, ADVANCE_READINGS_SETTINGS);
                    }
                    if (!readingType.isCumulative()) {
                        throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ADVANCE_READINGTYPE, ADVANCE_READINGS_SETTINGS);
                    }
                }
            } else if (property.getKey().equals(MAX_NUMBER_OF_SAMPLES)) {
                maxSamples = (Long) property.getValue();
            } else if (property.getKey().equals(MIN_NUMBER_OF_SAMPLES)) {
                minSamples = (Long) property.getValue();
            }
        }
        if ((maxSamples != null) && (minSamples != null) && (maxSamples < minSamples)) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_SAMPLES, MAX_NUMBER_OF_SAMPLES);
        }
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(
                MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS,
                MAX_NUMBER_OF_SAMPLES,
                MIN_NUMBER_OF_SAMPLES,
                ALLOW_NEGATIVE_VALUES,
                RELATIVE_PERIOD,
                ADVANCE_READINGS_SETTINGS
        );
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();

        builder.add(getPropertySpecService()
                .longSpec()
                .named(TranslationKeys.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS)
                .describedAs(TranslationKeys.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE)
                .finish());

        builder.add(
            getPropertySpecService()
                .booleanSpec()
                .named(TranslationKeys.ALLOW_NEGATIVE_VALUES)
                .describedAs(TranslationKeys.ALLOW_NEGATIVE_VALUES_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .finish());

        builder.add(
            getPropertySpecService()
                .specForValuesOf(new AdvanceReadingsSettingsFactory(meteringService))
                .named(TranslationKeys.ADVANCE_READINGS_SETTINGS)
                .describedAs(TranslationKeys.ADVANCE_READINGS_SETTINGS_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(NoneAdvanceReadingsSettings.INSTANCE)
                .finish());

        builder.add(
            getPropertySpecService()
                    .longSpec()
                    .named(TranslationKeys.MIN_NUMBER_OF_SAMPLES)
                    .describedAs(TranslationKeys.MIN_NUMBER_OF_SAMPLES_DESCRIPTION)
                    .fromThesaurus(this.getThesaurus())
                    .markRequired()
                    .setDefaultValue(MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE)
                    .finish());

        builder.add(
            getPropertySpecService()
                    .longSpec()
                    .named(TranslationKeys.MAX_NUMBER_OF_SAMPLES)
                    .describedAs(TranslationKeys.MAX_NUMBER_OF_SAMPLES_DESCRIPTION)
                    .fromThesaurus(this.getThesaurus())
                    .markRequired()
                    .setDefaultValue(MAX_NUMBER_OF_SAMPLES_DEFAULT_VALUE)
                    .finish());

        builder.add(
            getPropertySpecService()
                .relativePeriodSpec()
                .named(TranslationKeys.RELATIVE_PERIOD)
                .describedAs(TranslationKeys.RELATIVE_PERIOD_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(timeService.getAllRelativePeriod())
                .finish());

        return builder.build();
    }

    @Override
    public void init() {
        numberOfConsecutiveSuspects = getProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, Long.class)
                .orElse(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE);

        maxNumberOfSamples = getProperty(MAX_NUMBER_OF_SAMPLES, Long.class)
                .orElse(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE);

        minNumberOfSamples = getProperty(MIN_NUMBER_OF_SAMPLES, Long.class)
                .orElse(MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE);

        allowNegativeValues = getProperty(ALLOW_NEGATIVE_VALUES, Boolean.class)
                .orElse(false);

        relativePeriod = getProperty(RELATIVE_PERIOD, RelativePeriod.class).orElse(null);

        advanceReadingsSettings = getProperty(ADVANCE_READINGS_SETTINGS, AdvanceReadingsSettings.class)
                .orElse(NoneAdvanceReadingsSettings.INSTANCE);
    }

    private boolean canEstimate(EstimationBlock block) {
        return isBlockSizeOk(block) && isRegular(block);
    }

    private boolean isRegular(EstimationBlock block) {
        boolean regular = block.getReadingType().isRegular();
        if (!regular) {
            String message = "Failed estimation with {rule}: Block {block} since it has a reading type that is not regular : {readingType}";
            LoggingContext.get().info(getLogger(), message);
        }
        return regular;
    }

    private boolean isBlockSizeOk(EstimationBlock block) {
        boolean blockSizeOk = block.estimatables().size() <= numberOfConsecutiveSuspects;
        if (!blockSizeOk) {
            String message = "Failed estimation with {rule}: Block {block} since it contains {0} suspects, which exceeds the maximum of {1}";
            LoggingContext.get().info(getLogger(), message, block.estimatables().size(), numberOfConsecutiveSuspects);
        }
        return blockSizeOk;
    }

    private boolean isEstimable(EstimationBlock block) {
        if (!canEstimate(block)) {
            return false;
        }
        if (block.getReadingType().isCumulative()) {
            String message = "Failed estimation with {rule}: Block {block} since the reading type {readingType} is cumulative. Only delta readingtypes are allowed";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        return true;
    }

    private boolean estimate(EstimationBlock estimationBlock, Set<QualityCodeSystem> systems) {
        if (advanceReadingsSettings instanceof BulkAdvanceReadingsSettings) {
            return estimateWithBulk(estimationBlock, systems);
        } else if (advanceReadingsSettings instanceof ReadingTypeAdvanceReadingsSettings) {
            return estimateWithDeltas(estimationBlock, systems);
        } else {
            return estimateWithoutAdvances(estimationBlock, systems);
        }
    }

    private boolean estimateWithBulk(EstimationBlock estimationBlock, Set<QualityCodeSystem> systems) {
        if (!estimateWithoutAdvances(estimationBlock, systems)) {
            return false;
        }
        return calculateConsumptionUsingBulk(estimationBlock)
                .map(consumption -> {
                    rescaleEstimation(estimationBlock, consumption);
                    return true;
                }).orElse(false);
    }

    private Optional<BigDecimal> calculateConsumptionUsingBulk(EstimationBlock estimationBlock) {
        return estimationBlock.getReadingType().getBulkReadingType()
                .map(bulkReadingType -> {
                    Instant startInterval = estimationBlock.estimatables().get(0).getTimestamp();
                    Instant endInterval = estimationBlock.estimatables().get(estimationBlock.estimatables().size() - 1).getTimestamp();
                    return calculateConsumptionUsingBulk(estimationBlock.getChannel(), bulkReadingType, startInterval, endInterval);
                })
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since the reading type {readingType} has no bulk reading type";
                    LoggingContext.get().info(getLogger(), message);
                    return Optional.empty();
                });
    }

    private Optional<BigDecimal> calculateConsumptionUsingBulk(Channel channel, ReadingType bulkReadingType, Instant startInterval, Instant endInterval) {
        Optional<BaseReadingRecord> startBulkReading = channel.getReading(channel.getPreviousDateTime(startInterval));
        Optional<BaseReadingRecord> endBulkReading = channel.getReading(endInterval);
        if ((!startBulkReading.isPresent()) || (!endBulkReading.isPresent())) {
            String message = "Failed estimation with {rule}: Block {block} since the surrounding bulk readings are not available";
            LoggingContext.get().info(getLogger(), message);
            return Optional.empty();
        }
        return calculateConsumption(bulkReadingType, startBulkReading.get(), endBulkReading.get());
    }

    private Optional<BigDecimal> calculateConsumption(ReadingType readingType, BaseReadingRecord startReading, BaseReadingRecord endReading) {
        Quantity startQty = startReading.getQuantity(readingType);
        if (startQty == null) {
            String message = "Failed estimation with {rule}: Block {block} since the prior bulk reading has no value";
            LoggingContext.get().info(getLogger(), message);
            return Optional.empty();
        }
        Quantity endQty = endReading.getQuantity(readingType);
        if (endQty == null) {
            String message = "Failed estimation with {rule}: Block {block} since the later bulk reading has no value";
            LoggingContext.get().info(getLogger(), message);
            return Optional.empty();
        }
        return Optional.of(endQty.getValue().subtract(startQty.getValue()));
    }

    private static void rescaleEstimation(EstimationBlock estimationBlock, BigDecimal totalConsumption) {
        BigDecimal factor = totalConsumption.divide(getTotalEstimatedConsumption(estimationBlock), 10, HALF_UP);
        estimationBlock.estimatables().stream()
                .forEach(estimatable -> estimatable.setEstimation(estimatable.getEstimation().multiply(factor).setScale(6, HALF_UP)));
    }

    private static BigDecimal getTotalEstimatedConsumption(EstimationBlock estimationBlock) {
        return estimationBlock.estimatables().stream()
                .map(Estimatable::getEstimation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal getTotalConsumption(List<? extends BaseReadingRecord> readings, ReadingType readingType, Map<Instant, BigDecimal> percentages) {
        return readings.stream()
                .map(reading -> {
                    BigDecimal percentage = percentages.get(reading.getTimeStamp()).setScale(10, HALF_UP);
                    return Optional.ofNullable(reading.getQuantity(readingType))
                            .map(Quantity::getValue)
                            .filter(Objects::nonNull)
                            .map(percentage::multiply);
                })
                .flatMap(Functions.asStream())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static Optional<CimChannel> getCimChannel(EstimationBlock block, ReadingType readingType) {
        for (Channel channel : block.getChannel().getChannelsContainer().getChannels()) {
            if (channel.getReadingTypes().contains(readingType)) {
                return channel.getCimChannel(readingType);
            }
        }
        return Optional.empty();
    }

    private boolean estimateWithDeltas(EstimationBlock estimationBlock, Set<QualityCodeSystem> systems) {
        if (!estimateWithoutAdvances(estimationBlock, systems)) {
            return false;
        }
        List<? extends Estimatable> estimables = estimationBlock.estimatables();
        Instant startInterval = estimables.get(0).getTimestamp();
        Instant endInterval = estimables.get(estimables.size() - 1).getTimestamp();

        ReadingType registerReadingType =
                ((ReadingTypeAdvanceReadingsSettings) advanceReadingsSettings).getReadingType();
        List<? extends BaseReadingRecord> readingsBefore =
                estimationBlock.getChannel().getChannelsContainer().getReadingsBefore(startInterval, registerReadingType, 1);
        List<? extends BaseReadingRecord> readingsAfter =
                estimationBlock.getChannel().getChannelsContainer().getReadings(Range.atLeast(endInterval), registerReadingType);

        if (readingsBefore.isEmpty()) {
            String message = "Failed estimation with {rule}: Block {block} since the prior advance reading has no value";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        if (readingsAfter.isEmpty()) {
            String message = "Failed estimation with {rule}: Block {block} since the later advance reading has no value";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }

        BaseReadingRecord readingBefore = readingsBefore.get(0);
        Optional<CimChannel> cimChannelFound = getCimChannel(estimationBlock, registerReadingType);
        if (!cimChannelFound.isPresent()) {
            String message = "Failed estimation with {rule}: Block {block} since the meter does not have readings for the reading type {0}";
            LoggingContext.get().info(getLogger(), message, registerReadingType.getMRID());
            return false;
        }

        CimChannel cimChannel = cimChannelFound.get();
        if (!isValidReading(cimChannel, readingBefore, systems)) {
            String message = "Failed estimation with {rule}: Block {block} since the prior advance reading is suspect, estimated or overflow";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }

        BaseReadingRecord readingAfter = readingsAfter.get(0);

        if (!isValidReading(cimChannel, readingAfter, systems)) {
            String message = "Failed estimation with {rule}: Block {block} since the later advance reading is suspect, estimated or overflow";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }

        BigDecimal consumptionBetweenRegisterReadings =
                readingAfter.getQuantity(registerReadingType).getValue().add(readingBefore.getQuantity(registerReadingType).getValue().negate());

        CimChannel consumptionCimChannel = estimationBlock.getCimChannel();

        Range<Instant> preInterval = Range.openClosed(readingBefore.getTimeStamp(), estimationBlock.getChannel().getPreviousDateTime(startInterval));
        if (hasSuspects(consumptionCimChannel, preInterval, systems)) {
            String message = "Failed estimation with {rule}: Block {block} since there are other suspects between the advance readings";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        List<Instant> instants = new ArrayList<>(estimationBlock.getChannel().toList(preInterval));
        Map<Instant, BigDecimal> percentages = percentages(instants, readingBefore.getTimeStamp(),
                estimationBlock.getChannel().getPreviousDateTime(startInterval), estimationBlock.getChannel().getZoneId(),
                estimationBlock.getChannel().getIntervalLength().get());
        BigDecimal consumptionBetweenPreviousRegisterReadingAndStartOfBlock = getTotalConsumption(
                estimationBlock.getChannel().getReadings(preInterval), estimationBlock.getReadingType(), percentages);

        Range<Instant> postInterval = Range.openClosed(endInterval, readingAfter.getTimeStamp());
        instants = new ArrayList<>(estimationBlock.getChannel().toList(postInterval));
        if (!lastOf(instants).equals(readingAfter.getTimeStamp())) {
            Instant addedInstant = estimationBlock.getChannel().getNextDateTime(lastOf(instants));
            instants.add(addedInstant);
            postInterval = Range.openClosed(endInterval, addedInstant);
        }
        if (hasSuspects(consumptionCimChannel, postInterval, systems)) {
            String message = "Failed estimation with {rule}: Block {block} since there are other suspects between the advance readings";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        percentages = percentages(instants, endInterval, readingAfter.getTimeStamp(), estimationBlock.getChannel().getZoneId(),
                estimationBlock.getChannel().getIntervalLength().get());
        BigDecimal consumptionBetweenEndOfBlockAndNextRegisterReading = getTotalConsumption(
                estimationBlock.getChannel().getReadings(Range.openClosed(endInterval, lastOf(instants))), estimationBlock.getReadingType(), percentages);

        BigDecimal totalConsumption = consumptionBetweenRegisterReadings.subtract(
                consumptionBetweenPreviousRegisterReadingAndStartOfBlock).subtract(
                consumptionBetweenEndOfBlockAndNextRegisterReading);
        if (totalConsumption.compareTo(BigDecimal.ZERO) < 0) {
            String message = "Failed estimation with {rule}: Block {block} since no negative values are allowed.";
            LoggingContext.get().info(getLogger(), message);

            return false;
        }
        rescaleEstimation(estimationBlock, totalConsumption);
        return true;
    }

    private static boolean hasSuspects(CimChannel cimChannel, Range<Instant> interval, Set<QualityCodeSystem> systems) {
        return cimChannel
                .findReadingQualities()
                .ofQualitySystems(systems)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(interval)
                .actual()
                .anyMatch();
    }

    private static Instant lastOf(List<Instant> instants) {
        return instants.get(instants.size() - 1);
    }

    private static Map<Instant, BigDecimal> percentages(List<Instant> instants, Instant from, Instant to, ZoneId zoneId, TemporalAmount intervalLength) {
        Map<Instant, BigDecimal> percentages = instants.stream().collect(Collectors.toMap(Function.identity(), instant -> BigDecimal.ONE));
        if (!instants.get(0).equals(from)) {
            Instant intervalEnd = instants.get(0);
            Instant intervalStart = ZonedDateTime.ofInstant(intervalEnd, zoneId).minus(intervalLength).toInstant();
            BigDecimal percentage = BigDecimal.valueOf(intervalEnd.toEpochMilli() - from.toEpochMilli())
                    .divide(BigDecimal.valueOf(intervalEnd.toEpochMilli() - intervalStart.toEpochMilli()), 10, HALF_UP);
            percentages.put(intervalEnd, percentage);
        }
        if (!lastOf(instants).equals(to)) {
            Instant intervalEnd = lastOf(instants);
            Instant intervalStart = ZonedDateTime.ofInstant(intervalEnd, zoneId).minus(intervalLength).toInstant();
            BigDecimal percentage = BigDecimal.valueOf(to.toEpochMilli() - intervalStart.toEpochMilli())
                    .divide(BigDecimal.valueOf(intervalEnd.toEpochMilli() - intervalStart.toEpochMilli()), 10, HALF_UP);
            percentages.put(intervalEnd, percentage);
        }
        return percentages;
    }

    private boolean estimateWithoutAdvances(EstimationBlock estimationBlock, Set<QualityCodeSystem> systems) {
        for (Estimatable estimatable : estimationBlock.estimatables()) {
            if (!estimateWithoutAdvances(estimationBlock, estimatable, systems)) {
                return false;
            }
        }
        return true;
    }

    private boolean estimateWithoutAdvances(EstimationBlock estimationBlock, Estimatable estimatable, Set<QualityCodeSystem> systems) {
        Instant timeToEstimate = estimatable.getTimestamp();
        Range<Instant> period = getPeriod(estimationBlock.getChannel(), timeToEstimate);
        List<BaseReadingRecord> samples = getSamples(estimationBlock, timeToEstimate, period, systems);
        if (samples.size() < this.minNumberOfSamples.intValue()) {
            String message = "Failed estimation with {rule}: Block {block} since not enough samples are found.  Found {0} samples, requires {1} samples";
            LoggingContext.get().info(getLogger(), message, samples.size(), this.minNumberOfSamples.intValue());
            return false;
        } else {
            if (samples.size() > this.maxNumberOfSamples.intValue()) {
                samples = samples.subList(0, this.maxNumberOfSamples.intValue());
            }
            BigDecimal average = samples.stream()
                    .map(record -> record.getQuantity(estimationBlock.getReadingType()))
                    .map(Quantity::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(samples.size()), 6, HALF_UP);
            if (average.compareTo(BigDecimal.ZERO) >= 0 || allowNegativeValues) {
                estimatable.setEstimation(average);
            } else {
                String message = "Failed estimation with {rule}: Block {block} since no negative values are allowed.";
                LoggingContext.get().info(getLogger(), message);
                return false;
            }
        }
        return true;
    }

    private static List<BaseReadingRecord> getSamples(EstimationBlock estimationBlock, Instant estimableTime,
                                                      Range<Instant> period, Set<QualityCodeSystem> systems) {
        return estimationBlock.getChannel().getReadings(period).stream()
                .filter(record -> isValidSample(estimationBlock, estimableTime, record, systems))
                .sorted(new SamplesComparator(estimableTime, estimationBlock.getChannel().getZoneId()))
                .collect(Collectors.toList());
    }

    private static boolean isValidSample(EstimationBlock estimationBlock, Instant estimableTime,
                                         BaseReadingRecord record, Set<QualityCodeSystem> systems) {
        ZoneId zone = estimationBlock.getChannel().getZoneId();
        return sameTimeOfWeek(ZonedDateTime.ofInstant(record.getTimeStamp(), zone), ZonedDateTime.ofInstant(estimableTime, zone))
                && record.getQuantity(estimationBlock.getReadingType()) != null
                && ofRequiredQuality(estimationBlock, record.getTimeStamp(), systems)
                && !estimableTime.equals(record.getTimeStamp());
    }

    private static boolean ofRequiredQuality(EstimationBlock estimationBlock, Instant timeStamp, Set<QualityCodeSystem> systems) {
        return estimationBlock.getCimChannel().findReadingQualities()
                .atTimestamp(timeStamp)
                .actual()
                .ofQualitySystems(systems)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.ACCEPTED))
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.ESTIMATED, QualityCodeCategory.EDITED))
                .noneMatch();
    }

    private static boolean sameTimeOfWeek(ZonedDateTime first, ZonedDateTime second) {
        return first.getDayOfWeek().equals(second.getDayOfWeek())
                && first.getLong(ChronoField.NANO_OF_DAY) == second.getLong(ChronoField.NANO_OF_DAY);
    }

    private Range<Instant> getPeriod(Channel channel, Instant referenceTime) {
        if (relativePeriod != null && timeService.getAllRelativePeriod().getId() != relativePeriod.getId()) {
            return relativePeriod.getOpenClosedInterval(ZonedDateTime.ofInstant(referenceTime, channel.getZoneId()));
        } else {
            Instant start = channel.getChannelsContainer().getStart();
            Optional<Instant> lastChecked = validationService.getLastChecked(channel);
            return lastChecked.map(end -> Range.openClosed(start, end)).orElseGet(() -> Range.greaterThan(start));
        }
    }

    private static boolean isValidReading(CimChannel advanceCimChannel, BaseReadingRecord readingToEvaluate,
                                          Set<QualityCodeSystem> systems) {
        return advanceCimChannel.findReadingQualities()
                .atTimestamp(readingToEvaluate.getTimeStamp())
                .actual()
                .ofQualitySystems(systems)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.OVERFLOWCONDITIONDETECTED))
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategory(QualityCodeCategory.ESTIMATED)
                .noneMatch();
    }
}
