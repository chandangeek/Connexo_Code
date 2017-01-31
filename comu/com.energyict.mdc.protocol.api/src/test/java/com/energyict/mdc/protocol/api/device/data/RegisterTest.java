/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.common.ObisCode;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Register Tests
 *
 * @author gna
 * @since 4/04/12 - 9:54
 */
public class RegisterTest {

    private static final ObisCode obisCode = ObisCode.fromString("1.0.1.8.0.255");
    private static final String meterSerialNumber = "SerialNumber1";
    private static final int rtuRegisterId = 1;

    @Test
    public void equalsTest() {
        Register reg1 = new Register(rtuRegisterId, obisCode, meterSerialNumber);
        assertTrue(reg1.equals(reg1));

        Register reg2 = new Register(rtuRegisterId, obisCode, meterSerialNumber);
        assertTrue(reg1.equals(reg2));

        Register reg3 = new Register(1000, obisCode, meterSerialNumber);
        assertFalse(reg1.equals(reg3));

        Register regNull1 = new Register(rtuRegisterId, null, meterSerialNumber);
        assertFalse(reg1.equals(regNull1));

        Register regNull2 = new Register(rtuRegisterId, obisCode, null);
        assertFalse(reg1.equals(regNull2));

        Register regDifferent = new Register(1000, null, null);
        assertFalse(reg1.equals(regDifferent));

        assertFalse(reg1.equals(null));
    }

}