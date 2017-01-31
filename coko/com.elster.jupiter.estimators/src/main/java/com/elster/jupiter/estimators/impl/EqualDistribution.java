/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.AdvanceReadingsSettings;
import com.elster.jupiter.estimation.AdvanceReadingsSettingsWithoutNoneFactory;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

class EqualDistribution extends AbstractEstimator implements Estimator {
    static final String ADVANCE_READINGS_SETTINGS = TranslationKeys.ADVANCE_READINGS_SETTINGS.getKey();
    public static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = TranslationKeys.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS.getKey();
    private static final long MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = 10L;

    /**
     * Contains {@link TranslationKey}s for all the
     * {@link PropertySpec}s of this estimator.
     *
     * @author Rudi Vankeirsbilck (rudi)
     * @since 2015-12-07 (14:03)
     */
    public enum TranslationKeys implements TranslationKey {
        ESTIMATOR_NAME(EqualDistribution.class.getName(), "Equal distribution"),
        MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS("equaldistribution.maxNumberOfConsecutiveSuspects", "Max number of consecutive suspects"),
        MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DESCRIPTION("equaldistribution.maxNumberOfConsecutiveSuspects.description",
                "The maximum number of consecutive suspects that is allowed. If this amount is exceeded data is not estimated, but can be manually edited or estimated."),
        ADVANCE_READINGS_SETTINGS("equaldistribution.advanceReadingsSettings", "Use advance readings"),
        ADVANCE_READINGS_SETTINGS_DESCRIPTION("equaldistribution.advanceReadingsSettings.description",
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

    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    private final MeteringService meteringService;
    private AdvanceReadingsSettings advanceReadingsSettings;
    private long maxNumberOfConsecutiveSuspects;

    EqualDistribution(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService) {
        super(thesaurus, propertySpecService);
        this.meteringService = meteringService;
    }

    EqualDistribution(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.meteringService = meteringService;
    }

    @Override
    public void init() {
        advanceReadingsSettings = getProperty(ADVANCE_READINGS_SETTINGS, AdvanceReadingsSettings.class)
                .orElse(BulkAdvanceReadingsSettings.INSTANCE);
        maxNumberOfConsecutiveSuspects = getProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, Long.class)
                .orElse(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE);
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.singletonList(ADVANCE_READINGS_SETTINGS);
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system) {
        SimpleEstimationResult.EstimationResultBuilder builder = SimpleEstimationResult.builder();
        Set<QualityCodeSystem> systems = Estimator.qualityCodeSystemsToTakeIntoAccount(system);
        estimationBlocks.forEach(block -> {
            try (LoggingContext contexts = initLoggingContext(block)) {
                if (estimate(block, systems)) {
                    builder.addEstimated(block);
                } else {
                    builder.addRemaining(block);
                }
            }
        });
        return builder.build();
    }

    private boolean estimate(EstimationBlock block, Set<QualityCodeSystem> systems) {
        if (!canEstimate(block)) {
            return false;
        }
        if (BulkAdvanceReadingsSettings.INSTANCE.equals(advanceReadingsSettings)) {
            return estimateUsingBulk(block, systems);
        } else if (advanceReadingsSettings instanceof ReadingTypeAdvanceReadingsSettings) {
            return estimateUsingAdvances(block,
                    ((ReadingTypeAdvanceReadingsSettings) advanceReadingsSettings).getReadingType(), systems);
        }
        return false;
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
        boolean blockSizeOk = block.estimatables().size() <= maxNumberOfConsecutiveSuspects;
        if (!blockSizeOk) {
            String message = "Failed estimation with {rule}: Block {block} since it contains {0} suspects, which exceeds the maximum of {1}";
            LoggingContext.get().info(getLogger(), message, block.estimatables().size(), maxNumberOfConsecutiveSuspects);
        }
        return blockSizeOk;
    }

