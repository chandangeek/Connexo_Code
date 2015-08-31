/*
 * Unit.java
 *
 * Created on 4 oktober 2002, 22:53
 */

package com.elster.insight.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * BaseUnit represents a basic unit without scaler.
 * The integer codes used to identify units are based
 * on the DLMS meter protocol, for codes below 255.
 * Codes above 255 are not defined by DLMS.
 * This class should not be used directly by applications.
 *
 * @author Karel
 * @see Unit
 */
public class BaseUnit implements java.io.Serializable {

    public static final long serialVersionUID = 486478978169936067L;

    /**
     * year unit code
     */
    public static final int YEAR = 1;
    /**
     * month unit code
     */
    public static final int MONTH = 2;
    /**
     * week unit code
     */
    public static final int WEEK = 3;
    /**
     * day unit code
     */
    public static final int DAY = 4;
    /**
     * hour unit code
     */
    public static final int HOUR = 5;
    /**
     * minute unit code
     */
    public static final int MINUTE = 6;
    /**
     * second unit code
     */
    public static final int SECOND = 7;
    /**
     * the degree unit code
     */
    public static final int DEGREE = 8;
    /**
     * the degree celsius unit code
     */
    public static final int DEGREE_CELSIUS = 9;
    /**
     * the meter unit code
     */
    public static final int METER = 11;
    /**
     * the meter per second unit code
     */
    public static final int METERPERSECOND = 12;
    /**
     * the cubic meter unit code
     */
    public static final int CUBICMETER = 13;
    /**
     * the normal cubic meter code
     */
    public static final int NORMALCUBICMETER = 14;
    /**
     * the cubic meter per hour unit code
     */
    public static final int CUBICMETERPERHOUR = 15;
    /**
     * the normal cubic meter per hour unit code
     */
    public static final int NORMALCUBICMETERPERHOUR = 16;
    /**
     * the cubic meter per day unit code
     */
    public static final int CUBICMETERPERDAY = 17;
    /**
     * the normal cubic meter per day code
     */
    public static final int NORMALCUBICMETERPERDAY = 18;
    /**
     * The liter unit code
     */
    public static final int LITER = 19;
    /**
     * The kilogram unit code
     */
    public static final int KILOGRAM = 20;
    /**
     * The newton unit code
     */
    public static final int NEWTON = 21;
    /**
     * The newton meter unit code
     */
    public static final int NEWTONMETER = 22;
    /**
     * The pascal unit code
     */
    public static final int PASCAL = 23;
    /**
     * The bar unit code
     */
    public static final int BAR = 24;
    /**
     * The joule unit code
     */
    public static final int JOULE = 25;
    /**
     * The joule per hour unit code
     */
    public static final int JOULEPERHOUR = 26;
    /**
     * The Watt unit code
     */
    public static final int WATT = 27;
    /**
     * The volt ampere unit code
     */
    public static final int VOLTAMPERE = 28;
    /**
     * The volt ampere reactive unit code
     */
    public static final int VOLTAMPEREREACTIVE = 29;
    /**
     * The watt hour unit code
     */
    public static final int WATTHOUR = 30;
    /**
     * The volt ampere hour unit code
     */
    public static final int VOLTAMPEREHOUR = 31;
    /**
     * The volt ampere reactive hour unit code
     */
    public static final int VOLTAMPEREREACTIVEHOUR = 32;
    /**
     * The ampere unit code
     */
    public static final int AMPERE = 33;
    /**
     * The coulomb unit code
     */
    public static final int COULOMB = 34;
    /**
     * The volt unit code
     */
    public static final int VOLT = 35;
    /**
     * The volt per meter unit code
     */
    public static final int VOLTPERMETER = 36;
    /**
     * The farad unit code
     */
    public static final int FARAD = 37;
    /**
     * The ohm unit code
     */
    public static final int OHM = 38;
    /**
     * The Ohm square meter per meter unit code
     */
    public static final int OHMSQUAREMETERPERMETER = 39;
    /**
     * The weber unit code
     */
    public static final int WEBER = 40;
    /**
     * The tesla unit code
     */
    public static final int TESLA = 41;
    /**
     * The amp�re per meter unit code
     */
    public static final int AMPEREPERMETER = 42;
    /**
     * The henry unit code
     */
    public static final int HENRY = 43;
    /**
     * The herz unit code
     */
    public static final int HERTZ = 44;
    /**
     * The active energy unit code
     */
    public static final int ACTIVEENERGY = 45;
    /**
     * The reactive energy unit code
     */
    public static final int REACTIVEENERGY = 46;
    /**
     * The apparent energy unit code
     */
    public static final int APPARENTENERGY = 47;
    /**
     * The volt square per hour unit code
     */
    public static final int VOLTSQUAREHOUR = 48;
    /**
     * The amp�re square per hour unit code
     */
    public static final int AMPERESQUAREHOUR = 49;
    /**
     * The kilogram per second unit code
     */
    public static final int KILOGRAMPERSECOND = 50;

