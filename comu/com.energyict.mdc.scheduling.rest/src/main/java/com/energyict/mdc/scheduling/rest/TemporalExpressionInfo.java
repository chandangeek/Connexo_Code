package com.energyict.mdc.scheduling.rest;

import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.elster.jupiter.time.TemporalExpression;

public class TemporalExpressionInfo {
    public TimeDurationInfo every;
    public TimeDurationInfo offset;
    public boolean lastDay;

    public TemporalExpressionInfo() {
    }

    public static TemporalExpressionInfo from(TemporalExpression temporalExpression) {
        TemporalExpressionInfo info = new TemporalExpressionInfo();
        info.every=new TimeDurationInfo(temporalExpression.getEvery());
        info.offset=new TimeDurationInfo(temporalExpression.getOffset().getSeconds());
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
            if (this.lastDay) {
                temporalExpression.setLastDay();
            }
            return temporalExpression;
        }
    }
}

