/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.Validator;

import java.util.Map;

public interface IValidationRule extends ValidationRule {

    void delete();

    void save();

    void toggleActivation();

    void clearReadingTypes();

    PropertySpec getPropertySpec(String name);

    void rename(String name);

    void setAction(ValidationAction action);

    void setPosition(int position);

    void setProperties(Map<String, Object> map);

    ReadingQualityType getReadingQualityType();

    Validator createNewValidator(ChannelsContainer channelsContainer, ReadingType readingType);

    boolean appliesTo(Channel channel);
}
