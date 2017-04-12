package com.elster.jupiter.estimators.impl;


import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.CalendarWithEventSettings;
import com.elster.jupiter.estimation.CalendarWithEventSettingsFactory;
import com.elster.jupiter.estimation.DiscardDayWithEventSettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.NoneCalendarWithEventSettings;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.abs;


public class NearestAverageValueDayEstimator extends AbstractEstimator implements Estimator {

    public static final String DISCARD_SPECIFIC_DAY = TranslationKeys.DISCARD_SPECIFIC_DAY.getKey();
    public static final String NUMBER_OF_SAMPLES = TranslationKeys.NUMBER_OF_SAMPLES.getKey();
    public static final Long NUMBER_OF_SAMPLES_DEFAULT_VALUE = 3L;
    public static final String MAXIMUM_NUMBER_OF_WEEKS = TranslationKeys.MAXIMUM_NUMBER_OF_WEEKS.getKey();
    public static final Long MAXIMUM_NUMBER_OF_WEEKS_DEFAULT_VALUE = 4L;
    public static final String CALENDAR = TranslationKeys.CALENDAR.getKey();
    public static final String EVENT_CODE = TranslationKeys.EVENT_CODE.getKey();
    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);

    private final ValidationService validationService;
    private final MeteringService meteringService;
    private final TimeService timeService;
    private final CalendarService calendarService;

    private Long numberOfSamples;
    private Long maxNumberOfWeeks;
    private CalendarWithEventSettings discardSpecificDay ;
    private Calendar calendar;
    private long calendarEventCode;
