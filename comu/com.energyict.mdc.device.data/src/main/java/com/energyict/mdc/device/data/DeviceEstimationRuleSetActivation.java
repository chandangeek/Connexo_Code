package com.energyict.mdc.device.data;

import com.elster.jupiter.estimation.EstimationRuleSet;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DeviceEstimationRuleSetActivation {

    boolean isActive();

    void setActive(boolean active);

    EstimationRuleSet getEstimationRuleSet();

    Device getDevice();
}