    /**
     * The siemens unit code
     */
    public static final int SIEMENS = 51;
    /**
     * The kelvin unit code
     */
    public static final int KELVIN = 52;
    /**
     * the meter per hour unit code
     */
    public static final int METERPERHOUR = 53;

    /**
     * The dBm unit code
     */
    public static final int DECIBELPOWERRATIO = 70;

    /**
     * The not available code
     */
    public static final int NOTAVAILABLE = 254;
    /**
     * The count code
     */
    public static final int COUNT = 255;
    /**
     * The ratio code
     */
    public static final int RATIO = 255;
    /**
     * The unitless code
     */
    public static final int UNITLESS = 255;
    /**
     * The old kelvin unit code
     */
    private static final int OLDKELVIN = 301;
    /**
     * The per hour unit code
     */
    public static final int PERHOUR = 302;
    /**
     * The mol percentage unit code
     */
    public static final int MOLPERCENT = 303;
    /**
     * The joule per normal cubic meter unit code
     */
    public static final int JOULEPERNORMALCUBICMETER = 304;
    /**
     * The watt hour per normal cubic meter unit code
     */
    public static final int WATTHOURPERNORMALCUBICMETER = 305;
    /**
     * The watt hour per cubic meter unit code
     */
    public static final int WATTHOURPERCUBICMETER = 306;
    /**
     * The ton meter unit code
     */
    public static final int TON = 307;
    /**
     * The kilogram per hour unit code
     */
    public static final int KILOGRAMPERHOUR = 308;
    /**
     * The ton per hour unit code
     */
    public static final int TONPERHOUR = 309;
    /**
     * The liter per hour unit code
     */
    public static final int LITERPERHOUR = 311;
    /**
     * The Watt per square meter unit code
     */
    public static final int WATTPERSQUAREMETER = 312;

    // imperial and exotic units start at 401

    /**
     * The feet unit code
     */
    public static final int FEET = 401;
    /**
     * The feet per second unit code
     */
    public static final int FEETPERSECOND = 402;
    /**
     * The cubic feet unit code
     */
    public static final int CUBICFEET = 403;
    /**
     * The cubic feet per hour unit code
     */
    public static final int CUBICFEETPERHOUR = 404;
    /**
     * The cubic feet per day unit code
     */
    public static final int CUBICFEETPERDAY = 405;
    /**
     * The therm unit code
     */
    public static final int THERM = 406;
    /**
     * The therm per day unit code
     */
    public static final int THERMPERDAY = 407;
    /**
     * The therm per hour unit code
     */
    public static final int THERMPERHOUR = 408;
    /**
     * The therm per hour unit code
     */
    public static final int ACREFEET = 409;
    /**
     * The therm per hour unit code
     */
    public static final int ACREFEETPERHOUR = 410;

