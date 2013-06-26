package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.energyict.cbo.BusinessException;
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

    public CodeDayTypeObject getWeekday(int period) throws BusinessException {
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isWeekday() && dayType.isPeriod(period)) {
                return dayType;
            }
        }
        throw new BusinessException("No weekday found for period [" + period + "]!");
    }

    public CodeDayTypeObject getSaturday(int period) throws BusinessException {
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isSaturday() && dayType.isPeriod(period)) {
                return dayType;
            }
        }
        throw new BusinessException("No saturday found for period [" + period + "]!");
    }

    public CodeDayTypeObject getHoliday(int period) throws BusinessException {
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isHoliday() && dayType.isPeriod(period)) {
                return dayType;
            }
        }
        throw new BusinessException("No holiday found for period [" + period + "]!");
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

    public String getName() {
        return "todo!";     //TODO - check this out : name should be in format '[codetablename_XXX] where XXX is 000 - 255]'
    }

    public int getTariffIdentifier() {
        return -1;  //TODO - check this out!
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CodeObject");
        sb.append("{calendars=").append(calendars);
        sb.append(", id=").append(id);
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

    public int getDefaultBand() throws BusinessException {
        CodeDayTypeObject defaultDay = getDefaultDayType();
        List<CodeDayTypeDefObject> dayTypeDefs = defaultDay.getDayTypeDefs();
        if (!dayTypeDefs.isEmpty()) {
            CodeDayTypeDefObject defaultBand = dayTypeDefs.get(0);
            return defaultBand.getCodeValue();
        }
        throw new BusinessException("No default tariff found!");
    }

    private CodeDayTypeObject getDefaultDayType() throws BusinessException {
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.isDefault()) {
                return dayType;
            }
        }
        throw new BusinessException("No default dayType found!");
    }
}
