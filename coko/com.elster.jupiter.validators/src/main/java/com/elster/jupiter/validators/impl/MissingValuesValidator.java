package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.joda.time.DateTimeConstants;

import java.time.Duration;
import java.time.Instant;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.Ranges.copy;

/**
 * This Validator will interpret Intervals as being closed. i.e. start and end time are included in the interval. So when validating missing readings for a five minute interval over a period of five minutes will expect 2 readings.
 * <p/>
 */
class MissingValuesValidator extends AbstractValidator {

    private static final String READING_QUALITY_TYPE_CODE = "3.5.259";
    
    private Range<Instant> interval;
    private long millisBetweenReadings;
    private BitSet bitSet;
    private int expectedReadings;
    private boolean openEnded;

    MissingValuesValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public NlsKey getPropertyNlsKey(String property) {
        // there are no properties
        return null;
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        // there are no properties
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Range<Instant> interval) {
        if (!channel.isRegular() || readingType.getMeasuringPeriod().getMinutes() == 0) {
            this.interval = null;
            return;
        }
        this.millisBetweenReadings = readingType.getMeasuringPeriod().getMinutes() * DateTimeConstants.MILLIS_PER_MINUTE;
        this.interval = interval;
        if (interval.lowerEndpoint().toEpochMilli() % millisBetweenReadings != 0) {
            long adjustedStart = (interval.lowerEndpoint().toEpochMilli() / millisBetweenReadings) * millisBetweenReadings + millisBetweenReadings;
            this.interval = copy(interval).withClosedLowerBound(Instant.ofEpochMilli(adjustedStart));
        }
        openEnded = !interval.hasUpperBound();
        if (openEnded) {
            bitSet = new BitSet();
        } else {
            expectedReadings = (int) (durationInMillis(interval) / millisBetweenReadings + 1);
            bitSet = new BitSet(expectedReadings);
        }
    }

    private long durationInMillis(Range<Instant> range) {
        return Duration.between(range.lowerEndpoint(), range.upperEndpoint()).toMillis();
    }

    private int toIndex(Instant time) {
        return (int) ((time.toEpochMilli() - interval.lowerEndpoint().toEpochMilli()) / millisBetweenReadings);
    }

    private Instant toTime(int index) {
        long millis = (index * millisBetweenReadings) + interval.lowerEndpoint().toEpochMilli();
        return Instant.ofEpochMilli(millis);
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        int index = toIndex(intervalReadingRecord.getTimeStamp());
        bitSet.set(index);
        if (openEnded && index + 1 > expectedReadings) {
            expectedReadings = index + 1;
        }
        return ValidationResult.VALID;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        // this type of validation can only verify missings on intervalreadings
        return ValidationResult.VALID;
    }

    @Override
    public String getDefaultFormat() {
        return "Check missing values";
    }

    @Override
    public Optional<ReadingQualityType> getReadingQualityTypeCode() {
        return Optional.of(new ReadingQualityType(READING_QUALITY_TYPE_CODE));
    }

    @Override
    public Map<Instant, ValidationResult> finish() {
        if (interval == null) {
            return Collections.emptyMap();
        }
        ImmutableMap.Builder<Instant, ValidationResult> builder = ImmutableMap.builder();
        for (int i = 0; i < expectedReadings; i++) {
            if (!bitSet.get(i)) {
                builder.put(toTime(i), ValidationResult.SUSPECT);
            }
        }
        return builder.build();
    }
    
    @Override
    public List<String> getRequiredProperties() {
        return Collections.emptyList();
    }
}
