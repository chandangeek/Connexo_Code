package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.estimation.EstimationRuleSet;

@ProviderType
public interface DeviceConfigurationEstimationRuleSetUsage {

    DeviceConfiguration getDeviceConfiguration();

    EstimationRuleSet getEstimationRuleSet();

}
