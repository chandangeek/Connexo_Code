package com.energyict.mdc.device.data;

import com.elster.jupiter.estimation.EstimationRuleSet;

public interface DeviceEstimationRuleSetActivation {
    
    boolean isActive();
    
    void setActive(boolean active);
    
    EstimationRuleSet getEstimationRuleSet();
    
    DeviceEstimation getDeviceEstimationActivation();

}
