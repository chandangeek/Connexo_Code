package com.elster.jupiter.export.rest;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

public class TemporalExpressionInfo {
    public TimeDurationInfo every;
    public TimeDurationInfo offset;
    public boolean lastDay;

    public TemporalExpressionInfo() {
    }

    public static TemporalExpressionInfo from(TemporalExpression temporalExpression) {
        TemporalExpressionInfo info = new TemporalExpressionInfo();
        info.every = new TimeDurationInfo(temporalExpression.getEvery());
        info.offset = new TimeDurationInfo(temporalExpression.getOffset());
        info.lastDay = temporalExpression.isLastDay();
        return info;
    }

    public ScheduleExpression asScheduleExpression() {
        if (this.every == null) {
            return Never.NEVER;
        }
        return buildTemporalExpression();
    }

    private ScheduleExpression buildTemporalExpression() {
        TemporalExpression temporalExpression;
        if (this.offset == null) {
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