    private boolean estimateUsingAdvances(EstimationBlock block, ReadingType readingType, Set<QualityCodeSystem> systems) {
        return getAdvanceCimChannel(block, readingType)
                .map(cimChannel -> estimateUsingAdvances(block, cimChannel, systems))
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since the meter does not have readings for the reading type {0}";
                    LoggingContext.get().info(getLogger(), message, readingType.getMRID());
                    return false;
                });
    }

    private static Optional<CimChannel> getAdvanceCimChannel(EstimationBlock block, ReadingType readingType) {
        return block.getChannel().getChannelsContainer().getChannels().stream()
                .filter(channel -> channel.getReadingTypes().contains(readingType))
                .map(channel -> channel.getCimChannel(readingType))
                .flatMap(Functions.asStream())
                .findFirst();
    }

    private boolean estimateUsingAdvances(EstimationBlock block, CimChannel advanceCimChannel, Set<QualityCodeSystem> systems) {
        Instant before = block.getChannel().getPreviousDateTime(block.estimatables().get(0).getTimestamp());
        return advanceCimChannel.getReadingsOnOrBefore(before, 1).stream()
                .findFirst()
                .flatMap(priorReading ->
                        advanceCimChannel.getReadings(Range.atLeast(lastOf(block.estimatables()).getTimestamp())).stream()
                                .findFirst()
                                .map(laterReading -> estimateUsingAdvances(block, priorReading, laterReading,
                                        advanceCimChannel.getReadingType(), advanceCimChannel, systems)))
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since there was no prior and later advance reading.";
                    LoggingContext.get().info(getLogger(), message);
                    return false;
                });
    }

    private boolean estimateUsingAdvances(EstimationBlock block, BaseReadingRecord priorReading, BaseReadingRecord laterReading,
                                          ReadingType advanceReadingType, CimChannel advanceCimChannel, Set<QualityCodeSystem> systems) {
        if (!canEstimate(block, priorReading, laterReading, advanceReadingType, advanceCimChannel, systems)) {
            return false;
        }

        BigDecimal conversionFactor = calculateConversionFactor(block, advanceReadingType);

        BigDecimal advance = laterReading.getValue().subtract(priorReading.getValue()).setScale(10, RoundingMode.HALF_UP).multiply(conversionFactor);

        return block.getChannel().getCimChannel(block.getReadingType())
                .flatMap(cimChannel -> calculateConsumption(block, priorReading, laterReading, cimChannel, systems)
                        .map(advance::subtract)
                        .map(toDistribute -> toDistribute.divide(BigDecimal.valueOf(block.estimatables().size()), 6, RoundingMode.HALF_UP))
                        .map(perInterval -> {
                            block.estimatables().forEach(estimable-> estimable.setEstimation(perInterval));
                            return true;
                        })).orElse(false);
    }

    private static BigDecimal calculateConversionFactor(EstimationBlock block, ReadingType advanceReadingType) {
        int metricMultiplierConversion = advanceReadingType.getMultiplier().getMultiplier() - block.getReadingType().getMultiplier().getMultiplier();
        return BigDecimal.valueOf(1, -metricMultiplierConversion).setScale(10, RoundingMode.HALF_UP);
    }

    private boolean canEstimate(EstimationBlock block, BaseReadingRecord priorReading, BaseReadingRecord laterReading,
                                ReadingType advanceReadingType, CimChannel advanceCimChannel, Set<QualityCodeSystem> systems) {
        if (!block.getReadingType().getUnit().equals(advanceReadingType.getUnit())) {
            return false;
        }
        if (!isValidReading(advanceCimChannel, priorReading, systems)) {
            String message = "Failed estimation with {rule}: Block {block} since the prior advance reading is suspect, estimated or overflow";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        if (!isValidReading(advanceCimChannel, laterReading, systems)) {
            String message = "Failed estimation with {rule}: Block {block} since the later advance reading is suspect, estimated or overflow";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        if (priorReading.getValue() == null) {
            String message = "Failed estimation with {rule}: Block {block} since the prior advance reading has no value";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        if (laterReading.getValue() == null) {
            String message = "Failed estimation with {rule}: Block {block} since the later advance reading has no value";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        return true;
    }

    private static boolean isValidReading(CimChannel advanceCimChannel, BaseReadingRecord readingToEvaluate, Set<QualityCodeSystem> systems) {
        return advanceCimChannel.findReadingQualities()
                .atTimestamp(readingToEvaluate.getTimeStamp())
                .actual()
                .ofQualitySystems(systems)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.OVERFLOWCONDITIONDETECTED))
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategory(QualityCodeCategory.ESTIMATED)
                .noneMatch();
    }

    private Optional<BigDecimal> calculateConsumption(EstimationBlock block,
                                                      BaseReadingRecord priorReading, BaseReadingRecord laterReading,
                                                      CimChannel cimChannel, Set<QualityCodeSystem> systems) {
        Instant from = priorReading.getTimeStamp();
        Instant to = laterReading.getTimeStamp();
        List<Instant> instants = new ArrayList<>(cimChannel.toList(Range.closed(from, to)));
        if (!lastOf(instants).equals(to)) {
            instants.add(cimChannel.getNextDateTime(lastOf(instants)));
        }
        Map<Instant, List<ReadingQualityRecord>> invalidsByTimestamp =
                cimChannel.findReadingQualities()
                        .inTimeInterval(Range.closed(instants.get(0), lastOf(instants)))
                        .actual()
                        .ofQualitySystems(systems)
                        .ofQualityIndex(QualityCodeIndex.SUSPECT)
                        .orOfAnotherTypeInSameSystems()
                        .ofAnyQualityIndexInCategory(QualityCodeCategory.ESTIMATED)
                        .stream()
                        .collect(Collectors.groupingBy(ReadingQualityRecord::getReadingTimestamp));

        TemporalAmount intervalLength = cimChannel.getIntervalLength().get();

        Map<Instant, BigDecimal> percentages = instants.stream().collect(Collectors.toMap(Function.identity(), instant -> BigDecimal.ONE));
        if (!instants.get(0).equals(from)) {
            Instant intervalEnd = instants.get(0);
            Instant intervalStart = ZonedDateTime.ofInstant(intervalEnd, cimChannel.getZoneId()).minus(intervalLength).toInstant();
            BigDecimal percentage = BigDecimal.valueOf(intervalEnd.toEpochMilli() - from.toEpochMilli())
                    .divide(BigDecimal.valueOf(intervalEnd.toEpochMilli() - intervalStart.toEpochMilli()), 10, RoundingMode.HALF_UP);
            percentages.put(intervalEnd, percentage);
        }
        if (!lastOf(instants).equals(to)) {
            Instant intervalEnd = lastOf(instants);
            Instant intervalStart = ZonedDateTime.ofInstant(intervalEnd, cimChannel.getZoneId()).minus(intervalLength).toInstant();
            BigDecimal percentage = BigDecimal.valueOf(to.toEpochMilli() - intervalStart.toEpochMilli())
                    .divide(BigDecimal.valueOf(intervalEnd.toEpochMilli() - intervalStart.toEpochMilli()), 10, RoundingMode.HALF_UP);
            percentages.put(intervalEnd, percentage);
        }

        List<Instant> neededConsumption = instants.stream()
                .filter(instant -> block.estimatables().stream().map(Estimatable::getTimestamp).noneMatch(instant::equals))
                .collect(Collectors.toList());

        if (neededConsumption.stream().anyMatch(invalidsByTimestamp::containsKey)) {
            boolean suspects = neededConsumption.stream()
                    .anyMatch(instant -> Optional.ofNullable(invalidsByTimestamp.get(instant)).orElse(Collections.emptyList()).stream()
                            .anyMatch(ReadingQualityRecord::isSuspect));
            String message = suspects ? "Failed estimation with {rule}: Block {block} since there are additional suspects between the advance readings"
                    : "Failed estimation with {rule}: Block {block} since there are estimated consumptions between the advance readings";
            LoggingContext.get().info(getLogger(), message);
            return Optional.empty();
        }

        List<BaseReadingRecord> readings = instants.isEmpty() ? Collections.emptyList() : cimChannel.getReadings(Range.closed(instants.get(0), lastOf(instants)));

        return Optional.of(readings.stream()
                .filter(reading -> neededConsumption.contains(reading.getTimeStamp()))
                .map(reading -> reading.getValue().multiply(percentages.get(reading.getTimeStamp())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private boolean estimateUsingBulk(EstimationBlock block, Set<QualityCodeSystem> systems) {
        return block.getReadingType().getBulkReadingType()
                .map(bulkReadingType -> estimateUsingBulk(block, bulkReadingType, systems))
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since the reading type {readingType} has no bulk reading type.";
                    LoggingContext.get().info(getLogger(), message);
                    return false;
                });
    }

    private boolean estimateUsingBulk(EstimationBlock block, ReadingType bulkReadingType, Set<QualityCodeSystem> systems) {
        return block.getChannel().getCimChannel(bulkReadingType)
                .map(cimChannel -> estimateUsingBulk(block, cimChannel, systems))
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since the channel has no bulk reading type.";
                    LoggingContext.get().info(getLogger(), message);
                    return false;
                });
    }

    private boolean estimateUsingBulk(EstimationBlock block, CimChannel bulkCimChannel, Set<QualityCodeSystem> systems) {
        return getValueAt(bulkCimChannel, lastOf(block.estimatables()).getTimestamp(), systems)
                .flatMap(valueAt -> getValueAt(bulkCimChannel, bulkCimChannel.getPreviousDateTime(block.estimatables().get(0).getTimestamp()), systems)
                        .map(valueAt::subtract))
                .map(difference -> difference.divide(BigDecimal.valueOf(block.estimatables().size()), 6, BigDecimal.ROUND_HALF_UP))
                .map(equalValue -> {
                    block.estimatables().forEach(estimable -> estimable.setEstimation(equalValue));
                    return true;
                })
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since the surrounding bulk readings are not available or have the overflow flag.";
                    LoggingContext.get().info(getLogger(), message);
                    return false;
                });
    }

    private static <T> T lastOf(List<T> elements) {
        return elements.get(elements.size() - 1);
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.ESTIMATOR_NAME.getDefaultFormat();
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
                        .specForValuesOf(new AdvanceReadingsSettingsWithoutNoneFactory(meteringService))
                        .named(TranslationKeys.ADVANCE_READINGS_SETTINGS)
                        .describedAs(TranslationKeys.ADVANCE_READINGS_SETTINGS_DESCRIPTION)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(BulkAdvanceReadingsSettings.INSTANCE)
                        .finish());
        return builder.build();
    }

    private static Optional<BigDecimal> getValueAt(CimChannel bulkCimChannel, Instant timestamp, Set<QualityCodeSystem> systems) {
        return bulkCimChannel.getReading(timestamp)
                .filter(baseReadingRecord -> bulkCimChannel.findReadingQualities()
                        .atTimestamp(timestamp)
                        .actual()
                        .ofQualitySystems(systems)
                        .ofQualityIndex(QualityCodeIndex.OVERFLOWCONDITIONDETECTED)
                        .noneMatch())
                .flatMap(baseReadingRecord -> Optional.ofNullable(baseReadingRecord.getValue()));
    }

    private boolean isOverflow(IntervalReadingRecord intervalReadingRecord) {
        return intervalReadingRecord.hasReadingQuality(ProtocolReadingQualities.OVERFLOW.getReadingQualityType());
    }

    @Override
    public void validateProperties(Map<String, Object> estimatorProperties) {
        ImmutableMap.Builder<String, Consumer<Map.Entry<String, Object>>> builder = ImmutableMap.builder();
        builder.put(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, property -> {
            Long value = (Long) property.getValue();
            if (value.intValue() < 1) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS, MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
            }
        });
        builder.put(ADVANCE_READINGS_SETTINGS, property -> {
            if (NoneAdvanceReadingsSettings.INSTANCE.equals(property.getValue())) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ADVANCE_READINGTYPE_NONE_NOT_ALLOWED, ADVANCE_READINGS_SETTINGS);
            }
            if (BulkAdvanceReadingsSettings.INSTANCE.equals(property.getValue())) {
                return;
            }
            ReadingType readingType = ((ReadingTypeAdvanceReadingsSettings) property.getValue()).getReadingType();
            if (!readingType.isCumulative()) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ADVANCE_READINGTYPE, ADVANCE_READINGS_SETTINGS);
            }
        });

        ImmutableMap<String, Consumer<Map.Entry<String, Object>>> propertyValidations = builder.build();

        estimatorProperties.entrySet()
                .forEach(
                        property -> Optional
                                .ofNullable(propertyValidations.get(property.getKey()))
                                .ifPresent(validator -> validator.accept(property)));
    }

}
