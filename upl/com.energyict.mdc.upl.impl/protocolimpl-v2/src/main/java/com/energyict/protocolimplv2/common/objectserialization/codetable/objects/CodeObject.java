package com.energyict.protocolimplv2.common.objectserialization.codetable.objects;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.google.common.collect.Range;

import java.io.Serializable;
import java.time.Year;
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

    public static final int MIN_START_YEAR = 2000;
    public static final int MAX_START_YEAR = 2099;

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

    public static CodeObject fromCode(TariffCalendar calendar, TariffCalendarExtractor extractor) {
        CodeObject co = new CodeObject();
        co.setId(Integer.parseInt(extractor.id(calendar)));
        co.setName(extractor.name(calendar));
        co.setExternalName(null);
        Range<Year> range = extractor.range(calendar);
        if (range.hasLowerBound()) {
            co.setYearFrom(range.lowerEndpoint().getValue());
        } else {
            co.setYearFrom(MIN_START_YEAR);
        }
        if (range.hasUpperBound()) {
            co.setYearTo(range.upperEndpoint().getValue());
        } else {
            co.setYearTo(MAX_START_YEAR);
        }
        co.setInterval(extractor.intervalInSeconds(calendar));
        co.setVerified(true);
        co.setRebuilt(true);
        co.setDestinationTimeZone(extractor.destinationTimeZone(calendar));
        co.setDefinitionTimeZone(extractor.definitionTimeZone(calendar));
        extractor.season(calendar).map(SeasonSetObject::fromSeasonSet).ifPresent(co::setSeasonSet);
        co.setDayTypes(extractor.dayTypes(calendar).stream().map(CodeDayTypeObject::fromCodeDayType).collect(Collectors.toList()));
        co.setCalendars(extractor.rules(calendar).stream().map(CodeCalendarObject::fromCodeCalendar).collect(Collectors.toList()));
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
