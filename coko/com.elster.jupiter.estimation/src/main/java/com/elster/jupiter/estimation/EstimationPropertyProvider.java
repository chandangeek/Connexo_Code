/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ConsumerType;

import java.util.Map;

@ConsumerType
public interface EstimationPropertyProvider {

    /**
     * Provides property values that are defined for specified {@link EstimationRule} and channel's {@link ReadingType}
     *
     * @param estimationRule target {@link EstimationRule}
     * @param readingType {@link ReadingType} of target channel
     * @return a Map in which key is a validation rule's property name and value is a property value
     */
    Map<String, Object> getProperties(EstimationRule estimationRule, ReadingType readingType);

}
