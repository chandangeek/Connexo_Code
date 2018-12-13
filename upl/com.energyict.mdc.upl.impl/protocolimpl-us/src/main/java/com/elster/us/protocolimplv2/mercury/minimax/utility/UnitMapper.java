package com.elster.us.protocolimplv2.mercury.minimax.utility;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles mappings of UOMs between the Mercury EVC and EiServer
 *
 * @author James Fox
 */
public class UnitMapper {

    private static Unit pressureUnits;
    private static String pressureDecimals;
    private static Unit temperatureUnits;
    private static Unit uncVolUnits;
    private static Unit corVolUnits;
    private static Unit energyUnits;
    private static SimpleDateFormat dateFormat;
    private static SimpleDateFormat eventDateFormat;

    private static final Map<Integer,Unit> volUnitMap = new HashMap<Integer,Unit>();
    private static final Map<Integer,Unit> pressUnitMap = new HashMap<Integer,Unit>();
    private static final Map<Integer,Unit> tempUnitMap = new HashMap<Integer,Unit>();
    private static final Map<Integer,Unit> energyUnitMap = new HashMap<Integer,Unit>();

    private static final Map<ObisCode, Unit> obisToUnitMap = new HashMap();

    static {
        // Vol units
        volUnitMap.put(0, Unit.get(BaseUnit.CUBICFEET));      // Cu Ft
        volUnitMap.put(1, Unit.get(BaseUnit.CUBICFEET,1));    // Cu Ftx10
        volUnitMap.put(2, Unit.get(BaseUnit.CUBICFEET,2));    // CuFtx100
        volUnitMap.put(3, Unit.get(BaseUnit.CUBICFEET));      // CF
        volUnitMap.put(4, Unit.get(BaseUnit.CUBICFEET,1));    // CFx10
        volUnitMap.put(5, Unit.get(BaseUnit.CUBICFEET,2));    // CFx100
        volUnitMap.put(6, Unit.get(BaseUnit.CUBICFEET,3));    // CFx1000
        volUnitMap.put(7, Unit.get(BaseUnit.CUBICFEET,2));    // CCF
        volUnitMap.put(8, Unit.get(BaseUnit.CUBICFEET,3));    // MCF
        volUnitMap.put(9, Unit.get(BaseUnit.CUBICMETER,-1));  // m3x0.1
        volUnitMap.put(10, Unit.get(BaseUnit.CUBICMETER));    // m3
        volUnitMap.put(11, Unit.get(BaseUnit.CUBICMETER,1));  // m3x10
        volUnitMap.put(12, Unit.get(BaseUnit.CUBICMETER,2));  // m3x100
        volUnitMap.put(13, Unit.get(BaseUnit.CUBICMETER,3));  // m3x1000
        volUnitMap.put(14, Unit.get(BaseUnit.CUBICFEET,4));   // CFx10000
        volUnitMap.put(15, Unit.get(BaseUnit.THERM));         // Therms
        volUnitMap.put(16, Unit.get(BaseUnit.THERM, 1));      // DecaTherms
        volUnitMap.put(17, Unit.get(BaseUnit.JOULE, 6));      // MegaJoules
        volUnitMap.put(18, Unit.get(BaseUnit.JOULE, 9));      // GigaJoules
        volUnitMap.put(19, null);                             // KiloCals - not mapped in EiServer
        volUnitMap.put(20, Unit.get(BaseUnit.WATTHOUR, 3));   // kWh

        // Pressure units
        pressUnitMap.put(0, Unit.get(BaseUnit.POUNDPERSQUAREINCH));     // PSIG
        pressUnitMap.put(1, Unit.get(BaseUnit.POUNDPERSQUAREINCH));     // PSIA
        pressUnitMap.put(2, Unit.get(BaseUnit.PASCAL, 3));              // kPa
        pressUnitMap.put(3, Unit.get(BaseUnit.PASCAL, -3));             // mPa
        pressUnitMap.put(4, Unit.get(BaseUnit.BAR));                    // Bar
        pressUnitMap.put(5, Unit.get(BaseUnit.BAR, -3));                // mBar
        pressUnitMap.put(6, null);                                      // KGcm2 - no mapping in EiServer
        pressUnitMap.put(7, Unit.get(BaseUnit.INCHESWATER));            // in WC
        pressUnitMap.put(8, Unit.get(BaseUnit.INCHESMERCURY));          // in HG
        pressUnitMap.put(9, Unit.get(BaseUnit.METERMERCURY, -3));       // mm HG

        // Temp units
        tempUnitMap.put(0, Unit.get(BaseUnit.FAHRENHEIT));      // Fahrenheit
        tempUnitMap.put(1, Unit.get(BaseUnit.DEGREE_CELSIUS));  // Celsius
        tempUnitMap.put(2, null);                               // Rankine - not mapped in EIServer
        tempUnitMap.put(3, Unit.get(BaseUnit.KELVIN));          // Kelvin

        // Energy units
        energyUnitMap.put(0, Unit.get(BaseUnit.THERM));         // Therms
        energyUnitMap.put(1, Unit.get(BaseUnit.THERM, 1));      // DecaTherms
        energyUnitMap.put(2, Unit.get(BaseUnit.JOULE, 6));      // MegaJoules
        energyUnitMap.put(3, Unit.get(BaseUnit.JOULE, 9));      // GigaJoules
        energyUnitMap.put(4, null);                             // KiloCals - not mapped in EiServer
        energyUnitMap.put(5, Unit.get(BaseUnit.WATTHOUR, 3));   // kWh
    }

