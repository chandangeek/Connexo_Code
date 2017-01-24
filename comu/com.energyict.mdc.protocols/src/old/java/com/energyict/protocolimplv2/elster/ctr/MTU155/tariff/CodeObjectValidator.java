package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff;

import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeCalendarObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeDayTypeDefObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeDayTypeObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.SeasonObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.SeasonSetObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.SeasonTransitionObject;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11/04/11
 * Time: 10:12
 */
public class CodeObjectValidator {

    private static final String[] SEASON_NAMES = new String[]{
            "Billing period 1",
            "Billing period 2"
    };

    public static final int MAX_CUSTOM_DATES = 9;
    public static final int MAX_HOLIDAY_DATES = 11;
    public static final int MIN_TARIFF_ID = 0;
    public static final int MAX_TARIFF_ID = 255;
    public static final int MIN_START_YEAR = 2000;
    public static final int MAX_START_YEAR = 2099;

    public static void validateCodeObject(CodeObject codeObject) {
        if (codeObject == null) {
            throw new IllegalArgumentException("CodeObject cannot be 'null'");
        }
        validateCodeObjectProperties(codeObject);
        validateSeasons(codeObject.getSeasonSet(), codeObject.getYearFrom());
        validateDayTypes(codeObject.getDayTypes());
        validateCalendarDays(codeObject);
    }

    private static void validateCodeObjectProperties(CodeObject codeObject) {
        if (codeObject.getName() == null) {
            throw new IllegalArgumentException("Code table should have a name, but was 'null'.");
        }
        String[] nameParts = codeObject.getName().split("_");
        if (nameParts.length < 2) {
            throw new IllegalArgumentException("Code table name should have the following format: [codetablename_XXX] where XXX is 000 - 255, but was [" + codeObject.getName() + "]");
        }

        int tariffId = codeObject.getTariffIdentifier();
        if ((tariffId < MIN_TARIFF_ID) || (tariffId > MAX_TARIFF_ID)) {
            throw new IllegalArgumentException("Incorrect tariff ID. Name with format [codetablename_XXX] where XXX should lay between 000 - 255, but was [" + codeObject.getName() + "]");
        }

        if ((codeObject.getYearFrom() < MIN_START_YEAR) || (codeObject.getYearFrom() > MAX_START_YEAR)) {
            throw new IllegalArgumentException("From and to year should have a value from " + MIN_START_YEAR + " to " + MAX_START_YEAR + " but was [" + codeObject.getYearFrom() + "]");
        }

        if (codeObject.getYearFrom() != codeObject.getYearTo()) {
            throw new IllegalArgumentException("From and to year should have the same value but from was [" + codeObject.getYearFrom() + "] and to [" + codeObject.getYearTo() + "]");
        }

    }

    private static void validateCalendarDays(CodeObject codeObject) {
        validateSpecialDaysYear(codeObject);
        validateDuplicateSpecialDays(codeObject);
        validateNumberOfSpecialDays(codeObject);
    }

    private static void validateSpecialDaysYear(CodeObject codeObject) {
        List<CodeCalendarObject> specialDays = codeObject.getSpecialDayCalendars();
        for (CodeCalendarObject specialDay : specialDays) {
            if (specialDay.getYear() != codeObject.getYearFrom()) {
                throw new IllegalArgumentException("Only special days allowed for start year [" + codeObject.getYearFrom() + "], but found special date for [" + specialDay.getCalendar().getTime() + "].");
            }
        }
    }

    private static void validateNumberOfSpecialDays(CodeObject codeObject) {
        List<CodeCalendarObject> customDays = codeObject.getCustomDayCalendars();
        if (customDays.size() > MAX_CUSTOM_DATES) {
            throw new IllegalArgumentException("Only [" + MAX_CUSTOM_DATES + "] custom dates allowed but [" + customDays.size() + "] configured in EIServer.");
        }
        List<CodeCalendarObject> holidays = codeObject.getHolidayCalendars();
        if (holidays.size() > MAX_HOLIDAY_DATES) {
            throw new IllegalArgumentException("Only [" + MAX_HOLIDAY_DATES + "] holidays allowed but [" + holidays.size() + "] configured in EIServer.");
        }
        List<CodeCalendarObject> specialDays = codeObject.getSpecialDayCalendars();
        if ((holidays.size() + customDays.size()) != specialDays.size()) {
            throw new IllegalArgumentException("Found a total of [" + specialDays.size() + "] special days, but found [" + holidays.size() + "] holidays and [" + customDays.size() + "] custom days! Seems to be a protocol error!");
        }
    }

