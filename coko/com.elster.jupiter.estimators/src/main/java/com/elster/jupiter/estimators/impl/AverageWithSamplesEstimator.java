package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettings;
import com.elster.jupiter.estimation.AdvanceReadingsSettingsFactory;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.estimators.MessageSeeds;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.AllRelativePeriod;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableList;
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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.either;
import static java.lang.Math.abs;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.RoundingMode.HALF_UP;

/**
 * Created by igh on 25/03/2015.
 */
public class AverageWithSamplesEstimator extends AbstractEstimator {

    class SamplesComparator implements Comparator<BaseReadingRecord> {

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

    static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = "averagewithsamples.maxNumberOfConsecutiveSuspects";
    static final String MIN_NUMBER_OF_SAMPLES = "averagewithsamples.minNumberOfSamples";
    static final String MAX_NUMBER_OF_SAMPLES = "averagewithsamples.maxNumberOfSamples";
    static final String ALLOW_NEGATIVE_VALUES = "averagewithsamples.allowNegativeValues";
    static final String RELATIVE_PERIOD = "averagewithsamples.relativePeriod";
    static final String ADVANCE_READINGS_SETTINGS = "averagewithsamples.advanceReadingsSettings";

    private static final Long MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = 10L;
    private static final Long MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE = 1L;
    private static final Long MAX_NUMBER_OF_SAMPLES_DEFAULT_VALUE = 10L;

    private final ValidationService validationService;
    private final MeteringService meteringService;

    private Long numberOfConsecutiveSuspects;
    private Long minNumberOfSamples;
    private Long maxNumberOfSamples;
    private boolean allowNegativeValues = false;
    private RelativePeriod relativePeriod;
    private AdvanceReadingsSettings advanceReadingsSettings;


    AverageWithSamplesEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService) {
        super(thesaurus, propertySpecService);
        this.validationService = validationService;
        this.meteringService = meteringService;
    }

    AverageWithSamplesEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.validationService = validationService;
        this.meteringService = meteringService;
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks) {
        List<EstimationBlock> remain = new ArrayList<EstimationBlock>();
        List<EstimationBlock> estimated = new ArrayList<EstimationBlock>();
        for (EstimationBlock block : estimationBlocks) {
            if (!isEstimatable(block)) {
                remain.add(block);
            } else {
                boolean succes = estimate(block);
                if (succes) {
                    estimated.add(block);
                } else {
                    remain.add(block);
                }
            }
        }
        return SimpleEstimationResult.of(remain, estimated);
    }

    @Override
    public String getDefaultFormat() {
        return "Average with samples";
    }

    public void validateProperties(List<EstimationRuleProperties> estimatorProperties) {
        Long maxSamples = null;
        Long minSamples = null;
        for (EstimationRuleProperties property : estimatorProperties) {
            if (property.getName().equals(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS)) {
                Long value = (Long) property.getValue();
                if (value.intValue() < 1) {
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS_SHOULD_BE_INTEGER_VALUE, "properties." + MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
                }
            } else if (property.getName().equals(ADVANCE_READINGS_SETTINGS)) {
                Object settings = property.getValue();
                if (settings instanceof ReadingTypeAdvanceReadingsSettings) {
                    ReadingType readingType = ((ReadingTypeAdvanceReadingsSettings) settings).getReadingType();
                    if (!readingType.isCumulative()) {
                        throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ADVANCE_READINGTYPE, "properties." + ADVANCE_READINGS_SETTINGS);
                    }
                }
            } else if (property.getName().equals(MAX_NUMBER_OF_SAMPLES)) {
                maxSamples = (Long) property.getValue();
            } else if (property.getName().equals(MIN_NUMBER_OF_SAMPLES)) {
                minSamples = (Long) property.getValue();
            }
        }
        if ((maxSamples != null) && (minSamples != null) && (maxSamples < minSamples)) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_SAMPLES, "properties." + MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
        }
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
            case MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS:
                return "Max number of consecutive suspects";
            case MAX_NUMBER_OF_SAMPLES:
                return "Maximum samples";
            case MIN_NUMBER_OF_SAMPLES:
                return "Minimum samples";
            case ALLOW_NEGATIVE_VALUES:
                return "Allow negative values";
            case RELATIVE_PERIOD:
                return "Relative period";
            case ADVANCE_READINGS_SETTINGS:
                return "Use advance readings";
            default:
                return "";
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
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();

        builder.add(getPropertySpecService().longPropertySpec(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, true, MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE));

        builder.add(new BasicPropertySpec(ALLOW_NEGATIVE_VALUES, true, new BooleanFactory()));

        PropertySpec spec = getPropertySpecService().newPropertySpecBuilder(new AdvanceReadingsSettingsFactory(meteringService))
                .markRequired()
                .name(ADVANCE_READINGS_SETTINGS)
                .setDefaultValue(NoneAdvanceReadingsSettings.INSTANCE)
                .finish();
        builder.add(spec);

        builder.add(getPropertySpecService().longPropertySpec(MIN_NUMBER_OF_SAMPLES, true, MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE));

        builder.add(getPropertySpecService().longPropertySpec(MAX_NUMBER_OF_SAMPLES, true, MAX_NUMBER_OF_SAMPLES_DEFAULT_VALUE));

        builder.add(getPropertySpecService().relativePeriodPropertySpec(RELATIVE_PERIOD, true, new AllRelativePeriod()));

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

    private boolean isEstimatable(EstimationBlock block) {
        if (block.estimatables().size() > numberOfConsecutiveSuspects.intValue()) {
            Logger.getAnonymousLogger().log(Level.WARNING, "block cannot be estimated: maxNumberOfConsecutiveSuspects should be max " + this.numberOfConsecutiveSuspects);
            return false;
        }
        if (block.getReadingType().isCumulative()) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Invalid readingtype '" + block.getReadingType().getMRID() + "': only delta readingtypes can be estimated");
            return false;
        }
        return true;
    }

    private boolean estimate(EstimationBlock estimationBlock) {
        if (advanceReadingsSettings instanceof BulkAdvanceReadingsSettings) {
            return estimateWithBulk(estimationBlock);
        } else if (advanceReadingsSettings instanceof ReadingTypeAdvanceReadingsSettings) {
            return estimateWithDeltas(estimationBlock);
        } else {
            return estimateWithoutAdvances(estimationBlock);
        }
    }

    private boolean estimateWithBulk(EstimationBlock estimationBlock) {
        boolean estimationWithoutAdvances = estimateWithoutAdvances(estimationBlock);
        if (!estimationWithoutAdvances) {
            return false;
        }
        if (estimationBlock.estimatables().size() == 1) {
            Logger.getAnonymousLogger().log(Level.WARNING, "block size should be more then 1 to use bulk advances");
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
                    Logger.getAnonymousLogger().log(Level.WARNING, "no bulk reading type found for delta reading type " + estimationBlock.getReadingType());
                    return Optional.empty();
                });
    }

    private Optional<BigDecimal> calculateConsumptionUsingBulk(Channel channel, ReadingType bulkReadingType, Instant startInterval, Instant endInterval) {
        Optional<BaseReadingRecord> startBulkReading = channel.getReading(channel.getPreviousDateTime(startInterval));
        Optional<BaseReadingRecord> endBulkReading = channel.getReading(endInterval);
        if ((!startBulkReading.isPresent()) || (!endBulkReading.isPresent())) {
            Logger.getAnonymousLogger().log(Level.WARNING, "no bulk reading available");
            return Optional.empty();
        }
        return calculateConsumption(bulkReadingType, startInterval, endInterval, startBulkReading.get(), endBulkReading.get());
    }

    private Optional<BigDecimal> calculateConsumption(ReadingType readingType, Instant startInterval, Instant endInterval, BaseReadingRecord startReading, BaseReadingRecord endReading) {
        Quantity startQty = startReading.getQuantity(readingType);
        if (startQty == null) {
            Logger.getAnonymousLogger().log(Level.WARNING, "no bulk reading found on " + startInterval);
            return Optional.empty();
        }
        Quantity endQty = endReading.getQuantity(readingType);
        if (endQty == null) {
            Logger.getAnonymousLogger().log(Level.WARNING, "no bulk reading found on " + endInterval);
            return Optional.empty();
        }
        return Optional.of(endQty.getValue().subtract(startQty.getValue()));
    }

    private void rescaleEstimation(EstimationBlock estimationBlock, BigDecimal totalConsumption) {
        BigDecimal factor = totalConsumption.divide(getTotalEstimatedConsumption(estimationBlock), 10, HALF_UP);
        estimationBlock.estimatables().stream()
                .forEach(estimatable -> estimatable.setEstimation(estimatable.getEstimation().multiply(factor).setScale(6, HALF_UP)));
    }

    private BigDecimal getTotalEstimatedConsumption(EstimationBlock estimationBlock) {
        return estimationBlock.estimatables().stream()
                .map(Estimatable::getEstimation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalConsumption(List<? extends BaseReadingRecord> readings, ReadingType readingType, Map<Instant, BigDecimal> percentages) {
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

    private boolean estimateWithDeltas(EstimationBlock estimationBlock) {
        boolean estimationWithoutAdvances = estimateWithoutAdvances(estimationBlock);
        if (!estimationWithoutAdvances) {
            return false;
        }
        List<? extends Estimatable> esimatables = estimationBlock.estimatables();
        Instant startInterval = esimatables.get(0).getTimestamp();
        Instant endInterval = esimatables.get(esimatables.size() - 1).getTimestamp();

        ReadingType registerReadingType =
                ((ReadingTypeAdvanceReadingsSettings) advanceReadingsSettings).getReadingType();
        List<? extends BaseReadingRecord> readingsBefore =
                estimationBlock.getChannel().getMeterActivation().getReadingsBefore(startInterval, registerReadingType, 1);
        List<? extends BaseReadingRecord> readingsAfter =
                estimationBlock.getChannel().getMeterActivation().getReadings(Range.atLeast(endInterval), registerReadingType);

        if (readingsBefore.isEmpty()) {
            Logger.getAnonymousLogger().log(Level.WARNING, "no reading found before start of the block");
            return false;
        }
        if (readingsAfter.isEmpty()) {
            Logger.getAnonymousLogger().log(Level.WARNING, "no reading found after end of the block");
            return false;
        }

        BaseReadingRecord readingBefore = readingsBefore.get(0);
        BaseReadingRecord readingAfter = readingsAfter.get(0);

        BigDecimal consumptionBetweenRegisterReadings =
                readingAfter.getQuantity(registerReadingType).getValue().add(readingBefore.getQuantity(registerReadingType).getValue().negate());

        Range<Instant> preInterval = Range.openClosed(readingBefore.getTimeStamp(), estimationBlock.getChannel().getPreviousDateTime(startInterval));
        List<Instant> instants = new ArrayList<>(estimationBlock.getChannel().toList(preInterval));
        Map<Instant, BigDecimal> percentages = percentages(instants, readingBefore.getTimeStamp(), estimationBlock.getChannel().getPreviousDateTime(startInterval), estimationBlock.getChannel().getZoneId(), estimationBlock.getChannel().getIntervalLength().get());
        BigDecimal consumptionBetweenPreviousRegisterReadingAndStartOfBlock = getTotalConsumption(
                estimationBlock.getChannel().getReadings(preInterval), estimationBlock.getReadingType(), percentages);

        Range<Instant> postInterval = Range.openClosed(endInterval, readingAfter.getTimeStamp());
        instants = new ArrayList<>(estimationBlock.getChannel().toList(postInterval));
        if (!lastOf(instants).equals(readingAfter.getTimeStamp())) {
            instants.add(estimationBlock.getChannel().getNextDateTime(lastOf(instants)));
        }
        percentages = percentages(instants, endInterval, readingAfter.getTimeStamp(), estimationBlock.getChannel().getZoneId(), estimationBlock.getChannel().getIntervalLength().get());
        BigDecimal consumptionBetweenEndOfBlockAndNextRegisterReading = getTotalConsumption(
                estimationBlock.getChannel().getReadings(Range.openClosed(endInterval, lastOf(instants))), estimationBlock.getReadingType(), percentages);

        BigDecimal totalConsumption = consumptionBetweenRegisterReadings.subtract(
                consumptionBetweenPreviousRegisterReadingAndStartOfBlock).subtract(
                consumptionBetweenEndOfBlockAndNextRegisterReading);
        rescaleEstimation(estimationBlock, totalConsumption);
        return true;
    }

    private Instant lastOf(List<Instant> instants) {
        return instants.get(instants.size() - 1);
    }

    private Map<Instant, BigDecimal> percentages(List<Instant> instants, Instant from, Instant to, ZoneId zoneId, TemporalAmount intervalLength) {
        Map<Instant, BigDecimal> percentages = instants.stream().collect(Collectors.toMap(Function.<Instant>identity(), instant -> BigDecimal.ONE));
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

    private boolean estimateWithoutAdvances(EstimationBlock estimationBlock) {
        for (Estimatable estimatable : estimationBlock.estimatables()) {
            boolean succes = estimateWithoutAdvances(estimationBlock, estimatable);
            if (!succes) {
                return false;
            }
        }
        return true;
    }

    private boolean estimateWithoutAdvances(EstimationBlock estimationBlock, Estimatable estimatable) {
        Instant timeToEstimate = estimatable.getTimestamp();
        Range<Instant> period = getPeriod(estimationBlock.getChannel(), timeToEstimate);
        List<BaseReadingRecord> samples = getSamples(estimationBlock, timeToEstimate, period);
        if (samples.size() < this.minNumberOfSamples.intValue()) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Not enough samples to estimate " + estimatable.getTimestamp());
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
                Logger.getAnonymousLogger().log(Level.FINE, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
            } else {
                Logger.getAnonymousLogger().log(Level.WARNING, estimatable.getTimestamp() + ": " + estimatable.getEstimation() + ", no negative values allowed");
                return false;
            }
        }
        return true;
    }

    private List<BaseReadingRecord> getSamples(EstimationBlock estimationBlock, Instant estimatableTime, Range<Instant> period) {
        List<BaseReadingRecord> records = estimationBlock.getChannel().getReadings(period);
        List<BaseReadingRecord> samples = new ArrayList<BaseReadingRecord>();
        for (BaseReadingRecord record : records) {
            if (isValidSample(estimationBlock, estimatableTime, record)) {
                samples.add(record);
            }
        }
        samples.sort(new SamplesComparator(estimatableTime, estimationBlock.getChannel().getZoneId()));
        return samples;
    }

    private boolean isValidSample(EstimationBlock estimationBlock, Instant estimatableTime, BaseReadingRecord record) {
        ZoneId zone = estimationBlock.getChannel().getZoneId();
        return sameTimeOfWeek(ZonedDateTime.ofInstant(record.getTimeStamp(), zone), ZonedDateTime.ofInstant(estimatableTime, zone))
                && record.getQuantity(estimationBlock.getReadingType()) != null
                && ofRequiredQuality(estimationBlock, record.getTimeStamp())
                && !estimatableTime.equals(record.getTimeStamp());
    }

    private boolean ofRequiredQuality(EstimationBlock estimationBlock, Instant timeStamp) {
        return estimationBlock.getChannel().findReadingQuality(timeStamp).stream()
                .filter(ReadingQualityRecord::isActual)
                .filter(readingQualityRecord -> readingQualityRecord.getReadingType().equals(estimationBlock.getReadingType()))
                .noneMatch(either(ReadingQualityRecord::isSuspect)
                        .or(ReadingQualityRecord::hasEstimatedCategory)
                        .or(ReadingQualityRecord::hasEditCategory)
                        .or(ReadingQualityRecord::isConfirmed));
    }

    private boolean sameTimeOfWeek(ZonedDateTime first, ZonedDateTime second) {
        return first.getDayOfWeek().equals(second.getDayOfWeek()) && first.getLong(ChronoField.NANO_OF_DAY) == second.getLong(ChronoField.NANO_OF_DAY);
    }

    private Range<Instant> getPeriod(Channel channel, Instant referenceTime) {
        if (relativePeriod != null && !(relativePeriod instanceof AllRelativePeriod)) {
            Range<ZonedDateTime> range = relativePeriod.getInterval(ZonedDateTime.ofInstant(referenceTime, channel.getZoneId()));
            return Ranges.map(range, ZonedDateTime::toInstant);
        } else {
            Instant start = channel.getMeterActivation().getStart();
            Optional<Instant> lastChecked = validationService.getLastChecked(channel);
            return lastChecked.map(end -> Range.openClosed(start, end)).orElseGet(() -> Range.atLeast(start));
        }
    }

    private BigDecimal average(List<BaseReadingRecord> values, ReadingType readingType) {
        int size = values.size();
        BigDecimal value;
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            value = ((BaseReadingRecord) values.get(i)).getQuantity(readingType).getValue();
            sum = sum.add(value);
        }
        return sum.divide(new BigDecimal(size), ROUND_HALF_UP);
    }
}

