package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
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
import com.elster.jupiter.estimators.MessageSeeds;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.streams.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.either;

public class EqualDistribution extends AbstractEstimator implements Estimator {
    public static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = "equaldistribution.maxNumberOfConsecutiveSuspects";
    public static final String ADVANCE_READINGS_SETTINGS = "equaldistribution.advanceReadingsSettings";
    private static final long MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = 10L;

    private final MeteringService meteringService;
    private AdvanceReadingsSettings advanceReadingsSettings;
    private long maxNumberOfConsecutiveSuspects;

    public EqualDistribution(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService) {
        super(thesaurus, propertySpecService);
        this.meteringService = meteringService;
    }

    public EqualDistribution(Thesaurus thesaurus, PropertySpecService propertySpecService, MeteringService meteringService, Map<String, Object> properties) {
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
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
            case MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS:
                return "Max number of consecutive suspects";
            case ADVANCE_READINGS_SETTINGS:
                return "Use advance readings";
            default:
                return "";
        }
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.singletonList(ADVANCE_READINGS_SETTINGS);
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks) {
        SimpleEstimationResult.EstimationResultBuilder builder = SimpleEstimationResult.builder();
        estimationBlocks.forEach(block -> {
            try (LoggingContext context = initLoggingContext(block)) {
                if (estimate(block)) {
                    builder.addEstimated(block);
                } else {
                    builder.addRemaining(block);
                }
            }
        });
        return builder.build();
    }

    private boolean estimate(EstimationBlock block) {
        if (!canEstimate(block)) {
            return false;
        }
        if (BulkAdvanceReadingsSettings.INSTANCE.equals(advanceReadingsSettings)) {
            return estimateUsingBulk(block);
        } else if (advanceReadingsSettings instanceof ReadingTypeAdvanceReadingsSettings) {
            return estimateUsingAdvances(block, ((ReadingTypeAdvanceReadingsSettings) advanceReadingsSettings).getReadingType());
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


    private boolean estimateUsingAdvances(EstimationBlock block, ReadingType readingType) {
        Optional<CimChannel> advanceCimChannel = getAdvanceCimChannel(block, readingType);
        if (!advanceCimChannel.isPresent()) {
            String message = "Failed estimation with {rule}: Block {block} since the meter does not have readings for the reading type {0}";
            LoggingContext.get().info(getLogger(), message, readingType.getMRID());
            return false;
        }
        return advanceCimChannel
                .map(cimChannel -> estimateUsingAdvances(block, cimChannel))
                .orElse(false);
    }

    private Optional<CimChannel> getAdvanceCimChannel(EstimationBlock block, ReadingType readingType) {
        return block.getChannel().getMeterActivation().getChannels().stream()
                .filter(channel -> channel.getReadingTypes().contains(readingType))
                .map(channel -> channel.getCimChannel(readingType))
                .flatMap(Functions.asStream())
                .findFirst();
    }

    private boolean estimateUsingAdvances(EstimationBlock block, CimChannel advanceCimChannel) {
        Instant before = block.getChannel().getPreviousDateTime(block.estimatables().get(0).getTimestamp());
        return advanceCimChannel.getReadingsOnOrBefore(before, 1).stream()
                .findFirst()
                .flatMap(priorReading -> advanceCimChannel.getReadings(Range.atLeast(lastOf(block.estimatables()).getTimestamp())).stream()
                        .findFirst()
                        .map(laterReading -> estimateUsingAdvances(block, priorReading, laterReading, advanceCimChannel.getReadingType(), advanceCimChannel)))
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since there was no prior and later advance reading.";
                    LoggingContext.get().info(getLogger(), message);
                    return false;
                });
    }

    private boolean estimateUsingAdvances(EstimationBlock block, BaseReadingRecord priorReading, BaseReadingRecord laterReading, ReadingType advanceReadingType, CimChannel advanceCimChannel) {
        if (!canEstimate(block, priorReading, laterReading, advanceReadingType, advanceCimChannel)) {
            return false;
        }

        BigDecimal conversionFactor = calculateConversionFactor(block, advanceReadingType);

        BigDecimal advance = laterReading.getValue().subtract(priorReading.getValue()).setScale(10, RoundingMode.HALF_UP).multiply(conversionFactor);

        return block.getChannel().getCimChannel(block.getReadingType())
                .flatMap(cimChannel -> calculateConsumption(block, priorReading, laterReading, cimChannel)
                        .map(advance::subtract)
                        .map(toDistribute -> toDistribute.divide(BigDecimal.valueOf(block.estimatables().size()), 6, RoundingMode.HALF_UP))
                        .map(perInterval -> {
                            block.estimatables().forEach(estimatable -> estimatable.setEstimation(perInterval));
                            return true;
                        })).orElse(false);
    }

    private BigDecimal calculateConversionFactor(EstimationBlock block, ReadingType advanceReadingType) {
        int metricMultiplierConversion = advanceReadingType.getMultiplier().getMultiplier() - block.getReadingType().getMultiplier().getMultiplier();
        return BigDecimal.valueOf(1, -metricMultiplierConversion).setScale(10, RoundingMode.HALF_UP);
    }

    private boolean canEstimate(EstimationBlock block, BaseReadingRecord priorReading, BaseReadingRecord laterReading, ReadingType advanceReadingType, CimChannel advanceCimChannel) {
        if (!block.getReadingType().getUnit().equals(advanceReadingType.getUnit())) {
            return false;
        }
        if (!isValidReading(advanceCimChannel, priorReading)) {
            String message = "Failed estimation with {rule}: Block {block} since the prior advance reading is suspect, estimated or overflow";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        if (!isValidReading(advanceCimChannel, laterReading)) {
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

    private boolean isValidReading(CimChannel advanceCimChannel, BaseReadingRecord readingToEvaluate) {
        return advanceCimChannel.findReadingQuality(readingToEvaluate.getTimeStamp()).stream()
                .filter(ReadingQualityRecord::isActual)
                .noneMatch(
                        either(ReadingQualityRecord::isSuspect)
                                .or(ReadingQualityRecord::hasEstimatedCategory)
                                .or(readingQualityRecord -> readingQualityRecord.getType().qualityIndex().filter(QualityCodeIndex.OVERFLOWCONDITIONDETECTED::equals).isPresent())
                );
    }

    private Optional<BigDecimal> calculateConsumption(EstimationBlock block, BaseReadingRecord priorReading, BaseReadingRecord laterReading, CimChannel cimChannel) {
        Instant from = priorReading.getTimeStamp();
        Instant to = laterReading.getTimeStamp();
        List<Instant> instants = new ArrayList<>(cimChannel.toList(Range.closed(from, to)));
        if (!lastOf(instants).equals(to)) {
            instants.add(cimChannel.getNextDateTime(lastOf(instants)));
        }
        Map<Instant, List<ReadingQualityRecord>> invalidsByTimestamp =
                cimChannel.findActualReadingQuality(Range.closed(instants.get(0), lastOf(instants))).stream()
                        .filter(either(ReadingQualityRecord::isSuspect).or(ReadingQualityRecord::hasEstimatedCategory))
                        .collect(Collectors.groupingBy(ReadingQualityRecord::getReadingTimestamp));

        TemporalAmount intervalLength = cimChannel.getIntervalLength().get();

        Map<Instant, BigDecimal> percentages = instants.stream().collect(Collectors.toMap(Function.<Instant>identity(), instant -> BigDecimal.ONE));
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
            boolean suspects = neededConsumption.stream().anyMatch(instant -> Optional.ofNullable(invalidsByTimestamp.get(instant)).orElse(Collections.emptyList()).stream().anyMatch(ReadingQualityRecord::isSuspect));
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

    private boolean estimateUsingBulk(EstimationBlock block) {
        return block.getReadingType().getBulkReadingType()
                .map(bulkReadingType -> estimate(block, bulkReadingType))
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since the reading type {readingType} has no bulk reading type.";
                    LoggingContext.get().info(getLogger(), message);
                    return false;
                });
    }

    private boolean estimate(EstimationBlock block, ReadingType bulkReadingType) {
        return block.getChannel().getCimChannel(bulkReadingType)
                .map(cimChannel -> estimate(block, cimChannel))
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since the channel has no bulk reading type.";
                    LoggingContext.get().info(getLogger(), message);
                    return false;
                });
    }

    private boolean estimate(EstimationBlock block, CimChannel bulkCimChannel) {
        return getValueAt(bulkCimChannel, lastOf(block.estimatables()))
                .flatMap(valueAt -> getValueBefore(bulkCimChannel, block.estimatables().get(0))
                        .map(valueAt::subtract))
                .map(difference -> difference.divide(BigDecimal.valueOf(block.estimatables().size()), 6, BigDecimal.ROUND_HALF_UP))
                .map(equalValue -> {
                    block.estimatables().forEach(estimatable -> estimatable.setEstimation(equalValue));
                    return true;
                })
                .orElseGet(() -> {
                    String message = "Failed estimation with {rule}: Block {block} since the surrounding bulk readings are not available or have the overflow flag.";
                    LoggingContext.get().info(getLogger(), message);
                    return false;
                });
    }

    private <T> T lastOf(List<? extends T> elements) {
        return elements.get(elements.size() - 1);
    }

    @Override
    public String getDefaultFormat() {
        return "Equal distribution";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().longPropertySpec(
                MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, true, MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE));
        builder.add(getPropertySpecService().newPropertySpecBuilder(new AdvanceReadingsSettingsWithoutNoneFactory(meteringService))
                .markRequired()
                .name(ADVANCE_READINGS_SETTINGS)
                .setDefaultValue(BulkAdvanceReadingsSettings.INSTANCE)
                .finish());
        return builder.build();
    }

    private Optional<BigDecimal> getValueAt(CimChannel bulkCimChannel, Estimatable last) {
        return bulkCimChannel.getReading(last.getTimestamp())
                .map(IntervalReadingRecord.class::cast)
                .filter(intervalReadingRecord -> !intervalReadingRecord.getProfileStatus().get(ProfileStatus.Flag.OVERFLOW))
                .filter(intervalReadingRecord -> bulkCimChannel.findReadingQuality(last.getTimestamp()).stream()
                        .filter(ReadingQualityRecord::isActual)
                        .noneMatch(readingQualityRecord -> readingQualityRecord.getType().qualityIndex().filter(QualityCodeIndex.OVERFLOWCONDITIONDETECTED::equals).isPresent()))
                .flatMap(baseReadingRecord -> Optional.ofNullable(baseReadingRecord.getValue()));
    }

    private Optional<BigDecimal> getValueBefore(CimChannel bulkCimChannel, Estimatable first) {
        Instant timestampBefore = bulkCimChannel.getPreviousDateTime(first.getTimestamp());
        return bulkCimChannel.getReading(timestampBefore)
                .map(IntervalReadingRecord.class::cast)
                .filter(intervalReadingRecord -> !intervalReadingRecord.getProfileStatus().get(ProfileStatus.Flag.OVERFLOW))
                .filter(intervalReadingRecord -> bulkCimChannel.findReadingQuality(first.getTimestamp()).stream()
                        .filter(ReadingQualityRecord::isActual)
                        .noneMatch(readingQualityRecord -> readingQualityRecord.getType().qualityIndex().filter(QualityCodeIndex.OVERFLOWCONDITIONDETECTED::equals).isPresent()))
                .flatMap(baseReadingRecord -> Optional.ofNullable(baseReadingRecord.getValue()));
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

        estimatorProperties.entrySet().forEach(property -> {
            Optional.ofNullable(propertyValidations.get(property.getKey()))
                    .ifPresent(validator -> validator.accept(property));
        });
    }
}
