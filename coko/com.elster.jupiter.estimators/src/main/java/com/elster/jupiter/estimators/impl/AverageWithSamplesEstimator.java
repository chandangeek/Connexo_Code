package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettings;
import com.elster.jupiter.estimation.AdvanceReadingsSettingsFactory;
import com.elster.jupiter.estimation.AdvanceReadingsSettingsWithoutNoneFactory;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.AllRelativePeriod;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by igh on 25/03/2015.
 */
public class AverageWithSamplesEstimator extends AbstractEstimator {

    private final ValidationService validationService;
    private final MeteringService meteringService = null;

    static final String MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS = "maxNumberOfConsecutiveSuspects";
    static final BigDecimal MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE = new BigDecimal(10);

    static final String MIN_NUMBER_OF_SAMPLES = "minNumberOfSamples";
    static final BigDecimal MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE = new BigDecimal(1);
    static final String MAX_NUMBER_OF_SAMPLES = "maxNumberOfSamples";
    static final BigDecimal MAX_NUMBER_OF_SAMPLES_DEFAULT_VALUE = new BigDecimal(10);

    static final String ALLOW_NEGATIVE_VALUES = "allowNegativeValues";
    static final String RELATIVE_PERIOD = "relativePeriod";
    static final String ADVANCE_READINGS_SETTINGS = "advanceReadingsSettings";

    private BigDecimal numberOfConsecutiveSuspects;
    private BigDecimal minNumberOfSamples;
    private BigDecimal maxNumberOfSamples;
    private boolean allowNegativeValues = false;
    private RelativePeriod relativePeriod;
    private AdvanceReadingsSettings advanceReadingsSettings;


    AverageWithSamplesEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService) {
        super(thesaurus, propertySpecService);
        this.validationService = validationService;
    }

    AverageWithSamplesEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, ValidationService validationService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.validationService = validationService;
    }

    @Override
    public void init() {
        numberOfConsecutiveSuspects = (BigDecimal) properties.get(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS);
        if (numberOfConsecutiveSuspects == null) {
            this.numberOfConsecutiveSuspects = MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE;
        }

        maxNumberOfSamples = (BigDecimal) properties.get(MIN_NUMBER_OF_SAMPLES);
        if (maxNumberOfSamples == null) {
            this.maxNumberOfSamples = MAX_NUMBER_OF_SAMPLES_DEFAULT_VALUE;
        }

        minNumberOfSamples = (BigDecimal) properties.get(MAX_NUMBER_OF_SAMPLES);
        if (minNumberOfSamples == null) {
            this.minNumberOfSamples = MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE;
        }

        Object allowNegativeValuesPropertyValue = properties.get(ALLOW_NEGATIVE_VALUES);
        if (allowNegativeValuesPropertyValue != null) {
            this.allowNegativeValues = ((Boolean) allowNegativeValuesPropertyValue).booleanValue();
        }

        relativePeriod = (RelativePeriod) properties.get(RELATIVE_PERIOD);

        advanceReadingsSettings = (AdvanceReadingsSettings) properties.get(ADVANCE_READINGS_SETTINGS);
        if (advanceReadingsSettings == null) {
            advanceReadingsSettings = new NoneAdvanceReadingsSettings();
        }
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        return null;
    }

    @Override
    public List<String> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public EstimationResult estimate(List<EstimationBlock> estimationBlocks) {
        List<EstimationBlock> remain = new ArrayList<EstimationBlock>();
        List<EstimationBlock> estimated = new ArrayList<EstimationBlock>();
        for (EstimationBlock block : estimationBlocks) {
            boolean succes = estimate(block);
            if (succes) {
                estimated.add(block);
            } else {
                remain.add(block);
            }
        }
        return SimpleEstimationResult.of(remain, estimated);
    }

    private boolean estimate(EstimationBlock estimationBlock) {
        if (advanceReadingsSettings.equals(new BulkAdvanceReadingsSettings())) {
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
        List<? extends Estimatable> esimatables = estimationBlock.estimatables();
        if (esimatables.size() == 1) {
            Logger.getAnonymousLogger().log(Level.WARNING, "block size should be more then 1 to use bulk advances");
            return false;
        }
        Instant startInterval = esimatables.get(0).getTimestamp();
        Instant endInterval = esimatables.get(esimatables.size() - 1).getTimestamp();
        Optional<BaseReadingRecord> startBulkReading = estimationBlock.getChannel().getReading(startInterval);
        Optional<BaseReadingRecord> endBulkReading = estimationBlock.getChannel().getReading(endInterval);
        if ((!startBulkReading.isPresent()) || (!endBulkReading.isPresent())) {
            Logger.getAnonymousLogger().log(Level.WARNING, "no bulk reading available");
            return false;
        }
        Optional<ReadingType> bulkReadingType = this.getBulkReadingType(estimationBlock.getReadingType());
        if (!bulkReadingType.isPresent()) {
            Logger.getAnonymousLogger().log(Level.WARNING, "no bulk reading type found for delta reading type " + estimationBlock.getReadingType());
            return false;
        }
        Quantity startQty = startBulkReading.get().getQuantity(bulkReadingType.get());
        if (startQty == null) {
            Logger.getAnonymousLogger().log(Level.WARNING, "no bulk reading found on " + startInterval);
            return false;
        }
        Quantity endQty = endBulkReading.get().getQuantity(bulkReadingType.get());
        if (endQty == null) {
            Logger.getAnonymousLogger().log(Level.WARNING, "no bulk reading found on " + endInterval);
            return false;
        }
        BigDecimal totalConsumption = endQty.getValue().add(startQty.getValue().negate());
        rescaleEstimation(estimationBlock, totalConsumption);
        return true;
    }

    private void rescaleEstimation(EstimationBlock estimationBlock, BigDecimal totalConsumption) {
        BigDecimal totalEstimation = getTotalEstimatedConsumption(estimationBlock);
        BigDecimal factor = totalConsumption.divide(totalEstimation, 10, BigDecimal.ROUND_HALF_UP);
        for (Estimatable estimatable : estimationBlock.estimatables()) {
            estimatable.setEstimation(estimatable.getEstimation().multiply(factor));
        }
    }

    private BigDecimal getTotalEstimatedConsumption(EstimationBlock estimationBlock) {
        BigDecimal total = BigDecimal.ZERO;
        for (Estimatable estimatable : estimationBlock.estimatables()) {
            total.add(estimatable.getEstimation());
        }
        return total;
    }

    private BigDecimal getTotalConsumption(List<? extends BaseReadingRecord> readings, ReadingType readingType) {
        BigDecimal total = BigDecimal.ZERO;
        for (BaseReadingRecord reading : readings) {
            Quantity qty = reading.getQuantity(readingType);
            if ((qty != null) && (qty.getValue() != null)) {
                total.add(qty.getValue());
            }
        }
        return total;
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
                ((ReadingTypeAdvanceReadingsSettings) advanceReadingsSettings).getReadingType() ;
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

        BigDecimal consumptionBetweenPreviousRegisterReadingAndStartOfBlock = getTotalConsumption(
                estimationBlock.getChannel().getReadings(Range.open(readingBefore.getTimeStamp(), startInterval)), estimationBlock.getReadingType());

        BigDecimal consumptionBetweenEndOfBlockAndNextRegisterReading = getTotalConsumption(
                estimationBlock.getChannel().getReadings(Range.open(endInterval, readingAfter.getTimeStamp())), estimationBlock.getReadingType());

        BigDecimal totalConsumption = consumptionBetweenRegisterReadings.add(
                consumptionBetweenPreviousRegisterReadingAndStartOfBlock.negate()).add(
                consumptionBetweenEndOfBlockAndNextRegisterReading.negate());
        rescaleEstimation(estimationBlock, totalConsumption);
        return true;
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

    private boolean estimateWithBulk(EstimationBlock estimationBlock, Estimatable estimatable) {
        estimatable.setEstimation(null);
        Logger.getAnonymousLogger().log(Level.FINE, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
        return true;
    }

    private boolean estimateWithDeltas(EstimationBlock estimationBlock, Estimatable estimatable) {
        estimatable.setEstimation(null);
        Logger.getAnonymousLogger().log(Level.FINE, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
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
            if (samples.size() >= this.maxNumberOfSamples.intValue()) {
                samples = samples.subList(0, this.maxNumberOfSamples.intValue());
            }
            estimatable.setEstimation(avg(samples, estimationBlock.getReadingType()));
            Logger.getAnonymousLogger().log(Level.FINE, "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
        }
        return true;
    }

    private List<BaseReadingRecord> getSamples(EstimationBlock estimationBlock, Instant estimatableTime, Range<Instant> period) {
        Channel channel = estimationBlock.getChannel();
        List<BaseReadingRecord> records = channel.getReadings(period);
        List<BaseReadingRecord> samples = new ArrayList<BaseReadingRecord>();
        for (BaseReadingRecord record : records) {
            if ((sameTime(channel.getZoneId(), estimatableTime, record.getTimeStamp())) &&
                (record.getQuantity(estimationBlock.getReadingType()) != null)) {
                samples.add(record);
            }
        }
        samples.sort(new SamplesComparator(estimatableTime));
        return samples;
    }

    private boolean sameTime(ZoneId zoneId, Instant a, Instant b) {
        LocalDateTime ldtA = LocalDateTime.ofInstant(a, zoneId);
        LocalDateTime ldtB = LocalDateTime.ofInstant(b, zoneId);
        return ((ldtA.getHour() == ldtB.getHour()) &&
                (ldtA.getMinute() == ldtB.getMinute()) &&
                (ldtA.getSecond() == ldtB.getSecond()));
    }

    private Range<Instant> getPeriod(Channel channel, Instant referenceTime) {
        if (relativePeriod != null) {
            Range<ZonedDateTime> range = relativePeriod.getInterval(ZonedDateTime.ofInstant(referenceTime, channel.getZoneId()));
            Instant start = range.lowerEndpoint().toInstant();
            Instant end = range.upperEndpoint().toInstant();
            return Range.open(start, end);
        } else {
            Instant start = channel.getMeterActivation().getStart();
            Optional<Instant> end = validationService.getLastChecked(channel);
            return (end.isPresent()) ? Range.open(start, end.get()) : Range.atLeast(start);
        }
    }

    @Override
    public String getDefaultFormat() {
        return "Average with samples";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, false, MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS_DEFAULT_VALUE));

        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                MIN_NUMBER_OF_SAMPLES, false, MIN_NUMBER_OF_SAMPLES_DEFAULT_VALUE));

        builder.add(getPropertySpecService().bigDecimalPropertySpec(
                MAX_NUMBER_OF_SAMPLES, false, MAX_NUMBER_OF_SAMPLES_DEFAULT_VALUE));

        builder.add(new BasicPropertySpec(ALLOW_NEGATIVE_VALUES, false, new BooleanFactory()));

        builder.add(getPropertySpecService().relativePeriodPropertySpec(RELATIVE_PERIOD, true, new AllRelativePeriod()));

        PropertySpecBuilder propertySpecBuilder = getPropertySpecService().newPropertySpecBuilder(new AdvanceReadingsSettingsFactory(meteringService));
        propertySpecBuilder.markRequired();
        PropertySpec spec =
                propertySpecBuilder.name(ADVANCE_READINGS_SETTINGS).setDefaultValue(new NoneAdvanceReadingsSettings()).finish();
        builder.add(spec);


        return builder.build();
    }

    private Optional<ReadingType> getBulkReadingType(ReadingType deltaReadingType) {
        return deltaReadingType.getBulkReadingType();
    }

    private BigDecimal avg(List<BaseReadingRecord> values, ReadingType readingType) {
        int size = values.size();
        BigDecimal value;
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            value = ((BaseReadingRecord) values.get(i)).getQuantity(readingType).getValue();
            sum = sum.add(value);
        }
        return sum.divide(new BigDecimal(size), BigDecimal.ROUND_HALF_UP);
    }

    class SamplesComparator implements Comparator<BaseReadingRecord> {

        private Instant estimatableTime;

        SamplesComparator(Instant estimatableTime) {
            this.estimatableTime = estimatableTime;
        }

        @Override
        public int compare(BaseReadingRecord a, BaseReadingRecord b) {
            Instant timeA = a.getTimeStamp();
            Instant timeB = b.getTimeStamp();

            long dif1 = Math.abs(timeA.getEpochSecond() - estimatableTime.getEpochSecond());
            long dif2 = Math.abs(timeB.getEpochSecond() - estimatableTime.getEpochSecond());

            return new Long(dif1).compareTo(new Long(dif2));
        }
    }
}

