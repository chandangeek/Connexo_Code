/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.estimation.EstimationRuleSet;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DeviceConfigurationEstimationRuleSetUsage {

    DeviceConfiguration getDeviceConfiguration();

    EstimationRuleSet getEstimationRuleSet();

}
