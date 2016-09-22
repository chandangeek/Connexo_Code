package com.energyict.protocolimplv2.common.objectserialization.codetable.objects;

import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.mdw.core.CodeDayType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 11:52
 */
public class CodeObject implements Serializable {

    private int id;
    private String name;
    private String externalName;
    private int yearFrom;
    private int yearTo;
    private int interval;
    private boolean verified;
    private boolean rebuilt;
    private TimeZone destinationTimeZone;
    private TimeZone definitionTimeZone;
    private SeasonSetObject seasonSet;

    private List<CodeDayTypeObject> dayTypes = new ArrayList<>();
    private List<CodeCalendarObject> calendars = new ArrayList<>();

    public CodeObject() {
    }

    public static CodeObject fromCode(Code code) {
        CodeObject co = new CodeObject();
        co.setId(code.getId());
        co.setName(code.getName());
        co.setExternalName(code.getExternalName());
        co.setYearFrom(code.getYearFrom());
        co.setYearTo(code.getYearTo());
        co.setInterval(code.getIntervalInSeconds());
        co.setVerified(code.getVerified());
        co.setRebuilt(code.getRebuilt());
        co.setDestinationTimeZone(code.getDestinationTimeZone());
        co.setDefinitionTimeZone(code.getDefinitionTimeZone());
        co.setSeasonSet(SeasonSetObject.fromSeasonSet(code.getSeasonSet()));

        co.setDayTypes(new ArrayList<CodeDayTypeObject>());
        List<CodeDayType> dt = code.getDayTypes();
        for (CodeDayType codeDayType : dt) {
            co.getDayTypes().add(CodeDayTypeObject.fromCodeDayType(codeDayType));
        }

        co.setCalendars(new ArrayList<CodeCalendarObject>());
        List<CodeCalendar> cal = code.getCalendars();
        for (CodeCalendar codeCalendar : cal) {
            co.getCalendars().add(CodeCalendarObject.fromCodeCalendar(codeCalendar));
        }

        return co;
    }

    public List<CodeCalendarObject> getCalendars() {
        return calendars;
    }

    public void setCalendars(List<CodeCalendarObject> calendars) {
        this.calendars = calendars;
    }

    public List<CodeCalendarObject> getSpecialDayCalendars() {
        List<CodeCalendarObject> specialDays = new ArrayList<>();
        for (CodeCalendarObject co : calendars) {
            if (co.isSpecialDay() && !specialDays.contains(co)) {
                specialDays.add(co);
            }
        }
        return specialDays;
    }

    public List<CodeDayTypeObject> getDayTypes() {
        return dayTypes;
    }

    public void setDayTypes(List<CodeDayTypeObject> dayTypes) {
        this.dayTypes = dayTypes;
    }

    public TimeZone getDefinitionTimeZone() {
        return definitionTimeZone;
    }

    public void setDefinitionTimeZone(TimeZone definitionTimeZone) {
        this.definitionTimeZone = definitionTimeZone;
    }

    public TimeZone getDestinationTimeZone() {
        return destinationTimeZone;
    }

    public void setDestinationTimeZone(TimeZone destinationTimeZone) {
        this.destinationTimeZone = destinationTimeZone;
    }

    public String getExternalName() {
        return externalName;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRebuilt() {
        return rebuilt;
    }

    public void setRebuilt(boolean rebuilt) {
        this.rebuilt = rebuilt;
    }

    public SeasonSetObject getSeasonSet() {
        return seasonSet;
    }

    public void setSeasonSet(SeasonSetObject seasonSet) {
        this.seasonSet = seasonSet;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(int yearFrom) {
        this.yearFrom = yearFrom;
    }

    public int getYearTo() {
        return yearTo;
    }

    public void setYearTo(int yearTo) {
        this.yearTo = yearTo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CodeObject");
        sb.append("{calendars=").append(calendars);
        sb.append(", id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", externalName='").append(externalName).append('\'');
        sb.append(", yearFrom=").append(yearFrom);
        sb.append(", yearTo=").append(yearTo);
        sb.append(", interval=").append(interval);
        sb.append(", verified=").append(verified);
        sb.append(", rebuilt=").append(rebuilt);
        sb.append(", destinationTimeZone=").append(destinationTimeZone);
        sb.append(", definitionTimeZone=").append(definitionTimeZone);
        sb.append(", seasonSet=").append(seasonSet);
        sb.append(", dayTypes=").append(dayTypes);
        sb.append('}');
        return sb.toString();
    }
}
