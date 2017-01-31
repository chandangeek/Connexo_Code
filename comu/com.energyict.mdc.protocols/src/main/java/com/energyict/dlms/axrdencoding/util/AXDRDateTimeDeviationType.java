/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding.util;

/**
 * AXDRDateTimeDeviationType refer to the interpretation of the Deviation field in a DLMS DateTime Object.
 * <b>The BlueBook 10th</b> defines the deviation as the offset in minutes of the local time to GMT (-720 ... 720).
 * <ul><li>Some interpret this as when the deviation is positive, then the local time is <i>before</i> GMT time.
 * <li>Other interpret this as when the deviation is positive, then the local time is <i>after</i> GMT time.
 * </ul>
 * Multiple meter vendors (including EICT) have either of the two possibilities. To cooperate with both, we need to be able to make a distinction which one to use.
 * This is what this simple Enumeration is for.
 */
public enum AXDRDateTimeDeviationType {

    /**
     * When the deviation is positive, then the GMT offset is positive.<br>
     * ex.<ul> <li>deviation is +2 -> {@link #getGmtNotation(int)} will result in <b>GMT+2</b>
     * <li>deviation is -2 ->{@link #getGmtNotation(int)} will result in <b>GMT-2</b>
     */
    Positive(false),

    /**
     * When the deviation is positive, then the GMT offset is negative.<br>
     * ex.<ul> <li>deviation is +2 -> {@link #getGmtNotation(int)} will result in <b>GMT-2</b>
     * <li>deviation is -2 ->{@link #getGmtNotation(int)} will result in <b>GMT+2</b>
     */
    Negative(true);

    private static final String GMT = "GMT";

    private final boolean inverse;

    AXDRDateTimeDeviationType(final boolean inverse) {
        this.inverse = inverse;
    }

    /**
     * Returns the GMT notation string from the given deviation argument
     *
     * @param deviation the deviation
     * @return the GMT notation of the given deviation
     */
    public String getGmtNotation(int deviation) {
        if(deviation == 0){
            return GMT;
        } else if (inverse) {
            deviation *= -1;
        }
        return GMT + (deviation < 0 ? "" : "+") + deviation;
    }

    public int getGmtOffset(int deviation){
        if(inverse){
            return deviation *= -1;
        } else {
            return deviation;
        }
    }
}
