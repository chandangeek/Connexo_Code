package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import org.codehaus.jackson.annotate.JsonProperty;

public class ComTaskFrequencyInfo {
    @JsonProperty("temporalExpression")
    public TemporalExpressionInfo temporalExpression;
}
