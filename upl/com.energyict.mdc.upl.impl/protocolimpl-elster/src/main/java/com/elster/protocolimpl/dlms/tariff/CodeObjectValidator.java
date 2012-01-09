package com.elster.protocolimpl.dlms.tariff;

import com.elster.protocolimpl.dlms.tariff.objects.*;
import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.MeteringWarehouse;

import java.io.IOException;
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

    public static void validateCodeTable(int codeTableId) throws IOException, BusinessException {
        Code code = MeteringWarehouse.getCurrent().getCodeFactory().find(codeTableId);
        validateCodeTable(code);
    }

    private static void validateCodeTable(Code code) throws IOException, BusinessException {
        byte[] base64 = CodeTableBase64Builder.getBase64FromCodeTable(code);
        CodeObject codeObject = CodeTableBase64Parser.getCodeTableFromBase64(base64);
        validateCodeObject(codeObject);
    }

    public static void validateCodeObject(CodeObject codeObject) throws BusinessException {
        if (codeObject == null) {
            throw new BusinessException("CodeObject cannot be 'null'");
        }
        validateCodeObjectProperties(codeObject);
        validateSeasons(codeObject.getSeasonSet(), codeObject.getYearFrom());
        validateDayTypes(codeObject.getDayTypes());
        validateCalendarDays(codeObject);
    }

    private static void validateCodeObjectProperties(CodeObject codeObject) throws BusinessException {
        if (codeObject.getName() == null) {
            throw new BusinessException("Code table should have a name, but was 'null'.");
        }
        String[] nameParts = codeObject.getName().split("_");
        if (nameParts.length < 2) {
            throw new BusinessException("Code table name should have the following format: [codetablename_XXX] where XXX is 000 - 255, but was [" + codeObject.getName() + "]");
        }

        int tariffId = codeObject.getTariffIdentifier();
        if ((tariffId < MIN_TARIFF_ID) || (tariffId > MAX_TARIFF_ID)) {
            throw new BusinessException("Incorrect tariff ID. Name with format [codetablename_XXX] where XXX should lay between 000 - 255, but was [" + codeObject.getName() + "]");
        }

        if ((codeObject.getYearFrom() < MIN_START_YEAR) || (codeObject.getYearFrom() > MAX_START_YEAR)) {
            throw new BusinessException("From and to year should have a value from " + MIN_START_YEAR + " to " + MAX_START_YEAR + " but was [" + codeObject.getYearFrom() + "]");
        }

        boolean sameYear = codeObject.getYearFrom() == codeObject.getYearTo();
        boolean nextYear = (codeObject.getYearFrom() + 1) == codeObject.getYearTo();
        if (!sameYear && !nextYear) {
            throw new BusinessException("From and to year should have the same value but from was [" + codeObject.getYearFrom() + "] and to [" + codeObject.getYearTo() + "]");
        }

    }

    private static void validateCalendarDays(CodeObject codeObject) throws BusinessException {
        validateSpecialDaysYear(codeObject);
        validateDuplicateSpecialDays(codeObject);
        validateNumberOfSpecialDays(codeObject);
    }

    private static void validateSpecialDaysYear(CodeObject codeObject) throws BusinessException {
        List<CodeCalendarObject> specialDays = codeObject.getSpecialDayCalendars();
        for (CodeCalendarObject specialDay : specialDays) {
            if (specialDay.getYear() != codeObject.getYearFrom()) {
                throw new BusinessException("Only special days allowed for start year [" + codeObject.getYearFrom() + "], but found special date for [" + specialDay.getCalendar().getTime() + "].");
            }
        }
    }

    private static void validateNumberOfSpecialDays(CodeObject codeObject) throws BusinessException {
        List<CodeCalendarObject> customDays = codeObject.getCustomDayCalendars();
        if (customDays.size() > MAX_CUSTOM_DATES) {
            throw new BusinessException("Only [" + MAX_CUSTOM_DATES + "] custom dates allowed but [" + customDays.size() + "] configured in EIServer.");
        }
        List<CodeCalendarObject> holidays = codeObject.getHolidayCalendars();
        if (holidays.size() > MAX_HOLIDAY_DATES) {
            throw new BusinessException("Only [" + MAX_HOLIDAY_DATES + "] holidays allowed but [" + holidays.size() + "] configured in EIServer.");
        }
        List<CodeCalendarObject> specialDays = codeObject.getSpecialDayCalendars();
        if ((holidays.size() + customDays.size()) != specialDays.size()) {
            throw new BusinessException("Found a total of [" + specialDays.size() + "] special days, but found [" + holidays.size() + "] holidays and [" + customDays.size() + "] custom days! Seems to be a protocol error!");
        }
    }

    private static void validateDuplicateSpecialDays(CodeObject codeObject) throws BusinessException {
        List<CodeCalendarObject> specialDays = codeObject.getSpecialDayCalendars();
        for (CodeCalendarObject sd : specialDays) {
            List<CodeCalendarObject> specialDaysToValidate = codeObject.getSpecialDayCalendars();
            for (CodeCalendarObject sde : specialDaysToValidate) {
                if (sd.isSameDate(sde) && !sd.equals(sde)) {
                    throw new BusinessException("Duplicate special dates found for [" + sd.getCalendar().getTime() + "]. Conflicting day types: [" + sde.getDayTypeName() + "] and [" + sd.getDayTypeName() + "]");
                }
            }
        }
    }

    private static void validateSeasons(SeasonSetObject seasonSet, int year) throws BusinessException {
        if (seasonSet == null) {
            throw new BusinessException("SeasonSet cannot be 'null'");
        }

        List<SeasonObject> seasons = seasonSet.getSeasons();
        if ((seasons == null) || (seasons.isEmpty())) {
            throw new BusinessException("Seasons cannot be 'null' or empty.");
        }

        if (seasons.size() != SEASON_NAMES.length) {
            throw new BusinessException("SeasonSet should have [" + SEASON_NAMES.length + "] but had [" + seasons.size() + "] seasons.");
        }

        for (String seasonName : SEASON_NAMES) {
            if (!containsSeasonWithName(seasons, seasonName)) {
                throw new BusinessException("SeasonSet should have a season with name [" + seasonName + "].");
            }
        }

        for (SeasonObject season : seasons) {
            List<SeasonTransitionObject> transitions = season.getTransitionsPerYear(year);
            if (transitions.isEmpty()) {
                throw new BusinessException("Season [" + season.getName() + "] has no start date for year [" + year + "]. Every season should have one and only one start date.");
            } else if (transitions.size() > 1) {
                throw new BusinessException("Season [" + season.getName() + "] has more then one start date [" + transitions.size() + "] for year [" + year + "]. Every season should have one and only one start date.");
            }
        }

    }

    private static void validateDayTypes(List<CodeDayTypeObject> dayTypes) throws BusinessException {
        if ((dayTypes == null) || (dayTypes.isEmpty())) {
            throw new BusinessException("Day types cannot be 'null' or empty.");
        }
        if (dayTypes.size() != CodeDayTypeObject.DAYTYPE_NAMES.length) {
            throw new BusinessException("Calendar needs to have [" + CodeDayTypeObject.DAYTYPE_NAMES.length + "] day types to match tariff calendar, but only had [" + dayTypes.size() + "]");
        }

        for (String dayTypeName : CodeDayTypeObject.DAYTYPE_NAMES) {
            if (!containsDayTypeWithName(dayTypes, dayTypeName)) {
                throw new BusinessException("Calendar should have a day type with name [" + dayTypeName + "].");
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

    private static void validateTimeBands(CodeDayTypeObject dayType) throws BusinessException {
        List<CodeDayTypeDefObject> dayTypeDefs = dayType.getDayTypeDefs();
        for (CodeDayTypeDefObject dayTypeDef : dayTypeDefs) {
            int codeValue = dayTypeDef.getCodeValue();
            if ((codeValue < 1) || (codeValue > 3)) {
                throw new BusinessException("Time bands can only have a code value from 1 to 3, but was [" + codeValue + "] in day type [" + dayType.getName() + "]");
            }
        }
    }

    private static void validateBasicDayType(CodeDayTypeObject dayType) throws BusinessException {
        List<CodeDayTypeDefObject> defs = dayType.getDayTypeDefs();
        if (defs.isEmpty()) {
            throw new BusinessException("Daytype [" + dayType.getName() + "] should have 5 or 6 time bands, but has none!");
        }

        Collections.sort(defs);
        int numberOfBands = defs.size();
        CodeDayTypeDefObject firstBand = defs.get(0);
        CodeDayTypeDefObject lastBand = defs.get(numberOfBands - 1);

        if (firstBand.getFrom() != 0) {
            throw new BusinessException("Daytype [" + dayType.getName() + "] should start with a band at 00:00!");
        }

        if (numberOfBands == 6) {
            if (firstBand.getCodeValue() != lastBand.getCodeValue()) {
                throw new BusinessException("Daytype [" + dayType.getName() + "] has 6 bands, and starts with code [" + firstBand.getCodeValue() + "], " +
                        "so it should also end with this code, but ended on code [" + lastBand.getCodeValue() + "]");
            }
        } else if (numberOfBands > 5) {
            throw new BusinessException("Daytype [" + dayType.getName() + "] should have only up to 6 time bands, but has [" + numberOfBands + "]");
        }
    }

    private static void validateForcedDayType(CodeDayTypeObject dayType) throws BusinessException {
        List<CodeDayTypeDefObject> defs = dayType.getDayTypeDefs();
        String name = dayType.getName();
        if (defs.size() != 1) {
            throw new BusinessException("Daytype [" + name + "] should have one and only one time band, but has [" + defs.size() + "]");
        }

        for (int i = 1; i <= 3; i++) {
            if (name.endsWith("" + i)) {
                int codeValue = dayType.getDayTypeDefs().get(0).getCodeValue();
                if (codeValue != i) {
                    throw new BusinessException("Forced day type [" + name + "] should only have one time band with codeValue [" + i + "], but was [" + codeValue + "]");
                }
            }
        }

    }

    private static void validateDefaultDayType(CodeDayTypeObject dayType) throws BusinessException {
        List<CodeDayTypeDefObject> defs = dayType.getDayTypeDefs();
        if (defs.size() != 1) {
            throw new BusinessException("Daytype [" + dayType.getName() + "] should have one and only one time band, but has [" + defs.size() + "]");
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
            if (season.getName().equalsIgnoreCase(seasonName)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws BusinessException, IOException {
        MeteringWarehouse.createBatchContext();
        validateCodeTable(1);
    }

}