    private UnitMapper() {}

    public static void setPressureUnits(String pressureUnits, Logger logger) {
        if (pressureUnits == null) {
            logger.severe("Pressure units cannot be set to null");
            throw new IllegalArgumentException("Pressure units cannot be null");
        }

        Unit unit = null;
        try {
            unit = pressUnitMap.get(Integer.parseInt(pressureUnits.trim()));
        } catch (NumberFormatException nfe) {
            logger.severe("Cannot parse " + pressureUnits.trim() + " to a number");
            throw new IllegalArgumentException("Pressure unit string cannot be parsed to number");
        }

        if (unit != null) {
            if (logger != null) {
                logger.fine("UnitMapper: pressure unit is " + unit);
            }
        } else {
            if (logger != null) {
                logger.warning("UnitMapper: failed to set units for pressure");
            }
        }
        UnitMapper.pressureUnits = unit;
    }

    public static void setPressureDecimals(String pressureDecimals) {
        UnitMapper.pressureDecimals = pressureDecimals;
    }

    public static void setTemperatureUnits(String temperatureUnits, Logger logger) {
        if (temperatureUnits == null) {
            logger.severe("Temperature units cannot be set to null");
            throw new IllegalArgumentException("Temperature units cannot be null");
        }

        Unit unit = null;
        try {
            unit = tempUnitMap.get(Integer.parseInt(temperatureUnits.trim()));
        } catch (NumberFormatException nfe) {
            if (logger != null) {
                logger.warning("NumberFormatException for temperature unit " + temperatureUnits.trim());
            }
            throw new IllegalArgumentException(nfe);
        }

        if (unit != null) {
            if (logger != null) {
                logger.fine("UnitMapper: temperature unit is " + unit);
            }
        } else {
            if (logger != null) {
                logger.warning("UnitMapper: failed to set units for temperature");
            }
        }
        UnitMapper.temperatureUnits = unit;
    }

    public static void setCorVolUnits(String corVolUnits, Logger logger) {
        if (corVolUnits == null) {
            logger.severe("CorVol units cannot be set to null");
            throw new IllegalArgumentException("CorVol units cannot be null");
        }
        Unit unit = null;
        try {
            unit = volUnitMap.get(Integer.parseInt(corVolUnits.trim()));
        } catch (NumberFormatException nfe) {
            if (logger != null) {
                logger.warning("NumberFormatException for CorVol unit " + corVolUnits.trim());
            }
            throw new IllegalArgumentException(nfe);
        }

        if (unit != null) {
            if (logger != null) {
                logger.fine("UnitMapper: CorVol unit is " + unit);
            }
        } else {
            if (logger != null) {
                logger.warning("UnitMapper: failed to set units for CorVol");
            }
        }
        UnitMapper.corVolUnits = unit;
    }

