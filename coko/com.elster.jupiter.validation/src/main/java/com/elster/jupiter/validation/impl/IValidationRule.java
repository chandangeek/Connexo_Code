package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationRule;

import java.util.Date;

public interface IValidationRule extends ValidationRule {

    Date validateChannel(Channel channel, Interval interval);

    void delete();

    void save();

    void toggleActivation();

    void clearReadingTypes();

    PropertySpec<?> getPropertySpec(String name);
}
