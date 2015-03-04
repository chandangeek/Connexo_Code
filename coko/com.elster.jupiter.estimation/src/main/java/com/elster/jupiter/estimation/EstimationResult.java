package com.elster.jupiter.estimation;

import java.util.List;

public interface EstimationResult {

    List<EstimationBlock> remainingToBeEstimated();

    List<EstimationBlock> estimated();

}
