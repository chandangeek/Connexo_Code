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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.validation.ValidationResult.SUSPECT;
import static com.elster.jupiter.validation.ValidationResult.VALID;
import static com.elster.jupiter.validators.impl.MessageSeeds.MAX_PERIOD_SHORTER_THEN_MIN_PERIOD;

public class ConsecutiveValidator extends AbstractValidator{

    static final String MINIMUM_PERIOD = "minimumPeriod";
    static final String MAXIMUM_PERIOD = "maximumPeriod";
    static final String MINIMUM_THRESHOLD = "minimumThreshold";
    private static final Set<QualityCodeSystem> QUALITY_CODE_SYSTEMS = ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM);

    private final Logger logger = Logger.getLogger(ConsecutiveValidator.class.getName());

    private ReadingType readingType;
    private TimeDuration maxPeriod;
    private TimeDuration minPeriod;
    private BigDecimal minThreshold;
    private RangeSet<Instant> zeroIntervals;

    ConsecutiveValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ConsecutiveValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(MINIMUM_PERIOD, MAXIMUM_PERIOD, MINIMUM_THRESHOLD);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService()
                        .timeDurationSpec()
                        .named(MINIMUM_PERIOD, TranslationKeys.CONSECUTIVE_VALIDATOR_MIN_PERIOD)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(TimeDuration.hours(2))
                        .finish());
        builder.add(getPropertySpecService()
                        .timeDurationSpec()
                        .named(MAXIMUM_PERIOD, TranslationKeys.CONSECUTIVE_VALIDATOR_MAX_PERIOD)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(TimeDuration.days(1))
                        .finish());
        builder.add(getPropertySpecService()
                .bigDecimalSpec()
                .named(MINIMUM_THRESHOLD, TranslationKeys.CONSECUTIVE_VALIDATOR_MIN_THRESHOLD)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(BigDecimal.ZERO)
                .finish());
        return builder.build();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
            this.readingType = readingType;
            this.minPeriod = getRequiredPeriod(properties, MINIMUM_PERIOD);
            this.maxPeriod = getRequiredPeriod(properties, MAXIMUM_PERIOD);
            this.minThreshold = getRequiredThreshold(properties, MINIMUM_THRESHOLD);
            this.zeroIntervals = getZeroIntervalsFromValidationInterval(channel, interval);
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        if(readingType.getAccumulation().equals(Accumulation.DELTADELTA)) {
            return zeroIntervals.contains(intervalReadingRecord.getTimeStamp()) ? SUSPECT : VALID;
        }
        logger.log(Level.INFO, "{0} is not a delta reading type", intervalReadingRecord.getReadingType().getMRID());
        return VALID;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        // this type of validation can only use on intervalreadings
        return VALID;
    }

    @Override
    public String getDefaultFormat() {
        return TranslationKeys.CONSECUTIVE_VALIDATOR.getDefaultFormat();
    }

    @Override
    public Set<QualityCodeSystem> getSupportedQualityCodeSystems() {
        return QUALITY_CODE_SYSTEMS;
    }

    @Override
    public List<PropertySpec> getPropertySpecs(ValidationPropertyDefinitionLevel level) {
        switch (level)
        {
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
        if (minPeriod.compareTo(maxPeriod) > 0) {
            throw new LocalizedFieldValidationException(MAX_PERIOD_SHORTER_THEN_MIN_PERIOD, "properties." + MAXIMUM_PERIOD);
        }
    }

    private TimeDuration getRequiredPeriod(Map<String, Object> properties, String key){
        TimeDuration period = (TimeDuration) properties.get(key);
        if (period == null) {
            throw new MissingRequiredProperty(getThesaurus(), key);
        }
        return period;
    }

    private BigDecimal getRequiredThreshold(Map<String, Object> properties, String key){
        BigDecimal threshold = (BigDecimal) properties.get(key);
        if (threshold == null) {
            throw new MissingRequiredProperty(getThesaurus(), key);
        }
        return threshold;
    }


    private RangeSet<Instant> getZeroIntervalsFromValidationInterval(Channel channel, Range<Instant> interval) {
        Range<Instant> validationInterval = Range.openClosed(interval.lowerEndpoint().minusMillis(minPeriod.getMilliSeconds()), interval.upperEndpoint());
        TreeMap<Instant, IntervalReadingRecord> intervalReadingRecords = new TreeMap<>();
        List<Instant> timestampsFromInterval = channel.toList(validationInterval);
        timestampsFromInterval.forEach(instant -> intervalReadingRecords.putIfAbsent(instant, null));
        channel.getIntervalReadings(validationInterval).forEach(record -> intervalReadingRecords.putIfAbsent(record.getTimeStamp(), record));
        RangeSet<Instant> zeroIntervals = TreeRangeSet.create();
        Instant startZeroInterval = validationInterval.lowerEndpoint();
        boolean intervalStarted = false;
        for (Instant timeStamp:
                timestampsFromInterval) {
            IntervalReadingRecord record = intervalReadingRecords.get(timeStamp);
            if(record != null && record.getValue() != null && record.getValue().compareTo(this.minThreshold) <= 0) {
                if (!intervalStarted) {
                        startZeroInterval = timeStamp;
                        intervalStarted = true;
                }
            } else {
                if (intervalStarted) {
                    long periodLength = timeStamp.getEpochSecond() - startZeroInterval.getEpochSecond();
                    if (periodLength > minPeriod.getSeconds() && periodLength < maxPeriod.getSeconds()) {
                        zeroIntervals.add(Range.closedOpen(startZeroInterval, timeStamp));
                    }
                    intervalStarted = false;
                }
            }
        }
        return zeroIntervals;
    }
}
