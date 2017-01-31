/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class MBusValueTranslator {

    /**
     * Conversion is needed for 8, 16, 24, 32, 48 and 64 bit integer values.
     * The value (e.g. 154) received from the am500 is actually a hex representation, so the interpreted value is 340 (= 0x154)
     *
     * @param value the raw MBus value
     * @return the interpreted value
     * @throws java.io.IOException
     */
    public static long interpret(long value, int dif) throws IOException {
        if (dif == 0x01 || dif == 0x02 || dif == 0x03 || dif == 0x04 || dif == 0x06 || dif == 0x07) {
            value = ProtocolTools.getIntFromBytes(ProtocolTools.getBytesFromHexString(pad(String.valueOf(value), dif), ""));
        }
        return value;
    }

    private static String pad(String valueString, int dif) throws IOException {
        while (valueString.length() < (getValueLength(dif) * 2)) {
            valueString = "0" + valueString;
        }
        return valueString;
    }

    private static int getValueLength(int dif) throws IOException {
        switch (dif) {
            case 0x01:
                return 1;
            case 0x02:
                return 2;
            case 0x03:
                return 3;
            case 0x04:
                return 4;
            case 0x06:
                return 6;
            case 0x07:
                return 8;
            default:
                throw new IOException("Unexpected DIF while interpreting MBus value: '" + dif + "'");
        }
    }
}