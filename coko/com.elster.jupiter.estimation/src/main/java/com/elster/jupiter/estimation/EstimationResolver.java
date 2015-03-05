package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.MeterActivation;

import java.util.List;

public interface EstimationResolver {

    List<String> resolve(MeterActivation meterActivation);

    boolean isInUse(EstimationRuleSet estimationRuleSet);
}
