package com.energyict.dlms;

import com.energyict.mdc.common.ApplicationException;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

/**
 * This enum contains the mapping between DLMS unit codes and the EIServer event codes.
 * Most values map 1v1 but there are some exceptions. This class can be used safely to translate
 * from DLMS to EIServer without worrying about these exceptions. It also simplifies the mapping
 * if there are new units added to the blue book or EIServer (single point of changes).
 * <p/>
 * Copyrights EnergyICT
 * Date: 27/12/11
 * Time: 10:23
 */
public enum DlmsUnit {

    YEAR(1, BaseUnit.YEAR),
    MONTH(2, BaseUnit.MONTH),
    WEEK(3, BaseUnit.WEEK),
    DAY(4, BaseUnit.DAY),
    HOUR(5, BaseUnit.HOUR),
    MINUTE(6, BaseUnit.MINUTE),
    SECOND(7, BaseUnit.SECOND),
    DEGREE(8, BaseUnit.DEGREE),
    DEGREE_CELSIUS(9, BaseUnit.DEGREE_CELSIUS),
    CURRENCY(10), // TODO: No EIServer unit for generic currency. Mapped as BaseUnit.UNITLESS at the moment.
    METER(11, BaseUnit.METER),
    METERPERSECOND(12, BaseUnit.METERPERSECOND),
    CUBICMETER(13, BaseUnit.CUBICMETER),
    CUBICMETER_NORMAL(14, BaseUnit.NORMALCUBICMETER),
    CUBICMETERPERHOUR(15, BaseUnit.CUBICMETERPERHOUR),
    CUBICMETERPERHOUR_NORMAL(16, BaseUnit.NORMALCUBICMETERPERHOUR),
    CUBICMETERPERDAY(17, BaseUnit.CUBICMETERPERDAY),
    CUBICMETERPERDAY_NORMAL(18, BaseUnit.NORMALCUBICMETERPERDAY),
    LITER(19, BaseUnit.LITER),
    KILOGRAM(20, BaseUnit.KILOGRAM),
    NEWTON(21, BaseUnit.NEWTON),
    NEWTONMETER(22, BaseUnit.NEWTONMETER),
    PASCAL(23, BaseUnit.PASCAL),
    BAR(24, BaseUnit.BAR),
    JOULE(25, BaseUnit.JOULE),
    JOULEPERHOUR(26, BaseUnit.JOULEPERHOUR),
    WATT(27, BaseUnit.WATT),
    VOLTAMPERE(28, BaseUnit.VOLTAMPERE),
    VOLTAMPEREREACTIVE(29, BaseUnit.VOLTAMPEREREACTIVE),
    WATTHOUR(30, BaseUnit.WATTHOUR),
    VOLTAMPEREHOUR(31, BaseUnit.VOLTAMPEREHOUR),
    VOLTAMPEREREACTIVEHOUR(32, BaseUnit.VOLTAMPEREREACTIVEHOUR),
    AMPERE(33, BaseUnit.AMPERE),
    COULOMB(34, BaseUnit.COULOMB),
    VOLT(35, BaseUnit.VOLT),
    VOLTPERMETER(36, BaseUnit.VOLTPERMETER),
    FARAD(37, BaseUnit.FARAD),
    OHM(38, BaseUnit.OHM),
    OHMSQUAREMETERPERMETER(39, BaseUnit.OHMSQUAREMETERPERMETER),
    WEBER(40, BaseUnit.WEBER),
    TESLA(41, BaseUnit.TESLA),
    AMPEREPERMETER(42, BaseUnit.AMPEREPERMETER),
    HENRY(43, BaseUnit.HENRY),
    HERTZ(44, BaseUnit.HERTZ),
    ACTIVEENERGY(45, BaseUnit.ACTIVEENERGY),
    REACTIVEENERGY(46, BaseUnit.REACTIVEENERGY),
    APPARENTENERGY(47, BaseUnit.APPARENTENERGY),
    VOLTSQUAREHOUR(48, BaseUnit.VOLTSQUAREHOUR),
    AMPERESQUAREHOUR(49, BaseUnit.AMPERESQUAREHOUR),
    KILOGRAMPERSECOND(50, BaseUnit.KILOGRAMPERSECOND),
    SIEMENS(51, BaseUnit.SIEMENS),
    KELVIN(52, BaseUnit.KELVIN),
    VOLTSQUAREHOUR_PULSE(53), // TODO: No EIServer unit for this dlms unit. Mapped as BaseUnit.UNITLESS at the moment.
    AMPERESQUAREHOUR_PULSE(54), // TODO: No EIServer unit for this dlms unit. Mapped as BaseUnit.UNITLESS at the moment.
    CUBICMETER_PULSE(55), // TODO: No EIServer unit for this dlms unit. Mapped as BaseUnit.UNITLESS at the moment.
    PERCENT(56, BaseUnit.PERCENT),
    AMPEREHOUR(57, BaseUnit.AMPEREHOUR),

