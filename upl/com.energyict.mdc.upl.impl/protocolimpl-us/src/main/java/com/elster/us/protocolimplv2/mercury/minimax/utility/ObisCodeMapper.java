package com.elster.us.protocolimplv2.mercury.minimax.utility;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.*;

import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps OBIS codes to the correct item ID for the mercury protocol spec
 *
 * @author James Fox
 */
public final class ObisCodeMapper {

    public final static String OBIS_CORRECTED_VOLUME = "7.11.12.1.0.255";
    public final static String OBIS_BATTERY_READING = "0.0.96.6.3.255";
    public final static String OBIS_UNCORRECTED_VOLUME = "7.0.7.0.0.255";
    public final static String OBIS_MAX_DAY_CORRECTED_VOLUME = "7.0.99.13.34.255";

    public final static String OBIS_CHANNEL_1 = "0.1.128.0.0.255";
    public final static String OBIS_CHANNEL_2 = "0.2.128.0.0.255";
    public final static String OBIS_CHANNEL_3 = "0.3.128.0.0.255";
    public final static String OBIS_CHANNEL_4 = "0.4.128.0.0.255";
    public final static String OBIS_CHANNEL_5 = "0.5.128.0.0.255";
    public final static String OBIS_CHANNEL_6 = "0.6.128.0.0.255";
    public final static String OBIS_CHANNEL_7 = "0.7.128.0.0.255";
    public final static String OBIS_CHANNEL_8 = "0.8.128.0.0.255";
    public final static String OBIS_CHANNEL_9 = "0.9.128.0.0.255";
    public final static String OBIS_CHANNEL_10 = "0.10.128.0.0.255";

    private static Map<ObisCode, String> map = new HashMap<ObisCode, String>();

    private static Map<String,ObisCode> channelToObisMap = new HashMap();

    static {
        // Register OBIS mappings
        map.put(ObisCode.fromString(OBIS_CORRECTED_VOLUME), OBJECT_CORRECTED_VOLUME);
        map.put(ObisCode.fromString(OBIS_BATTERY_READING), OBJECT_BATTERY_READING);
        map.put(ObisCode.fromString(OBIS_UNCORRECTED_VOLUME), OBJECT_UNCORRECTED_VOLUME);
        map.put(ObisCode.fromString(OBIS_MAX_DAY_CORRECTED_VOLUME), OBJECT_MAX_DAY);

        // Channel OBIS mappings - these are defined by us, i.e. we assign an OBIS to a channel ID
        // If the device is setup to have any other objects in the 10 "channels" in the audit log, then
        // mappings to OBIS codes will need to be added here
        channelToObisMap.put(OBJECT_INTERVAL_AVG_TEMP, ObisCode.fromString(OBIS_CHANNEL_1)); // Interval average temp
        channelToObisMap.put(OBJECT_INTERVAL_AVG_PRESS, ObisCode.fromString(OBIS_CHANNEL_2)); // Interval average pressure
        channelToObisMap.put(OBJECT_INTERVAL_UNC_VOL, ObisCode.fromString(OBIS_CHANNEL_3)); // Unc vol for interval
        channelToObisMap.put(OBJECT_INTERVAL_COR_VOL, ObisCode.fromString(OBIS_CHANNEL_4)); // Cor vol for interval
        channelToObisMap.put(OBJECT_BATTERY_READING, ObisCode.fromString(OBIS_CHANNEL_5)); // Battery voltage
        channelToObisMap.put(OBJECT_MAX_DAY, ObisCode.fromString(OBIS_CHANNEL_6)); // Max day cor vol (since last reset)
        channelToObisMap.put(OBJECT_MAX_DAY_DATE, ObisCode.fromString(OBIS_CHANNEL_7)); // Max day date
        channelToObisMap.put(OBJECT_UNCORRECTED_VOLUME, ObisCode.fromString(OBIS_CHANNEL_8)); // Unc vol accumulated
        channelToObisMap.put(OBJECT_CORRECTED_VOLUME, ObisCode.fromString(OBIS_CHANNEL_9)); // Cor vol accumulated
        channelToObisMap.put(OBJECT_INST_FLOW_RATE, ObisCode.fromString(OBIS_CHANNEL_10)); // Inst flow rate (CorVol/hour)
    }

    // Prevent instantiation
    private ObisCodeMapper() {}

    /**
     * Map an obis code to the object ID from the mercury spec
     * @param obisCode The obis code
     * @return mercury object ID, or null if not mapped
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

    public static List<ObisCode> mapDeviceChannels(List<String> channelsFromDevice) {
        List<ObisCode> retVal = new ArrayList<ObisCode>();
        if (channelsFromDevice != null) {
            for (String channelFromDevice : channelsFromDevice) {
                try {
                    channelFromDevice = String.format("%03d", Integer.parseInt(channelFromDevice));
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Channel from device must be parseable as integer number: " + channelFromDevice);
                }
                if (channelFromDevice.equals(CHANNEL_UNDEFINED)) {
                    retVal.add(null);
                } else {
                    retVal.add(mapChannelFromDevice(channelFromDevice));
                }
            }
        }
        return retVal;
    }

    private static ObisCode mapChannelFromDevice(String channelFromDevice) {
        return channelToObisMap.get(channelFromDevice);
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
