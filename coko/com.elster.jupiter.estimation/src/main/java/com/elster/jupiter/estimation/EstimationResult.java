/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface EstimationResult {

    List<EstimationBlock> remainingToBeEstimated();

    List<EstimationBlock> estimated();

}