    /* DLMS Bluebook v10 does not specify units for 58 and 59 */

    WATTHOURPERCUBICMETER(60, BaseUnit.WATTHOURPERCUBICMETER),
    JOULEPERNORMALCUBICMETER(61, BaseUnit.JOULEPERNORMALCUBICMETER),
    MOLPERCENT(62, BaseUnit.MOLPERCENT),
    GRAMPERCUBICMETER(63), // TODO: No EIServer unit for this dlms unit. Mapped as BaseUnit.UNITLESS at the moment.
    PASCALSECOND(64), // TODO: No EIServer unit for this dlms unit. Mapped as BaseUnit.UNITLESS at the moment.
    JOULEPERKILOGRAM(65), // TODO: No EIServer unit for this dlms unit. Mapped as BaseUnit.UNITLESS at the moment.

    /* DLMS Bluebook v10 does not specify units for 66, 67, 68 and 69 */

    DECIBELLMETER(70, BaseUnit.DECIBELPOWERRATIO),

    /* DLMS Bluebook v10 does not specify units for 71-253. They are marked as RESERVED */

    OTHER(254),
    UNITLESS(255, BaseUnit.UNITLESS);

    private final int dlmsUnitCode;
    private final int eisUnitCode;

    private DlmsUnit(int dlmsUnitCode, int eisUnitCode) {
        this.dlmsUnitCode = dlmsUnitCode;
        this.eisUnitCode = eisUnitCode;
    }

    private DlmsUnit(int dlmsUnitCode) {
        this.dlmsUnitCode = dlmsUnitCode;
        this.eisUnitCode = BaseUnit.UNITLESS;
    }

    /**
     * The dlms unit code as defined in the dlms blue book
     *
     * @return The dlms unit code as defined in the dlms blue book as an int
     */
    public int getDlmsUnitCode() {
        return dlmsUnitCode;
    }

    /**
     * The translated EIServer base unit code as an int.
     *
     * @return The translated EIServer base unit code as an int.
     */
    public int getEisUnitCode() {
        return eisUnitCode;
    }

    /**
     * The translated EIServer base unit without scalers applied (scalervalue = 0)
     *
     * @return the EIServer Unit
     */
    public Unit getEisUnit() {
        return Unit.get(eisUnitCode);
    }

    /**
     * Look up a matching DlmsUnit given the dlms unit code.
     * If there is no match, the 'OTHER' DlmsUnit will be returned
     *
     * @param dlmsUnitCode the dlms unit code to look for
     * @return The matching DlmsUnit or DlmsUnit.OTHER if not found.
     */
    public static DlmsUnit fromDlmsCode(int dlmsUnitCode) {
        for (DlmsUnit dlmsUnit : values()) {
            if (dlmsUnit.getDlmsUnitCode() == dlmsUnitCode) {
                return dlmsUnit;
            }
        }
        return OTHER;
    }

    /**
     * Look up a matching DlmsUnit given the dlms unit code.
     * If there is no match, we will throw the same exception as BaseUnit would do
     *
     * @param dlmsUnitCode the dlms unit code to look for
     * @return The matching DlmsUnit
     */
    public static DlmsUnit fromValidDlmsCode(int dlmsUnitCode) {
        for (DlmsUnit dlmsUnit : values()) {
            if (dlmsUnit.getDlmsUnitCode() == dlmsUnitCode) {
                return dlmsUnit;
            }
        }
        throw new ApplicationException("Invalid DLMS unit code [" + dlmsUnitCode + "]");
    }

    /**
     * Check if a given DLMS code is defined in the bluebook.
     *
     * @param dlmsUnitCode the dlms code to validate
     * @return true if the dlms code is undefined, false otherwise
     */
    public static boolean isValidDlmsUnitCode(int dlmsUnitCode) {
        for (DlmsUnit dlmsUnit : values()) {
            if (dlmsUnit.getDlmsUnitCode() == dlmsUnitCode) {
                return true;
            }
        }
        return false;
    }

}
