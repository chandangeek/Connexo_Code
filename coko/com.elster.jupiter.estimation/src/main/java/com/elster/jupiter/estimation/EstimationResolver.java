package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;

import java.util.List;

public interface EstimationResolver {

    boolean isEstimationActive(Meter meter);

    List<EstimationRuleSet> resolve(MeterActivation meterActivation);

    boolean isInUse(EstimationRuleSet estimationRuleSet);

    Priority getPriority();
}
