/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;

public class CalculateTest {


    //    static byte[] data = {(byte)0xF0,(byte)0x7F};

    @Test
    public void testNormSignedFP2NumberLE() {
        byte[] data = {(byte) 0x03, (byte) 0x00};
        Number number = Calculate.convertNormSignedFP2NumberLE(data, 0);
        assertEquals(BigDecimal.valueOf(0.000091552734375), number);
        System.out.println(number);

    }

}