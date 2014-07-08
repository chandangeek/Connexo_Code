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
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MessageSeeds;
import com.elster.jupiter.validators.MissingRequiredProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class ThresholdValidator implements IValidator {

    private static final String MIN = "minimum";
    private static final String MAX = "maximum";
    public static final String BASE_KEY = "com.elster.jupiter.validators.impl.ThresholdValidator";

    private Quantity minimum;
    private Quantity maximum;
    private ReadingType readingType;
    private final Thesaurus thesaurus;

    ThresholdValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ThresholdValidator(Thesaurus thesaurus, Map<String, Quantity> properties) {
        this.thesaurus = thesaurus;
        Quantity min = getRequiredQuantity(properties, MIN);
        Quantity max = getRequiredQuantity(properties, MAX);
        minimum = min;
        maximum = max;
    }

    private Quantity getRequiredQuantity(Map<String, Quantity> properties, String key) {
        Quantity quantity = properties.get(key);
        if (quantity == null) {
            throw new MissingRequiredProperty(thesaurus, key);
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
        return thesaurus.getString(getNlsKey().getKey(), getDefaultFormat());
    }

    @Override
    public String getDisplayName(String property) {
        return thesaurus.getString(getPropertyNlsKey(property).getKey(), getPropertyDefaultFormat(property));
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
        if (getRequiredKeys().contains(property) || getOptionalKeys().contains(property)) {
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
        return isWithinBounds(toValidate) ? ValidationResult.PASS : ValidationResult.SUSPECT;
    }

    private boolean isWithinBounds(Quantity toValidate) {
        return minimum.compareTo(toValidate) <= 0 && maximum.compareTo(toValidate) >= 0;
    }
}
