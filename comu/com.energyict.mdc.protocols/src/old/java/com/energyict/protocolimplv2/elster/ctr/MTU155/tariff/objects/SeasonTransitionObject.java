package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.PeriodTransition;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 14:07
 */
public class SeasonTransitionObject implements Serializable {

    private long seasonId;
    private String seasonName;
    private LocalDate start;
    private Instant startDate;

    public SeasonTransitionObject() {
    }

    public static SeasonTransitionObject from(PeriodTransition transition, Calendar calendar) {
        SeasonTransitionObject sto = new SeasonTransitionObject();
        sto.setSeasonId(transition.getPeriod().getId());
        sto.setSeasonName(transition.getPeriod().getName());
        sto.setStartDate(transition.getOccurrence().atStartOfDay(ZoneId.systemDefault()).toInstant());
        return sto;
    }

    public long getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(long seasonId) {
        this.seasonId = seasonId;
    }

    public String getSeasonName() {
        return seasonName;
    }

    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "SeasonTransitionObject" +
               "{seasonId=" + seasonId +
               ", seasonName='" + seasonName + '\'' +
               ", startDate=" + startDate +
               '}';
    }

}