/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.estimation.EstimationRuleSet;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface DeviceEstimationRuleSetActivation {

    boolean isActive();

    void setActive(boolean active);

    EstimationRuleSet getEstimationRuleSet();

    Device getDevice();
}
