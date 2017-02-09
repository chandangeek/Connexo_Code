/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import java.util.List;

public interface EstimationResult {

    List<EstimationBlock> remainingToBeEstimated();

    List<EstimationBlock> estimated();

}
