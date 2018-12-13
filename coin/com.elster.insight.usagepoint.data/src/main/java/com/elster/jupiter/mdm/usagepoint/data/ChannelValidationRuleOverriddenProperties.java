/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.validation.ValidationAction;

import aQute.bnd.annotation.ProviderType;

import java.util.Map;

@ProviderType
public interface ChannelValidationRuleOverriddenProperties {

    long getId();

    long getVersion();

    UsagePoint getUsagePoint();

    ReadingType getReadingType();

    String getValidationRuleName();

    String getValidatorImpl();

    ValidationAction getValidationAction();

    Map<String, Object> getProperties();

    void setProperties(Map<String, Object> properties);

    void update();

    void delete();

}
