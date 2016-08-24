package com.elster.us.protocolimplv2.sel.utility;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;


import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

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

  public static void setEnergyUnits(String energyUnits, Logger logger) {
      if (energyUnits == null) {
          logger.severe("Energy units cannot be set to null");
          throw new IllegalArgumentException("Energy units cannot be null");
      }

      Unit unit = null;
      unit = energyUnitMap.get(energyUnits.trim());
     
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
  
  public static void setVoltageAmpereUnits(String voltageAmpereUnits, Logger logger) {
    if (voltageAmpereUnits == null) {
        logger.severe("Energy units cannot be set to null");
        throw new IllegalArgumentException("Energy units cannot be null");
    }

    Unit unit = null;
    unit = energyUnitMap.get(voltageAmpereUnits.trim());
    
    if (unit != null) {
        if (logger != null) {
            logger.fine("UnitMapper: energy unit is " + unit);
        }
    } else {
        if (logger != null) {
            logger.warning("UnitMapper: failed to set units for voltageAmpereUnits");
        }
    }
    UnitMapper.energyUnits = unit;
}

//  public static void setDateFormat(String dateFormat, Logger logger) {
//      if (dateFormat == null) {
//          if (logger != null) {
//              logger.severe("Date format cannot be set to null");
//          }
//          throw new IllegalArgumentException("Date format cannot be null");
//      }
//
//      // Will return default if string cannot be resolved
//      SimpleDateFormat format = DateFormatHelper.get(dateFormat.trim());
//
//      if (format != null) {
//          if (logger != null) {
//              logger.fine("UnitMapper: date format is " + format);
//          }
//      } else {
//          if (logger != null) {
//              logger.warning("UnitMapper: failed to set date format");
//          }
//      }
//      UnitMapper.dateFormat = format;
//
//      SimpleDateFormat eventFormat = DateFormatHelper.getForEvent(dateFormat.trim());
//      if (eventFormat != null) {
//          if (logger != null) {
//              logger.fine("UnitMapper: date format for events is " + eventFormat);
//          }
//      } else {
//          if (logger != null) {
//              logger.warning("UnitMapper: failed to set event date format");
//          }
//      }
//      UnitMapper.eventDateFormat = eventFormat;
//  }

  

  public static Unit getVoltageAmpereUnits() { 
    return voltageAmpereUnits; 
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
