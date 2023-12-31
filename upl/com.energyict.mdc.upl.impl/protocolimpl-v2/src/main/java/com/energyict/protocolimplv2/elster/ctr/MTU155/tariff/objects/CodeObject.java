package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.CodeObjectValidator;
import com.google.common.collect.Range;

import java.io.Serializable;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 11:52
 */
public class CodeObject implements Serializable {

    private int id;
    private String externalName;
    private String name;
    private int yearFrom;
    private int interval;
    private boolean verified;
    private boolean rebuilt;
    private int yearTo;
    private TimeZone destinationTimeZone;
    private TimeZone definitionTimeZone;
    private SeasonSetObject seasonSet;

    private List<CodeDayTypeObject> dayTypes = new ArrayList<>();
    private List<CodeCalendarObject> calendars = new ArrayList<>();

    public CodeObject() {
    }

    public static CodeObject fromCode(TariffCalendar calendar, TariffCalendarExtractor extractor) {
        CodeObject co = new CodeObject();
        co.setId(extractor.id(calendar));
        co.setName(extractor.name(calendar));
        co.setExternalName(null);
        Range<Year> range = extractor.range(calendar);
        if (range.hasLowerBound()) {
            co.setYearFrom(range.lowerEndpoint().getValue());
        } else {
            co.setYearFrom(CodeObjectValidator.MIN_START_YEAR);
        }
        if (range.hasUpperBound()) {
            co.setYearTo(range.upperEndpoint().getValue());
        } else {
            co.setYearTo(CodeObjectValidator.MAX_START_YEAR);
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

    public List<CodeCalendarObject> getHolidayCalendars() {
        return this.distinctCalendars(CodeCalendarObject::isHoliday);
    }

    public List<CodeCalendarObject> getCustomDayCalendars() {
        return this.distinctCalendars(CodeCalendarObject::isCustomDay);
    }

    public List<CodeCalendarObject> getSpecialDayCalendars() {
        return this.distinctCalendars(CodeCalendarObject::isSpecialDay);
    }

    private List<CodeCalendarObject> distinctCalendars(Predicate<CodeCalendarObject> predicate) {
        return this.calendars
                    .stream()
                    .filter(predicate)
                    .distinct()
                    .collect(Collectors.toList());
    }

    public void setCalendars(List<CodeCalendarObject> calendars) {
        this.calendars = calendars;
    }

    public List<CodeDayTypeObject> getDayTypes() {
        return dayTypes;
    }

    public CodeDayTypeObject getWeekday(int period) throws IllegalArgumentException {
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isWeekday() && dayType.isPeriod(period)) {
                return dayType;
            }
        }
        throw new IllegalArgumentException("No weekday found for period [" + period + "]!");
    }

    public CodeDayTypeObject getSaturday(int period) throws IllegalArgumentException {
        return this.get(period, "saturday", CodeDayTypeObject::isSaturday);
    }

    public CodeDayTypeObject getHoliday(int period) throws IllegalArgumentException {
        return this.get(period, "holiday", CodeDayTypeObject::isHoliday);
    }

    private CodeDayTypeObject get(int period, String dayName, Predicate<CodeDayTypeObject> predicate) throws IllegalArgumentException {
        return this.dayTypes
                .stream()
                .filter(predicate)
                .filter(dayType -> dayType.isPeriod(period))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No " + dayName + " found for period [" + period + "]!"));
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

    private void setId(String id) {
        this.setId(Integer.parseInt(id));
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
                ", yearFrom=" + yearFrom +
                ", yearTo=" + yearTo +
                ", destinationTimeZone=" + destinationTimeZone +
                ", definitionTimeZone=" + definitionTimeZone +
                ", seasonSet=" + seasonSet +
                ", dayTypes=" + dayTypes +
                '}';
    }

    public int getDefaultBand() throws IllegalArgumentException {
        CodeDayTypeObject defaultDay = getDefaultDayType();
        List<CodeDayTypeDefObject> dayTypeDefs = defaultDay.getDayTypeDefs();
        if (!dayTypeDefs.isEmpty()) {
            CodeDayTypeDefObject defaultBand = dayTypeDefs.get(0);
            return defaultBand.getCodeValue();
        }
        throw new IllegalArgumentException("No default tariff found!");
    }

    private CodeDayTypeObject getDefaultDayType() throws IllegalArgumentException {
        return this.dayTypes
                .stream()
                .filter(CodeDayTypeObject::isDefault)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No default dayType found!"));
    }

}