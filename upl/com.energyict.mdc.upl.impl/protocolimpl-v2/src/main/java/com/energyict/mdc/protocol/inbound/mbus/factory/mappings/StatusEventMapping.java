/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */
package com.energyict.mdc.protocol.inbound.mbus.factory.mappings;

import com.energyict.protocol.MeterEvent;

/**
 * Mappings of the StatusField from the telegram body header
 * The table below is the Coding of the Status Field as defined by the 13757-3:2012 table 7
 * ----------------------------------------------------
 * Bit	status and error frames disabled
 * ----------------------------------------------------
 * 0,1  0b00 = No error
 *      0b01 = Application busy
 *      0b10 = Any application error
 *      0b11= Abnormal condition / alarm (any alarm)
 *
 * 2    0b1 Power low
 *      0b0 Power ok
 *
 * 3    0b1 permanent error
 *      0b0 No permanent error
 *
 * 4    0b1 temporary error
 *      0b0 No temporary error
 *
 * ---  Specific to manufacturer ---
 * 5    0b0 Leakage alarm cleared
 *      0b1 Leakage alarm (MLF)
 *
 * 6    0b0: Overconsumption alarm cleared
 *      0b1 : Actual Alarm burst (MBA)
 *
 * 7    0b0: device paired
 *      0b1 :a removal (or cable cut) detected
 * ---------------------------------------------------
 */

public enum StatusEventMapping {

    APPLICATION_OK          (false,0x03, 0,0 , "Application OK", MeterEvent.WATER_APPLICATION_OK),
    APPLICATION_BUSY        (true, 0x03, 0,1 , "Application Busy", MeterEvent.WATER_APPLICATION_BUSY),
    APPLICATION_ERROR       (true, 0x03, 0,2 , "Application Error", MeterEvent.WATER_APPLICATION_ERROR),
    ABNORMAL_SITUATION      (true, 0x03, 0,3 , "Abnormal Situation", MeterEvent.WATER_ABNORMAL_SITUATION),

    POWER_OK                (false, 0x04, 2, 0, "Power OK", MeterEvent.WATER_POWER_OK),
    POWER_LOW               (true,  0x04, 2, 1, "Power low", MeterEvent.WATER_POWER_LOW),

    PERMANENT_ERROR_NO      (false, 0x08, 3, 0, "No permanent error", MeterEvent.WATER_PERMANENT_ERROR_NO),
    PERMANENT_ERROR         (true,  0x08, 3, 1, "Permanent error", MeterEvent.WATER_PERMANENT_ERROR),

    TEMPORARY_ERROR_NO      (false, 0x10, 4, 0, "No temporary error", MeterEvent.WATER_TEMPORARY_ERROR_NO),
    TEMPORARY_ERROR         (true,  0x10, 4, 1, "Temporary error", MeterEvent.WATER_TEMPORARY_ERROR),

    LEAKAGE_ALARM_CLEARED   (false, 0x20, 5, 0, "Leakage alarm cleared", MeterEvent.WATER_LEAKAGE_ALARM_CLEARED),
    LEAKAGE_ALARM           (true,  0x20, 5, 1, "Leakage alarm MLF", MeterEvent.WATER_LEAKAGE_ALARM),

    OVERCONSUMPTION_CLEARED (false, 0x40, 6, 0, "Overconsumption alarm cleared", MeterEvent.WATER_OVERCONSUMPTION_CLEARED),
    OVERCONSUMPTION         (true,  0x40, 6, 1, "Actual alarm burst (MBA)", MeterEvent.WATER_OVERCONSUMPTION),

    DEVICE_PAIRED           (true,  0x80, 7, 0, "Device paired", MeterEvent.WATER_DEVICE_PAIRED),
    DEVICE_REMOVAL          (true,  0x80, 7, 1, "Removal or cable cut detected", MeterEvent.WATER_DEVICE_REMOVAL),

    ;


    private final int mask;
    private final int shift;
    private final int expectedValue;
    private final String message;
    private final int meterEvent;
    private final boolean isError;


    /**
     *  Will do the following: ((status & mask) >> shift) == expectedValue
     */
    StatusEventMapping(boolean isError, int mask, int shift, int expectedValue, String message, int meterEvent) {
        this.isError = isError;
        this.mask = mask;
        this.shift = shift;
        this.expectedValue = expectedValue;
        this.message = message;
        this.meterEvent = meterEvent;
    }

    public int getMask() {
        return mask;
    }

    public int getShift() {
        return shift;
    }

    public int getExpectedValue() {
        return expectedValue;
    }

    public String getMessage() {
        return message;
    }

    public int getMeterEvent() {
        return meterEvent;
    }

    public boolean isError() {
        return isError;
    }
}
