package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.CommunicationTopologyEntry;
import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.util.time.Interval;
import org.joda.time.DateMidnight;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the methods that return topology information for a {@link DeviceImpl}.
 * More in details, the following methods will be tested.
 * <ul>
 * <li>{@link DeviceImpl#getCommunicationReferencingDevices()}</li>
 * <li>{@link DeviceImpl#getAllCommunicationReferencingDevices()}</li>
 * <li>{@link DeviceImpl#getCommunicationReferencingDevices(Date)}</li>
 * <li>{@link DeviceImpl#getAllCommunicationReferencingDevices(Date)}</li>
 * <li>{@link DeviceImpl#getAllCommunicationTopologies(Interval)}</li>
 * </ul>
 */
public class DeviceImplTopologyTest extends PersistenceIntegrationTest {

    private static final String DEVICENAME = "DeviceImplTopologyTest";
    private static final String MRID = "DeviceImplTopologyTest";
    private static final DateMidnight MIDNIGHT_MAY_31ST_2014 = new DateMidnight(2014, 5, 31);
    private static final DateMidnight MIDNIGHT_MAY_2ND_2014 = new DateMidnight(2014, 5, 2);
    private static final DateMidnight MIDNIGHT_JAN_1ST_2013 = new DateMidnight(2013, 1, 1);
    private static final DateMidnight MIDNIGHT_JAN_1ST_2014 = new DateMidnight(2014, 1, 1);
    private static final DateMidnight MIDNIGHT_JAN_10TH_2014 = new DateMidnight(2014, 1, 10);
    private static final DateMidnight MIDNIGHT_JAN_20TH_2014 = new DateMidnight(2014, 1, 20);
    private static final DateMidnight MIDNIGHT_FEB_1ST_2014 = new DateMidnight(2014, 2, 1);

    @Test
    @Transactional
    public void testCommunicationReferencingDevicesOnTodaysDateWithoutReferencingDevices () {
        Device master = this.createSimpleDevice();

        // Business method
        List<Device> slaves = master.getCommunicationReferencingDevices();

        // Asserts
        assertThat(slaves).isEmpty();
    }

    @Test
    @Transactional
    public void testCommunicationReferencingDevicesOnTodaysDateWithReferencingDevices () {
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        Device slave2 = this.createSlaveDevice("Slave2", master);
        long slave1Id = slave1.getId();
        long slave2Id = slave2.getId();

        // Business method
        List<Device> slaves = master.getCommunicationReferencingDevices();
        Set<Long> actualSlaveIds = new HashSet<>();
        for (Device slave : slaves) {
            actualSlaveIds.add(slave.getId());
        }

        // Asserts
        assertThat(actualSlaveIds).containsOnly(slave1Id, slave2Id);
    }

    @Test
    @Transactional
    public void testCommunicationReferencingDevicesOnDateWithoutAnyReferencingDevices () {
        Device master = this.createSimpleDevice();

        // Business method
        List<Device> slaves = master.getCommunicationReferencingDevices(new DateMidnight(2014, 5, 2).toDate());

        // Asserts
        assertThat(slaves).isEmpty();
    }

    @Test
    @Transactional
    public void testCommunicationReferencingDevicesOnDateWithoutReferencingDevicesOnThatDate () {
        when(clock.now()).thenReturn(MIDNIGHT_MAY_31ST_2014.toDate());
        Device master = this.createSimpleDevice();
        this.createSlaveDevice("Slave1", master);
        this.createSlaveDevice("Slave2", master);

        // Business method
        List<Device> slaves = master.getCommunicationReferencingDevices(MIDNIGHT_MAY_2ND_2014.toDate());

        // Asserts
        assertThat(slaves).isEmpty();
    }

    @Test
    @Transactional
    public void testCommunicationReferencingDevicesOnDateWithReferencingDevices () {
        when(clock.now()).thenReturn(MIDNIGHT_MAY_31ST_2014.toDate());
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        Device slave2 = this.createSlaveDevice("Slave2", master);
        long slave1Id = slave1.getId();
        long slave2Id = slave2.getId();

        // Business method
        List<Device> slaves = master.getCommunicationReferencingDevices(MIDNIGHT_MAY_31ST_2014.plusDays(1).toDate());
        Set<Long> actualSlaveIds = new HashSet<>();
        for (Device slave : slaves) {
            actualSlaveIds.add(slave.getId());
        }

        // Asserts
        assertThat(actualSlaveIds).containsOnly(slave1Id, slave2Id);
    }

    @Test
    @Transactional
    public void testAllCommunicationReferencingDevicesOnTodaysDateWithoutReferencingDevices () {
        Device master = this.createSimpleDevice();

        // Business method
        List<Device> slaves = master.getAllCommunicationReferencingDevices();

        // Asserts
        assertThat(slaves).isEmpty();
    }

    @Test
    @Transactional
    public void testAllCommunicationReferencingDevicesOnTodaysDateWithReferencingDevices () {
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        Device slave2 = this.createSlaveDevice("Slave2", slave1);
        long slave1Id = slave1.getId();
        long slave2Id = slave2.getId();

        // Business method
        List<Device> slaves = master.getAllCommunicationReferencingDevices();
        Set<Long> actualSlaveIds = new HashSet<>();
        for (Device slave : slaves) {
            actualSlaveIds.add(slave.getId());
        }

        // Asserts
        assertThat(actualSlaveIds).containsOnly(slave1Id, slave2Id);
    }

    @Test
    @Transactional
    public void testAllCommunicationReferencingDevicesOnDateWithoutAnyReferencingDevices () {
        Device master = this.createSimpleDevice();

        // Business method
        List<Device> slaves = master.getAllCommunicationReferencingDevices(new DateMidnight(2014, 5, 2).toDate());

        // Asserts
        assertThat(slaves).isEmpty();
    }

    @Test
    @Transactional
    public void testAllCommunicationReferencingDevicesOnDateWithoutReferencingDevicesOnThatDate () {
        when(clock.now()).thenReturn(MIDNIGHT_MAY_31ST_2014.toDate());
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        this.createSlaveDevice("Slave2", slave1);

        // Business method
        List<Device> slaves = master.getAllCommunicationReferencingDevices(MIDNIGHT_MAY_2ND_2014.toDate());

        // Asserts
        assertThat(slaves).isEmpty();
    }

    @Test
    @Transactional
    public void testAllCommunicationReferencingDevicesOnDateWithReferencingDevices () {
        when(clock.now()).thenReturn(MIDNIGHT_MAY_31ST_2014.toDate());
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        Device slave2 = this.createSlaveDevice("Slave2", slave1);
        long slave1Id = slave1.getId();
        long slave2Id = slave2.getId();

        // Business method
        List<Device> slaves = master.getAllCommunicationReferencingDevices(MIDNIGHT_MAY_31ST_2014.plusDays(1).toDate());
        Set<Long> actualSlaveIds = new HashSet<>();
        for (Device slave : slaves) {
            actualSlaveIds.add(slave.getId());
        }

        // Asserts
        assertThat(actualSlaveIds).containsOnly(slave1Id, slave2Id);
    }

    @Test
    @Transactional
    public void testAllCommunicationTopologiesWithoutReferencingDevices () {
        Device master = this.createSimpleDevice();

        // Business method
        List<CommunicationTopologyEntry> topologyEntries = master.getAllCommunicationTopologies(Interval.sinceEpoch());

        // Asserts
        assertThat(topologyEntries).isEmpty();
    }

    @Test
    @Transactional
    public void testAllCommunicationTopologiesWithoutReferencingDevicesOnThatDate () {
        when(clock.now()).thenReturn(MIDNIGHT_MAY_31ST_2014.toDate());
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        Device slave2 = this.createSlaveDevice("Slave2", slave1);

        // Business method
        List<CommunicationTopologyEntry> topologyEntries = master.getAllCommunicationTopologies(new Interval(null, MIDNIGHT_MAY_2ND_2014.toDate()));

        // Asserts
        assertThat(topologyEntries).isEmpty();
    }

    @Test
    @Transactional
    public void testAllCommunicationTopologiesWithReferencingDevices () {
        when(clock.now()).thenReturn(MIDNIGHT_MAY_31ST_2014.toDate());
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        Device slave2 = this.createSlaveDevice("Slave2", slave1);
        long slave1Id = slave1.getId();
        long slave2Id = slave2.getId();

        // Business method
        DateMidnight midnight_june_1st_2014 = MIDNIGHT_MAY_31ST_2014.plusDays(1);
        List<CommunicationTopologyEntry> topologyEntries = master.getAllCommunicationTopologies(new Interval(null, midnight_june_1st_2014.toDate()));

        // Asserts
        assertThat(topologyEntries).hasSize(1);
        CommunicationTopologyEntry topologyEntry = topologyEntries.get(0);
        assertThat(topologyEntry.getInterval()).isEqualTo(new Interval(MIDNIGHT_MAY_31ST_2014.toDate(), midnight_june_1st_2014.toDate()));
        List<Device> slaves = topologyEntry.getDevices();
        Set<Long> actualSlaveIds = new HashSet<>();
        for (Device slave : slaves) {
            actualSlaveIds.add(slave.getId());
        }
        assertThat(actualSlaveIds).containsOnly(slave1Id, slave2Id);
    }

    /**
     * Test the {@link DeviceImpl#getAllCommunicationTopologies(Interval)} method for the following case:
     * D1- jan 2013 - D8
     *   - jan 2014 - D2 - (2014-01-01, 2014-01-20] - D4
     *                      (2014-01-20, 2014-02-01] - D5
     *               - D3 - (2014-01-01, 2014-01-10] - D6
     *                      (2014-01-10, 2014-01-20] - D7
     *                      (2014-01-20, 2014-02-01] - D4, D6
     */
    @Test
    @Transactional
    public void testAllCommunicationTopologiesForComplexCase () {
        Device D1 = this.createSimpleDevice();
        when(clock.now()).thenReturn(MIDNIGHT_JAN_1ST_2013.toDate());
        Device D8 = this.createSlaveDevice("D8", D1);
        when(clock.now()).thenReturn(MIDNIGHT_JAN_1ST_2014.toDate());
        Device D2 = this.createSlaveDevice("D2", D1);
        Device D3 = this.createSlaveDevice("D3", D1);
        Device D4 = this.createSlaveDevice("D4", D2);
        Device D6 = this.createSlaveDevice("D6", D3);
        when(clock.now()).thenReturn(MIDNIGHT_JAN_20TH_2014.toDate());
        D4.setCommunicationGateway(D3);
        D4.save();
        Device D5 = this.createSlaveDevice("D5", D2);
        when(clock.now()).thenReturn(MIDNIGHT_JAN_10TH_2014.toDate());
        Device D7 = this.createSlaveDevice("D7", D3);
        D6.clearCommunicationGateway();
        D6.save();
        when(clock.now()).thenReturn(MIDNIGHT_JAN_20TH_2014.toDate());
        D6.setCommunicationGateway(D3);
        D6.save();
        D7.clearCommunicationGateway();
        D7.save();
        long D2_ID = D2.getId();
        long D3_ID = D3.getId();
        long D4_ID = D4.getId();
        long D5_ID = D5.getId();
        long D6_ID = D6.getId();
        long D7_ID = D7.getId();
        long D8_ID = D8.getId();

        // Business method
        List<CommunicationTopologyEntry> topologyEntries = D1.getAllCommunicationTopologies(new Interval(null, MIDNIGHT_MAY_31ST_2014.toDate()));

        // Asserts
        assertThat(topologyEntries).hasSize(4);
        CommunicationTopologyEntry firstTopologyEntry = topologyEntries.get(0);
        assertThat(firstTopologyEntry.getInterval()).isEqualTo(new Interval(MIDNIGHT_JAN_1ST_2013.toDate(), MIDNIGHT_JAN_1ST_2014.toDate()));
        this.assertDeviceIds(firstTopologyEntry.getDevices(), D8_ID);
        CommunicationTopologyEntry secondTopologyEntry = topologyEntries.get(1);
        assertThat(secondTopologyEntry.getInterval()).isEqualTo(new Interval(MIDNIGHT_JAN_1ST_2014.toDate(), MIDNIGHT_JAN_10TH_2014.toDate()));
        this.assertDeviceIds(secondTopologyEntry.getDevices(), D2_ID, D3_ID, D4_ID, D6_ID, D8_ID);
        CommunicationTopologyEntry thirdTopologyEntry = topologyEntries.get(2);
        assertThat(thirdTopologyEntry.getInterval()).isEqualTo(new Interval(MIDNIGHT_JAN_10TH_2014.toDate(), MIDNIGHT_JAN_20TH_2014.toDate()));
        this.assertDeviceIds(thirdTopologyEntry.getDevices(), D2_ID, D3_ID, D4_ID, D7_ID, D8_ID);
        CommunicationTopologyEntry fourthTopologyEntry = topologyEntries.get(3);
        assertThat(fourthTopologyEntry.getInterval()).isEqualTo(new Interval(MIDNIGHT_JAN_20TH_2014.toDate(), MIDNIGHT_MAY_31ST_2014.toDate()));
        this.assertDeviceIds(fourthTopologyEntry.getDevices(), D2_ID, D3_ID, D4_ID, D5_ID, D6_ID, D8_ID);
    }

    private void assertDeviceIds (List<Device> devices, Long... expectedDeviceIds) {
        Set<Long> actualIds = new HashSet<>();
        for (Device slave : devices) {
            actualIds.add(slave.getId());
        }
        assertThat(actualIds).containsOnly(expectedDeviceIds);
    }

    private Device createSimpleDevice() {
        return createSimpleDeviceWithName(DEVICENAME);
    }

    private Device createSimpleDeviceWithName(String name) {
        return createSimpleDeviceWithName(name, MRID);
    }

    private Device createSimpleDeviceWithName(String name, String mRID){
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, name, mRID);
        device.save();
        return device;
    }

    private Device createSlaveDevice(String name, Device master){
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, name, MRID + name);
        device.setCommunicationGateway(master);
        device.save();
        return device;
    }

}