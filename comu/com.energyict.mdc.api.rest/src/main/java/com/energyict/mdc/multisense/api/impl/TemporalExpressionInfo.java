package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.function.Function;

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

    /**
     * This method wraps the passed time value in the REST info object {@link TemporalExpressionInfo}.
     * @param temporalAmount time which will be wrapped, should have a single-unit value, i.e
     * time value '1 day 30 min' will be converted to '1 day' (the biggest dimension will be used)
     * @return the info wrapper which can be send to front-end
     */
    public static TemporalExpressionInfo from(TemporalAmount temporalAmount) {
        TemporalExpressionInfo info = new TemporalExpressionInfo();
        if (temporalAmount instanceof Duration){
            /* Special case for Duration class, because it supports only two units: seconds and nanos */
            info.every = new TimeDurationInfo();
            Duration duration = (Duration) temporalAmount;
            fromDuration(info.every, duration, Duration::toDays, ChronoUnit.DAYS);
            fromDuration(info.every, duration, Duration::toHours, ChronoUnit.HOURS);
            fromDuration(info.every, duration, Duration::toMinutes, ChronoUnit.MINUTES);
        } else {
            info.every = fromTemporalAmount(temporalAmount);
        }
        return info;
    }

    private static void fromDuration(TimeDurationInfo info, Duration duration, Function<Duration, Long> conv, TemporalUnit unit){
        if (info != null && info.count == 0){
            info.count = conv.apply(duration);
            info.timeUnit = unit.toString().toLowerCase();
        }
    }

    private static void fromTemporalAmount(TimeDurationInfo info, TemporalAmount temporalAmount, TemporalUnit unit){
        if (info != null && info.count == 0){
            info.count = temporalAmount.get(unit);
            info.timeUnit = unit.toString().toLowerCase();
        }
    }
    private static TimeDurationInfo fromTemporalAmount(TemporalAmount temporalAmount){
        TimeDurationInfo durationInfo = new TimeDurationInfo();
        for (TemporalUnit unit : temporalAmount.getUnits()) {
            fromTemporalAmount(durationInfo, temporalAmount, unit);
        }
        return durationInfo;
    }

    public TemporalExpression asTemporalExpression() {
        if (this.every==null) {
            return null;
        } else {
            TemporalExpression temporalExpression;
            if (this.offset==null) {
                try {
                    temporalExpression = new TemporalExpression(this.every.asTimeDuration());
                } catch (LocalizedFieldValidationException e) {
                    throw e.fromSubField("every");
                }
            } else {
                TimeDuration every;
                try {
                    every = this.every.asTimeDuration();
                } catch (LocalizedFieldValidationException e) {
                    throw e.fromSubField("every");
                }
                try {
                    temporalExpression = new TemporalExpression(every, this.offset.asTimeDuration());
                } catch (LocalizedFieldValidationException e) {
                    throw e.fromSubField("offset");
                }
            }
            if (this.lastDay) {
                temporalExpression.setLastDay();
            }
            return temporalExpression;
        }
    }
}