    // ANSI C12.19 table 12 units start at 501
    /**
     * The volt square unit code
     */
    public static final int VOLTSQUARE = 501;
    /**
     * The amp�re square unit code
     */
    public static final int AMPERESQUARE = 502;
    /**
     * The total harmonic distortion V IEEE unit code
     */
    public static final int TOTALHARMONICDISTORTIONV_IEEE = 503;
    /**
     * The total harmonic distortion I IEEE unit code
     */
    public static final int TOTALHARMONICDISTORTIONI_IEEE = 504;
    /**
     * The total harmonic distortion V IC unit code
     */
    public static final int TOTALHARMONICDISTORTIONV_IC = 505;
    /**
     * The total harmonic distortion I IC unit code
     */
    public static final int TOTALHARMONICDISTORTIONI_IC = 506;
    /**
     * The pascal per hour unit code
     */
    public static final int PASCALPERHOUR = 509;
    /**
     * The pound per square inch unit code
     */
    public static final int POUNDPERSQUAREINCH = 510;
    /**
     * The gram per square centimeter unit code
     */
    public static final int GRAMPERSQUARECENTIMETER = 511;
    /**
     * The meter mercury unit code
     */
    public static final int METERMERCURY = 512;
    /**
     * The inches mercury unit code
     */
    public static final int INCHESMERCURY = 513;
    /**
     * The inches water unit code
     */
    public static final int INCHESWATER = 514;
    /**
     * The percent unit code
     */
    public static final int PERCENT = 515;
    /**
     * The parts per million unit code
     */
    public static final int PARTSPERMILLION = 516;
    /**
     * The gallon unit code
     */
    public static final int GALLON = 517;
    /**
     * The gallon per hour unit code
     */
    public static final int GALLONPERHOUR = 518;
    /**
     * The inch unit code
     */
    public static final int INCH = 519;
    /**
     * Ampere per hour unit code
     */
    public static final int AMPEREHOUR = 520;
    /**
     * Volt per hour unit code
     */
    public static final int VOLTHOUR = 521;
    /**
     * Power quantity unit code
     */
    public static final int QUANTITYPOWER = 522;
    /**
     * Power quantity per hour unit code
     */
    public static final int QUANTITYPOWERHOUR = 523;
    /**
     * degree Fahrenheit unit code
     */
    public static final int FAHRENHEIT = 524;
    /**
     * US gallon unit code
     */
    public static final int US_GALLON = 525;
    /**
     * US gallon per hour unit code
     */
    public static final int US_GALLONPERHOUR = 526;
    /**
     * Imperial Gallon unit code
     */
    public static final int IMP_GALLON = 527;
    /**
     * Imperial gallon per hour unit code
     */
    public static final int IMP_GALLONPERHOUR = 528;
    /**
     * The cubic inch unit code
     */
    public static final int CUBICINCH = 529;
    /**
     * Cubic inch per hour unit code
     */
    public static final int CUBICINCHPERHOUR = 530;
    /**
     * The yard unit code
     */
    public static final int YARD = 531;
    /**
     * Cubic Yard unit code
     */
    public static final int CUBICYARD = 532;
    /**
     * Cubic Yard per hour unit code
     */
    public static final int CUBICYARDPERHOUR = 533;

    /**
     * British pound sign
     */
    public static final int POUND = 601;

    /**
     * Euro sign
     */
    public static final int EURO = 602;

    /**
     * Dollar sign
     */
    public static final int USD = 603;

    private static final Map<Integer, BaseUnit> units = new HashMap<Integer, BaseUnit>();

