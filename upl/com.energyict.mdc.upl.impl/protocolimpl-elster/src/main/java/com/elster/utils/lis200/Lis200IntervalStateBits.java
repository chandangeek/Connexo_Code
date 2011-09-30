/**
 *
 */
package com.elster.utils.lis200;

import com.energyict.protocol.IntervalStateBits;

/**
 * @author gna
 * @since 28-mrt-2010
 */
public final class Lis200IntervalStateBits {

    /**
     * Private instance
     */
    private Lis200IntervalStateBits() {
    }

    private static final int RESTART = 0x0001;   // =  1
    // private static final int RESTART 0x0002;  // = 2;
    private static final int CLOCK_STOPPED = 0x0004; // = 3;
    private static final int POWER_SUPPLY_FAULT = 0x0008; // = 4;
    // private static final int RESTART = 0x0010; // = 5;
    // private static final int RESTART = 0x0020; // = 6;
    private static final int SOFTWARE_ERROR = 0x0040; // = 7;
    private static final int SETTING_ERROR = 0x0080; // = 8;
    private static final int BATTERY_LOW = 0x0100; // = 9;
    // private static final int RESTART = 0x0200; // = 10;
    private static final int CLOCK_NOT_ADJUSTED = 0x0400; // = 11;
    // private static final int RESTART = 0x0800; // = 12;
    @SuppressWarnings("unused")
    private static final int DATA_TRANSFER_RUNNING = 0x1000; // = 13;
    @SuppressWarnings("unused")
    private static final int CLOCK_SYNC = 0x2000; // = 14;
    @SuppressWarnings("unused")
    private static final int BATTERY_MODE = 0x4000; // = 15;
    private static final int TIME_IN_SUMMERTIME = 0x8000; // = 16;

    /**
     * Create the EICode for the given status code
     *
     * @param stateList - the statusCode(s) from the interval record from the device.
     *                     The status can contain multiple values separated by a
     *                     semicolon.
     * @return a valid EICode
     */
    public static int intervalStateBits(int stateList) {

        int result = 0;

        if ((RESTART & stateList) != 0) {
            result |= IntervalStateBits.OTHER;
        }
        if ((CLOCK_STOPPED & stateList) != 0) {
            result |= IntervalStateBits.BADTIME;
        }
        if ((POWER_SUPPLY_FAULT & stateList) != 0) {
            result |= IntervalStateBits.OTHER;
        }
        if ((SOFTWARE_ERROR & stateList) != 0) {
            result |= IntervalStateBits.DEVICE_ERROR;
        }
        if ((SETTING_ERROR & stateList) != 0) {
            result |= IntervalStateBits.DEVICE_ERROR;
        }
        if ((BATTERY_LOW & stateList) != 0) {
            result |= IntervalStateBits.BATTERY_LOW;
        }
        if ((CLOCK_NOT_ADJUSTED & stateList) != 0) {
            result |= IntervalStateBits.SHORTLONG;
            result |= IntervalStateBits.DEVICE_ERROR;
        }
        /* not longer marked in interval state
        * 06/30/2010 gh
        */
        //if (DATA_TRANSFER_RUNNING == Integer.valueOf(code)) {
        //	result |= IntervalStateBits.OTHER;
        //}

        // it seems like this one is not always set ...
        /* not longer marked in interval state, because it is marked separately in archive
        * 06/30/2010 gh
        */
        //if (CLOCK_SYNC == Integer.valueOf(code)) {
        //	result |= IntervalStateBits.SHORTLONG;
        //}
        // if(BATTERY_MODE == Integer.valueOf(code)) {
        // // just leave it like this, it's almost always in this mode ...
        // }
        // if(TIME_IN_SUMMERTIME == Integer.valueOf(code)) {
        // // Just leave it like this, it's have of the year in summer time,
        // just change it if
        // // the customer wants it.

        return result;
    }

    /**
     * Checks if given system status indicates that device time is summer time
     *
     * @param status - system status string from archive
     * @return boolean - true is bit 16 in status is set
     */
    public static boolean isSummerTime
            (String
                    status) {
        String[] codes = status.split(";");
        for (String code : codes) {
            if (TIME_IN_SUMMERTIME == Integer.valueOf(code)) {
                return true;
            }
        }
        return false;
    }
}