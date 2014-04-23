package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.scheduling.TemporalExpression;

public class NextExecutionsSpecsInfo {

    public TemporalExpressionInfo temporalExpression;

    public NextExecutionsSpecsInfo(TemporalExpression temporalExpression) {
        this.temporalExpression = TemporalExpressionInfo.from(temporalExpression);
    }

    public NextExecutionsSpecsInfo() {
    }

    public NextExecutionsSpecsInfo(TemporalExpressionInfo temporalExpression) {
        this.temporalExpression = temporalExpression;
    }

    public TemporalExpression asTemporalExpression() {
        return temporalExpression.asTemporalExpression();
    }

}
