package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PowerGapFill extends AbstractEstimator implements Estimator {

    public static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = "powergapfill.maxNumberOfConsecutiveSuspects";
    private static final long MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = 10L;
    private long maxNnumberOfConsecutiveSuspects;

    public PowerGapFill(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    public PowerGapFill(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public void init() {
        maxNnumberOfConsecutiveSuspects = getProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.class)
                .map(BigDecimal::longValue)
                .orElse(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE);
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks) {
        SimpleEstimationResult.EstimationResultBuilder builder = SimpleEstimationResult.builder();
        estimationBlocks.forEach(block -> {
            if (estimate(block)) {
                builder.addEstimated(block);
            } else {
                builder.addRemaining(block);
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
        return isNotTooLarge(estimationBlock) && estimationBlock.getReadingType().isRegular();
    }

    private boolean isNotTooLarge(EstimationBlock estimationBlock) {
        return estimationBlock.estimatables().size() <= maxNnumberOfConsecutiveSuspects;
    }

    private boolean estimate(Channel channel, ReadingType readingType, List<? extends Estimatable> estimatables) {
        return readingType.isCumulative() ? estimateBulk(channel, readingType, estimatables) : estimateDelta(channel, readingType, estimatables);
    }

    private boolean estimateBulk(Channel channel, ReadingType readingType, List<? extends Estimatable> estimatables) {
        Optional<CimChannel> cimChannel = channel.getCimChannel(readingType);

        Instant nextDateTime = channel.getNextDateTime(lastOf(estimatables).getTimestamp());
        boolean ok = cimChannel.get().getReading(nextDateTime)
                .map(IntervalReadingRecord.class::cast)
                .filter(intervalReadingRecord -> intervalReadingRecord.getProfileStatus().getFlags().contains(ProfileStatus.Flag.POWERUP))
                .map(baseReadingRecord -> doEstimateBulk(estimatables, baseReadingRecord))
                .orElse(false);
        if (!ok) {
            return false;
        }
        Instant previousDateTime = channel.getPreviousDateTime(estimatables.get(0).getTimestamp());
        return cimChannel.get().getReading(previousDateTime)
                .map(IntervalReadingRecord.class::cast)
                .filter(intervalReadingRecord -> intervalReadingRecord.getProfileStatus().getFlags().contains(ProfileStatus.Flag.POWERDOWN))
                .map(baseReadingRecord -> doEstimateBulk(estimatables, baseReadingRecord))
                .orElse(false);
    }

    private boolean doEstimateBulk(List<? extends Estimatable> estimatables, BaseReadingRecord baseReadingRecord) {
        BigDecimal value = baseReadingRecord.getValue();
        if (value == null) {
            return false;
        }
        estimatables.forEach(estimatable -> {
            estimatable.setEstimation(value);
        });
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
        return readingType.getBulkReadingType()
                .flatMap(channel::getCimChannel)
                .flatMap(bulkCimChannel -> getValueAt(bulkCimChannel, lastOf(estimatables))
                        .flatMap(valueAtLast -> getValueBefore(bulkCimChannel, estimatables.get(0))
                                .map(valueAtLast::subtract)));
    }

    private Optional<BigDecimal> getValueAt(CimChannel bulkCimChannel, Estimatable last) {
        return bulkCimChannel.getReading(last.getTimestamp())
                .map(IntervalReadingRecord.class::cast)
                .filter(intervalReadingRecord -> intervalReadingRecord.getProfileStatus().getFlags().contains(ProfileStatus.Flag.POWERUP))
                .flatMap(baseReadingRecord -> Optional.ofNullable(baseReadingRecord.getValue()));
    }

    private Optional<BigDecimal> getValueBefore(CimChannel cimChannel, Estimatable first) {
        Instant timestampBefore = cimChannel.getPreviousDateTime(first.getTimestamp());
        return cimChannel.getReading(timestampBefore)
                .map(IntervalReadingRecord.class::cast)
                .filter(intervalReadingRecord -> intervalReadingRecord.getProfileStatus().getFlags().contains(ProfileStatus.Flag.POWERDOWN))
                .flatMap(baseReadingRecord -> Optional.ofNullable(baseReadingRecord.getValue()));
    }

    @Override
    public String getDefaultFormat() {
        return "Power gap fill";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, true, BigDecimal.valueOf(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE)));
        return builder.build();
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
            case MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS:
                return "Maximum consecutive suspects";
            default:
                return "";
        }
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.emptyList();
    }

}
