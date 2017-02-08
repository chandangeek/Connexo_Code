/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dialer.connections;

/**
 * Provides support for dialer classes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-27 (08:58)
 */
public final class DialerSupport {

    /**
     * returns a sub array from index to end
     *
     * @param data source array
     * @param from from index
     * @return subarray
     */
    public static byte[] getSubArray(byte[] data, int from) {
        byte[] subArray = new byte[data.length - from];
        for (int i = 0; i < subArray.length; i++) {
            subArray[i] = data[i + from];
        }
        return subArray;
    }

}