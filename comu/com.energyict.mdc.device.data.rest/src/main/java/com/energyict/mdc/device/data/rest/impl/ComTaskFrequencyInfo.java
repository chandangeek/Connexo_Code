package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ComTaskFrequencyInfo {
    @JsonProperty("temporalExpression")
    public TemporalExpressionInfo temporalExpression;
}
