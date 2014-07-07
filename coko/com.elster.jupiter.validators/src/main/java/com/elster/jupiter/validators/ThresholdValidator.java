package com.elster.jupiter.validators;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ThresholdValidator implements Validator {

    private static final String MIN = "minimum";
    private static final String MAX = "maximum";

    private Quantity minimum;
    private Quantity maximum;
    private ReadingType readingType;
    private final Thesaurus thesaurus;

    protected ThresholdValidator() {
        thesaurus = null;
    }

    public ThresholdValidator(Thesaurus thesaurus, Map<String, Quantity> properties) {
        this.thesaurus = thesaurus;
        Quantity min = getRequiredQuantity(properties, MIN);
        Quantity max = getRequiredQuantity(properties, MAX);
        minimum = min;
        maximum = max;
    }

    private Quantity getRequiredQuantity(Map<String, Quantity> properties, String key) {
        Quantity min = properties.get(key);
        if (min == null) {
            throw new MissingRequiredProperty(thesaurus, key);
        }
        return min;
    }

    @Override
    public List<String> getRequiredKeys() {
        return ImmutableList.of(MIN, MAX);
    }

    @Override
    public List<String> getOptionalKeys() {
        return Collections.emptyList();
    }

    @Override
    public Optional<ReadingQualityType> getReadingQualityTypeCode() {
        return Optional.absent();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Interval interval) {
        this.readingType = readingType;
    }


    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        Quantity toValidate = intervalReadingRecord.getQuantity(readingType);
        if (toValidate != null) {
            boolean withinBounds = isWithinBounds(toValidate);
            return withinBounds ? ValidationResult.PASS : ValidationResult.SUSPECT;
        } else {
            return ValidationResult.SKIPPED;
        }
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return validateQuantity(readingRecord.getQuantity(readingType));
    }

    @Override
    public String getDisplayName() {
        return "Min Max";
    }

    private ValidationResult validateQuantity(Quantity toValidate) {
        return isWithinBounds(toValidate) ? ValidationResult.PASS : ValidationResult.SUSPECT;
    }

    private boolean isWithinBounds(Quantity toValidate) {
        return minimum.compareTo(toValidate) <= 0 && maximum.compareTo(toValidate) >= 0;
    }
}