    static {
        // dlms units
        units.put(YEAR, new BaseUnit(YEAR, "a"));
        units.put(MONTH, new BaseUnit(MONTH, "mo"));
        units.put(WEEK, new BaseUnit(WEEK, "wk"));
        units.put(DAY, new BaseUnit(DAY, "d"));
        units.put(HOUR, new BaseUnit(HOUR, "h"));
        units.put(MINUTE, new BaseUnit(MINUTE, "min"));
        units.put(SECOND, new BaseUnit(SECOND, "s"));
        units.put(DEGREE, new BaseUnit(DEGREE, "\u00B0"));
        units.put(DEGREE_CELSIUS, new BaseUnit(DEGREE_CELSIUS, "\u00B0C"));
        units.put(METER, new BaseUnit(METER, "m"));
        units.put(METERPERSECOND, new BaseUnit(METERPERSECOND, "m/s"));
        units.put(CUBICMETER, new BaseUnit(CUBICMETER, "m3"));
        units.put(NORMALCUBICMETER, new BaseUnit(NORMALCUBICMETER, "Nm3"));
        units.put(METERPERHOUR, new BaseUnit(METERPERHOUR, "m/h"));
        units.put(CUBICMETERPERHOUR, new BaseUnit(CUBICMETERPERHOUR, "m3/h"));
        units.put(NORMALCUBICMETERPERHOUR, new BaseUnit(NORMALCUBICMETERPERHOUR, "Nm3/h"));
        units.put(CUBICMETERPERDAY, new BaseUnit(CUBICMETERPERDAY, "m3/d"));
        units.put(NORMALCUBICMETERPERDAY, new BaseUnit(NORMALCUBICMETERPERDAY, "Nm3/d"));
        units.put(LITER, new BaseUnit(LITER, "l"));
        units.put(KILOGRAM, new BaseUnit(KILOGRAM, "kg"));
        units.put(NEWTON, new BaseUnit(NEWTON, "N"));
        units.put(NEWTONMETER, new BaseUnit(NEWTONMETER, "Nm"));
        units.put(PASCAL, new BaseUnit(PASCAL, "Pa"));
        units.put(BAR, new BaseUnit(BAR, "bar"));
        units.put(JOULE, new BaseUnit(JOULE, "J"));
        units.put(JOULEPERHOUR, new BaseUnit(JOULEPERHOUR, "J/h"));
        units.put(WATT, new BaseUnit(WATT, "W"));
        units.put(VOLTAMPERE, new BaseUnit(VOLTAMPERE, "VA"));
        units.put(VOLTAMPEREREACTIVE, new BaseUnit(VOLTAMPEREREACTIVE, "var"));
        units.put(WATTHOUR, new BaseUnit(WATTHOUR, "Wh"));
        units.put(VOLTAMPEREHOUR, new BaseUnit(VOLTAMPEREHOUR, "VAh"));
        units.put(VOLTAMPEREREACTIVEHOUR, new BaseUnit(VOLTAMPEREREACTIVEHOUR, "varh"));
        units.put(AMPERE, new BaseUnit(AMPERE, "A"));
        units.put(COULOMB, new BaseUnit(COULOMB, "C"));
        units.put(VOLT, new BaseUnit(VOLT, "V"));
        units.put(VOLTPERMETER, new BaseUnit(VOLTPERMETER, "V/m"));
        units.put(FARAD, new BaseUnit(FARAD, "F"));
        units.put(OHM, new BaseUnit(OHM, "Ohm"));
        units.put(OHMSQUAREMETERPERMETER, new BaseUnit(OHMSQUAREMETERPERMETER, "Ohmm2/m"));
        units.put(WEBER, new BaseUnit(WEBER, "Wb"));
        units.put(TESLA, new BaseUnit(TESLA, "T"));
        units.put(AMPEREPERMETER, new BaseUnit(AMPEREPERMETER, "A/m"));
        units.put(HENRY, new BaseUnit(HENRY, "H"));
        units.put(HERTZ, new BaseUnit(HERTZ, "Hz"));
        units.put(ACTIVEENERGY, new BaseUnit(ACTIVEENERGY, "Rac"));
        units.put(REACTIVEENERGY, new BaseUnit(REACTIVEENERGY, "Rre"));
        units.put(APPARENTENERGY, new BaseUnit(APPARENTENERGY, "Rap"));
        units.put(VOLTSQUAREHOUR, new BaseUnit(VOLTSQUAREHOUR, "V2h"));
        units.put(AMPERESQUAREHOUR, new BaseUnit(AMPERESQUAREHOUR, "A2h"));
        units.put(KILOGRAMPERSECOND, new BaseUnit(KILOGRAMPERSECOND, "kg/s"));
        units.put(SIEMENS, new BaseUnit(SIEMENS, "Smho"));
        units.put(KELVIN, new BaseUnit(KELVIN, "K"));
        units.put(DECIBELPOWERRATIO, new BaseUnit(DECIBELPOWERRATIO, "dBm"));
        // end of dlms units

        units.put(NOTAVAILABLE, new BaseUnit(NOTAVAILABLE, "NA"));  // not available
        units.put(UNITLESS, new BaseUnit(UNITLESS, ""));   // count or unitless
        // end of resevered dlms codes


        // non dlms units start at 301
        // 301 used to be Kelvin now 52
        units.put(PERHOUR, new BaseUnit(PERHOUR, "/h"));  // per hour
        units.put(MOLPERCENT, new BaseUnit(MOLPERCENT, "mol%"));  // mol %
        units.put(JOULEPERNORMALCUBICMETER, new BaseUnit(JOULEPERNORMALCUBICMETER, "J/Nm3"));  // Joule per m3
        units.put(WATTHOURPERNORMALCUBICMETER, new BaseUnit(WATTHOURPERNORMALCUBICMETER, "Wh/Nm3"));  // Watt hour per normal m3
        units.put(WATTHOURPERCUBICMETER, new BaseUnit(WATTHOURPERCUBICMETER, "Wh/m3"));  // Watt hour per m3
        units.put(TON, new BaseUnit(TON, "t"));  // Ton
        units.put(KILOGRAMPERHOUR, new BaseUnit(KILOGRAMPERHOUR, "kg/h"));  // kg / hour
        units.put(TONPERHOUR, new BaseUnit(TONPERHOUR, "t/h"));  // Ton per hour
        units.put(LITERPERHOUR, new BaseUnit(LITERPERHOUR, "l/h"));
        units.put(WATTPERSQUAREMETER, new BaseUnit(WATTPERSQUAREMETER, "W/m2"));
        // end of normal units

        // exotic (uk) units start at 401
        units.put(FEET, new BaseUnit(FEET, "ft"));
        units.put(FEETPERSECOND, new BaseUnit(FEETPERSECOND, "ft/s"));
        units.put(CUBICFEET, new BaseUnit(CUBICFEET, "cf"));
        units.put(CUBICFEETPERHOUR, new BaseUnit(CUBICFEETPERHOUR, "cf/h"));
        units.put(CUBICFEETPERDAY, new BaseUnit(CUBICFEETPERDAY, "cf/d"));
        units.put(THERM, new BaseUnit(THERM, "therm"));
        units.put(THERMPERHOUR, new BaseUnit(THERMPERHOUR, "therm/h"));
        units.put(THERMPERDAY, new BaseUnit(THERMPERDAY, "therm/d"));
        units.put(ACREFEET, new BaseUnit(ACREFEET, "ac ft"));
        units.put(ACREFEETPERHOUR, new BaseUnit(ACREFEETPERHOUR, "ac ft/h"));

        // ANSI C12 units start at 501 (C12.19 table 12)
        units.put(VOLTSQUARE, new BaseUnit(VOLTSQUARE, "V2"));
        units.put(AMPERESQUARE, new BaseUnit(AMPERESQUARE, "A2"));
        units.put(TOTALHARMONICDISTORTIONV_IEEE, new BaseUnit(TOTALHARMONICDISTORTIONV_IEEE, "THDVIEEE")); // total harmonic distortion V IEEE
        units.put(TOTALHARMONICDISTORTIONI_IEEE, new BaseUnit(TOTALHARMONICDISTORTIONI_IEEE, "THDIIEEE")); // total harmonic distortion I IEEE
        units.put(TOTALHARMONICDISTORTIONV_IC, new BaseUnit(TOTALHARMONICDISTORTIONV_IC, "THDVIC")); // total harmonic distortion V IC
        units.put(TOTALHARMONICDISTORTIONI_IC, new BaseUnit(TOTALHARMONICDISTORTIONI_IC, "THDIIC")); // total harmonic distortion I IC
        units.put(PASCALPERHOUR, new BaseUnit(PASCALPERHOUR, "Pa/h"));
        units.put(POUNDPERSQUAREINCH, new BaseUnit(POUNDPERSQUAREINCH, "lb/in2"));
        units.put(GRAMPERSQUARECENTIMETER, new BaseUnit(GRAMPERSQUARECENTIMETER, "g/cm2"));
        units.put(METERMERCURY, new BaseUnit(METERMERCURY, "mHg"));
        units.put(INCHESMERCURY, new BaseUnit(INCHESMERCURY, "inHg"));
        units.put(INCHESWATER, new BaseUnit(INCHESWATER, "inH2O"));
        units.put(PERCENT, new BaseUnit(PERCENT, "%"));
        units.put(PARTSPERMILLION, new BaseUnit(PARTSPERMILLION, "ppm"));
        units.put(GALLON, new BaseUnit(GALLON, "gal"));
        units.put(GALLONPERHOUR, new BaseUnit(GALLONPERHOUR, "gal/h"));
        units.put(INCH, new BaseUnit(INCH, "in"));
        units.put(AMPEREHOUR, new BaseUnit(AMPEREHOUR, "Ah"));
        units.put(VOLTHOUR, new BaseUnit(VOLTHOUR, "Vh"));
        units.put(QUANTITYPOWER, new BaseUnit(QUANTITYPOWER, "Q"));
        units.put(QUANTITYPOWERHOUR, new BaseUnit(QUANTITYPOWERHOUR, "Qh"));
        units.put(FAHRENHEIT, new BaseUnit(FAHRENHEIT, "\u00B0F"));
        units.put(US_GALLON, new BaseUnit(US_GALLON, "gal"));
        units.put(US_GALLONPERHOUR, new BaseUnit(US_GALLONPERHOUR, "gal/h"));
        units.put(IMP_GALLON, new BaseUnit(IMP_GALLON, "IMP gal"));
        units.put(IMP_GALLONPERHOUR, new BaseUnit(IMP_GALLONPERHOUR, "IMP gal/h"));
        units.put(CUBICINCH, new BaseUnit(CUBICINCH, "in3"));
        units.put(CUBICINCHPERHOUR, new BaseUnit(CUBICINCHPERHOUR, "in3/h"));
        units.put(YARD, new BaseUnit(YARD, "yd"));
        units.put(CUBICYARD, new BaseUnit(CUBICYARD, "yd3"));
        units.put(CUBICYARDPERHOUR, new BaseUnit(CUBICYARDPERHOUR, "yd3/h"));

        units.put(POUND, new BaseUnit(POUND, "\u00A3"));
        units.put(EURO, new BaseUnit(EURO, "\u20AC"));
        units.put(USD, new BaseUnit(USD, "$"));

    }

