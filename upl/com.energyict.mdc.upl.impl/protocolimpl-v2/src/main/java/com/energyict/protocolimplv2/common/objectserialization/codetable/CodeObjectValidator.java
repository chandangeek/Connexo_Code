package com.energyict.protocolimplv2.common.objectserialization.codetable;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.*;

import java.io.IOException;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11/04/11
 * Time: 10:12
 */
public class CodeObjectValidator {

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

        if ((codeObject.getYearFrom() < MIN_START_YEAR) || (codeObject.getYearFrom() > MAX_START_YEAR)) {
            throw new BusinessException("From and to year should have a value from " + MIN_START_YEAR + " to " + MAX_START_YEAR + " but was [" + codeObject.getYearFrom() + "]");
        }

        if (codeObject.getYearFrom() != codeObject.getYearTo()) {
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
        List<CodeCalendarObject> specialDays = codeObject.getSpecialDayCalendars();
        if (specialDays.size() > 36) {
            throw new BusinessException("Only [" + 36 + "] special days allowed but [" + specialDays.size() + "] configured in EIServer.");
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
    }

    private static void validateDayTypes(List<CodeDayTypeObject> dayTypes) throws BusinessException {
        if ((dayTypes == null) || (dayTypes.isEmpty())) {
            throw new BusinessException("Day types cannot be 'null' or empty.");
        }

        for (CodeDayTypeObject dayType : dayTypes) {
            try {
                int dayTypeName = Integer.parseInt(dayType.getName());
                if (dayTypeName < 1 || dayTypeName > 16) {
                    throw new BusinessException("Day type name should be a number between 1 and 16");
                }
            } catch (NumberFormatException e) {
                throw new BusinessException("Day type name should be a number between 1 and 16");
            }

            validateTimeBands(dayType);
        }
    }

    private static void validateTimeBands(CodeDayTypeObject dayType) throws BusinessException {
        List<CodeDayTypeDefObject> dayTypeDefs = dayType.getDayTypeDefs();
        if (dayTypeDefs.size() > 8) {
            throw new BusinessException("Time bind of a day type should contain less than 8 times");
        }

        for (CodeDayTypeDefObject dayTypeDef : dayTypeDefs) {
            int codeValue = dayTypeDef.getCodeValue();
            if ((codeValue < 1) || (codeValue > 4)) {
                throw new BusinessException("Only tariff codes 1, 2, 3 or 4 are supported in the time bands of the day types");
            }
        }
    }
}
