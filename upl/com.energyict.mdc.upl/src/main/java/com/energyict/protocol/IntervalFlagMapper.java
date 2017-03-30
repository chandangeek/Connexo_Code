package com.energyict.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author khe
 * @since 13/05/2016 - 17:11
 */
public class IntervalFlagMapper {

    private static final Map<Integer, String> mapping = new HashMap<>();

    static {
        mapping.put(IntervalStateBits.POWERDOWN, ProtocolReadingQualities.POWERDOWN.getCimCode());
        mapping.put(IntervalStateBits.POWERUP, ProtocolReadingQualities.POWERUP.getCimCode());
        mapping.put(IntervalStateBits.SHORTLONG, ProtocolReadingQualities.SHORTLONG.getCimCode());
        mapping.put(IntervalStateBits.WATCHDOGRESET, ProtocolReadingQualities.WATCHDOGRESET.getCimCode());
        mapping.put(IntervalStateBits.CONFIGURATIONCHANGE, ProtocolReadingQualities.CONFIGURATIONCHANGE.getCimCode());
        mapping.put(IntervalStateBits.CORRUPTED, ProtocolReadingQualities.CORRUPTED.getCimCode());
        mapping.put(IntervalStateBits.OVERFLOW, ProtocolReadingQualities.OVERFLOW.getCimCode());
        mapping.put(IntervalStateBits.MISSING, ProtocolReadingQualities.MISSING.getCimCode());
        mapping.put(IntervalStateBits.MODIFIED, ProtocolReadingQualities.MODIFIED.getCimCode());
        mapping.put(IntervalStateBits.OTHER, ProtocolReadingQualities.OTHER.getCimCode());
        mapping.put(IntervalStateBits.REVERSERUN, ProtocolReadingQualities.REVERSERUN.getCimCode());
        mapping.put(IntervalStateBits.PHASEFAILURE, ProtocolReadingQualities.PHASEFAILURE.getCimCode());
        mapping.put(IntervalStateBits.BADTIME, ProtocolReadingQualities.BADTIME.getCimCode());
        mapping.put(IntervalStateBits.DEVICE_ERROR, ProtocolReadingQualities.DEVICE_ERROR.getCimCode());
        mapping.put(IntervalStateBits.BATTERY_LOW, ProtocolReadingQualities.BATTERY_LOW.getCimCode());
        mapping.put(IntervalStateBits.TEST, ProtocolReadingQualities.TEST.getCimCode());
    }

    /**
     * Map the given interval flags to the proper reading qualities.
     */
    public static List<String> map(int intervalFlags) {
        List<String> result = new ArrayList<>();

        for (int index = 0; index < Integer.SIZE; index++) {
            int bit = 1 << index;
            if ((intervalFlags & bit) == bit) {
                String rqt = mapping.get(bit);
                if (rqt != null) {
                    result.add(rqt);
                }
            }
        }

        return result;
    }
}