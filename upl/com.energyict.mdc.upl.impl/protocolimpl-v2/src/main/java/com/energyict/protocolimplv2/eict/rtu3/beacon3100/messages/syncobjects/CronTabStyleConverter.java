package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.mdc.shadow.tasks.NextExecutionSpecsShadow;
import com.energyict.mdc.tasks.NextExecutionSpecs;

import com.energyict.cbo.TimeDuration;
import com.energyict.protocol.exceptions.DeviceConfigurationException;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/07/2015 - 10:39
 */
public class CronTabStyleConverter {

    public static String convert(NextExecutionSpecs nextExecutionSpecs, TimeZone timeZone, TimeZone localTimezone) {
        if (nextExecutionSpecs == null) {
            return "";
        } else if (nextExecutionSpecs.getTemporalExpression() != null) {

            final TimeDuration every = nextExecutionSpecs.getTemporalExpression().getEvery();
            final NextExecutionSpecsShadow.Offset offset = nextExecutionSpecs.getShadow().getOffsetInDaysHoursMinutes();

            Calendar localTime = Calendar.getInstance();
            localTime.setTimeZone(localTimezone);
            if(every.getTimeUnitCode() == TimeDuration.MONTHS){
                localTime.set(Calendar.DAY_OF_MONTH, offset.getDays() + 1); //+1 because EiServer has 0-based days
            } else if(every.getTimeUnitCode() == TimeDuration.WEEKS){
                localTime.set(Calendar.DAY_OF_WEEK, offset.getDays() + 1);
            }
            localTime.set(Calendar.HOUR_OF_DAY, offset.getHours());
            localTime.set(Calendar.MINUTE, offset.getMinutes());

            //convert localTime to beaconTime
            Calendar beaconCalendar = Calendar.getInstance();
            beaconCalendar.setTimeZone(timeZone);
            beaconCalendar.setTimeInMillis(localTime.getTimeInMillis());
            int dayOfMonth = beaconCalendar.get(Calendar.DAY_OF_MONTH);
            int dayOfWeek = beaconCalendar.get(Calendar.DAY_OF_WEEK);
            int hours = beaconCalendar.get(Calendar.HOUR_OF_DAY);
            int minutes = beaconCalendar.get(Calendar.MINUTE);

            switch (every.getTimeUnitCode()) {
                case TimeDuration.MONTHS:
                    return "0 " + minutes + " " + hours + " " + dayOfMonth + " */" + every.getCount() + " *";  //E.g. '0 0 6 16 */3 *' means 'every 3 months, on day 16, at 06:00:00'
                case TimeDuration.WEEKS:
                    return "0 " + minutes + " " + hours + " */" + (every.getCount() * 7) + " * " + dayOfWeek;  //E.g. '0 0 6 */14 * 1' means 'every 2 weeks, on monday, at 06:00:00'
                case TimeDuration.DAYS:
                    return "0 " + minutes + " " + hours + " */" + every.getCount() + " * *";  //E.g. '0 0 6 */3 * *' means 'every 3 days, at 06:00:00'
                case TimeDuration.HOURS:
                    return "0 " + offset.getMinutes() + " */" + every.getCount() + " * * *";  //E.g. '0 3 */2 * * *' means 'every 2 hours, at 00:03:00'
                case TimeDuration.MINUTES:
                    return "0 */" + every.getCount() + " * * * *";  //E.g. '0 */5 * * * *' means 'every 5 minutes'
                default:
                    return "";
            }
        } else if (nextExecutionSpecs.getDialCalendar() != null) {
            throw DeviceConfigurationException.invalidPropertyFormat("Comtask schedule", "Read schedule with ID " + String.valueOf(nextExecutionSpecs.getDialCalendar().getId()), "A read schedule (dial calendar) is not supported by this message");
        } else {
            return "";
        }
    }

}