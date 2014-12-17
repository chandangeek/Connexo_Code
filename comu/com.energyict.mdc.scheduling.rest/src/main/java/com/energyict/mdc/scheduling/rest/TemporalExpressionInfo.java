package com.energyict.mdc.scheduling.rest;

import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

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

    public static TemporalExpressionInfo from(TemporalAmount temporalAmount) {
        TemporalExpressionInfo info = new TemporalExpressionInfo();
        info.every=new TimeDurationInfo();
        List<TemporalUnit> supportedUnits = temporalAmount.getUnits();
        if (supportedUnits!=null && !supportedUnits.isEmpty()) {
            TemporalUnit biggestUnit = supportedUnits.get(0);
            info.every.count = temporalAmount.get(biggestUnit);
            info.every.timeUnit = biggestUnit.toString().toLowerCase();
        }
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

