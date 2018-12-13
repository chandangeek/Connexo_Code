/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;

import aQute.bnd.annotation.ProviderType;

import java.util.Map;

@ProviderType
public interface ChannelEstimationRuleOverriddenProperties {

    long getId();

    long getVersion();

    UsagePoint getUsagePoint();

    ReadingType getReadingType();

    String getEstimationRuleName();

    String getEstimatorImpl();

    Map<String, Object> getProperties();

    void setProperties(Map<String, Object> properties);

    void update();

    void delete();

}
