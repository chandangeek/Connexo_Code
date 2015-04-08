package com.elster.jupiter.estimation;

import com.elster.jupiter.properties.HasDynamicProperties;

import java.util.List;

public interface Estimator extends HasDynamicProperties {

    default void init() {
        // empty by default
    }

    EstimationResult estimate(List<EstimationBlock> estimationBlock);

    String getDisplayName();

    String getDisplayName(String property);

    String getDefaultFormat();

}
