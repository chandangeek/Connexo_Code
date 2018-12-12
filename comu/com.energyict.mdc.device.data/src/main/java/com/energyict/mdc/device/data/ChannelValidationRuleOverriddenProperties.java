/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationAction;

import java.util.Map;

@ProviderType
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