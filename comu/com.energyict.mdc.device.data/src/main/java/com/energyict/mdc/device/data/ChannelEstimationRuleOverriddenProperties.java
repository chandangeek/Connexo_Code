/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;

import java.util.Map;

@ProviderType
public interface ChannelEstimationRuleOverriddenProperties {

    long getId();

    long getVersion();

    Device getDevice();

    ReadingType getReadingType();

    String getEstimationRuleName();

    String getEstimatorImpl();

    Map<String, Object> getProperties();

    void setProperties(Map<String, Object> properties);

    void update();

    void delete();

}