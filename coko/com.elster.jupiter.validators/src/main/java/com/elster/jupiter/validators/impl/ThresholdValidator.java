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
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MessageSeeds;
import com.elster.jupiter.validators.MissingRequiredProperty;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.validation.ValidationResult.*;

class ThresholdValidator extends AbstractValidator {

    private static final String MIN = "minimum";
    private static final String MAX = "maximum";
    public static final String BASE_KEY = ThresholdValidator.class.getName();
    private final Map<String, Object> properties;

    private Quantity minimum;
    private Quantity maximum;
    private ReadingType readingType;

    ThresholdValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
        this.properties = Collections.emptyMap();
    }

    public ThresholdValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        super(thesaurus, propertySpecService);
        checkProperty(MIN, properties);
        checkProperty(MAX, properties);
        this.properties = properties;
    }

    private void checkProperty(String propertyName, Map<String, Object> properties) {
        if (!properties.containsKey(propertyName)) {
            throw new MissingRequiredProperty(getThesaurus(), propertyName);
        }

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
