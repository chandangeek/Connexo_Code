package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.energyict.mdc.upl.messages.legacy.Extractor;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 13:44
 */
public class SeasonObject implements Serializable {

    private String name;
    private int id;
    private List<SeasonTransitionObject> transitions;

    public SeasonObject() {
    }

    public static SeasonObject fromSeason(Extractor.CalendarSeason uplSeason) {
        SeasonObject season = new SeasonObject();
        season.setId(uplSeason.id());
        season.setName(uplSeason.name());
        season.setTransitions(uplSeason.transistions().stream().map(SeasonTransitionObject::fromSeasonTransition).collect(Collectors.toList()));
        return season;
    }

    public List<SeasonTransitionObject> getTransitions() {
        return transitions;
    }

    public List<SeasonTransitionObject> getTransitionsPerYear(int year) {
        return this.transitions
                .stream()
                .filter(transition -> transition.getStartCalendar().get(Calendar.YEAR) == year)
                .collect(Collectors.toList());
    }

    public List<SeasonTransitionObject> getTransitionsForCurrentYear() {
        return getTransitionsPerYear(Calendar.getInstance().get(Calendar.YEAR));
    }

    public void setTransitions(List<SeasonTransitionObject> transitions) {
        this.transitions = transitions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private void setId(String id) {
        this.setId(Integer.parseInt(id));
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
        if (transitions.isEmpty()) {
            return 0;
        } else {
            return transitions.get(0).getStartCalendar().get(Calendar.MONTH) + 1;
        }
    }

    public int getStartDay(int year) {
        List<SeasonTransitionObject> transitions = getTransitionsPerYear(year);
        if (transitions.isEmpty()) {
            return 0;
        } else {
            return transitions.get(0).getStartCalendar().get(Calendar.DAY_OF_MONTH);
        }
    }

}
