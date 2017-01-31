/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.energyict.mdc.common.interval.IntervalStateBits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntervalFlagMapper {

    private static final Map<Integer, ReadingQualityType> mapping = new HashMap<>();

    static {
        mapping.put(IntervalStateBits.POWERDOWN, ProtocolReadingQualities.POWERDOWN.getReadingQualityType());
        mapping.put(IntervalStateBits.POWERUP, ProtocolReadingQualities.POWERUP.getReadingQualityType());
        mapping.put(IntervalStateBits.SHORTLONG, ProtocolReadingQualities.SHORTLONG.getReadingQualityType());
        mapping.put(IntervalStateBits.WATCHDOGRESET, ProtocolReadingQualities.WATCHDOGRESET.getReadingQualityType());
        mapping.put(IntervalStateBits.CONFIGURATIONCHANGE, ProtocolReadingQualities.CONFIGURATIONCHANGE.getReadingQualityType());
        mapping.put(IntervalStateBits.CORRUPTED, ProtocolReadingQualities.CORRUPTED.getReadingQualityType());
        mapping.put(IntervalStateBits.OVERFLOW, ProtocolReadingQualities.OVERFLOW.getReadingQualityType());
        mapping.put(IntervalStateBits.MISSING, ProtocolReadingQualities.MISSING.getReadingQualityType());
        mapping.put(IntervalStateBits.MODIFIED, ProtocolReadingQualities.MODIFIED.getReadingQualityType());
        mapping.put(IntervalStateBits.OTHER, ProtocolReadingQualities.OTHER.getReadingQualityType());
        mapping.put(IntervalStateBits.REVERSERUN, ProtocolReadingQualities.REVERSERUN.getReadingQualityType());
        mapping.put(IntervalStateBits.PHASEFAILURE, ProtocolReadingQualities.PHASEFAILURE.getReadingQualityType());
        mapping.put(IntervalStateBits.BADTIME, ProtocolReadingQualities.BADTIME.getReadingQualityType());
        mapping.put(IntervalStateBits.DEVICE_ERROR, ProtocolReadingQualities.DEVICE_ERROR.getReadingQualityType());
        mapping.put(IntervalStateBits.BATTERY_LOW, ProtocolReadingQualities.BATTERY_LOW.getReadingQualityType());
        mapping.put(IntervalStateBits.TEST, ProtocolReadingQualities.TEST.getReadingQualityType());
    }

    /**
     * Map the given interval flags to the proper reading qualities.
     */
    public static List<ReadingQualityType> map(int intervalFlags) {
        List<ReadingQualityType> result = new ArrayList<>();

        for (int index = 0; index < Integer.SIZE; index++) {
            int bit = 1 << index;
            if ((intervalFlags & bit) == bit) {
                ReadingQualityType rqt = mapping.get(bit);
                if (rqt != null) {
                    result.add(rqt);
                }
            }
        }

        return result;
    }
}