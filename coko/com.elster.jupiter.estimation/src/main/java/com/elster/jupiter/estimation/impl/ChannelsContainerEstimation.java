/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.util.HasId;

/**
 * Manages rule set activation
 */
public interface ChannelsContainerEstimation extends HasId {

    boolean isActive();

    void setActive(boolean active);

    EstimationRuleSet getEstimationRuleSet();

    ChannelsContainer getChannelsContainer();

    void save();
}
