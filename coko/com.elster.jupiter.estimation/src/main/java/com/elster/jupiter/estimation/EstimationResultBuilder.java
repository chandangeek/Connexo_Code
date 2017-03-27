/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;


import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface EstimationResultBuilder {
    void addRemaining(EstimationBlock block);

    void addEstimated(EstimationBlock block);

    EstimationResult build();
}
