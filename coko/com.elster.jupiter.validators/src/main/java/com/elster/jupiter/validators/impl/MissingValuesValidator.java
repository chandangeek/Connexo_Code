package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MessageSeeds;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTimeConstants;

import java.util.BitSet;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This Validator will interpret Intervals as being closed. i.e. start and end time are included in the interval. So when validating missing readings for a five minute interval over a period of five minutes will expect 2 readings.
 *
 * Copyrights EnergyICT
 * Date: 10/07/2014
 * Time: 14:29
 */
public class MissingValuesValidator extends AbstractValidator {

    private static final String READING_QUALITY_TYPE_CODE = "3.5.259";
    public static final String BASE_KEY = MissingValuesValidator.class.getName();
    private Interval interval;
    private int millisBetweenReadings;
    private BitSet bitSet;
    private int expectedReadings;

    MissingValuesValidator(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public NlsKey getNlsKey() {
        return SimpleNlsKey.key(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN, BASE_KEY);
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
    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getOptionalKeys() {
        return Collections.emptyList();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Interval interval) {
        if (!channel.isRegular() || readingType.getMeasuringPeriod().getMinutes() == 0) {
            this.interval = null;
            return;
        }
        this.millisBetweenReadings = readingType.getMeasuringPeriod().getMinutes() * DateTimeConstants.MILLIS_PER_MINUTE;
        this.interval = interval;
        if (interval.getStart().getTime() % millisBetweenReadings != 0) {
            long adjustedStart = (interval.getStart().getTime() / millisBetweenReadings) * millisBetweenReadings + millisBetweenReadings;
            this.interval = interval.withStart(new Date(adjustedStart));
        }
        expectedReadings = (int) (interval.durationInMillis() / millisBetweenReadings + 1);
        bitSet = new BitSet(expectedReadings);
    }

    private int toIndex(Date time) {
        return (int) ((time.getTime() - interval.getStart().getTime()) / millisBetweenReadings);
    }

    private Date toTime(int index) {
        long millis = (index * millisBetweenReadings) + interval.getStart().getTime();
        return new Date(millis);
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        bitSet.set(toIndex(intervalReadingRecord.getTimeStamp()));
        return ValidationResult.PASS;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        // this type of validation can only verify missings on intervalreadings
        return ValidationResult.PASS;
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
    public Map<Date, ValidationResult> finish() {
        if (interval == null) {
            return Collections.emptyMap();
        }
        ImmutableMap.Builder<Date, ValidationResult> builder = ImmutableMap.builder();
        for (int i = 0; i < expectedReadings; i++) {
            if (!bitSet.get(i)) {
                builder.put(toTime(i), ValidationResult.SUSPECT);
            }
        }
        return builder.build();
    }

}
