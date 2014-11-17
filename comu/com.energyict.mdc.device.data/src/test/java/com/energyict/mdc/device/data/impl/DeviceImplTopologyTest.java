package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceTopology;
import com.energyict.mdc.device.data.TopologyTimeline;
import com.energyict.mdc.device.data.TopologyTimeslice;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.google.common.collect.Range;
import org.joda.time.DateMidnight;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
 * <li>{@link DeviceImpl#getCommunicationReferencingDevices(Instant)}</li>
 * <li>{@link DeviceImpl#getAllCommunicationReferencingDevices(Instant)}</li>
 * <li>{@link DeviceImpl#getCommunicationTopology(Range)}</li>
 * </ul>
 */
public class DeviceImplTopologyTest extends PersistenceIntegrationTest {

    private static final String DEVICENAME = "DeviceImplTopologyTest";
    private static final String MRID = "DeviceImplTopologyTest";
    private static final LocalDateTime MIDNIGHT_MAY_31ST_2014 = LocalDateTime.of(2014, Month.MAY, 31, 0, 0, 0);
    private static final Instant MIDNIGHT_MAY_2ND_2014 = LocalDateTime.of(2014, Month.MAY, 2, 0, 0, 0).toInstant(ZoneOffset.UTC);
    private static final Instant MIDNIGHT_JAN_1ST_2013 = LocalDateTime.of(2013, Month.JANUARY, 1, 0, 0, 0).toInstant(ZoneOffset.UTC);
    private static final Instant MIDNIGHT_JAN_1ST_2014 = LocalDateTime.of(2014, Month.JANUARY, 1, 0, 0, 0).toInstant(ZoneOffset.UTC);
    private static final Instant MIDNIGHT_JAN_10TH_2014 = LocalDateTime.of(2014, Month.JANUARY, 10, 0, 0, 0).toInstant(ZoneOffset.UTC);
    private static final Instant MIDNIGHT_JAN_20TH_2014 = LocalDateTime.of(2014, Month.JANUARY, 20, 0, 0, 0).toInstant(ZoneOffset.UTC);
    private static final Instant MIDNIGHT_FEB_1ST_2014 = LocalDateTime.of(2014, Month.FEBRUARY, 1, 0, 0, 0).toInstant(ZoneOffset.UTC);

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
        List<Device> slaves = master.getCommunicationReferencingDevices(new DateMidnight(2014, 5, 2).toDate().toInstant());

        // Asserts
        assertThat(slaves).isEmpty();
    }

    @Test
    @Transactional
    public void testCommunicationReferencingDevicesOnDateWithoutReferencingDevicesOnThatDate () {
        when(clock.instant()).thenReturn(MIDNIGHT_MAY_31ST_2014.toInstant(ZoneOffset.UTC));
        Device master = this.createSimpleDevice();
        this.createSlaveDevice("Slave1", master);
        this.createSlaveDevice("Slave2", master);

        // Business method
        List<Device> slaves = master.getCommunicationReferencingDevices(MIDNIGHT_MAY_2ND_2014);

        // Asserts
        assertThat(slaves).isEmpty();
    }

    @Test
    @Transactional
    public void testCommunicationReferencingDevicesOnDateWithReferencingDevices () {
        when(clock.instant()).thenReturn(MIDNIGHT_MAY_31ST_2014.toInstant(ZoneOffset.UTC));
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        Device slave2 = this.createSlaveDevice("Slave2", master);
        long slave1Id = slave1.getId();
        long slave2Id = slave2.getId();

        // Business method
        List<Device> slaves = master.getCommunicationReferencingDevices(MIDNIGHT_MAY_31ST_2014.plusDays(1).toInstant(ZoneOffset.UTC));
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
        List<Device> slaves = master.getAllCommunicationReferencingDevices(LocalDateTime.of(2014, Month.MAY, 2, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));

        // Asserts
        assertThat(slaves).isEmpty();
    }

    @Test
    @Transactional
    public void testAllCommunicationReferencingDevicesOnDateWithoutReferencingDevicesOnThatDate () {
        when(clock.instant()).thenReturn(MIDNIGHT_MAY_31ST_2014.toInstant(ZoneOffset.UTC));
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        this.createSlaveDevice("Slave2", slave1);

        // Business method
        List<Device> slaves = master.getAllCommunicationReferencingDevices(MIDNIGHT_MAY_2ND_2014);

        // Asserts
        assertThat(slaves).isEmpty();
    }

    @Test
    @Transactional
    public void testAllCommunicationReferencingDevicesOnDateWithReferencingDevices () {
        when(clock.instant()).thenReturn(MIDNIGHT_MAY_31ST_2014.toInstant(ZoneOffset.UTC));
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        Device slave2 = this.createSlaveDevice("Slave2", slave1);
        long slave1Id = slave1.getId();
        long slave2Id = slave2.getId();

        // Business method
        List<Device> slaves = master.getAllCommunicationReferencingDevices(MIDNIGHT_MAY_31ST_2014.plusDays(1).toInstant(ZoneOffset.UTC));
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
        DeviceTopology topology = master.getCommunicationTopology(Range.atMost(Instant.now()));

        // Asserts
        assertThat(topology.getAllDevices()).isEmpty();
    }

    @Test
    @Transactional
    public void testAllCommunicationTopologiesWithoutReferencingDevicesOnThatDate () {
        when(clock.instant()).thenReturn(MIDNIGHT_MAY_31ST_2014.toInstant(ZoneOffset.UTC));
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        Device slave2 = this.createSlaveDevice("Slave2", slave1);

        // Business method
        DeviceTopology topology = master.getCommunicationTopology(Range.atMost(Instant.from(MIDNIGHT_MAY_2ND_2014)));

        // Asserts
        assertThat(topology.getDevices()).isEmpty();
        assertThat(topology.getAllDevices()).isEmpty();
    }

    @Test
    @Transactional
    public void testAllCommunicationTopologiesWithReferencingDevices () {
        when(clock.instant()).thenReturn(MIDNIGHT_MAY_31ST_2014.toInstant(ZoneOffset.UTC));
        Device master = this.createSimpleDevice();
        Device slave1 = this.createSlaveDevice("Slave1", master);
        Device slave2 = this.createSlaveDevice("Slave2", slave1);
        long slave1Id = slave1.getId();
        long slave2Id = slave2.getId();

        // Business method
        LocalDateTime midnight_june_1st_2014 = MIDNIGHT_MAY_31ST_2014.plusDays(1);
        DeviceTopology topology = master.getCommunicationTopology(Range.atMost(midnight_june_1st_2014.toInstant(ZoneOffset.UTC)));
        TopologyTimeline timeline = topology.timelined();

        // Asserts
        assertThat(timeline.getSlices()).hasSize(1);
        TopologyTimeslice topologyEntry = timeline.getSlices().get(0);
        assertThat(topologyEntry.getPeriod()).isEqualTo(Range.<Instant>closed(MIDNIGHT_MAY_31ST_2014.toInstant(ZoneOffset.UTC), midnight_june_1st_2014.toInstant(ZoneOffset.UTC)));
        List<Device> slaves = topologyEntry.getDevices();
        Set<Long> actualSlaveIds = new HashSet<>();
        for (Device slave : slaves) {
            actualSlaveIds.add(slave.getId());
        }
        assertThat(actualSlaveIds).containsOnly(slave1Id, slave2Id);
    }

    /**
     * Test the {@link DeviceImpl#getCommunicationTopology(Range)} method for the following case:
     * D1- jan 2013 - D8
     *   - jan 2014 - D2 - (2014-01-01, 2014-01-20] - D4
     *                     (2014-01-20, 2014-02-01] - D5
     *              - D3 - (2014-01-01, 2014-01-10] - D6
     *                     (2014-01-10, 2014-01-20] - D7
     *                     (2014-01-20, 2014-02-01] - D4, D6
     */
    @Test
    @Transactional
    public void testAllCommunicationTopologiesForComplexCase () {
        Device D1 = this.createSimpleDevice();
        when(clock.instant()).thenReturn(MIDNIGHT_JAN_1ST_2013);
        Device D8 = this.createSlaveDevice("D8", D1);
        when(clock.instant()).thenReturn(MIDNIGHT_JAN_1ST_2014);
        Device D2 = this.createSlaveDevice("D2", D1);
        Device D3 = this.createSlaveDevice("D3", D1);
        Device D4 = this.createSlaveDevice("D4", D2);
        Device D6 = this.createSlaveDevice("D6", D3);
        when(clock.instant()).thenReturn(MIDNIGHT_JAN_20TH_2014);
        D4.setCommunicationGateway(D3);
        D4.save();
        Device D5 = this.createSlaveDevice("D5", D2);
        when(clock.instant()).thenReturn(MIDNIGHT_JAN_10TH_2014);
        Device D7 = this.createSlaveDevice("D7", D3);
        D6.clearCommunicationGateway();
        D6.save();
        when(clock.instant()).thenReturn(MIDNIGHT_JAN_20TH_2014);
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
        DeviceTopology topology = D1.getCommunicationTopology(Range.atMost(MIDNIGHT_MAY_31ST_2014.toInstant(ZoneOffset.UTC)));

        // Asserts
        assertThat(topology.getAllDevices()).hasSize(7);
        List<TopologyTimeslice> topologyTimeslices = topology.timelined().getSlices();
        assertThat(topologyTimeslices).hasSize(4);
        TopologyTimeslice firstSlaveTopology = topologyTimeslices.get(0);
        assertThat(firstSlaveTopology.getPeriod()).isEqualTo(Range.closed(Instant.from(MIDNIGHT_JAN_1ST_2013), Instant.from(MIDNIGHT_JAN_1ST_2014)));
        this.assertDeviceIds(firstSlaveTopology.getDevices(), D8_ID);
        TopologyTimeslice secondSlaveTopology = topologyTimeslices.get(1);
        assertThat(secondSlaveTopology.getPeriod()).isEqualTo(Range.closed(Instant.from(MIDNIGHT_JAN_1ST_2014), Instant.from(MIDNIGHT_JAN_10TH_2014)));
        this.assertDeviceIds(secondSlaveTopology.getDevices(), D2_ID, D3_ID, D4_ID, D6_ID, D8_ID);
        TopologyTimeslice thirdSlaveTopolog = topologyTimeslices.get(2);
        assertThat(thirdSlaveTopolog.getPeriod()).isEqualTo(Range.closed(Instant.from(MIDNIGHT_JAN_10TH_2014), Instant.from(MIDNIGHT_JAN_20TH_2014)));
        this.assertDeviceIds(thirdSlaveTopolog.getDevices(), D2_ID, D3_ID, D4_ID, D7_ID, D8_ID);
        TopologyTimeslice fourthSlaveTopology = topologyTimeslices.get(3);
        assertThat(fourthSlaveTopology.getPeriod()).isEqualTo(Range.closed(Instant.from(MIDNIGHT_JAN_20TH_2014), MIDNIGHT_MAY_31ST_2014.toInstant(ZoneOffset.UTC)));
        this.assertDeviceIds(fourthSlaveTopology.getDevices(), D2_ID, D3_ID, D4_ID, D5_ID, D6_ID, D8_ID);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, mRID);
        device.save();
        return device;
    }

    private Device createSlaveDevice(String name, Device master){
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, MRID + name);
        device.setCommunicationGateway(master);
        device.save();
        return device;
    }

}