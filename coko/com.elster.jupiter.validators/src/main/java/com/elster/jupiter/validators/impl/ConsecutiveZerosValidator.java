/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MissingRequiredProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.validation.ValidationResult.SUSPECT;
import static com.elster.jupiter.validation.ValidationResult.VALID;
import static com.elster.jupiter.validators.impl.MessageSeeds.MAX_PERIOD_SHORTER_THEN_MIN_PERIOD;
import static com.elster.jupiter.validators.impl.MessageSeeds.NOT_DELTA_READING_TYPE;

public class ConsecutiveZerosValidator extends AbstractValidator {

    static final String MINIMUM_PERIOD = "minimumPeriod";
    static final String MAXIMUM_PERIOD = "maximumPeriod";
    static final String MINIMUM_THRESHOLD = "minimumThreshold";
    static final String CHECK_RETROACTIVELY = "checkRetroactively";
    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);

    private final Logger logger = Logger.getLogger(ConsecutiveZerosValidator.class.getName());

    private enum TranslationKeys implements TranslationKey {
        CHECK_RETROACTIVELY(ConsecutiveZerosValidator.CHECK_RETROACTIVELY, "Check retroactively"),
        MIN_PERIOD(ConsecutiveZerosValidator.MINIMUM_PERIOD, "Minimum period"),
        MAX_PERIOD(ConsecutiveZerosValidator.MAXIMUM_PERIOD, "Maximum period"),
        MIN_THRESHOLD(ConsecutiveZerosValidator.MINIMUM_THRESHOLD, "Minimum threshold");

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return ConsecutiveZerosValidator.class.getName() + "." + key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }
    }

    private ReadingType readingType;
    private TimeDuration maxPeriod;
    private TimeDuration minPeriod;
    private BigDecimal minThreshold;
    private boolean checkRetroactively;
    private RangeSet<Instant> zeroIntervals;
    private Instant lastCheck;
    private List<IntervalReadingRecord> retroactivelyRecords;

    ConsecutiveZerosValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ConsecutiveZerosValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
        checkRequiredProperties();
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(MINIMUM_PERIOD, MAXIMUM_PERIOD, MINIMUM_THRESHOLD, CHECK_RETROACTIVELY);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService()
                .timeDurationSpec()
                .named(MINIMUM_PERIOD, TranslationKeys.MIN_PERIOD)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(TimeDuration.hours(2))
                .finish());
        builder.add(getPropertySpecService()
                .timeDurationSpec()
                .named(MAXIMUM_PERIOD, TranslationKeys.MAX_PERIOD)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(TimeDuration.days(1))
                .finish());
        builder.add(getPropertySpecService()
                .bigDecimalSpec()
                .named(MINIMUM_THRESHOLD, TranslationKeys.MIN_THRESHOLD)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(BigDecimal.ZERO)
                .finish());
        builder.add(getPropertySpecService()
                .booleanSpec()
                .named(CHECK_RETROACTIVELY, TranslationKeys.CHECK_RETROACTIVELY)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(false)
                .finish());
        return builder.build();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        this.readingType = readingType;
        this.checkRetroactively = getRequiredShift(properties, CHECK_RETROACTIVELY);
        this.minPeriod = getRequiredPeriod(properties, MINIMUM_PERIOD);
        this.maxPeriod = getRequiredPeriod(properties, MAXIMUM_PERIOD);
        this.minThreshold = getRequiredThreshold(properties, MINIMUM_THRESHOLD);
        this.zeroIntervals = getZeroIntervalsFromValidationInterval(channel, getValidationInterval(interval));
        this.lastCheck = interval.lowerEndpoint();
        this.retroactivelyRecords = getRetroactivelyRecords(channel, lastCheck);
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        if (!readingType.getAccumulation().equals(Accumulation.DELTADELTA)) {
            logger.log(NOT_DELTA_READING_TYPE.getLevel(), NOT_DELTA_READING_TYPE.getDefaultFormat(), intervalReadingRecord.getReadingType().getMRID());
            return VALID;
        }
        return zeroIntervals.contains(intervalReadingRecord.getTimeStamp()) ? SUSPECT : VALID;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        // this type of validation can only use on intervalreadings
        return VALID;
    }

    @Override
    public String getDefaultFormat() {
        return "Consecutive zero's";
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    @Override
    public List<PropertySpec> getPropertySpecs(ValidationPropertyDefinitionLevel level) {
        switch (level) {
            case VALIDATION_RULE:
                return getPropertySpecs();
            case TARGET_OBJECT:
                return getPropertySpecs();
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public void validateProperties(Map<String, Object> properties) {
        TimeDuration minPeriod = (TimeDuration) properties.get(MINIMUM_PERIOD);
        TimeDuration maxPeriod = (TimeDuration) properties.get(MAXIMUM_PERIOD);
        if (minPeriod != null && maxPeriod != null && minPeriod.compareTo(maxPeriod) > 0) {
            throw new LocalizedFieldValidationException(MAX_PERIOD_SHORTER_THEN_MIN_PERIOD, "properties." + MAXIMUM_PERIOD);
        }
    }

    @Override
    public Map<Instant, ValidationResult> finish() {
        if (checkRetroactively) {
            Optional<Range<Instant>> retroactivelyZeroInterval = zeroIntervals.asRanges()
                    .stream()
                    .filter(interval -> interval.contains(lastCheck))
                    .findFirst();
            if (retroactivelyZeroInterval.isPresent()) {
                return retroactivelyRecords.stream()
                        .filter(record -> record.getTimeStamp().compareTo(lastCheck) <= 0 && retroactivelyZeroInterval.get().contains(record.getTimeStamp()))
                        .map(IntervalReadingRecord::getTimeStamp)
                        .collect(Collectors.toMap(Function.identity(), instant -> ValidationResult.SUSPECT));
            }

        }
        return super.finish();
    }

    private boolean getRequiredShift(Map<String, Object> properties, String key) {
        return (boolean) properties.get(key);
    }

    private TimeDuration getRequiredPeriod(Map<String, Object> properties, String key) {
        TimeDuration period = (TimeDuration) properties.get(key);
        if (period == null) {
            throw new MissingRequiredProperty(getThesaurus(), key);
        }
        return period;
    }

    private BigDecimal getRequiredThreshold(Map<String, Object> properties, String key) {
        BigDecimal threshold = (BigDecimal) properties.get(key);
        if (threshold == null) {
            throw new MissingRequiredProperty(getThesaurus(), key);
        }
        return threshold;
    }

    private Range<Instant> getValidationInterval(Range<Instant> interval) {
        if (checkRetroactively) {
            return Range.openClosed(interval.lowerEndpoint().minusMillis(maxPeriod.getMilliSeconds()), interval.upperEndpoint());
        }
        return interval;
    }

    private RangeSet<Instant> getZeroIntervalsFromValidationInterval(Channel channel, Range<Instant> validationInterval) {
        Map<Instant, IntervalReadingRecord> intervalReadingRecords = new TreeMap<>();
        List<Instant> timeStampsFromInterval = channel.toList(validationInterval);
        timeStampsFromInterval.forEach(instant -> intervalReadingRecords.putIfAbsent(instant, null));
        channel.getIntervalReadings(validationInterval).forEach(record -> intervalReadingRecords.putIfAbsent(record.getTimeStamp(), record));
        RangeSet<Instant> zeroIntervals = TreeRangeSet.create();
        Instant startZeroInterval = validationInterval.lowerEndpoint();
        Instant endZeroInterval = startZeroInterval;
        boolean intervalStarted = false;
        for (Instant timeStamp :
                timeStampsFromInterval) {
            IntervalReadingRecord record = intervalReadingRecords.get(timeStamp);
            if (record != null && record.getValue() != null && record.getValue().compareTo(this.minThreshold) <= 0) {
                if (!intervalStarted) {
                    startZeroInterval = ZonedDateTime.ofInstant(timeStamp, ZoneId.systemDefault()).minus(channel.getIntervalLength().get()).toInstant();
                    intervalStarted = true;
                } else {
                    endZeroInterval = timeStamp;
                }
            } else if (intervalStarted) {
                long periodLength = endZeroInterval.toEpochMilli() - startZeroInterval.toEpochMilli();
                if (periodLength > minPeriod.getMilliSeconds() && periodLength < maxPeriod.getMilliSeconds()) {
                    zeroIntervals.add(Range.openClosed(startZeroInterval, endZeroInterval));
                }
                intervalStarted = false;
            }
        }
        if(intervalStarted){
            long periodLength = endZeroInterval.toEpochMilli() - startZeroInterval.toEpochMilli();
            if (periodLength > minPeriod.getMilliSeconds() && periodLength < maxPeriod.getMilliSeconds()) {
                zeroIntervals.add(Range.openClosed(startZeroInterval, endZeroInterval));
            }
        }
        return zeroIntervals;
    }

    private List<IntervalReadingRecord> getRetroactivelyRecords(Channel channel, Instant lastCheck) {
        if (checkRetroactively) {
            return channel
                    .getIntervalReadings(Range.openClosed(lastCheck.minus(maxPeriod.asTemporalAmount()), lastCheck))
                    .stream()
                    .filter(intervalReadingRecord -> intervalReadingRecord.getReadingQualities().stream()
                            .filter(readingQuality -> readingQuality.getReadingType().equals(readingType))
                            .anyMatch(readingQuality -> !readingQuality.isSuspect()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<TranslationKey> getExtraTranslationKeys() {
        return Arrays.asList(TranslationKeys.values());
    }
}
