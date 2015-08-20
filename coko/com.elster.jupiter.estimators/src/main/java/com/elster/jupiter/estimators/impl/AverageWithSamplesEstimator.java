package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
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
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.logging.LoggingContext;
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
    private final TimeService timeService;

    private Long numberOfConsecutiveSuspects;
    private Long minNumberOfSamples;
    private Long maxNumberOfSamples;
    private boolean allowNegativeValues = false;
    private RelativePeriod relativePeriod;
    private AdvanceReadingsSettings advanceReadingsSettings;


    AverageWithSamplesEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService) {
        super(thesaurus, propertySpecService);
        this.validationService = validationService;
        this.meteringService = meteringService;
        this.timeService = timeService;
    }

    AverageWithSamplesEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, MeteringService meteringService, TimeService timeService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.validationService = validationService;
        this.meteringService = meteringService;
        this.timeService = timeService;
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks) {
        List<EstimationBlock> remain = new ArrayList<EstimationBlock>();
        List<EstimationBlock> estimated = new ArrayList<EstimationBlock>();
        for (EstimationBlock block : estimationBlocks) {

            try (LoggingContext context = initLoggingContext(block)) {

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
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_CONSECUTIVE_SUSPECTS_SHOULD_BE_INTEGER_VALUE, MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
                }
            } else if (property.getName().equals(ADVANCE_READINGS_SETTINGS)) {
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
            } else if (property.getName().equals(MAX_NUMBER_OF_SAMPLES)) {
                maxSamples = (Long) property.getValue();
            } else if (property.getName().equals(MIN_NUMBER_OF_SAMPLES)) {
                minSamples = (Long) property.getValue();
            }
        }
        if ((maxSamples != null) && (minSamples != null) && (maxSamples < minSamples)) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_SAMPLES, MAX_NUMBER_OF_SAMPLES);
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

        builder.add(getPropertySpecService().relativePeriodPropertySpec(RELATIVE_PERIOD, true, timeService.getAllRelativePeriod()));

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

    private boolean isEstimatable(EstimationBlock block) {
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
        return calculateConsumption(bulkReadingType, startInterval, endInterval, startBulkReading.get(), endBulkReading.get());
    }

    private Optional<BigDecimal> calculateConsumption(ReadingType readingType, Instant startInterval, Instant endInterval, BaseReadingRecord startReading, BaseReadingRecord endReading) {
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

    private Optional<CimChannel> getCimChannel(EstimationBlock block, ReadingType readingType) {
        for (Channel channel : block.getChannel().getMeterActivation().getChannels()) {
            if (channel.getReadingTypes().contains(readingType)) {
                return channel.getCimChannel(readingType);
            }
        }
        return Optional.empty();
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
        if (!isValidReading(cimChannel, readingBefore)) {
            String message = "Failed estimation with {rule}: Block {block} since the prior advance reading is suspect, estimated or overflow";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }

        BaseReadingRecord readingAfter = readingsAfter.get(0);

        if (!isValidReading(cimChannel, readingAfter)) {
            String message = "Failed estimation with {rule}: Block {block} since the later advance reading is suspect, estimated or overflow";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }

        BigDecimal consumptionBetweenRegisterReadings =
                readingAfter.getQuantity(registerReadingType).getValue().add(readingBefore.getQuantity(registerReadingType).getValue().negate());

        CimChannel consumptionCimChannel = estimationBlock.getCimChannel();

        Range<Instant> preInterval = Range.openClosed(readingBefore.getTimeStamp(), estimationBlock.getChannel().getPreviousDateTime(startInterval));
        if (hasSuspects(consumptionCimChannel, preInterval)) {
            String message = "Failed estimation with {rule}: Block {block} since there are other suspects between the advance readings";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        List<Instant> instants = new ArrayList<>(estimationBlock.getChannel().toList(preInterval));
        Map<Instant, BigDecimal> percentages = percentages(instants, readingBefore.getTimeStamp(), estimationBlock.getChannel().getPreviousDateTime(startInterval), estimationBlock.getChannel().getZoneId(), estimationBlock.getChannel().getIntervalLength().get());
        BigDecimal consumptionBetweenPreviousRegisterReadingAndStartOfBlock = getTotalConsumption(
                estimationBlock.getChannel().getReadings(preInterval), estimationBlock.getReadingType(), percentages);

        Range<Instant> postInterval = Range.openClosed(endInterval, readingAfter.getTimeStamp());
        instants = new ArrayList<>(estimationBlock.getChannel().toList(postInterval));
        if (!lastOf(instants).equals(readingAfter.getTimeStamp())) {
            Instant addedInstant = estimationBlock.getChannel().getNextDateTime(lastOf(instants));
            instants.add(addedInstant);
            postInterval = Range.openClosed(endInterval, addedInstant);
        }
        if (hasSuspects(consumptionCimChannel, postInterval)) {
            String message = "Failed estimation with {rule}: Block {block} since there are other suspects between the advance readings";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        percentages = percentages(instants, endInterval, readingAfter.getTimeStamp(), estimationBlock.getChannel().getZoneId(), estimationBlock.getChannel().getIntervalLength().get());
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

    private boolean hasSuspects(CimChannel cimChannel, Range<Instant> preInterval) {
        return !cimChannel.findActualReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), preInterval).isEmpty();
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
        if (relativePeriod != null && timeService.getAllRelativePeriod().getId() != relativePeriod.getId()) {
            return relativePeriod.getOpenClosedInterval(ZonedDateTime.ofInstant(referenceTime, channel.getZoneId()));
        } else {
            Instant start = channel.getMeterActivation().getStart();
            Optional<Instant> lastChecked = validationService.getLastChecked(channel);
            return lastChecked.map(end -> Range.openClosed(start, end)).orElseGet(() -> Range.greaterThan(start));
        }
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

