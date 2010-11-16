package com.energyict.genericprotocolimpl.elster.ctr.common;

import com.energyict.genericprotocolimpl.elster.ctr.info.DeviceStatus;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 27-okt-2010
 * Time: 17:26:05
 */
public class DeviceStatusTest {

    @Test
    public void testDeviceStatus() {

        DeviceStatus deviceStatus1 = DeviceStatus.TO_BE_CONFIGURED;
        DeviceStatus deviceStatus2 = DeviceStatus.NORMAL;
        DeviceStatus deviceStatus3 = DeviceStatus.UNDER_MAINTENANCE;
        DeviceStatus deviceStatus4 = DeviceStatus.RESERVED_3;
        DeviceStatus deviceStatus5 = DeviceStatus.RESERVED_4;
        DeviceStatus deviceStatus6 = DeviceStatus.RESERVED_5;
        DeviceStatus deviceStatus7 = DeviceStatus.RESERVED_6;
        DeviceStatus deviceStatus8 = DeviceStatus.RESERVED_7;
        DeviceStatus deviceStatus9 = DeviceStatus.RESERVED_8;
        DeviceStatus deviceStatus10 = DeviceStatus.AVAILABLE_9;
        DeviceStatus deviceStatus11 = DeviceStatus.AVAILABLE_10;
        DeviceStatus deviceStatus12 = DeviceStatus.AVAILABLE_11;
        DeviceStatus deviceStatus13 = DeviceStatus.AVAILABLE_12;
        DeviceStatus deviceStatus14 = DeviceStatus.AVAILABLE_13;
        DeviceStatus deviceStatus15 = DeviceStatus.AVAILABLE_14;
        DeviceStatus deviceStatus16 = DeviceStatus.RESERVED_15;
        DeviceStatus deviceStatus17 = DeviceStatus.UNKNOWN;

        assertNotNull(deviceStatus1.getDescription());
        assertNotNull(deviceStatus1.getStatusCode());
        assertNotNull(deviceStatus2.getDescription());
        assertNotNull(deviceStatus2.getStatusCode());
        assertNotNull(deviceStatus3.getDescription());
        assertNotNull(deviceStatus3.getStatusCode());
        assertNotNull(deviceStatus4.getDescription());
        assertNotNull(deviceStatus4.getStatusCode());
        assertNotNull(deviceStatus5.getDescription());
        assertNotNull(deviceStatus5.getStatusCode());
        assertNotNull(deviceStatus6.getDescription());
        assertNotNull(deviceStatus6.getStatusCode());
        assertNotNull(deviceStatus7.getDescription());
        assertNotNull(deviceStatus7.getStatusCode());
        assertNotNull(deviceStatus8.getDescription());
        assertNotNull(deviceStatus8.getStatusCode());
        assertNotNull(deviceStatus9.getDescription());
        assertNotNull(deviceStatus9.getStatusCode());
        assertNotNull(deviceStatus10.getDescription());
        assertNotNull(deviceStatus10.getStatusCode());
        assertNotNull(deviceStatus11.getDescription());
        assertNotNull(deviceStatus11.getStatusCode());
        assertNotNull(deviceStatus12.getDescription());
        assertNotNull(deviceStatus12.getStatusCode());
        assertNotNull(deviceStatus13.getDescription());
        assertNotNull(deviceStatus13.getStatusCode());
        assertNotNull(deviceStatus14.getDescription());
        assertNotNull(deviceStatus14.getStatusCode());
        assertNotNull(deviceStatus15.getDescription());
        assertNotNull(deviceStatus15.getStatusCode());
        assertNotNull(deviceStatus16.getDescription());
        assertNotNull(deviceStatus16.getStatusCode());
        assertNotNull(deviceStatus17.getDescription());
        assertNotNull(deviceStatus17.getStatusCode());
    }
}
