/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.estimation.EstimationRuleSet;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface DeviceConfigurationEstimationRuleSetUsage {

    DeviceConfiguration getDeviceConfiguration();

    EstimationRuleSet getEstimationRuleSet();

    boolean isRuleSetActive();

    void setRuleSetStatus(boolean active);

}
