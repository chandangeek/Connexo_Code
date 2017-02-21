/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.estimation.EstimationRuleSet;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface DeviceEstimation {

    boolean isEstimationActive();

    void activateEstimation();

    void deactivateEstimation();

    List<DeviceEstimationRuleSetActivation> getEstimationRuleSetActivations();

    void activateEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    void deactivateEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    Device getDevice();
}