    private static void validateDuplicateSpecialDays(CodeObject codeObject) {
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

    private static void validateSeasons(SeasonSetObject seasonSet, int year) {
        if (seasonSet == null) {
            throw new IllegalArgumentException("SeasonSet cannot be 'null'");
        }

        List<SeasonObject> seasons = seasonSet.getSeasons();
        if ((seasons == null) || (seasons.isEmpty())) {
            throw new IllegalArgumentException("Seasons cannot be 'null' or empty.");
        }

        if (seasons.size() != SEASON_NAMES.length) {
            throw new IllegalArgumentException("SeasonSet should have [" + SEASON_NAMES.length + "] but had [" + seasons.size() + "] seasons.");
        }

        for (String seasonName : SEASON_NAMES) {
            if (!containsSeasonWithName(seasons, seasonName)) {
                throw new IllegalArgumentException("SeasonSet should have a season with name [" + seasonName + "].");
            }
        }

        for (SeasonObject season : seasons) {
            List<SeasonTransitionObject> transitions = season.getTransitionsPerYear(year);
            if (transitions.isEmpty()) {
                throw new IllegalArgumentException("Season [" + season.getName() + "] has no start date for year [" + year + "]. Every season should have one and only one start date.");
            } else if (transitions.size() > 1) {
                throw new IllegalArgumentException("Season [" + season.getName() + "] has more then one start date [" + transitions.size() + "] for year [" + year + "]. Every season should have one and only one start date.");
            }
        }

    }

    private static void validateDayTypes(List<CodeDayTypeObject> dayTypes) {
        if ((dayTypes == null) || (dayTypes.isEmpty())) {
            throw new IllegalArgumentException("Day types cannot be 'null' or empty.");
        }
        if (dayTypes.size() != CodeDayTypeObject.DAYTYPE_NAMES.length) {
            throw new IllegalArgumentException("Calendar needs to have [" + CodeDayTypeObject.DAYTYPE_NAMES.length + "] day types to match MTU155 calendar, but only had [" + dayTypes.size() + "]");
        }

        for (String dayTypeName : CodeDayTypeObject.DAYTYPE_NAMES) {
            if (!containsDayTypeWithName(dayTypes, dayTypeName)) {
                throw new IllegalArgumentException("Calendar should have a day type with name [" + dayTypeName + "].");
            }
        }

        for (CodeDayTypeObject dayType : dayTypes) {
            String dayTypeName = dayType.getName();
            if (dayTypeName.startsWith("PT")) {
                validateBasicDayType(dayType);
            } else if (dayTypeName.startsWith("Forc")) {
                validateForcedDayType(dayType);
            } else {
                validateDefaultDayType(dayType);
            }
            validateTimeBands(dayType);
        }

    }

    private static void validateTimeBands(CodeDayTypeObject dayType) {
        List<CodeDayTypeDefObject> dayTypeDefs = dayType.getDayTypeDefs();
        for (CodeDayTypeDefObject dayTypeDef : dayTypeDefs) {
            long codeValue = dayTypeDef.getCodeValue();
            if ((codeValue < 1) || (codeValue > 3)) {
                throw new IllegalArgumentException("Time bands can only have a code value from 1 to 3, but was [" + codeValue + "] in day type [" + dayType.getName() + "]");
            }
        }
    }

    private static void validateBasicDayType(CodeDayTypeObject dayType) {
        List<CodeDayTypeDefObject> defs = dayType.getDayTypeDefs();
        if (defs.isEmpty()) {
            throw new IllegalArgumentException("Daytype [" + dayType.getName() + "] should have 5 or 6 time bands, but has none!");
        }

        Collections.sort(defs);
        int numberOfBands = defs.size();
        CodeDayTypeDefObject firstBand = defs.get(0);
        CodeDayTypeDefObject lastBand = defs.get(numberOfBands - 1);

        if (firstBand.getFrom() != 0) {
            throw new IllegalArgumentException("Daytype [" + dayType.getName() + "] should start with a band at 00:00!");
        }

        if (numberOfBands == 6) {
            if (firstBand.getCodeValue() != lastBand.getCodeValue()) {
                throw new IllegalArgumentException("Daytype [" + dayType.getName() + "] has 6 bands, and starts with code [" + firstBand.getCodeValue() + "], " +
                        "so it should also end with this code, but ended on code [" + lastBand.getCodeValue() + "]");
            }
        } else if (numberOfBands != 5) {
            throw new IllegalArgumentException("Daytype [" + dayType.getName() + "] should have 5 or 6 time bands, but has [" + numberOfBands + "]");
        }
    }

    private static void validateForcedDayType(CodeDayTypeObject dayType) {
        List<CodeDayTypeDefObject> defs = dayType.getDayTypeDefs();
        String name = dayType.getName();
        if (defs.size() != 1) {
            throw new IllegalArgumentException("Daytype [" + name + "] should have one and only one time band, but has [" + defs.size() + "]");
        }

        for (int i = 1; i <= 3; i++) {
            if (name.endsWith("" + i)) {
                long codeValue = dayType.getDayTypeDefs().get(0).getCodeValue();
                if (codeValue != i) {
                    throw new IllegalArgumentException("Forced day type [" + name + "] should only have one time band with codeValue [" + i + "], but was [" + codeValue + "]");
                }
            }
        }

    }

    private static void validateDefaultDayType(CodeDayTypeObject dayType) {
        List<CodeDayTypeDefObject> defs = dayType.getDayTypeDefs();
        if (defs.size() != 1) {
            throw new IllegalArgumentException("Daytype [" + dayType.getName() + "] should have one and only one time band, but has [" + defs.size() + "]");
        }
    }

    private static boolean containsDayTypeWithName(List<CodeDayTypeObject> dayTypes, String daytypeName) {
        for (CodeDayTypeObject dayType : dayTypes) {
            if (dayType.getName().equals(daytypeName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsSeasonWithName(List<SeasonObject> seasons, String seasonName) {
        for (SeasonObject season : seasons) {
            if (season.getName().equals(seasonName)) {
                return true;
            }
        }
        return false;
    }

}
