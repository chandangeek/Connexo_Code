/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SeasonObject implements Serializable {

    private String name;
    private long id;
    private List<SeasonTransitionObject> transitions;

    public SeasonObject() {
    }

    public static SeasonObject from(Period period) {
        SeasonObject so = new SeasonObject();
        so.setId(period.getId());
        so.setName(period.getName());
        so.setTransitions(
                transitionsTo(period)
                        .map(trans -> SeasonTransitionObject.from(trans, period.getCalendar()))
                        .collect(Collectors.toList()));
        return so;
    }

    private static Stream<PeriodTransition> transitionsTo(Period period) {
        return period.getCalendar()
                .getTransitions()
                .stream()
                .filter(transition -> transition.getPeriod().equals(period));
    }

    public List<SeasonTransitionObject> getTransitions() {
        return transitions;
    }

    public List<SeasonTransitionObject> getTransitionsPerYear(int year) {
        List<SeasonTransitionObject> transitionsForYear = new ArrayList<>();
        for (SeasonTransitionObject transition : transitions) {
            if (transition.getStart().getYear() == year) {
                transitionsForYear.add(transition);
            }
        }
        return transitionsForYear;
    }

    public void setTransitions(List<SeasonTransitionObject> transitions) {
        this.transitions = transitions;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPeriod(int period) {
        return (getName() != null) && getName().contains("" + period);
    }

    @Override
    public String toString() {
        return "SeasonObject" +
               "{id=" + id +
               ", name='" + name + '\'' +
               ", transitions=" + transitions +
               '}';
    }

    public int getStartMonth(int year) {
        List<SeasonTransitionObject> transitions = getTransitionsPerYear(year);
        return transitions.isEmpty() ? 0 : transitions.get(0).getStart().getMonthValue();
    }

    public int getStartDay(int year) {
        List<SeasonTransitionObject> transitions = getTransitionsPerYear(year);
        return transitions.isEmpty() ? 0 : transitions.get(0).getStart().getDayOfMonth();
    }

}
