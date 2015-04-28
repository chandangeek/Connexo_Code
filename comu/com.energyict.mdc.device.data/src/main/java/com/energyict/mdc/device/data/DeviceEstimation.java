package com.energyict.mdc.device.data;

import java.util.List;

import com.elster.jupiter.estimation.EstimationRuleSet;

public interface DeviceEstimation {

    boolean isEstimationActive();

    void activateEstimation();

    void deactivateEstimation();

    List<DeviceEstimationRuleSetActivation> getEstimationRuleSetActivations();

    void activateEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    void deactivateEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    void save();

}
