package com.energyict.protocolimplv2.common.objectserialization.codetable;

import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeCalendarObject;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeDayTypeDefObject;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeDayTypeObject;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeObject;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.SeasonObject;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.SeasonSetObject;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11/04/11
 * Time: 10:12
 */
public class CodeObjectValidator {

    public static final int MIN_START_YEAR = 2000;
    public static final int MAX_START_YEAR = 2099;

    public static void validateCodeObject(CodeObject codeObject) throws IllegalArgumentException {
        if (codeObject == null) {
            throw new IllegalArgumentException("CodeObject cannot be 'null'");
        }
        validateCodeObjectProperties(codeObject);
        validateSeasons(codeObject.getSeasonSet(), codeObject.getYearFrom());
        validateDayTypes(codeObject.getDayTypes());
        validateCalendarDays(codeObject);
    }

    private static void validateCodeObjectProperties(CodeObject codeObject) throws IllegalArgumentException {
        if (codeObject.getName() == null) {
            throw new IllegalArgumentException("Code table should have a name, but was 'null'.");
        }

        if ((codeObject.getYearFrom() < MIN_START_YEAR) || (codeObject.getYearFrom() > MAX_START_YEAR)) {
            throw new IllegalArgumentException("From and to year should have a value from " + MIN_START_YEAR + " to " + MAX_START_YEAR + " but was [" + codeObject.getYearFrom() + "]");
        }

        if (codeObject.getYearFrom() != codeObject.getYearTo()) {
            throw new IllegalArgumentException("From and to year should have the same value but from was [" + codeObject.getYearFrom() + "] and to [" + codeObject.getYearTo() + "]");
        }
    }

    private static void validateCalendarDays(CodeObject codeObject) throws IllegalArgumentException {
        validateSpecialDaysYear(codeObject);
        validateDuplicateSpecialDays(codeObject);
        validateNumberOfSpecialDays(codeObject);
    }

    private static void validateSpecialDaysYear(CodeObject codeObject) throws IllegalArgumentException {
        List<CodeCalendarObject> specialDays = codeObject.getSpecialDayCalendars();
        for (CodeCalendarObject specialDay : specialDays) {
            if (specialDay.getYear() != codeObject.getYearFrom()) {
                throw new IllegalArgumentException("Only special days allowed for start year [" + codeObject.getYearFrom() + "], but found special date for [" + specialDay.getCalendar().getTime() + "].");
            }
        }
    }

    private static void validateNumberOfSpecialDays(CodeObject codeObject) throws IllegalArgumentException {
        List<CodeCalendarObject> specialDays = codeObject.getSpecialDayCalendars();
        if (specialDays.size() > 36) {
            throw new IllegalArgumentException("Only [" + 36 + "] special days allowed but [" + specialDays.size() + "] configured in EIServer.");
        }
    }

    private static void validateDuplicateSpecialDays(CodeObject codeObject) throws IllegalArgumentException {
        List<CodeCalendarObject> specialDays = codeObject.getSpecialDayCalendars();
        for (CodeCalendarObject sd : specialDays) {
            List<CodeCalendarObject> specialDaysToValidate = codeObject.getSpecialDayCalendars();
            for (CodeCalendarObject sde : specialDaysToValidate) {
                if (sd.isSameDate(sde) && !sd.equals(sde)) {
                    throw new IllegalArgumentException("Duplicate special dates found for [" + sd.getCalendar().getTime() + "]. Conflicting day types: [" + sde.getDayTypeName() + "] and [" + sd.getDayTypeName() + "]");
                }
            }
        }
    }

    private static void validateSeasons(SeasonSetObject seasonSet, int year) throws IllegalArgumentException {
        if (seasonSet == null) {
            throw new IllegalArgumentException("SeasonSet cannot be 'null'");
        }

        List<SeasonObject> seasons = seasonSet.getSeasons();
        if ((seasons == null) || (seasons.isEmpty())) {
            throw new IllegalArgumentException("Seasons cannot be 'null' or empty.");
        }
    }

    private static void validateDayTypes(List<CodeDayTypeObject> dayTypes) throws IllegalArgumentException {
        if ((dayTypes == null) || (dayTypes.isEmpty())) {
            throw new IllegalArgumentException("Day types cannot be 'null' or empty.");
        }

        for (CodeDayTypeObject dayType : dayTypes) {
            try {
                int dayTypeName = Integer.parseInt(dayType.getName());
                if (dayTypeName < 1 || dayTypeName > 16) {
                    throw new IllegalArgumentException("Day type name should be a number between 1 and 16");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Day type name should be a number between 1 and 16");
            }

            validateTimeBands(dayType);
        }
    }

    private static void validateTimeBands(CodeDayTypeObject dayType) throws IllegalArgumentException {
        List<CodeDayTypeDefObject> dayTypeDefs = dayType.getDayTypeDefs();
        if (dayTypeDefs.size() > 8) {
            throw new IllegalArgumentException("Time bind of a day type should contain less than 8 times");
        }

        for (CodeDayTypeDefObject dayTypeDef : dayTypeDefs) {
            int codeValue = dayTypeDef.getCodeValue();
            if ((codeValue < 1) || (codeValue > 4)) {
                throw new IllegalArgumentException("Only tariff codes 1, 2, 3 or 4 are supported in the time bands of the day types");
            }
        }
    }
}
