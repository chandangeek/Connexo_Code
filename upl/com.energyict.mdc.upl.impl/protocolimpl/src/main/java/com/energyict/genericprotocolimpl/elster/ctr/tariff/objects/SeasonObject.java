package com.energyict.genericprotocolimpl.elster.ctr.tariff.objects;

import com.energyict.mdw.core.Season;
import com.energyict.mdw.core.SeasonTransition;

import java.io.Serializable;
import java.util.*;

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

    public static SeasonObject fromSeason(Season season) {
        SeasonObject so = new SeasonObject();
        so.setId(season.getId());
        so.setName(season.getName());
        so.setTransitions(new ArrayList<SeasonTransitionObject>());
        List<SeasonTransition> eis = season.getTransitions();
        for (SeasonTransition trans : eis) {
            so.getTransitions().add(SeasonTransitionObject.fromSeasonTransition(trans));
        }
        return so;
    }

    public List<SeasonTransitionObject> getTransitions() {
        return transitions;
    }

    public List<SeasonTransitionObject> getTransitionsPerYear(int year) {
        List<SeasonTransitionObject> transitionsForYear = new ArrayList<SeasonTransitionObject>();
        for (SeasonTransitionObject transition : transitions) {
            if (transition.getStartCalendar().get(Calendar.YEAR) == year) {
                transitionsForYear.add(transition);
            }
        }
        return transitionsForYear;
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
        final StringBuilder sb = new StringBuilder();
        sb.append("SeasonObject");
        sb.append("{id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", transitions=").append(transitions);
        sb.append('}');
        return sb.toString();
    }

    public int getStartMonth(int year) {
        List<SeasonTransitionObject> transitions = getTransitionsPerYear(year);
        return transitions.isEmpty() ? 0 : transitions.get(0).getStartCalendar().get(Calendar.MONTH) + 1;
    }

    public int getStartDay(int year) {
        List<SeasonTransitionObject> transitions = getTransitionsPerYear(year);
        return transitions.isEmpty() ? 0 : transitions.get(0).getStartCalendar().get(Calendar.DAY_OF_MONTH);
    }

}
