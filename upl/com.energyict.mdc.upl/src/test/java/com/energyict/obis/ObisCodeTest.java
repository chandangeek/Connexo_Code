/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.obis;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObisCodeTest {

    @Test
    public void equalsTest() {
        ObisCode obisCode = ObisCode.fromString("1.0.1.8.0.255");

        assertTrue(ObisCode.fromString("1.0.1.8.0.255").equals(obisCode));
        assertFalse(ObisCode.fromString("1.x.1.8.0.255").equals(obisCode));

        assertTrue(ObisCode.fromString("1.0.1.8.0.255").equalsIgnoreBChannel(obisCode));
        assertTrue(ObisCode.fromString("1.x.1.8.0.255").equalsIgnoreBChannel(obisCode));
        assertTrue(ObisCode.fromString("1.255.1.8.0.255").equalsIgnoreBChannel(obisCode));
        assertFalse(ObisCode.fromString("1.x.2.8.0.255").equalsIgnoreBChannel(obisCode));

        assertTrue(obisCode.nextB().equals(ObisCode.fromString("1.1.1.8.0.255")));
        assertTrue(ObisCode.fromString("1.1.1.8.0.255").setB(0).equals(obisCode));
    }

}