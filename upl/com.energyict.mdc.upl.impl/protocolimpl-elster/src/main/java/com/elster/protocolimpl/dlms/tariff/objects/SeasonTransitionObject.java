package com.elster.protocolimpl.dlms.tariff.objects;

import com.energyict.mdw.core.SeasonTransition;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 14:07
 */
public class SeasonTransitionObject implements Serializable {

    private int seasonId;
    private String seasonName;
    private Date startDate;

    public SeasonTransitionObject() {

    }

    public static SeasonTransitionObject fromSeasonTransition(SeasonTransition trans) {
        SeasonTransitionObject sto = new SeasonTransitionObject();
        sto.setSeasonId(trans.getSeasonId());
        sto.setSeasonName(trans.getSeason().getName());
        sto.setStartDate(trans.getStartDate() != null ? new Date(trans.getStartDate().getTime()) : null);
        return sto;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public String getSeasonName() {
        return seasonName;
    }

    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
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
        final StringBuilder sb = new StringBuilder();
        sb.append("SeasonTransitionObject");
        sb.append("{seasonId=").append(seasonId);
        sb.append(", seasonName='").append(seasonName).append('\'');
        sb.append(", startDate=").append(startDate);
        sb.append('}');
        return sb.toString();
    }
}