    public static Iterator<BaseUnit> iterator() {
        return units.values().iterator();
    }

    private int dlmsCode;
    private String name;

    private BaseUnit(int dlmsCode, String name) {
        this.dlmsCode = dlmsCode;
        this.name = name;
    }

    /**
     * get the unit with the requested code
     *
     * @param code the code of the requested BaseUnit
     * @return with the requested code
     */
    public static BaseUnit get(int code) {
        // migration issues 10 used to be degree celsius
        if (code == 10) {
            code = DEGREE_CELSIUS;
        }
        if (code == OLDKELVIN) {
            code = KELVIN;
        }
        BaseUnit result = units.get(code);
        if (result == null) {
            throw new ApplicationException("Invalid code");
        }
        return result;
    }

    /**
     * Get code field
     *
     * @return the receiver's DMLS code
     */
    public int getDlmsCode() {
        return dlmsCode;
    }

    /**
     * Obtain a string representation of the receiver
     *
     * @return representation of the receiver
     */
    public String toString() {
        return name;
    }

    /**
     * Indicates whether some other object is equal to this one.
     * (has the same code)
     *
     * @param anObject - the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals(Object anObject) {
        if (anObject instanceof BaseUnit) {
            BaseUnit other = (BaseUnit) anObject;
            return dlmsCode == other.getDlmsCode();
        }
        return false;
    }

    /**
     * Returns a hash code value for the receiver.
     *
     * @return the hash code value.
     */
    public int hashCode() {
        return dlmsCode;
    }

