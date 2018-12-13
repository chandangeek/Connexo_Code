package com.elster.protocolimpl.lis200.utils;

import com.energyict.protocol.IntervalStateBits;

/**
 * User: heuckeg
 * Date: 28.10.2010
 * Time: 13:37:31
 */
public class utils {

    public static final int SYSSTATE_RESTART = 0x0001;

    public static final int SYSSTATE_DATARESTORED = 0x0004;
    public static final int SYSSTATE_POWERSUPPLYFAULT = 0x0008;

    public static final int SYSSTATE_SOFTWAREERROR = 0x0040;
    public static final int SYSSTATE_SETTINGERROR = 0x0080;
    public static final int SYSSTATE_BATTERYLOW = 0x0100;

    public static final int SYSSTATE_CLOCKNOTADJUSTED = 0x0400;

    @SuppressWarnings({"unused"})
    public static final int SYSSTATE_DATATRANSFERRUNNING = 0x1000;
    @SuppressWarnings({"unused"})
    public static final int SYSSTATE_CLOCKSYNC = 0x2000;
    @SuppressWarnings({"unused"})
    public static final int SYSSTATE_BATTERYMODE = 0x4000;
    @SuppressWarnings({"unused"})
    public static final int SYSSTATE_SUMMERTIME = 0x8000;

    public static final int SYSSTATES_USED = SYSSTATE_RESTART |
            SYSSTATE_DATARESTORED |
            SYSSTATE_POWERSUPPLYFAULT |
            SYSSTATE_SOFTWAREERROR |
            SYSSTATE_SETTINGERROR |
            SYSSTATE_BATTERYLOW |
            SYSSTATE_CLOCKNOTADJUSTED;

    /**
     * Create the EICode for the given status code
     *
     * @param sysState - the statusCode(s) from the interval record from the device.
     * @return a valid EICode
     */
    public static int SysStateToEIState(int sysState) {

        if ((sysState & SYSSTATES_USED) == 0)
            return IntervalStateBits.OK;

        int result = IntervalStateBits.OK;

        if ((SYSSTATE_RESTART & sysState) != 0) {
            result |= IntervalStateBits.OTHER;
        }
        if ((SYSSTATE_DATARESTORED & sysState) != 0) {
            result |= IntervalStateBits.BADTIME;
        }
        if ((SYSSTATE_POWERSUPPLYFAULT & sysState) != 0) {
            result |= IntervalStateBits.OTHER;
        }
        if ((SYSSTATE_SOFTWAREERROR & sysState) != 0) {
            result |= IntervalStateBits.DEVICE_ERROR;
        }
        if ((SYSSTATE_SETTINGERROR & sysState) != 0) {
            result |= IntervalStateBits.DEVICE_ERROR;
        }
        if ((SYSSTATE_BATTERYLOW & sysState) != 0) {
            result |= IntervalStateBits.BATTERY_LOW;
        }
        if ((SYSSTATE_CLOCKNOTADJUSTED & sysState) != 0) {
            result |= IntervalStateBits.SHORTLONG;
            result |= IntervalStateBits.DEVICE_ERROR;
        }
        return result;
    }


    public static String[] splitLine(String iecDataLine) {

        String[] result = iecDataLine.split("[)]");

        for (int i = 0; i < result.length; i++) {
            if (result[i].startsWith("(")) {
                result[i] = result[i].substring(1);
            }
        }

        return result;
    }

    public static int StateToInt(String s) {

        int state = 0;

        String[] codes = s.split(";");
        for (String code : codes) {
            int i = Integer.valueOf(code);
            if (i > 0) {
                state |= (1 << (i - 1));
            }
        }

        return state;
    }
}
