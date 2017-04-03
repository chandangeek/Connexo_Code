/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.properties;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationRule;

import aQute.bnd.annotation.ConsumerType;

import java.util.Map;

@ConsumerType
public interface ValidationPropertyProvider {

    /**
     * Provides property values that are defined for specified {@link ValidationRule} and channel's {@link ReadingType}
     *
     * @param validationRule target {@link ValidationRule}
     * @param readingType {@link ReadingType} of target channel
     * @return a Map in which key is a validation rule's property name and value is a property value
     */
    Map<String, Object> getProperties(ValidationRule validationRule, ReadingType readingType);

}
