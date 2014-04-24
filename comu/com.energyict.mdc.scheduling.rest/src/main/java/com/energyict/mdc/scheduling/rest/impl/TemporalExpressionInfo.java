package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.scheduling.TemporalExpression;

public class TemporalExpressionInfo {
    public TimeDurationInfo every;
    public TimeDurationInfo offset;
    public boolean lastDay;

    public TemporalExpressionInfo() {
    }

    public static TemporalExpressionInfo from(TemporalExpression temporalExpression) {
        TemporalExpressionInfo info = new TemporalExpressionInfo();
        info.every=new TimeDurationInfo(temporalExpression.getEvery());
        info.offset=new TimeDurationInfo(temporalExpression.getOffset());
        info.lastDay=temporalExpression.isLastDay();
        return info;
    }

    public TemporalExpression asTemporalExpression() {
        if (this.every==null) {
            return null;
        } else {
            TemporalExpression temporalExpression;
            if (this.offset==null) {
                temporalExpression = new TemporalExpression(this.every.asTimeDuration());
            } else {
                temporalExpression = new TemporalExpression(this.every.asTimeDuration(), this.offset.asTimeDuration());
            }
            temporalExpression.setLastDay(this.lastDay);
            return temporalExpression;
        }
    }
}

