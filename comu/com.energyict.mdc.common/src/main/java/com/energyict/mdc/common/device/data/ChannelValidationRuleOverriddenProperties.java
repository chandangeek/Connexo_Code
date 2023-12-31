/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationAction;

import aQute.bnd.annotation.ConsumerType;

import java.util.Map;

@ConsumerType
public interface ChannelValidationRuleOverriddenProperties {

    long getId();

    long getVersion();

    Device getDevice();

    ReadingType getReadingType();

    String getValidationRuleName();

    String getValidatorImpl();

    ValidationAction getValidationAction();

    Map<String, Object> getProperties();

    void setProperties(Map<String, Object> properties);

    void update();

    void delete();

}