    public static void setUncVolUnits(String uncVolUnits, Logger logger) {

        if (uncVolUnits == null) {
            if (logger != null) {
                logger.severe("UncVol units cannot be set to null");
            }
            throw new IllegalArgumentException("UncVol units cannot be null");
        }

        Unit unit = null;
        try {
            unit = volUnitMap.get(Integer.parseInt(uncVolUnits.trim()));
        } catch (NumberFormatException nfe) {
            if (logger != null) {
                logger.warning("NumberFormatException for UncVol unit " + uncVolUnits.trim());
            }
            throw new IllegalArgumentException(nfe);
        }
        if (unit != null) {
            if (logger != null) {
                logger.fine("UnitMapper: UncVol unit is " + unit);
            }
        } else {
            if (logger != null) {
                logger.warning("UnitMapper: failed to set units for UncVol");
            }
        }
        UnitMapper.uncVolUnits = unit;
    }

    public static void setEnergyUnits(String energyUnits, Logger logger) {
        if (energyUnits == null) {
            logger.severe("Energy units cannot be set to null");
            throw new IllegalArgumentException("Energy units cannot be null");
        }

        Unit unit = null;
        try {
            unit = energyUnitMap.get(Integer.parseInt(energyUnits.trim()));
        } catch (NumberFormatException nfe) {
            if (logger != null) {
                logger.warning("NumberFormatException for Energy unit " + energyUnits.trim());
            }
            throw new IllegalArgumentException(nfe);
        }

        if (unit != null) {
            if (logger != null) {
                logger.fine("UnitMapper: energy unit is " + unit);
            }
        } else {
            if (logger != null) {
                logger.warning("UnitMapper: failed to set units for energy");
            }
        }
        UnitMapper.energyUnits = unit;
    }

    public static void setDateFormat(String dateFormat, Logger logger) {
        if (dateFormat == null) {
            if (logger != null) {
                logger.severe("Date format cannot be set to null");
            }
            throw new IllegalArgumentException("Date format cannot be null");
        }

        // Will return default if string cannot be resolved
        SimpleDateFormat format = DateFormatHelper.get(dateFormat.trim());

        if (format != null) {
            if (logger != null) {
                logger.fine("UnitMapper: date format is " + format);
            }
        } else {
            if (logger != null) {
                logger.warning("UnitMapper: failed to set date format");
            }
        }
        UnitMapper.dateFormat = format;

        SimpleDateFormat eventFormat = DateFormatHelper.getForEvent(dateFormat.trim());
        if (eventFormat != null) {
            if (logger != null) {
                logger.fine("UnitMapper: date format for events is " + eventFormat);
            }
        } else {
            if (logger != null) {
                logger.warning("UnitMapper: failed to set event date format");
            }
        }
        UnitMapper.eventDateFormat = eventFormat;
    }

    public static Unit getPressureUnits() {
        return pressureUnits;
    }

    public static String getPressureDecimals() {
        return pressureDecimals;
    }

    public static Unit getTemperatureUnits() {
        return temperatureUnits;
    }

    public static Unit getUncVolUnits() {
        return uncVolUnits;
    }

    public static Unit getCorVolUnits() {
        return corVolUnits;
    }

    public static Unit getVoltageUnits() { return Unit.get("V"); }

    public static Unit getCorFlowUnits(Logger logger) {
        Unit corVolUnit = getCorVolUnits();
        BaseUnit cubicFeet = BaseUnit.get(BaseUnit.CUBICFEET);
        if (corVolUnit.getBaseUnit().equals(cubicFeet)) {
            return Unit.get(BaseUnit.CUBICFEETPERHOUR,corVolUnit.getScale());
        }
        logger.warning("Failed to determine flow units");
        return null;
    }

    public static Unit getEnergyUnits() {
        return energyUnits;
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }


    public static SimpleDateFormat getEventDateFormat() {return eventDateFormat;}

    public static void setupUnitMappings(Logger logger) {
        // Setup the units for the registers
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CORRECTED_VOLUME), getCorVolUnits());
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_UNCORRECTED_VOLUME), getUncVolUnits());
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_MAX_DAY_CORRECTED_VOLUME), getCorVolUnits());

        // Setup the units for the channels...
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_1), getTemperatureUnits());
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_2), getPressureUnits());
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_3), getUncVolUnits());
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_4), getCorVolUnits());
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_5), getVoltageUnits());
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_6), null);
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_7), null);
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_8), getUncVolUnits());
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_9), getCorVolUnits());
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_10), getCorFlowUnits(logger));
    }

    public static Unit getUnitForObisCode(ObisCode obisCode) {
        return obisToUnitMap.get(obisCode);
    }
}
