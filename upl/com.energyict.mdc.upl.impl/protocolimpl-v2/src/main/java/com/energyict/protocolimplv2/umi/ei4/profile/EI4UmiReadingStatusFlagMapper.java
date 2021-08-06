package com.energyict.protocolimplv2.umi.ei4.profile;

import com.energyict.protocol.IntervalStateBits;

import java.util.HashMap;
import java.util.Map;

public class EI4UmiReadingStatusFlagMapper {

    private static final Map<EI4UmiReadingStatusBits, Integer> mapping = new HashMap<>();

    static {
        mapping.put(EI4UmiReadingStatusBits.CLOCK_SYNC,                  IntervalStateBits.SHORTLONG);
        mapping.put(EI4UmiReadingStatusBits.CLOCK_SET,                   IntervalStateBits.CONFIGURATIONCHANGE);
        mapping.put(EI4UmiReadingStatusBits.ESTIMATED_VALUE,             IntervalStateBits.ESTIMATED);
        mapping.put(EI4UmiReadingStatusBits.INVALID_INTERVAL_DATA,       IntervalStateBits.CORRUPTED);
        mapping.put(EI4UmiReadingStatusBits.INVALID_TARIFF_STRUCTURE,    IntervalStateBits.DEVICE_ERROR);
        mapping.put(EI4UmiReadingStatusBits.SOFTWARE_RESTART,            IntervalStateBits.WATCHDOGRESET);
        mapping.put(EI4UmiReadingStatusBits.INCORRECT_DATETIME_INTERVAL, IntervalStateBits.BADTIME);
    }

    public static int map(int readingStatusFlags) throws NoSuchFieldException {
        int result = 0;
        for (int index = 0; index < Integer.SIZE; index++) {
            int bit = 1 << index;
            if ((readingStatusFlags & bit) == bit) {
                EI4UmiReadingStatusBits readingStatusBit = EI4UmiReadingStatusBits.fromBitNumber(index);
                result |= mapping.get(readingStatusBit);
            }
        }
        return result;
    }
}