    /**
     * Tests if the receiver is an undefined unit
     *
     * @return true if the unit is undefined, false otherwise
     */
    public boolean isUndefined() {
        return dlmsCode == UNITLESS;
    }

    /**
     * test if the receiver is a "volume" unit.
     * Each "volume" unit has a correspondig flow unit,
     * that represents the same quantity differentiated to time.
     * e.g. kWh -> kW  or m3 -> m3/h
     *
     * @return true if the receiver is a "volume" unit.
     */
    public boolean isVolumeUnit() {
        switch (dlmsCode) {
            case CUBICMETER:
            case NORMALCUBICMETER:
            case JOULE:
            case PASCAL:
            case VOLTSQUAREHOUR:
            case QUANTITYPOWERHOUR:
            case AMPERESQUAREHOUR:
            case WATTHOUR:
            case VOLTAMPEREHOUR:
            case VOLTAMPEREREACTIVEHOUR:
            case COUNT:
            case KILOGRAM:
            case TON:
            case LITER:
            case GALLON:
            case US_GALLON:
            case IMP_GALLON:
            case CUBICINCH:
            case CUBICYARD:
            case ACREFEET:
                return true;
            default:
                return false;
        }
    }

    /**
     * test if the receiver is a "flow" unit.
     * Quantities expressed in "flow" unit represent
     * an instantenous value. Each "flow" unit
     * has a correspondig flow unit, that represents
     * the same quantity integrated over time.
     * e.g. kW -> kWh and m3/h -> m3
     *
     * @return true if the receiver is a flow unit.
     */
    public boolean isFlowUnit() {
        switch (dlmsCode) {
            case CUBICMETERPERHOUR:
            case NORMALCUBICMETERPERHOUR:
            case JOULEPERHOUR:
            case PASCALPERHOUR:
            case VOLTSQUARE:
            case QUANTITYPOWER:
            case AMPERESQUARE:
            case WATT:
            case VOLTAMPERE:
            case VOLTAMPEREREACTIVE:
            case PERHOUR:
            case KILOGRAMPERHOUR:
            case TONPERHOUR:
            case LITERPERHOUR:
            case GALLONPERHOUR:
            case US_GALLONPERHOUR:
            case IMP_GALLONPERHOUR:
            case CUBICINCHPERHOUR:
            case CUBICYARDPERHOUR:
            case ACREFEETPERHOUR:
                return true;
            default:
                return false;
        }
    }

