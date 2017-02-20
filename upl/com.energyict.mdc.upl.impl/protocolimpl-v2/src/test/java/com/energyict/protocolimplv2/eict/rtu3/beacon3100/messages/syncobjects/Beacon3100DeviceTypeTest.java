package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.mdc.tasks.ComTaskEnablement;
import com.energyict.mdc.tasks.ComTaskEnablementImpl;
import com.energyict.obis.ObisCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class Beacon3100DeviceTypeTest {

    @Test
    public void testEquals() throws Exception {
        Beacon3100MeterSerialConfiguration beaconSerial = new Beacon3100MeterSerialConfiguration(new ObisCode(0,0,96,1,0,255), 1);
        Beacon3100MeterSerialConfiguration beaconSerial1 = new Beacon3100MeterSerialConfiguration(new ObisCode(0,0,96,2,0,255), 1);


        Beacon3100ProtocolConfiguration beaconProtocol = new Beacon3100ProtocolConfiguration("com.beacon.1", new TypedProperties());
        Beacon3100ProtocolConfiguration beaconProtocol1 = new Beacon3100ProtocolConfiguration("com.beacon.2", new TypedProperties());

        List<Object> lpObisCodes= new ArrayList<>();
        lpObisCodes.add(new ObisCode(0,0,99,1,0,255));
        lpObisCodes.add(new ObisCode(0,0,99,2,0,255));


        List<Object> registersObisCodes= new ArrayList<>();
        registersObisCodes.add(new ObisCode(0,1,8,0,0,255));
        registersObisCodes.add(new ObisCode(0,2,8,0,0,255));
        registersObisCodes.add(new ObisCode(0,3,8,0,0,255));
        registersObisCodes.add(new ObisCode(0,4,8,0,0,255));


        List<Object> eventLogsObisCodes= new ArrayList<>();
        eventLogsObisCodes.add(new ObisCode(0,0,99,98,0,255));
        eventLogsObisCodes.add(new ObisCode(0,0,99,98,1,255));


        Beacon3100Schedulable schedulable1 = new Beacon3100Schedulable(new ComTaskEnablementImpl(), 1, 2, 3, lpObisCodes, registersObisCodes, eventLogsObisCodes );
        Beacon3100Schedulable schedulable2 = new Beacon3100Schedulable(new ComTaskEnablementImpl(), 10, 20, 30, lpObisCodes, registersObisCodes, eventLogsObisCodes );
        Beacon3100Schedulable schedulable3 = new Beacon3100Schedulable(new ComTaskEnablementImpl(), 2, 2, 3, lpObisCodes, registersObisCodes, eventLogsObisCodes );


        List<Beacon3100Schedulable> schedulableList = new ArrayList<Beacon3100Schedulable>();
        schedulableList.add(0, schedulable1);
        schedulableList.add(0, schedulable2);


        List<Beacon3100Schedulable> schedulableList1 = new ArrayList<Beacon3100Schedulable>();
        schedulableList.add(0, schedulable1);

        List<Beacon3100Schedulable> schedulableList3 = new ArrayList<Beacon3100Schedulable>();
        schedulableList.add(0, schedulable3);
        schedulableList.add(0, schedulable2);



        Beacon3100ClockSyncConfiguration clockSyncConfiguration = new Beacon3100ClockSyncConfiguration(true, 5, 3600);

        Beacon3100DeviceType    beacon3100DeviceType1 = new Beacon3100DeviceType(10, "SourceBeacon", beaconSerial, beaconProtocol, schedulableList, clockSyncConfiguration, true);

        Beacon3100DeviceType    beacon3100DeviceType2 = new Beacon3100DeviceType(20, "CheckBeacon", beaconSerial1, beaconProtocol1, schedulableList, clockSyncConfiguration, true);

        Beacon3100DeviceType    beacon3100DeviceType3 = new Beacon3100DeviceType(10, "SourceBeacon", beaconSerial, beaconProtocol, schedulableList, clockSyncConfiguration, true);
        Beacon3100DeviceType    beacon3100DeviceType4 = new Beacon3100DeviceType(10, "SourceBeacon", beaconSerial, beaconProtocol, schedulableList1, clockSyncConfiguration, true);
        Beacon3100DeviceType    beacon3100DeviceType5 = new Beacon3100DeviceType(10, "SourceBeacon", beaconSerial, beaconProtocol, schedulableList3, clockSyncConfiguration, true);

       // Logger logger =  Logger.getLogger(this.getClass().getName());
       // logger.info(beacon3100DeviceType1.toStructure().toString());
       // logger.info(beacon3100DeviceType2.toStructure().toString());
        assertFalse(beacon3100DeviceType1.equals(beacon3100DeviceType2.toStructure()));
        assertTrue(beacon3100DeviceType1.equals(beacon3100DeviceType3.toStructure()));
        assertFalse(beacon3100DeviceType1.equals(beacon3100DeviceType4.toStructure()));
        assertFalse(beacon3100DeviceType1.equals(beacon3100DeviceType5));


    }

    @Test
    public void testEquals_NewFW() throws Exception {
        Beacon3100MeterSerialConfiguration beaconSerial = new Beacon3100MeterSerialConfiguration(new ObisCode(0,0,96,1,0,255), 1);
        Beacon3100MeterSerialConfiguration beaconSerial1 = new Beacon3100MeterSerialConfiguration(new ObisCode(0,0,96,2,0,255), 1);


        Beacon3100ProtocolConfiguration beaconProtocol = new Beacon3100ProtocolConfiguration("com.beacon.1", new TypedProperties());
        Beacon3100ProtocolConfiguration beaconProtocol1 = new Beacon3100ProtocolConfiguration("com.beacon.2", new TypedProperties());

        List<Object> lpObisCodes= new ArrayList<>();
        lpObisCodes.add(new LoadProfileItem(new ObisCode(0,0,99,1,0,255)));
        lpObisCodes.add(new LoadProfileItem(new ObisCode(0,0,99,2,0,255)));


        List<Object> registersObisCodes= new ArrayList<>();
        registersObisCodes.add(new RegisterItem(new ObisCode(0,1,8,0,0,255), new Unsigned16(100)));
        registersObisCodes.add(new RegisterItem(new ObisCode(0,2,8,0,0,255), new Unsigned16(100)));
        registersObisCodes.add(new RegisterItem(new ObisCode(0,3,8,0,0,255), new Unsigned16(100)));
        registersObisCodes.add(new RegisterItem(new ObisCode(0,4,8,0,0,255), new Unsigned16(100)));


        List<Object> eventLogsObisCodes= new ArrayList<>();
        eventLogsObisCodes.add(new EventLogItem(new ObisCode(0,0,99,98,0,255)));
        eventLogsObisCodes.add(new EventLogItem(new ObisCode(0,0,99,98,1,255)));


        Beacon3100Schedulable schedulable1 = new Beacon3100Schedulable(new ComTaskEnablementImpl(), 1, 2, 3, lpObisCodes, registersObisCodes, eventLogsObisCodes, false );
        Beacon3100Schedulable schedulable2 = new Beacon3100Schedulable(new ComTaskEnablementImpl(), 10, 20, 30, lpObisCodes, registersObisCodes, eventLogsObisCodes , false);
        Beacon3100Schedulable schedulable3 = new Beacon3100Schedulable(new ComTaskEnablementImpl(), 2, 2, 3, lpObisCodes, registersObisCodes, eventLogsObisCodes, false );


        List<Beacon3100Schedulable> schedulableList = new ArrayList<Beacon3100Schedulable>();
        schedulableList.add(0, schedulable1);
        schedulableList.add(0, schedulable2);


        List<Beacon3100Schedulable> schedulableList1 = new ArrayList<Beacon3100Schedulable>();
        schedulableList.add(0, schedulable1);

        List<Beacon3100Schedulable> schedulableList3 = new ArrayList<Beacon3100Schedulable>();
        schedulableList.add(0, schedulable3);
        schedulableList.add(0, schedulable2);



        Beacon3100ClockSyncConfiguration clockSyncConfiguration = new Beacon3100ClockSyncConfiguration(true, 5, 3600);

        Beacon3100DeviceType    beacon3100DeviceType1 = new Beacon3100DeviceType(10, "SourceBeacon", beaconSerial, beaconProtocol, schedulableList, clockSyncConfiguration, false);

        Beacon3100DeviceType    beacon3100DeviceType2 = new Beacon3100DeviceType(20, "CheckBeacon", beaconSerial1, beaconProtocol1, schedulableList, clockSyncConfiguration, false);

        Beacon3100DeviceType    beacon3100DeviceType3 = new Beacon3100DeviceType(10, "SourceBeacon", beaconSerial, beaconProtocol, schedulableList, clockSyncConfiguration, false);
        Beacon3100DeviceType    beacon3100DeviceType4 = new Beacon3100DeviceType(10, "SourceBeacon", beaconSerial, beaconProtocol, schedulableList1, clockSyncConfiguration, false);
        Beacon3100DeviceType    beacon3100DeviceType5 = new Beacon3100DeviceType(10, "SourceBeacon", beaconSerial, beaconProtocol, schedulableList3, clockSyncConfiguration, false);

        // Logger logger =  Logger.getLogger(this.getClass().getName());
        // logger.info(beacon3100DeviceType1.toStructure().toString());
        // logger.info(beacon3100DeviceType2.toStructure().toString());
        assertFalse(beacon3100DeviceType1.equals(beacon3100DeviceType2.toStructure(false)));
        assertTrue(beacon3100DeviceType1.equals(beacon3100DeviceType3.toStructure(false)));
        assertFalse(beacon3100DeviceType1.equals(beacon3100DeviceType4.toStructure(false)));
        assertFalse(beacon3100DeviceType1.equals(beacon3100DeviceType5));


    }
}