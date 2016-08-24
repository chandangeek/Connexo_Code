package com.elster.us.protocolimplv2.sel.utility;


import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_KWH_DELIVERED;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_KVARH_DELIVERED;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_INTERVAL_KWH;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_INTERVAL_KVARH;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_INTERVAL_MWH3I;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_INTERVAL_MVRH3I;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_INTERVAL_MVRH3O;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_INTERVAL_MWH3O;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;

public class ObisCodeMapper {
  //TODO: fix/lookup correct obis codes
  public final static String OBIS_KWH_DELIVERED = "1.1.1.8.0.255";
  public final static String OBIS_KVARH_DELIVERED = "1.1.7.8.0.255";
  public final static String OBIS_KWH_RECEIVED = "1.1.2.8.0.255";
  public final static String OBIS_KVARH_RECEIVED = "1.1.6.8.0.255";

  public final static String OBIS_CHANNEL_1 = "0.1.128.0.0.255";
  public final static String OBIS_CHANNEL_2 = "0.2.128.0.0.255";
  public final static String OBIS_CHANNEL_3 = "0.3.128.0.0.255";
  public final static String OBIS_CHANNEL_4 = "0.4.128.0.0.255";
  public final static String OBIS_CHANNEL_5 = "0.5.128.0.0.255";
  public final static String OBIS_CHANNEL_6 = "0.6.128.0.0.255";

  private static Map<ObisCode, String> map = new HashMap<ObisCode, String>();

  private static Map<Integer,ObisCode> channelToObisMap = new HashMap();

  static {
      // Register OBIS mappings
      map.put(ObisCode.fromString(OBIS_KWH_DELIVERED), OBJECT_KWH_DELIVERED);
      map.put(ObisCode.fromString(OBIS_KVARH_DELIVERED), OBJECT_KVARH_DELIVERED);

      // Channel OBIS mappings - these are defined by us, i.e. we assign an OBIS to a channel ID
      // If the device is setup to have any other objects in the 2 "channels" in the LPData, then
      // mappings to OBIS codes will need to be added here
//      channelToObisMap.put(OBJECT_INTERVAL_KWH, ObisCode.fromString(OBIS_CHANNEL_1));     // kwh WH3_DEL
//      channelToObisMap.put(OBJECT_INTERVAL_KVARH, ObisCode.fromString(OBIS_CHANNEL_2));   // kvarh or QH3_DEL
//      channelToObisMap.put(OBJECT_INTERVAL_MWH3I, ObisCode.fromString(OBIS_CHANNEL_3));   // Mega-Watt-Hours-In
//      channelToObisMap.put(OBJECT_INTERVAL_MVRH3I, ObisCode.fromString(OBIS_CHANNEL_4));  // Mega-Volt-Amp-Hours-In
//      channelToObisMap.put(OBJECT_INTERVAL_MVRH3O, ObisCode.fromString(OBIS_CHANNEL_5));  // Mega-Volt-Amp-Hours-Out
//      channelToObisMap.put(OBJECT_INTERVAL_MWH3O, ObisCode.fromString(OBIS_CHANNEL_6));   // Mega-Watt-Hours-Out
      channelToObisMap.put(1, ObisCode.fromString(OBIS_CHANNEL_1));
      channelToObisMap.put(2, ObisCode.fromString(OBIS_CHANNEL_2));
      channelToObisMap.put(3, ObisCode.fromString(OBIS_CHANNEL_3));
      channelToObisMap.put(4, ObisCode.fromString(OBIS_CHANNEL_4));
      channelToObisMap.put(5, ObisCode.fromString(OBIS_CHANNEL_5));
      channelToObisMap.put(6, ObisCode.fromString(OBIS_CHANNEL_6));
      
  }

  // Prevent instantiation
  private ObisCodeMapper() {}

  /**
   * Map an obis code to the object ID from the sel spec
   * @param obisCode The obis code
   * @return sel object ID, or null if not mapped
   */
  public static String mapObisCode(ObisCode obisCode) {
      return map.get(obisCode);
  }

  /**
   * Map a list of obis codes to the object IDs from the mercury spec
   * @param obisCodes The list of obis codes
   * @return list containing mercury object IDs, or empty string if not mapped
   */
  public static List<String> mapObisCodes(List<ObisCode> obisCodes) {
      List<String> retVal = new ArrayList<String>();
      if (obisCodes != null) {
          for (ObisCode obisCode : obisCodes) {
              if (map.containsKey(obisCode)) {
                  retVal.add(map.get(obisCode));
              } else {
                  retVal.add("");
              }
          }
      }
      return retVal;
  }

  public static List<ObisCode> mapDeviceChannels(List<Integer> channelsFromDevice) {
      List<ObisCode> retVal = new ArrayList<ObisCode>();
      if (channelsFromDevice != null) {
          for (Integer channelFromDevice : channelsFromDevice) {
            retVal.add(mapChannelFromDevice(channelFromDevice));
          }
      }
      return retVal;
  }

  private static ObisCode mapChannelFromDevice(Integer channelFromDevice) {
      return channelToObisMap.get(channelFromDevice);
  }
  
  public static Integer getObisKeyByValue(ObisCode obis) {
    for(Entry<Integer, ObisCode> entry : channelToObisMap.entrySet()) {
      if(entry.getValue().equals(obis)) {
        return entry.getKey();
      }
    }
    return null;
  }

  /**
   * Map a register to a mercury object ID
   * @param register The {@link Register} to map
   * @return the mercury object ID, or null if not mapped
   */
  public static String mapRegister(Register register) {
      if (register == null) {
          throw new IllegalArgumentException("Register must not be null");
      }
      return mapObisCode(register.getObisCode());
  }

  /**
   * Map a list of registers to the object IDs from the mercury spec
   * @param registers The list of registers
   * @return list containing mercury object IDs, or empty string if not mapped
   */
  public static List<String> mapRegisters(List<OfflineRegister> registers) {
      if (registers == null) {
          throw new IllegalArgumentException("Register list cannot be null");
      }
      List<ObisCode> obisCodes = new ArrayList<ObisCode>();
      for (OfflineRegister register : registers) {
          obisCodes.add(register.getObisCode());
      }
      return mapObisCodes(obisCodes);
  }

}
