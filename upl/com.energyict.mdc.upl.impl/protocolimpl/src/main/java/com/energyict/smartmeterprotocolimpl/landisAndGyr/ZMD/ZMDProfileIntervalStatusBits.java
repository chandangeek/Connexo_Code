package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 19/12/11
 * Time: 17:17
 */

public class ZMDProfileIntervalStatusBits implements ProfileIntervalStatusBits {

    static final int CRITICAL_ERROR = 0x01;
    static final int CLOCK_INVALID = 0x02;
    static final int CORRUPTED_MEASUREMENT = 0x04;
    static final int DAYLIGHT_SAVING = 0x08;
    static final int RESET_ALERT = 0x10;
    static final int CLOCK_ADJUSTED = 0x20;
    static final int POWER_UP = 0x40;
    static final int POWER_DOWN = 0x80;
    static final int EVENT_LOG_CLEARED = 0x2000;
    static final int LOAD_PROFILE_CLEARED = 0x4000;
    static final int STATUS_BEFORE_LAST_CLOCK_ADJUSTMENT = 0x8000;
    static final int START_OF_INTERVAL_SOI = 0x80000;
    static final int END_OF_INTERVAL_EOI = 0x100000;
    static final int ENERGY_REGISTER_CLEARED = 0x200000;
    static final int END_OF_INTERVAL_REGULAR_EXTERNAL = 0x400000;
    static final int END_OF_INTERVAL_REGULAR_INTERNAL = 0x800000;

    public static boolean isCriticalError(final int statusCodeProfile) {
        return (statusCodeProfile & CRITICAL_ERROR) == CRITICAL_ERROR;
    }

    public static boolean isClockInvalid(final int statusCodeProfile) {
        return (statusCodeProfile & CLOCK_INVALID) == CLOCK_INVALID;
    }

    public static boolean isCorruptedMeasurement(final int statusCodeProfile) {
        return (statusCodeProfile & CORRUPTED_MEASUREMENT) == CORRUPTED_MEASUREMENT;
    }

    public static boolean isDaylightSaving(final int statusCodeProfile) {
        return (statusCodeProfile & DAYLIGHT_SAVING) == DAYLIGHT_SAVING;
    }

    public static boolean isResetAlert(final int statusCodeProfile) {
        return (statusCodeProfile & RESET_ALERT) == RESET_ALERT;
    }

    public static boolean isClockAdjusted(final int statusCodeProfile) {
        return (statusCodeProfile & CLOCK_ADJUSTED) == CLOCK_ADJUSTED;
    }

    public static boolean isPowerUp(final int statusCodeProfile) {
        return (statusCodeProfile & POWER_UP) == POWER_UP;
    }

    public static boolean isPowerDown(final int statusCodeProfile) {
        return (statusCodeProfile & POWER_DOWN) == POWER_DOWN;
    }

    public static boolean isEventLogCleared(final int statusCodeProfile) {
        return (statusCodeProfile & EVENT_LOG_CLEARED) == EVENT_LOG_CLEARED;
    }

    public static boolean isLoadProfileCleared(final int statusCodeProfile) {
        return (statusCodeProfile & LOAD_PROFILE_CLEARED) == LOAD_PROFILE_CLEARED;
    }

    public static boolean isStatusBeforelastClockAdjustment(final int statusCodeProfile) {
        return (statusCodeProfile & STATUS_BEFORE_LAST_CLOCK_ADJUSTMENT) == STATUS_BEFORE_LAST_CLOCK_ADJUSTMENT;
    }

    public static boolean isSOI(final int statusCodeProfile) {
        return (statusCodeProfile & START_OF_INTERVAL_SOI) == START_OF_INTERVAL_SOI;
    }

    public static boolean isEOI(final int statusCodeProfile) {
        return (statusCodeProfile & END_OF_INTERVAL_EOI) == END_OF_INTERVAL_EOI;
    }

    public static boolean isEnergyRegistersCleared(final int statusCodeProfile) {
        return (statusCodeProfile & ENERGY_REGISTER_CLEARED) == ENERGY_REGISTER_CLEARED;
    }

    public static boolean isEOIRegularExternal(final int statusCodeProfile) {
        return (statusCodeProfile & END_OF_INTERVAL_REGULAR_EXTERNAL) == END_OF_INTERVAL_REGULAR_EXTERNAL;
    }

    public static boolean isEOIRegularInternal(final int statusCodeProfile) {
        return (statusCodeProfile & END_OF_INTERVAL_REGULAR_INTERNAL) == END_OF_INTERVAL_REGULAR_INTERNAL;
    }

    public int getEisStatusCode(final int statusCodeProfile) {
        int eiCode = 0;

        if (isCriticalError(statusCodeProfile)) {
            eiCode |= IntervalStateBits.DEVICE_ERROR;
        }
        if (isClockInvalid(statusCodeProfile)) {
            eiCode |= IntervalStateBits.BADTIME;
        }
        if (isCorruptedMeasurement(statusCodeProfile)) {
            eiCode |= IntervalStateBits.CORRUPTED;
        }
        if (isDaylightSaving(statusCodeProfile)) {
//            eiCode |= IntervalStateBits.OTHER;
            //do nothing...or do we want to see dst (other) flag for 6 months?
        }
        if (isResetAlert(statusCodeProfile)) {
            eiCode |= IntervalStateBits.OTHER;
        }
        if (isClockAdjusted(statusCodeProfile)) {
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        if (isPowerUp(statusCodeProfile)) {
            eiCode |= IntervalStateBits.POWERUP;
        }
        if (isPowerDown(statusCodeProfile)) {
            eiCode |= IntervalStateBits.POWERDOWN;
        }

        return eiCode;
    }
}
