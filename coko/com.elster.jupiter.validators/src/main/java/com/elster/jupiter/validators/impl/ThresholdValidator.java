package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MessageSeeds;
import com.elster.jupiter.validators.MissingRequiredProperty;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.validation.ValidationResult.*;

class ThresholdValidator extends AbstractValidator {

    private static final String MIN = "minimum";
    private static final String MAX = "maximum";
    public static final String BASE_KEY = ThresholdValidator.class.getName();

    private Quantity minimum;
    private Quantity maximum;
    private ReadingType readingType;

    ThresholdValidator(Thesaurus thesaurus) {
        super(thesaurus);
    }

    public ThresholdValidator(Thesaurus thesaurus, Map<String, Quantity> properties) {
        super(thesaurus);
        Quantity min = getRequiredQuantity(properties, MIN);
        Quantity max = getRequiredQuantity(properties, MAX);
        minimum = min;
        maximum = max;
    }

    private Quantity getRequiredQuantity(Map<String, Quantity> properties, String key) {
        Quantity quantity = properties.get(key);
        if (quantity == null) {
            throw new MissingRequiredProperty(getThesaurus(), key);
        }
        return quantity;
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
    public void init(Channel channel, ReadingType readingType, Interval interval) {
        this.readingType = readingType;
    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        return validateBaseReadingRecord(intervalReadingRecord);
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        return validateBaseReadingRecord(readingRecord);
    }

    private ValidationResult validateBaseReadingRecord(BaseReadingRecord baseReadingRecord) {
        return validateQuantity(baseReadingRecord.getQuantity(readingType));
    }

    @Override
    public NlsKey getNlsKey() {
        return SimpleNlsKey.key(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN, BASE_KEY);
    }

    @Override
    public String getDefaultFormat() {
        return "Threshold violation";
    }

    @Override
    public NlsKey getPropertyNlsKey(String property) {
        if (isAProperty(property)) {
            return SimpleNlsKey.key(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN, BASE_KEY + '.' + property);
        }
        return null;
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        switch (property) {
            case MIN:
                return "Minimum";
            case MAX:
                return "Maximum";
            default:
                return null;
        }
    }

    private ValidationResult validateQuantity(Quantity toValidate) {
        if (toValidate == null) {
            return SKIPPED;
        }
        return isWithinBounds(toValidate) ? PASS : SUSPECT;
    }

    private boolean isWithinBounds(Quantity toValidate) {
        return minimum.compareTo(toValidate) <= 0 && maximum.compareTo(toValidate) >= 0;
    }
}
