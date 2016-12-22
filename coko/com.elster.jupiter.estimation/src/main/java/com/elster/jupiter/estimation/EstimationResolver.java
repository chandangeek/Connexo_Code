package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface EstimationResolver {

    boolean isEstimationActive(Meter meter);

    List<EstimationRuleSet> resolve(ChannelsContainer channelsContainer);

    boolean isInUse(EstimationRuleSet estimationRuleSet);

    Priority getPriority();
}
