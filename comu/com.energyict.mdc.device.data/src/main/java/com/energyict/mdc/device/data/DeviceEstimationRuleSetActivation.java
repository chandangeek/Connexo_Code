package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.estimation.EstimationRuleSet;

@ProviderType
public interface DeviceEstimationRuleSetActivation {

    boolean isActive();

    void setActive(boolean active);

    EstimationRuleSet getEstimationRuleSet();

    DeviceEstimation getDeviceEstimationActivation();

}
