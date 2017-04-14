/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.logging.LoggingContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class PowerGapFill extends AbstractEstimator implements Estimator {

    public static final String MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS = TranslationKeys.MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS.getKey();
    private static final TimeDuration MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = TimeDuration.days(1);

    /**
     * Contains {@link TranslationKey}s for all the
     * {@link PropertySpec}s of this estimator.
     *
     * @author Rudi Vankeirsbilck (rudi)
     * @since 2015-12-07 (14:03)
     */
    public enum TranslationKeys implements TranslationKey {
        ESTIMATOR_NAME(PowerGapFill.class.getName(), "Power gap fill"),
        MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS("powergapfill.maxPeriodOfConsecutiveSuspects", "Maximum period of consecutive suspects"),
        MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DESCRIPTION("powergapfill.maxPeriodOfConsecutiveSuspects.description",
                "The maximum period of consecutive suspects that is allowed. If this period is exceeded, data is not estimated, but can be manually edited or estimated.");

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
    private TimeDuration maxPeriodOfConsecutiveSuspects;

    PowerGapFill(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        checkRequiredProperties();
    }

    PowerGapFill(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public void init() {
        maxPeriodOfConsecutiveSuspects = getProperty(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.class)
                .orElse(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE);
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system) {
        SimpleEstimationResult.EstimationResultBuilder builder = SimpleEstimationResult.builder();
        estimationBlocks.forEach(block -> {
            try (LoggingContext contexts = initLoggingContext(block)) {
                if (estimate(block)) {
                    builder.addEstimated(block);
                } else {
                    builder.addRemaining(block);
                }
            }
        });
        return builder.build();
    }

    private boolean estimate(EstimationBlock estimationBlock) {
        if (!canEstimate(estimationBlock)) {
            return false;
        }
        return estimate(estimationBlock.getChannel(), estimationBlock.getReadingType(), estimationBlock.estimatables());
    }

    private boolean canEstimate(EstimationBlock estimationBlock) {
        return isBlockSizeOk(estimationBlock) && isRegular(estimationBlock);
    }

    private boolean isRegular(EstimationBlock estimationBlock) {
        boolean regular = estimationBlock.getReadingType().isRegular();
        if (!regular) {
            String message = "Failed estimation with {rule}: Block {block} since its reading type {readingType} is not regular.";
            LoggingContext.get().info(getLogger(), message);
        }
        return regular;
    }

    private boolean isBlockSizeOk(EstimationBlock block) {
        Range<Instant> actualPeriodOfSuspects = Range.encloseAll(block.estimatables().stream()
                .map(Estimatable::getTimestamp)
                .collect(Collectors.toSet()));
        boolean blockSizeOk = actualPeriodOfSuspects.upperEndpoint().toEpochMilli() - actualPeriodOfSuspects.lowerEndpoint().toEpochMilli()
                < maxPeriodOfConsecutiveSuspects.getMilliSeconds(); // max period of consecutive suspects is assumed open-closed, thus strict inequality
        if (!blockSizeOk) {
            String message = "Failed estimation with {rule}: Block {block} since its size exceeds the maximum of {0}";
            LoggingContext.get().info(getLogger(), message, maxPeriodOfConsecutiveSuspects);
        }
        return blockSizeOk;
    }

    private boolean estimate(Channel channel, ReadingType readingType, List<? extends Estimatable> estimatables) {
        return readingType.isCumulative() ? estimateBulk(channel, readingType, estimatables) : estimateDelta(channel, readingType, estimatables);
    }

    private boolean estimateBulk(Channel channel, ReadingType readingType, List<? extends Estimatable> estimatables) {
        Optional<CimChannel> cimChannel = channel.getCimChannel(readingType);

        IntervalReadingRecord successiveReading = getSuccessiveReading(channel, cimChannel.get(), lastOf(estimatables).getTimestamp());
        if (successiveReading == null) {
            return false;
        }

        if (!successiveReading.hasReadingQuality(ProtocolReadingQualities.POWERUP.getReadingQualityType())) {
            String message = "Failed estimation with {rule}: Block {block} since the successive reading does not have the power up flag.";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        IntervalReadingRecord precedingReading = getPrecedingReading(cimChannel.get(), channel.getPreviousDateTime(estimatables.get(0).getTimestamp()));
        if (precedingReading == null) {
            return false;
        }

        if (!precedingReading.hasReadingQuality(ProtocolReadingQualities.POWERDOWN.getReadingQualityType())) {
            String message = "Failed estimation with {rule}: Block {block} since the preceding reading does not have the power down flag.";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        return doEstimateBulk(estimatables, precedingReading, successiveReading);
    }

    private IntervalReadingRecord getPrecedingReading(CimChannel cimChannel, Instant previousDateTime) {
        return cimChannel.getReading(previousDateTime)
                    .map(IntervalReadingRecord.class::cast)
                    .orElseGet(() -> {
                        String message = "Failed estimation with {rule}: Block {block} since the preceding reading is missing.";
                        LoggingContext.get().info(getLogger(), message);
                        return null;
                    });
    }

    private IntervalReadingRecord getSuccessiveReading(Channel channel, CimChannel cimChannel, Instant timestamp) {
        Instant nextDateTime = channel.getNextDateTime(timestamp);
        return cimChannel.getReading(nextDateTime)
                .map(IntervalReadingRecord.class::cast)
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since the successive reading is missing.";
                    LoggingContext.get().info(getLogger(), message);
                    return null;
                });
    }

    private boolean doEstimateBulk(List<? extends Estimatable> estimatables, BaseReadingRecord precedingReadingRecord, BaseReadingRecord successiveReadingRecord) {
        BigDecimal value = precedingReadingRecord.getValue();
        if (value == null) {
            String message = "Failed estimation with {rule}: Block {block} since the preceding reading has no value.";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        BigDecimal successiveValue = successiveReadingRecord.getValue();
        if (successiveValue == null) {
            String message = "Failed estimation with {rule}: Block {block} since the successive reading has no value.";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        estimatables.forEach(estimatable -> estimatable.setEstimation(value));
        return true;
    }

    private boolean estimateDelta(Channel channel, ReadingType readingType, List<? extends Estimatable> estimatables) {
        return consumptionDifference(channel, readingType, estimatables)
                .map(consumptionDifference -> doEstimateDelta(estimatables, consumptionDifference))
                .orElse(false);
    }

    private boolean doEstimateDelta(List<? extends Estimatable> estimatables, BigDecimal consumptionDifference) {
        allButLast(estimatables).forEach(estimatable -> estimatable.setEstimation(BigDecimal.ZERO));
        lastOf(estimatables).setEstimation(consumptionDifference);
        return true;
    }

    private Estimatable lastOf(List<? extends Estimatable> estimatables) {
        return estimatables.get(estimatables.size() - 1);
    }

    private List<? extends Estimatable> allButLast(List<? extends Estimatable> estimatables) {
        return estimatables.subList(0, Math.max(0, estimatables.size() - 1));
    }

    private Optional<BigDecimal> consumptionDifference(Channel channel, ReadingType readingType, List<? extends Estimatable> estimatables) {
        return Optional.ofNullable(getBulkCimChannel(channel, readingType))
                .flatMap(bulkCimChannel -> getValueAt(bulkCimChannel, lastOf(estimatables))
                        .flatMap(valueAtLast -> getValueBefore(bulkCimChannel, estimatables.get(0))
                                .map(valueAtLast::subtract)));
    }

    private CimChannel getBulkCimChannel(Channel channel, ReadingType readingType) {
        ReadingType bulkReadingType = getBulkReadingType(readingType);
        if (bulkReadingType == null) {
            return null;
        }
        return channel.getCimChannel(bulkReadingType).orElseGet(() -> {
            String message = "Failed estimation with {rule}: Block {block} since the reading type {readingType} has no bulk reading type.";
            LoggingContext.get().info(getLogger(), message);
            return null;
        });
    }

    private ReadingType getBulkReadingType(ReadingType readingType) {
        return readingType.getBulkReadingType().orElseGet(() -> {
            String message = "Failed estimation with {rule}: Block {block} since the reading type {readingType} has no bulk reading type.";
            LoggingContext.get().info(getLogger(), message);
            return null;
        });
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.ESTIMATOR_NAME.getDefaultFormat();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService()
                .timeDurationSpec()
                .named(TranslationKeys.MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS)
                .describedAs(TranslationKeys.MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE)
                .finish());
        return builder.build();
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    private Optional<BigDecimal> getValueAt(CimChannel bulkCimChannel, Estimatable last) {
        Optional<BaseReadingRecord> reading = bulkCimChannel.getReading(last.getTimestamp());
        if (!reading.isPresent()) {
            String message = "Failed estimation with {rule}: Block {block} since there is no reading at the end of the block.";
            LoggingContext.get().info(getLogger(), message);
            return Optional.empty();
        }
        IntervalReadingRecord intervalReadingRecord = (IntervalReadingRecord) reading.get();
        if (!intervalReadingRecord.hasReadingQuality(ProtocolReadingQualities.POWERUP.getReadingQualityType())) {
            String message = "Failed estimation with {rule}: Block {block} since the reading at the end of the block does not have the power up flag.";
            LoggingContext.get().info(getLogger(), message);
            return Optional.empty();
        }
        BigDecimal value = intervalReadingRecord.getValue();
        if (value == null) {
            String message = "Failed estimation with {rule}: Block {block} since the reading at the end of the block does not have a bulk value.";
            LoggingContext.get().info(getLogger(), message);
            return Optional.empty();
        }
        return Optional.of(value);
    }

    private Optional<BigDecimal> getValueBefore(CimChannel cimChannel, Estimatable first) {
        Instant timestampBefore = cimChannel.getPreviousDateTime(first.getTimestamp());
        Optional<BaseReadingRecord> reading = cimChannel.getReading(timestampBefore);
        if (!reading.isPresent()) {
            String message = "Failed estimation with {rule}: Block {block} since there is no reading preceding the block.";
            LoggingContext.get().info(getLogger(), message);
            return Optional.empty();
        }
        IntervalReadingRecord intervalReadingRecord = (IntervalReadingRecord) reading.get();
        if (!intervalReadingRecord.hasReadingQuality(ProtocolReadingQualities.POWERDOWN.getReadingQualityType())) {
            String message = "Failed estimation with {rule}: Block {block} since the reading preceding the block does not have the power down flag.";
            LoggingContext.get().info(getLogger(), message);
            return Optional.empty();
        }
        if (intervalReadingRecord.getValue() == null) {
            String message = "Failed estimation with {rule}: Block {block} since the reading preceding the block does not have a bulk value.";
            LoggingContext.get().info(getLogger(), message);
            return Optional.empty();
        }
        return Optional.of(intervalReadingRecord.getValue());
    }

    @Override
    public void validateProperties(Map<String, Object> estimatorProperties) {
        ImmutableMap.Builder<String, Consumer<Map.Entry<String, Object>>> builder = ImmutableMap.builder();
        builder.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, property -> {
            TimeDuration value = (TimeDuration) property.getValue();
            if (value.getMilliSeconds() < 1) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_PERIOD_OF_ZERO_OR_NEGATIVE_LENGTH,
                        "properties." + MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS);
            }
        });

        ImmutableMap<String, Consumer<Map.Entry<String, Object>>> propertyValidations = builder.build();

        estimatorProperties.entrySet().forEach(property ->
                Optional.ofNullable(propertyValidations.get(property.getKey()))
                        .ifPresent(validator -> validator.accept(property)));
    }

}
