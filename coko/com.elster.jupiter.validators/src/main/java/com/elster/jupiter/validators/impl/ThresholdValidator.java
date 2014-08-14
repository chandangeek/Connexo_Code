package com.elster.jupiter.validators.impl;

import static com.elster.jupiter.validation.ValidationResult.PASS;
import static com.elster.jupiter.validation.ValidationResult.SKIPPED;
import static com.elster.jupiter.validation.ValidationResult.SUSPECT;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MissingRequiredProperty;
import com.google.common.collect.ImmutableList;

class ThresholdValidator extends AbstractValidator {

    static final String MIN = "minimum";
    static final String MAX = "maximum";

    private Quantity minimum;
    private Quantity maximum;
    private ReadingType readingType;

    ThresholdValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    ThresholdValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService, properties);
    }

    private Quantity getRequiredQuantity(Map<String, Object> properties, String key, ReadingType readingType) {
        BigDecimal quantity = (BigDecimal) properties.get(key);
        if (quantity == null) {
            throw new MissingRequiredProperty(getThesaurus(), key);
        }
        return readingType.getUnit().getUnit().amount(quantity, readingType.getMultiplier().getMultiplier());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().bigDecimalPropertySpec(MIN, true, BigDecimal.ZERO));
        builder.add(getPropertySpecService().bigDecimalPropertySpec(MAX, true, BigDecimal.ZERO));

        return builder.build();
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Interval interval) {
        this.readingType = readingType;
        this.minimum = getRequiredQuantity(properties, MIN, readingType);
        this.maximum = getRequiredQuantity(properties, MAX, readingType);
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
    public String getDefaultFormat() {
        return "Threshold violation";
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
    
    @Override
    public List<String> getRequiredProperties() {
        return Arrays.asList(MIN, MAX);
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