    /**
     * return the corresponding "flow" unit if the receiver is a
     * "volume" unit , null otherwise
     *
     * @return the corresponding "flow" unit or null
     */
    public BaseUnit getFlowUnit() {
        if (isFlowUnit()) {
            return this;
        }
        switch (dlmsCode) {

            case VOLTHOUR:
                return get(VOLT);

            case AMPEREHOUR:
                return get(AMPERE);

            case QUANTITYPOWERHOUR:
                return get(QUANTITYPOWER);

            case VOLTSQUAREHOUR:
                return get(VOLTSQUARE);
            case AMPERESQUAREHOUR:
                return get(AMPERESQUARE);

            case CUBICMETER:
                return get(CUBICMETERPERHOUR);
            case NORMALCUBICMETER:
                return get(NORMALCUBICMETERPERHOUR);
            case JOULE:
                return get(JOULEPERHOUR);
            case PASCAL:
                return get(PASCALPERHOUR);
            case WATTHOUR:
                return get(WATT);
            case VOLTAMPEREHOUR:
                return get(VOLTAMPERE);
            case VOLTAMPEREREACTIVEHOUR:
                return get(VOLTAMPEREREACTIVE);
            case COUNT:
                return get(PERHOUR);
            case KILOGRAM:
                return get(KILOGRAMPERHOUR);
            case TON:
                return get(TONPERHOUR);
            case LITER:
                return get(LITERPERHOUR);
            case GALLON:
                return get(GALLONPERHOUR);
            case US_GALLON:
                return get(US_GALLONPERHOUR);
            case IMP_GALLON:
                return get(IMP_GALLONPERHOUR);
            case CUBICINCH:
                return get(CUBICINCHPERHOUR);
            case CUBICYARD:
                return get(CUBICYARDPERHOUR);
            case ACREFEET:
                return get(ACREFEETPERHOUR);
            default:
                return null;
        }
    }

    /**
     * return the corresponding "volume" unit if the receiver is a
     * "flow" unit , null otherwise
     *
     * @return the corresponding "volume" unit or null
     */
    public BaseUnit getVolumeUnit() {
        if (isVolumeUnit()) {
            return this;
        }
        switch (dlmsCode) {

            case VOLT:
                return get(VOLTHOUR);
            case AMPERE:
                return get(AMPEREHOUR);

            case QUANTITYPOWER:
                return get(QUANTITYPOWERHOUR);

            case VOLTSQUARE:
                return get(VOLTSQUAREHOUR);
            case AMPERESQUARE:
                return get(AMPERESQUAREHOUR);

            case CUBICMETERPERHOUR:
                return get(CUBICMETER);
            case NORMALCUBICMETERPERHOUR:
                return get(NORMALCUBICMETER);
            case JOULEPERHOUR:
                return get(JOULE);
            case PASCALPERHOUR:
                return get(PASCAL);

            case WATT:
                return get(WATTHOUR);
            case VOLTAMPERE:
                return get(VOLTAMPEREHOUR);
            case VOLTAMPEREREACTIVE:
                return get(VOLTAMPEREREACTIVEHOUR);
            case PERHOUR:
                return get(COUNT);
            case KILOGRAMPERHOUR:
                return get(KILOGRAM);
            case TONPERHOUR:
                return get(TON);
            case LITERPERHOUR:
                return get(LITER);
            case GALLONPERHOUR:
                return get(GALLON);
            case US_GALLONPERHOUR:
                return get(US_GALLON);
            case IMP_GALLONPERHOUR:
                return get(IMP_GALLON);
            case CUBICINCHPERHOUR:
                return get(CUBICINCH);
            case CUBICYARDPERHOUR:
                return get(CUBICYARD);
            case ACREFEETPERHOUR:
                return get(ACREFEET);
            default:
                return null;
        }
    }
}

