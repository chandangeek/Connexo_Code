/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;


import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.CalendarWithEventSettings;
import com.elster.jupiter.estimation.CalendarWithEventSettingsFactory;
import com.elster.jupiter.estimation.DiscardDaySettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.NoneCalendarWithEventSettings;
import com.elster.jupiter.estimators.AbstractEstimator;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class NearestAvgValueDayEstimator extends AbstractEstimator implements Estimator {

    public static final String DISCARD_SPECIFIC_DAY = TranslationKeys.DISCARD_SPECIFIC_DAY.getKey();
    public static final String NUMBER_OF_SAMPLES = TranslationKeys.NUMBER_OF_SAMPLES.getKey();
    public static final Long NUMBER_OF_SAMPLES_DEFAULT_VALUE = 3L;
    public static final String MAXIMUM_NUMBER_OF_WEEKS = TranslationKeys.MAXIMUM_NUMBER_OF_WEEKS.getKey();
    public static final Long MAXIMUM_NUMBER_OF_WEEKS_DEFAULT_VALUE = 4L;
    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DefaultDateTimeFormatters.mediumDate()
            .withShortTime()
            .build()
            .withZone(ZoneId
                    .systemDefault())
            .withLocale(Locale.ENGLISH);

    private final ValidationService validationService;
    private final MeteringService meteringService;
    private final TimeService timeService;
    private final CalendarService calendarService;

    private Long numberOfSamples;
    private Long maxNumberOfWeeks;
    private CalendarWithEventSettings discardSpecificDay;
    private Calendar calendar;
    private long calendarEventId;
    private final Set<Instant> skippedDays = new HashSet<>();

    public enum TranslationKeys implements TranslationKey {
        ESTIMATOR_NAME(NearestAvgValueDayEstimator.class.getName(), "Nearest average value (Day) [STD]"),
        DISCARD_SPECIFIC_DAY("nearestaveragevalueday.discardSpecificDay", "Discard specific day"),
        DISCARD_SPECIFIC_DAY_DESCRIPTION("nearestaveragevalueday.discardSpecificDay.description",
                "Estimated and sample days can be discarded if they contain a chosen event code"),
        NUMBER_OF_SAMPLES("nearestaveragevalueday.numberOfSamples", "Number of samples"),
        NUMBER_OF_SAMPLES_DESCRIPTION("nearestaveragevalueday.numberOfSamples.description",
                "Number of samples required to complete estimation for a value."),
        MAXIMUM_NUMBER_OF_WEEKS("nearestaveragevalueday.maximumNumberOfWeeks", "Maximum number of weeks"),
        MAXIMUM_NUMBER_OF_WEEKS_DESCRIPTION("nearestaveragevalueday.maximumNumberOfWeeks.description",
                "Limit for a number of weeks where the samples can be searched"),
        CALENDAR("nearestaveragevalueday.calendar", "ToU calendar"),
        EVENT_CODE("nearestaveragevalueday.eventCode", "Event code");

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

    NearestAvgValueDayEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService,
                                ValidationService validationService, MeteringService meteringService,
                                TimeService timeService, CalendarService calendarService) {
        super(thesaurus, propertySpecService);
        this.validationService = validationService;
        this.meteringService = meteringService;
        this.timeService = timeService;
        this.calendarService = calendarService;
    }

    NearestAvgValueDayEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService,
                                ValidationService validationService, MeteringService meteringService,
                                TimeService timeService, CalendarService calendarService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        this.validationService = validationService;
        this.meteringService = meteringService;
        this.timeService = timeService;
        this.calendarService = calendarService;
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
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER,
                            "properties." + NUMBER_OF_SAMPLES);
                }
            } else if (property.getKey().equals(MAXIMUM_NUMBER_OF_WEEKS)) {
                Long value = (Long) property.getValue();
                if (value.intValue() < 1) {
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER,
                            "properties." + MAXIMUM_NUMBER_OF_WEEKS);
                }
            } else if (property.getKey().equals(DISCARD_SPECIFIC_DAY)) {
                Object day = property.getValue();
                if (day instanceof DiscardDaySettings) {
                    DiscardDaySettings discardDaySettings = (DiscardDaySettings) day;
                    if (discardDaySettings.isDiscardDay()) {
                        if (discardDaySettings.getCalendar() == null) {
                            throw new LocalizedFieldValidationException(MessageSeeds.REQUIRED_FIELD,
                                    "properties." + DISCARD_SPECIFIC_DAY + ".calendar");
                        }
                        if (discardDaySettings.getEvent() == null) {
                            throw new LocalizedFieldValidationException(MessageSeeds.REQUIRED_FIELD,
                                    "properties." + DISCARD_SPECIFIC_DAY + ".eventCode");
                        }
                    }
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
                        .longSpec()
                        .named(TranslationKeys.MAXIMUM_NUMBER_OF_WEEKS)
                        .describedAs(TranslationKeys.MAXIMUM_NUMBER_OF_WEEKS_DESCRIPTION)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(MAXIMUM_NUMBER_OF_WEEKS_DEFAULT_VALUE)
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

        return builder.build();
    }

    @Override
    public void init() {
        numberOfSamples = getProperty(NUMBER_OF_SAMPLES, Long.class)
                .orElse(NUMBER_OF_SAMPLES_DEFAULT_VALUE);

        maxNumberOfWeeks = getProperty(MAXIMUM_NUMBER_OF_WEEKS, Long.class)
                .orElse(MAXIMUM_NUMBER_OF_WEEKS_DEFAULT_VALUE);

        discardSpecificDay = getProperty(DISCARD_SPECIFIC_DAY, CalendarWithEventSettings.class)
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

        if (block.getReadingType().getMacroPeriod() != MacroPeriod.NOTAPPLICABLE && block.getReadingType().getMacroPeriod() != MacroPeriod.DAILY) {
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
        List<? extends Estimatable> estimatables = estimationBlock.estimatables();
        ZoneId zone = estimationBlock.getChannel().getZoneId();
        Calendar.ZonedView zonedView = null;
        Boolean discardDay = discardSpecificDay instanceof NoneCalendarWithEventSettings ? false : ((DiscardDaySettings) discardSpecificDay).isDiscardDay();
        String estimateOn = estimationBlock.getChannel()
                .getChannelsContainer()
                .getUsagePoint()
                .map((usagePoint) -> usagePoint.getName())
                .orElseGet(() -> estimationBlock.getChannel().getChannelsContainer().getMeter().map((meter) -> meter.getName()).orElse(""));
        if (discardDay) {
            calendar = ((DiscardDaySettings) discardSpecificDay).getCalendar();
            calendarEventId = ((DiscardDaySettings) discardSpecificDay).getEvent().getId();
            zonedView = calendar.forZone(estimationBlock.getChannel().getZoneId(), calendar.getStartYear(), calendar.getEndYear().get());
        } else {
            calendar = null;
            calendarEventId = 0;
        }

        Map<Estimatable, BigDecimal> valuesForEstimatables = new HashMap<>();
        for (Estimatable estimatable : estimatables) {
            Instant estimatableTimestamp = estimatable.getTimestamp();
            BigDecimal result = new BigDecimal(0);
            Range<Instant> beforeRange = Range.closedOpen(estimatableTimestamp.minus(maxNumberOfWeeks * ChronoUnit.WEEKS.getDuration().toDays(), ChronoUnit.DAYS)
                    .minus(estimationBlock.getReadingType().getIntervalLength().get()), estimatableTimestamp.minus(estimationBlock.getReadingType().getIntervalLength().get()));
            Calendar.ZonedView finalZonedView = zonedView;
            List<? extends BaseReadingRecord> readingsBefore = estimationBlock.getChannel()
                    .getReadings(beforeRange)
                    .stream()
                    .sorted(Comparator.comparing(BaseReadingRecord::getTimeStamp))
                    .filter(brcc -> isValidSample(estimationBlock, estimatable.getTimestamp(), brcc, zone, finalZonedView, discardDay))
                    .collect(Collectors.toList());
            if (discardDay) {
                if (zonedView.dayTypeFor(estimatable.getTimestamp())
                        .getEventOccurrences()
                        .stream()
                        .map(EventOccurrence::getEvent)
                        .map(HasId::getId)
                        .anyMatch(eventId -> eventId == calendarEventId)) {
                    String message = getThesaurus().getFormat(MessageSeeds.NEAREST_AVG_VALUE_DAY_ESTIMATOR_FAIL_ESTIMATED_DAY_DISCARDED)
                            .format(blockToString(estimationBlock), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                    .format(), estimateOn, estimationBlock.getReadingType()
                                    .getFullAliasName());
                    LoggingContext.get().warning(getLogger(), message);
                    return false;
                }
            }
            if (readingsBefore.size() < numberOfSamples) {
                String message = getThesaurus().getFormat(MessageSeeds.NEAREST_AVG_VALUE_DAY_ESTIMATOR_FAIL_NOT_ENOUGH_SAMPLES)
                        .format(blockToString(estimationBlock), getThesaurus().getFormat(TranslationKeys.ESTIMATOR_NAME)
                                .format(), estimateOn, estimationBlock.getReadingType()
                                .getFullAliasName());
                LoggingContext.get().warning(getLogger(), message);
                return false;
            }
            result = readingsBefore
                    .stream()
                    .limit(numberOfSamples)
                    .map(record -> record.getQuantity(estimationBlock.getReadingType()))
                    .map(Quantity::getValue)
                    .reduce(BigDecimal::add).orElse(null);

            if (result != null) {
                result = result.divide(BigDecimal.valueOf(numberOfSamples), RoundingMode.HALF_UP);
                valuesForEstimatables.put(estimatable, result);
            }
        }
        if (valuesForEstimatables.size() > 0) {
            for (Estimatable estimatable : valuesForEstimatables.keySet()) {
                estimatable.setEstimation(valuesForEstimatables.get(estimatable));
            }
        }
        return true;
    }

    private boolean isValidSample(EstimationBlock estimationBlock, Instant estimableTime,
                                  BaseReadingRecord record,
                                  ZoneId zone, Calendar.ZonedView view, Boolean discardDay) {
        fillSkipDays(record, calendarEventId, view, discardDay);
        return sameTimeOfWeek(ZonedDateTime.ofInstant(record.getTimeStamp(), zone), ZonedDateTime.ofInstant(estimableTime, zone))
                && record.getQuantity(estimationBlock.getReadingType()) != null
                && !skippedDays.contains(record.getTimeStamp().truncatedTo(ChronoUnit.DAYS));
    }

    private void fillSkipDays(BaseReadingRecord record, Long calendarEventId, Calendar.ZonedView view, boolean discardDay) {
        record.getReadingQualities().forEach(quality -> {
            if (quality.isMissing() || quality.isSuspect()) {
                skippedDays.add(record.getTimeStamp().truncatedTo(ChronoUnit.DAYS));
            }
        });
        if (discardDay) {
            view.dayTypeFor(record.getTimeStamp()).getEventOccurrences().forEach(occurrence -> {
                if (occurrence.getEvent().getId() == calendarEventId) {
                    skippedDays.add(record.getTimeStamp().truncatedTo(ChronoUnit.DAYS));
                }
            });
        }
    }

    private static boolean sameTimeOfWeek(ZonedDateTime first, ZonedDateTime second) {
        return first.getDayOfWeek().equals(second.getDayOfWeek())
                && first.getLong(ChronoField.NANO_OF_DAY) == second.getLong(ChronoField.NANO_OF_DAY);
    }

    private String blockToString(EstimationBlock block) {
        return DATE_TIME_FORMATTER.format(block.estimatables()
                .get(0)
                .getTimestamp()) + " until " + DATE_TIME_FORMATTER.format(block
                .estimatables()
                .get(block.estimatables().size() - 1)
                .getTimestamp());
    }
}
