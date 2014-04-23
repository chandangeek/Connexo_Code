package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.scheduling.TemporalExpression;

public class NextExecutionsSpecsInfo {

    public TemporalExpressionInfo temporalExpression;

    public NextExecutionsSpecsInfo(TemporalExpression temporalExpression) {
        this.temporalExpression=new TemporalExpressionInfo();
        this.temporalExpression.every=new TimeDurationInfo(temporalExpression.getEvery());
        this.temporalExpression.offset=new TimeDurationInfo(temporalExpression.getOffset());
        this.temporalExpression.lastDay=temporalExpression.isLastDay();
    }

    public NextExecutionsSpecsInfo() {
    }

    public NextExecutionsSpecsInfo(TemporalExpressionInfo temporalExpression) {
        this.temporalExpression = temporalExpression;
    }

    public TemporalExpression asTemporalExpression() {
        if (temporalExpression.every==null) {
            return null;
        } else {
            TemporalExpression temporalExpression;
            if (this.temporalExpression.offset==null) {
                temporalExpression = new TemporalExpression(this.temporalExpression.every.asTimeDuration());
            }
            else {
                temporalExpression = new TemporalExpression(this.temporalExpression.every.asTimeDuration(), this.temporalExpression.offset.asTimeDuration());
            }
            temporalExpression.setLastDay(this.temporalExpression.lastDay);
            return temporalExpression;
        }
    }

    public class TemporalExpressionInfo {
        public TimeDurationInfo every;
        public TimeDurationInfo offset;
        public boolean lastDay;
    }

}
