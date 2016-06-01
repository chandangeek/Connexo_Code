package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.elster.jupiter.calendar.Calendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 11:52
 */
public class CodeObject implements Serializable {

    private long id;
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

    public static CodeObject from(Calendar calendar) {
        CodeObject co = new CodeObject();
        co.setId(calendar.getId());
        co.setName(calendar.getName());
        co.setExternalName(null);
        co.setYearFrom(calendar.getStartYear().getValue());
        co.setYearTo(calendar.getEndYear().getValue());
        co.setInterval(900);
        co.setVerified(true);
        co.setRebuilt(true);
        co.setDestinationTimeZone(calendar.getTimeZone());
        co.setDefinitionTimeZone(calendar.getTimeZone());
        co.setSeasonSet(SeasonSetObject.from(calendar));
        co.setDayTypes(calendar.getDayTypes().stream().map(CodeDayTypeObject::from).collect(Collectors.toList()));
        co.setCalendars(CodeCalendarObject.allFrom(calendar));
        return co;
    }

    public List<CodeCalendarObject> getCalendars() {
        return calendars;
    }

    public List<CodeCalendarObject> getHolidayCalendars() {
        List<CodeCalendarObject> holidays = new ArrayList<>();
        for (CodeCalendarObject co : calendars) {
            if (co.isHoliday() && !holidays.contains(co)) {
                holidays.add(co);
            }
        }
        return holidays;
    }

    public List<CodeCalendarObject> getCustomDayCalendars() {
        List<CodeCalendarObject> customDays = new ArrayList<>();
        for (CodeCalendarObject co : calendars) {
            if (co.isCustomDay() && !customDays.contains(co)) {
                customDays.add(co);
            }
        }
        return customDays;
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

    public void setCalendars(List<CodeCalendarObject> calendars) {
        this.calendars = calendars;
    }

    public List<CodeDayTypeObject> getDayTypes() {
        return dayTypes;
    }

    public CodeDayTypeObject getWeekday(int period) throws IllegalStateException {
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isWeekday() && dayType.isPeriod(period)) {
                return dayType;
            }
        }
        throw new IllegalStateException("No weekday found for period [" + period + "]!");
    }

    public CodeDayTypeObject getSaturday(int period) throws IllegalStateException {
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isSaturday() && dayType.isPeriod(period)) {
                return dayType;
            }
        }
        throw new IllegalStateException("No saturday found for period [" + period + "]!");
    }

    public CodeDayTypeObject getHoliday(int period) throws IllegalStateException {
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isHoliday() && dayType.isPeriod(period)) {
                return dayType;
            }
        }
        throw new IllegalStateException("No holiday found for period [" + period + "]!");
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public int getTariffIdentifier() {
        if (getName() != null) {
            String[] nameParts = getName().split("_");
            if (getName().split("_").length > 1) {
                try {
                    return Integer.valueOf(nameParts[nameParts.length - 1]);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
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
        return "CodeObject" +
               "{calendars=" + calendars +
               ", id=" + id +
               ", name='" + name + '\'' +
               ", externalName='" + externalName + '\'' +
               ", yearFrom=" + yearFrom +
               ", yearTo=" + yearTo +
               ", interval=" + interval +
               ", verified=" + verified +
               ", rebuilt=" + rebuilt +
               ", destinationTimeZone=" + destinationTimeZone +
               ", definitionTimeZone=" + definitionTimeZone +
               ", seasonSet=" + seasonSet +
               ", dayTypes=" + dayTypes +
               '}';
    }

    public long getDefaultBand() throws IllegalStateException {
        CodeDayTypeObject defaultDay = getDefaultDayType();
        List<CodeDayTypeDefObject> dayTypeDefs = defaultDay.getDayTypeDefs();
        if (!dayTypeDefs.isEmpty()) {
            CodeDayTypeDefObject defaultBand = dayTypeDefs.get(0);
            return defaultBand.getCodeValue();
        }
        throw new IllegalStateException("No default tariff found!");
    }

    private CodeDayTypeObject getDefaultDayType() throws IllegalStateException {
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isDefault()) {
                return dayType;
            }
        }
        throw new IllegalStateException("No default dayType found!");
    }

}