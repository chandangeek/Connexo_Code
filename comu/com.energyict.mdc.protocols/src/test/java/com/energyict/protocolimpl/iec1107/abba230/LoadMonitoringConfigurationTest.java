/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.protocolimpl.utils.ProtocolTools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author sva
 * @since 15/03/2016 - 10:16
 */
public class LoadMonitoringConfigurationTest {

    @Test
    public void testLoadMonitoringConfigurationConstructor() throws Exception {
        byte[] bytes = {1, 3, (byte) 0xBB, 0, 10, 0, 5, 4, (byte) 0xB0, 0, 8, 1, 0};

        // Business method
        LoadMonitoringConfiguration conf = new LoadMonitoringConfiguration(bytes);

        //Asserts
        assertEquals(1, conf.getActive());
        assertTrue(conf.isActive());
        assertEquals(955, conf.getLowerLevelThreshold());
        assertEquals(10, conf.getLowerLevelDuration());
        assertEquals(0, conf.getLowerContactorOpen());
        assertFalse(conf.isLowerContactorOpen());
        assertEquals(5, conf.getLowerAutoArmPeriod());

        assertEquals(1200, conf.getHigherLevelThreshold());
        assertEquals(8, conf.getHigherLevelDuration());
        assertEquals(1, conf.getHigherContactorOpen());
        assertTrue(conf.isHigherContactorOpen());
        assertEquals(0, conf.getHigherAutoArmPeriod());

        assertEquals(ProtocolTools.getHexStringFromBytes(bytes, ""), conf.buildData());
    }

    @Test
    public void testChangeHigherLevelValues() throws Exception {
        byte[] bytes = {1, 3, (byte) 0xBB, 0, 10, 0, 5, 4, (byte) 0xB0, 0, 8, 1, 0};

        LoadMonitoringConfiguration conf = new LoadMonitoringConfiguration(bytes);
        assertEquals(1200, conf.getHigherLevelThreshold());
        assertEquals(8, conf.getHigherLevelDuration());
        assertEquals(1, conf.getHigherContactorOpen());

        // Business method
        conf.setHigherLevelThreshold(10);
        conf.setHigherLevelDuration(120);
        conf.disableHigherContactorOpen();

        // Asserts
        assertEquals(10, conf.getHigherLevelThreshold());
        assertEquals(120, conf.getHigherLevelDuration());
        assertEquals(0, conf.getHigherContactorOpen());

        byte[] destinationBytes = {1, 3, (byte) 0xBB, 0, 10, 0, 5, 0, 10, 0, (byte) 120, 0, 0};
        assertEquals(ProtocolTools.getHexStringFromBytes(destinationBytes, ""), conf.buildData());
    }
}