//    private Event
//Translation Keys
    public enum TranslationKeys implements TranslationKey {
        ESTIMATOR_NAME(NearestAverageValueDayEstimator.class.getName(), "Nearest average value (Day)"),
        DISCARD_SPECIFIC_DAY("nearestaveragevalueday.discardSpecificDay","Discard specific day"),
        DISCARD_SPECIFIC_DAY_DESCRIPTION("nearestaveragevalueday.discardSpecificDay.description",
                "Estimated and sample days can be discarded if they contain a chosen event code"),
        NUMBER_OF_SAMPLES("nearestaveragevalueday.numberOfSamples", "Number of samples"),
        NUMBER_OF_SAMPLES_DESCRIPTION("nearestaveragevalueday.numberOfSamples.description",
                "Number of samples required to complete estimation for a value."),
        MAXIMUM_NUMBER_OF_WEEKS("nearestaveragevalueday.maximumNumberOfWeeks","Maximum number of weeks"),
        MAXIMUM_NUMBER_OF_WEEKS_DESCRIPTION("nearestaveragevalueday.maximumNumberOfWeeks.description",
                "Limit for a number of weeks where the samples can be searched"),
        CALENDAR("nearestaveragevalueday.calendar", "ToU calendar"),
        EVENT_CODE("nearestaveragevalueday.eventCode","Event code");

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
//SamplesComparator
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


    NearestAverageValueDayEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService,
    ValidationService validationService, MeteringService meteringService,
    TimeService timeService, CalendarService calendarService) {
        super(thesaurus, propertySpecService);
        this.validationService = validationService;
        this.meteringService = meteringService;
        this.timeService = timeService;
        this.calendarService = calendarService;
    }

    NearestAverageValueDayEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService,
                                ValidationService validationService, MeteringService meteringService,
                                TimeService timeService, CalendarService calendarService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.validationService = validationService;
        this.meteringService = meteringService;
        this.timeService = timeService;
        this.calendarService = calendarService;
    }

    //Rewrite
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
        return AverageWithSamplesEstimator.TranslationKeys.ESTIMATOR_NAME.getDefaultFormat();
    }

    @Override
    public void validateProperties(Map<String, Object> estimatorProperties) {

        if (estimatorProperties == null) {
            throw new IllegalArgumentException("Estimator properties should be provided");
        }
        for (Map.Entry<String, Object> property : estimatorProperties.entrySet()) {
            if (property.getKey().equals(NUMBER_OF_SAMPLES)) {
                Long value = (Long) property.getValue();
                if (value.intValue() < 1) {
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_SAMPLES_AVERAGE,
                            NUMBER_OF_SAMPLES);
                }
            } else if (property.getKey().equals(MAXIMUM_NUMBER_OF_WEEKS)) {
                Long value = (Long) property.getValue();
                if (value.intValue() < 1) {
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_MAXIMUM_NUMBER_OF_WEEKS,
                            MAXIMUM_NUMBER_OF_WEEKS);
                }
            }

        }
    }


    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(
                NUMBER_OF_SAMPLES,
                MAXIMUM_NUMBER_OF_WEEKS,
                DISCARD_SPECIFIC_DAY

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
                .named(TranslationKeys.NUMBER_OF_SAMPLES)
                .describedAs(TranslationKeys.NUMBER_OF_SAMPLES_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(NUMBER_OF_SAMPLES_DEFAULT_VALUE)
                .finish());


        builder.add(
                getPropertySpecService()
                        .specForValuesOf(new CalendarWithEventSettingsFactory(calendarService))
                        .named(TranslationKeys.DISCARD_SPECIFIC_DAY)
                        .describedAs(TranslationKeys.DISCARD_SPECIFIC_DAY_DESCRIPTION)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(null)
                        .finish()
        );


        builder.add(
                getPropertySpecService()
                        .longSpec()
                        .named(TranslationKeys.MAXIMUM_NUMBER_OF_WEEKS)
                        .describedAs(TranslationKeys.MAXIMUM_NUMBER_OF_WEEKS_DESCRIPTION)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(MAXIMUM_NUMBER_OF_WEEKS_DEFAULT_VALUE)
                        .finish());

        return builder.build();
    }

    @Override
    public void init() {
        numberOfSamples = getProperty(NUMBER_OF_SAMPLES,Long.class)
                .orElse(NUMBER_OF_SAMPLES_DEFAULT_VALUE);

        maxNumberOfWeeks = getProperty(MAXIMUM_NUMBER_OF_WEEKS,Long.class)
                .orElse(MAXIMUM_NUMBER_OF_WEEKS_DEFAULT_VALUE);

        discardSpecificDay = getProperty(DISCARD_SPECIFIC_DAY,CalendarWithEventSettings.class)
                .orElse(NoneCalendarWithEventSettings.INSTANCE);

    }

    private boolean canEstimate(EstimationBlock block) {
        return isRegular(block);
    }

    private boolean isRegular(EstimationBlock block) {
        boolean regular = block.getReadingType().isRegular();
        if (!regular) {
            String message = "Failed estimation with {rule}: Block {block} since it has a reading type that is not regular : {readingType}";
            LoggingContext.get().info(getLogger(), message);
        }
        return regular;
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

        if (block.getReadingType().getMacroPeriod()!= MacroPeriod.NOTAPPLICABLE && block.getReadingType().getMacroPeriod()!= MacroPeriod.DAILY){
            String message = "Failed estimation with {rule}: Block {block} since the reading type {readingType} measuring period  is larger than day.";
            LoggingContext.get().info(getLogger(), message);
            return false;
        }
        return true;
    }

    private boolean estimate(EstimationBlock estimationBlock, Set<QualityCodeSystem> systems) {
            return estimateWithDeltas(estimationBlock, systems);
    }

    private boolean estimateWithDeltas(EstimationBlock estimationBlock, Set<QualityCodeSystem> systems) {
        List<? extends Estimatable> estimables = estimationBlock.estimatables();
        ZoneId zone = estimationBlock.getChannel().getZoneId();
        Instant startInterval = estimables.get(0).getTimestamp();
        Instant endInterval = estimables.get(estimables.size() - 1).getTimestamp();
        ReadingType blockReadingType = estimationBlock.getReadingType();
        Boolean discardDay = ((DiscardDayWithEventSettings)discardSpecificDay).getDiscardDay();
        if (discardDay){
            calendar = ((DiscardDayWithEventSettings)discardSpecificDay).getCalendar();
            calendarEventCode = ((DiscardDayWithEventSettings)discardSpecificDay).getEvent().getId();
        }
        else {
            calendar = null;
            calendarEventCode = 0;
        }



        Map<Estimatable,BigDecimal> valuesForEstimatables = new HashMap<>();

        Calendar.ZonedView zonedView =calendar.forZone(estimationBlock.getChannel().getZoneId(), calendar.getStartYear(),calendar.getEndYear());

        for (Estimatable estimatable: estimables) {

                Instant estimatableTimestamp = estimatable.getTimestamp();
                BigDecimal result = new BigDecimal(0);
                List<? extends BaseReadingRecord> readingsBefore;
                Range<Instant> beforeRange = Range.closedOpen(estimatableTimestamp.minus(maxNumberOfWeeks * ChronoUnit.WEEKS.getDuration().toDays(),ChronoUnit.DAYS ),estimatableTimestamp);
                if (discardDay){
                            readingsBefore =estimationBlock.getChannel()
                            .getReadings(beforeRange)
                            .stream()
                            .filter(brrc->ZonedDateTime.ofInstant(brrc.getTimeStamp(), zone).get(ChronoField.DAY_OF_WEEK) == ZonedDateTime.ofInstant(estimatable.getTimestamp(), zone).get(ChronoField.DAY_OF_WEEK))
                            .filter(brrc->ZonedDateTime.ofInstant(brrc.getTimeStamp(), zone).get(ChronoField.MINUTE_OF_DAY) == ZonedDateTime.ofInstant(estimatable.getTimestamp(), zone).get(ChronoField.MINUTE_OF_DAY))
                            .filter(brrc->zonedView.eventFor(brrc.getTimeStamp()).getId()==calendarEventCode)
                            .collect(Collectors.toList());
                }

            else {
                    readingsBefore =estimationBlock.getChannel()
                            .getReadings(beforeRange)
                            .stream()
                            .filter(brrc->ZonedDateTime.ofInstant(brrc.getTimeStamp(), zone).get(ChronoField.DAY_OF_WEEK) == ZonedDateTime.ofInstant(estimatable.getTimestamp(), zone).get(ChronoField.DAY_OF_WEEK))
                            .filter(brrc->ZonedDateTime.ofInstant(brrc.getTimeStamp(), zone).get(ChronoField.MINUTE_OF_DAY) == ZonedDateTime.ofInstant(estimatable.getTimestamp(), zone).get(ChronoField.MINUTE_OF_DAY))
                            .collect(Collectors.toList());
            }

                ////Check
                if (readingsBefore.size()<numberOfSamples) {
                    String message = "Failed estimation with {rule}: Block {block} since not enough samples are found.  Found {0} samples, requires {1} samples";
                    LoggingContext.get().info(getLogger(), message, readingsBefore.size(), this.numberOfSamples.intValue());
                    return false;
                }

                if (zonedView.eventFor(estimatable.getTimestamp()).getId()==calendarEventCode && discardDay==true){
                    String message = "Failed estimation with {rule}: Block {block} since the values to estimate belong to a day configured to be discarded";
                    LoggingContext.get().info(getLogger(), message);
                    return false;
                }

                int count=0;
                for (BaseReadingRecord reading:readingsBefore){

                    if (zonedView.eventFor(reading.getTimeStamp()).getId()!=calendarEventCode && isValidSample(estimationBlock,reading.getTimeStamp(),reading,systems) && count<numberOfSamples){
                        result = result.add(reading.getValue());
                        count++;
                    }

                }

                if (count!=numberOfSamples){
                        String message = "Failed estimation with {rule}: Block {block} since not enough valid samples found. Found {0} samples, requires {1} samples";
                        LoggingContext.get().info(getLogger(), message, count, numberOfSamples);
                        return false;
                }
                result = result.divide(BigDecimal.valueOf(count));
                valuesForEstimatables.put(estimatable,result);
            }

            if (valuesForEstimatables.size()>0){
                for (Estimatable estimatable:valuesForEstimatables.keySet()){
                    estimatable.setEstimation(valuesForEstimatables.get(estimatable));
                }
            }


        return true;
    }


    private static boolean isValidSample(EstimationBlock estimationBlock, Instant estimableTime,
                                         BaseReadingRecord record, Set<QualityCodeSystem> systems) {
        ZoneId zone = estimationBlock.getChannel().getZoneId();
        return sameTimeOfWeek(ZonedDateTime.ofInstant(record.getTimeStamp(), zone), ZonedDateTime.ofInstant(estimableTime, zone))
                && record.getQuantity(estimationBlock.getReadingType()) != null
                && isValidReading(estimationBlock.getCimChannel(),record,systems);
    }


    private static boolean isValidReading(CimChannel advanceCimChannel, BaseReadingRecord readingToEvaluate,
                                          Set<QualityCodeSystem> systems) {
        return advanceCimChannel.findReadingQualities()
                .atTimestamp(readingToEvaluate.getTimeStamp())
                .actual()
                .ofQualitySystems(systems)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.KNOWNMISSINGREAD))///Missing ??
                .noneMatch();
    }

    private static boolean sameTimeOfWeek(ZonedDateTime first, ZonedDateTime second) {
        return first.getDayOfWeek().equals(second.getDayOfWeek())
                && first.getLong(ChronoField.NANO_OF_DAY) == second.getLong(ChronoField.NANO_OF_DAY);
    }

}
