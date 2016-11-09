package com.energyict.protocolimplv2.eict.rtuplusserver.g3;

import com.energyict.cbo.LastSeenDateInfo;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.meterdata.DeviceTopology;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RtuPlusServerTest {

    @Mock
    RtuPlusServer   rtuPlusServer;

    Logger logger = Logger.getAnonymousLogger();

    LastSeenDateInfo    lsdOld = new LastSeenDateInfo("LastSeenDate", new Date(100,0,1)); // 2000 Jan 01

    @Test
    public void testDuplicatedLSDRemoveOnlyLSD(){
        DeviceIdentifier    gateway = new DeviceIdentifierBySerialNumber("12345678");
        CollectedTopology topology = new DeviceTopology(gateway);

        DeviceIdentifier    meter1 = new DeviceIdentifierBySerialNumber("METER1");
        DeviceIdentifier    meter2 = new DeviceIdentifierBySerialNumber("METER2");
        DeviceIdentifier    meter3 = new DeviceIdentifierBySerialNumber("METER3");

        LastSeenDateInfo    lsd1 = new LastSeenDateInfo("LastSeenDate", new Date());

        topology.addSlaveDevice(meter1, lsd1);
        topology.addSlaveDevice(meter2, lsd1);
        topology.addSlaveDevice(meter3, lsd1);

        when(rtuPlusServer.cleanupDuplicatesLastSeenDate(topology)).thenCallRealMethod();
        when(rtuPlusServer.getLogger()).thenReturn(logger);

        rtuPlusServer.cleanupDuplicatesLastSeenDate(topology);

        assertEquals(3, topology.getSlaveDeviceIdentifiers().size());
        assertEquals(lsdOld.toString(), topology.getSlaveDeviceIdentifiers().get(meter1).toString());
        assertEquals(lsdOld.toString(), topology.getSlaveDeviceIdentifiers().get(meter2).toString());
        assertEquals(lsdOld.toString(), topology.getSlaveDeviceIdentifiers().get(meter3).toString());
    }

    @Test
    public void testDuplicatedLSDRemoveDuplicate(){
        DeviceIdentifier    gateway = new DeviceIdentifierBySerialNumber("12345678");
        CollectedTopology topology = new DeviceTopology(gateway);

        DeviceIdentifier    meter1 = new DeviceIdentifierBySerialNumber("METER1");
        DeviceIdentifier    meter2 = new DeviceIdentifierBySerialNumber("METER2");
        DeviceIdentifier    meter3 = new DeviceIdentifierBySerialNumber("METER3");

        LastSeenDateInfo    lsd1 = new LastSeenDateInfo("LastSeenDate", new Date());
        LastSeenDateInfo    lsd2 = new LastSeenDateInfo("LastSeenDate", new Date().getTime()-1000000);

        topology.addSlaveDevice(meter1, lsd1);
        topology.addSlaveDevice(meter2, lsd2);
        topology.addSlaveDevice(meter3, lsd1);

        when(rtuPlusServer.cleanupDuplicatesLastSeenDate(topology)).thenCallRealMethod();
        when(rtuPlusServer.getLogger()).thenReturn(logger);

        rtuPlusServer.cleanupDuplicatesLastSeenDate(topology);

        assertEquals(3, topology.getSlaveDeviceIdentifiers().size());
        assertEquals(lsdOld.toString(), topology.getSlaveDeviceIdentifiers().get(meter1).toString());
        assertNotNull(topology.getSlaveDeviceIdentifiers().get(meter2));
        assertEquals(lsdOld.toString(), topology.getSlaveDeviceIdentifiers().get(meter3).toString());
    }

    @Test
    public void testDuplicatedLSDRemoveNoDuplicate(){
        DeviceIdentifier    gateway = new DeviceIdentifierBySerialNumber("12345678");
        CollectedTopology topology = new DeviceTopology(gateway);

        DeviceIdentifier    meter1 = new DeviceIdentifierBySerialNumber("METER1");
        DeviceIdentifier    meter2 = new DeviceIdentifierBySerialNumber("METER2");
        DeviceIdentifier    meter3 = new DeviceIdentifierBySerialNumber("METER3");

        LastSeenDateInfo    lsd1 = new LastSeenDateInfo("LastSeenDate", new Date());
        LastSeenDateInfo    lsd2 = new LastSeenDateInfo("LastSeenDate", new Date().getTime()-1000000);
        LastSeenDateInfo    lsd3 = new LastSeenDateInfo("LastSeenDate", new Date().getTime()-2000000);

        topology.addSlaveDevice(meter1, lsd1);
        topology.addSlaveDevice(meter2, lsd2);
        topology.addSlaveDevice(meter3, lsd3);

        when(rtuPlusServer.cleanupDuplicatesLastSeenDate(topology)).thenCallRealMethod();
        when(rtuPlusServer.getLogger()).thenReturn(logger);

        rtuPlusServer.cleanupDuplicatesLastSeenDate(topology);

        assertEquals(3, topology.getSlaveDeviceIdentifiers().size());
        assertNotNull(topology.getSlaveDeviceIdentifiers().get(meter1));
        assertNotNull(topology.getSlaveDeviceIdentifiers().get(meter2));
        assertNotNull(topology.getSlaveDeviceIdentifiers().get(meter3));
    }
}