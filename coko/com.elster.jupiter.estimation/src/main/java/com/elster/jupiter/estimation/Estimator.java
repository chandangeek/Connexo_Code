package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.google.common.collect.Range;

import java.time.Instant;

public interface Estimator extends HasDynamicProperties {

    void init(Channel channel, ReadingType readingType, Range<Instant> interval);

    EstimationResult estimate(EstimationBlock estimationBlock);

    String getDisplayName();

    String getDisplayName(String property);

    String getDefaultFormat();

}
