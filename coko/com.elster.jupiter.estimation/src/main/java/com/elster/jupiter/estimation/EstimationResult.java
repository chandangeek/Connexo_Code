/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface EstimationResult {

    List<EstimationBlock> remainingToBeEstimated();

    List<EstimationBlock> estimated();

}
