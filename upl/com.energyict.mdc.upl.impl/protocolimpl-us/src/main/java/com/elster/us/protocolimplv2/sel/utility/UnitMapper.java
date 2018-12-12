package com.elster.us.protocolimplv2.sel.utility;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class UnitMapper {
    private static Unit energyUnits;
    private static Unit voltageAmpereUnits;
    private static String energyDecimals;
    private static SimpleDateFormat dateFormat;
    private static SimpleDateFormat eventDateFormat;


    private static final Map<String,Unit> energyUnitMap = new TreeMap<String,Unit>(String.CASE_INSENSITIVE_ORDER);

    private static final Map<ObisCode, Unit> obisToUnitMap = new HashMap();

    private static final Map<String, Unit> channelToUnitMap = new TreeMap<String,Unit>(String.CASE_INSENSITIVE_ORDER);

    static {
        // Energy units
        energyUnitMap.put("kWh", Unit.get(BaseUnit.WATTHOUR, 3));               // kWh
        energyUnitMap.put("kVARh", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3)); //kvarh
        energyUnitMap.put("MWh", Unit.get(BaseUnit.WATTHOUR, 6));               // MWh
        energyUnitMap.put("MVARh", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 6)); //Mvarh
        // HPH1 SEL734 with EOI only -Setup the units for the registers
        energyUnitMap.put("MWH3I", Unit.get(BaseUnit.WATTHOUR, 3));
        energyUnitMap.put("MVRH3I", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3));
        energyUnitMap.put("MVRH3O", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3));
        energyUnitMap.put("MWH3O", Unit.get(BaseUnit.WATTHOUR, 3));


        //Energy units for channels
        channelToUnitMap.put("WH3_DEL", Unit.get(BaseUnit.WATTHOUR, 3));
        channelToUnitMap.put("QH3_DEL", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3));
        channelToUnitMap.put("MWH3I", Unit.get(BaseUnit.WATTHOUR, 6));
        channelToUnitMap.put("MVRH3I", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 6));
        channelToUnitMap.put("MVRH3O", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 6));
        channelToUnitMap.put("MWH3O", Unit.get(BaseUnit.WATTHOUR, 6));
    }

    private UnitMapper() {}



    public static void setEnergyDecimals(String energyDecimals) {
        UnitMapper.energyDecimals = energyDecimals;
    }

    public static Unit getVoltageAmpereUnits() {
        return voltageAmpereUnits;
    }


    public static Unit getEnergyUnits(String energyUnits) {
        Unit unit = null;
        unit = energyUnitMap.get(energyUnits.trim());
        return unit;
    }


    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }


    public static SimpleDateFormat getEventDateFormat() {return eventDateFormat;}

    public static void setupUnitMappings(Logger logger) {
        // HPH1 SEL734 with EOI only -Setup the units for the registers
//      obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_KWH_DELIVERED), getEnergyUnits());
//      obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_KVARH_DELIVERED), getVoltageAmpereUnits());
//      obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_KVARH_RECEIVED), getVoltageAmpereUnits());
//      obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_KWH_RECEIVED), getEnergyUnits());

        // Setup the units for the channels...
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_1), Unit.get(BaseUnit.WATTHOUR, 3));
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_2), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3));
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_3), Unit.get(BaseUnit.WATTHOUR, 6));
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_4), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 6));
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_5), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 6));
        obisToUnitMap.put(ObisCode.fromString(ObisCodeMapper.OBIS_CHANNEL_6), Unit.get(BaseUnit.WATTHOUR, 6));
    }

    public static Unit getUnitForObisCode(ObisCode obisCode) {
        return obisToUnitMap.get(obisCode);
    }

    public static Unit getUnitForChannelName(String channelName) {
        return channelToUnitMap.get(channelName);
    }


}
