package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.shadow.tasks.NextExecutionSpecsShadow;
import com.energyict.mdc.tasks.NextExecutionSpecs;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/07/2015 - 10:39
 */
public class CronTabStyleConverter {

    public static String convert(NextExecutionSpecs nextExecutionSpecs) {
        if (nextExecutionSpecs == null) {
            return "";
        } else if (nextExecutionSpecs.getTemporalExpression() != null) {

            final TimeDuration every = nextExecutionSpecs.getTemporalExpression().getEvery();
            final NextExecutionSpecsShadow.Offset offset = nextExecutionSpecs.getShadow().getOffsetInDaysHoursMinutes();

            switch (every.getTimeUnitCode()) {
                case TimeDuration.MONTHS:
                    return "0 " + offset.getMinutes() + " " + offset.getHours() + " " + getDayOfMonth(offset) + " */" + every.getCount() + " *";  //E.g. '0 0 6 16 */3 *' means 'every 3 months, on day 16, at 06:00:00'
                case TimeDuration.WEEKS:
                    return "0 " + offset.getMinutes() + " " + offset.getHours() + " */" + (every.getCount() * 7) + " * " + getDayOfWeek(offset);  //E.g. '0 0 6 */14 * 1' means 'every 2 weeks, on monday, at 06:00:00'
                case TimeDuration.DAYS:
                    return "0 " + offset.getMinutes() + " " + offset.getHours() + " */" + every.getCount() + " * *";  //E.g. '0 0 6 */3 * *' means 'every 3 days, at 06:00:00'
                case TimeDuration.HOURS:
                    return "0 " + offset.getMinutes() + " */" + every.getCount() + " * * *";  //E.g. '0 3 */2 * * *' means 'every 2 hours, at 00:03:00'
                case TimeDuration.MINUTES:
                    return "0 */" + every.getCount() + " * * * *";  //E.g. '0 */5 * * * *' means 'every 5 minutes'
                default:
                    return "";
            }
        } else if (nextExecutionSpecs.getDialCalendar() != null) {
            return "";    //TODO add support to convert dial calendar to crontab style
        } else {
            return "";
        }
    }

    /**
     * EIServer day is 0 based, return 1-based
     */
    private static int getDayOfMonth(NextExecutionSpecsShadow.Offset offset) {
        return offset.getDays() + 1;
    }

    /**
     * offset.getDays: 0 = monday, 1 = tuesday,... 6 = sunday
     * returns: 1 for monday, 2 for tuesday, ... 7 for sunday
     */
    private static int getDayOfWeek(NextExecutionSpecsShadow.Offset offset) {
        return offset.getDays() + 1;
    }
}