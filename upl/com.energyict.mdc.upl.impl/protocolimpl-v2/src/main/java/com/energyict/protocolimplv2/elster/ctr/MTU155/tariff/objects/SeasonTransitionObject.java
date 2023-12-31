package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 14:07
 */
public class SeasonTransitionObject implements Serializable {

    private Date startDate;

    public SeasonTransitionObject() {

    }

    public static SeasonTransitionObject fromSeasonTransition(TariffCalendarExtractor.CalendarSeasonTransition transition) {
        SeasonTransitionObject sto = new SeasonTransitionObject();
        sto.setStartDate(transition.start().map(SeasonTransitionObject::toDate).orElse(null));
        return sto;
    }

    private static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public Date getStartDate() {
        return startDate;
    }

    public Calendar getStartCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getStartDate());
        return calendar;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "SeasonTransitionObject{startDate=" + startDate + '}';
    }
}
