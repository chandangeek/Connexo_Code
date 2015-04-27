package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettings;
import com.elster.jupiter.estimation.AdvanceReadingsSettingsFactory;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.estimators.MessageSeeds;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
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
        maxNumberOfConsecutiveSuspects = getProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.class)
                .map(BigDecimal::longValue)
                .orElse(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE);
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
            case MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS:
                return "Maximum consecutive suspects";
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
            if (estimate(block)) {
                builder.addEstimated(block);
            } else {
                builder.addRemaining(block);
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
        return block.estimatables().size() <= maxNumberOfConsecutiveSuspects && block.getReadingType().isRegular();
    }

    private boolean estimateUsingAdvances(EstimationBlock block, ReadingType readingType) {
        return block.getChannel().getMeterActivation().getChannels().stream()
                .filter(channel -> channel.getReadingTypes().contains(readingType))
                .map(channel -> channel.getCimChannel(readingType))
                .flatMap(Functions.asStream())
                .findFirst()
                .map(cimChannel -> estimateUsingAdvances(block, cimChannel))
                .orElse(false);
    }

    private boolean estimateUsingAdvances(EstimationBlock block, CimChannel advanceCimChannel) {
        Instant before = block.getChannel().getPreviousDateTime(block.estimatables().get(0).getTimestamp());
        return advanceCimChannel.getReadingsOnOrBefore(before, 1).stream()
                .findFirst()
                .flatMap(priorReading -> advanceCimChannel.getReadings(Range.atLeast(lastOf(block.estimatables()).getTimestamp())).stream()
                        .findFirst()
                        .map(laterReading -> estimateUsingAdvances(block, priorReading, laterReading, advanceCimChannel.getReadingType(), advanceCimChannel)))
                .orElse(false);
    }

    private boolean estimateUsingAdvances(EstimationBlock block, BaseReadingRecord priorReading, BaseReadingRecord laterReading, ReadingType advanceReadingType, CimChannel advanceCimChannel) {
        // get consumption spanning those readings
        // divide by number of estimatables
        // assignes to each estimatable
        if (!block.getReadingType().getUnit().equals(advanceReadingType.getUnit())) {
            return false;
        }
        if (!isValidReading(advanceCimChannel, priorReading) || !isValidReading(advanceCimChannel, laterReading)) {
            return false;
        }

        int metricMultiplierConversion = advanceReadingType.getMultiplier().getMultiplier() - block.getReadingType().getMultiplier().getMultiplier();
        BigDecimal conversionFactor = BigDecimal.valueOf(1, -metricMultiplierConversion).setScale(10, RoundingMode.HALF_UP);

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

    private boolean isValidReading(CimChannel advanceCimChannel, BaseReadingRecord readingToEvaluate) {
        return advanceCimChannel.findReadingQuality(readingToEvaluate.getTimeStamp()).stream().noneMatch(either(ReadingQualityRecord::isSuspect).or(ReadingQualityRecord::hasEstimatedCategory));
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
                .orElse(false);
    }

    private boolean estimate(EstimationBlock block, ReadingType bulkReadingType) {
        return block.getChannel().getCimChannel(bulkReadingType)
                .map(cimChannel -> estimate(block, cimChannel))
                .orElse(false);
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
                .orElse(false);
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
        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, true, BigDecimal.valueOf(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE)));
        builder.add(getPropertySpecService().newPropertySpecBuilder(new AdvanceReadingsSettingsFactory(meteringService))
                .markRequired()
                .name(ADVANCE_READINGS_SETTINGS)
                .setDefaultValue(NoneAdvanceReadingsSettings.INSTANCE)
                .finish());
        return builder.build();
    }

    private Optional<BigDecimal> getValueAt(CimChannel bulkCimChannel, Estimatable last) {
        return bulkCimChannel.getReading(last.getTimestamp())
                .map(IntervalReadingRecord.class::cast)
                .flatMap(baseReadingRecord -> Optional.ofNullable(baseReadingRecord.getValue()));
    }

    private Optional<BigDecimal> getValueBefore(CimChannel cimChannel, Estimatable first) {
        Instant timestampBefore = cimChannel.getPreviousDateTime(first.getTimestamp());
        return cimChannel.getReading(timestampBefore)
                .map(IntervalReadingRecord.class::cast)
                .flatMap(baseReadingRecord -> Optional.ofNullable(baseReadingRecord.getValue()));
    }

    @Override
    public void validateProperties(List<EstimationRuleProperties> estimatorProperties) {
        ImmutableMap.Builder<String, Consumer<EstimationRuleProperties>> builder = ImmutableMap.builder();
        builder.put(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, property -> {
            BigDecimal value = (BigDecimal) property.getValue();
            if (hasFractionalPart(value)) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS_SHOULD_BE_INTEGER_VALUE, "properties." + MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
            }
            if (value.intValue() < 1) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS, "properties." + MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
            }
        });
        builder.put(ADVANCE_READINGS_SETTINGS, property -> {
            if (NoneAdvanceReadingsSettings.INSTANCE.equals(property.getValue())) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ADVANCE_READINGTYPE_NONE_NOT_ALLOWED, "properties." + ADVANCE_READINGS_SETTINGS);
            }
            if (BulkAdvanceReadingsSettings.INSTANCE.equals(property.getValue())) {
                return;
            }
            ReadingType readingType = ((ReadingTypeAdvanceReadingsSettings) property.getValue()).getReadingType();
            if (!readingType.isCumulative()) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ADVANCE_READINGTYPE, "properties." + ADVANCE_READINGS_SETTINGS);
            }
        });

        ImmutableMap<String, Consumer<EstimationRuleProperties>> propertyValidations = builder.build();

        estimatorProperties.forEach(property -> {
            Optional.ofNullable(propertyValidations.get(property.getName()))
                    .ifPresent(validator -> validator.accept(property));
        });
    }

    private boolean hasFractionalPart(BigDecimal value) {
        return value.setScale(0, RoundingMode.DOWN).compareTo(value) != 0;
    }
}
