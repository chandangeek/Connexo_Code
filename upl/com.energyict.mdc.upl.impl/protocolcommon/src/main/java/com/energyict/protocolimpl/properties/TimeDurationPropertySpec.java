package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.cbo.TimeDuration;

import java.util.Optional;

/**
 * Provides an implementation for the PropertySpec interface for 'TimeDurations'
 */
class TimeDurationPropertySpec extends AbstractPropertySpec<TimeDuration> {

    private final boolean onlySmallUnits;
    private Optional<TimeDuration> defaultValue = Optional.empty();

    TimeDurationPropertySpec(String name, boolean required) {
        super(name, required);
        this.onlySmallUnits = false;
    }

    public TimeDurationPropertySpec(String name, boolean required, boolean onlySmallUnits) {
        super(name, required);
        this.onlySmallUnits = onlySmallUnits;
    }

    public void setDefaultValue(TimeDuration defaultValue) {
        this.defaultValue = Optional.ofNullable(defaultValue);
    }

    @Override
    public Optional<TimeDuration> getDefaultValue() {
        return defaultValue;
    }

    public boolean isOnlySmallUnits() {
        return onlySmallUnits;
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        }
        if (value instanceof TimeDuration) {
            return true;
        } else if (value instanceof String) {
            try {
                TimeDuration timeDuration = new TimeDuration((String) value);
                return true;
            } catch (Exception e) {
                throw InvalidPropertyException.forNameAndValue(this.getName(), value, e);
            }
        }
        return false;
    }
}
