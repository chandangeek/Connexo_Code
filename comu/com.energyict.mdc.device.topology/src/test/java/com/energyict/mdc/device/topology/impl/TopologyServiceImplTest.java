package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import java.util.Optional;

import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link TopologyServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:00)
 */
public class TopologyServiceImplTest extends PersistenceIntegrationTest {

    @Test
    @Transactional
    public void clearPhysicalGatewayWhenThereIsNoGatewayTest() {
        Device origin = createSimpleDeviceWithName("Origin");

        // Business method
        this.getTopologyService().clearPhysicalGateway(origin);

        // Asserts: no exception should be thrown
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CANNOT_BE_GATEWAY_FOR_ITSELF + "}")
    public void setPhysicalGatewaySameAsOriginDeviceTest() {
        Device origin = createSimpleDeviceWithName("Origin");

        // Business method
        this.getTopologyService().setPhysicalGateway(origin, origin);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CANNOT_BE_GATEWAY_FOR_ITSELF + "}")
    public void updatePhysicalGatewayWithSameAsOriginDeviceTest() {
        Device physicalGateway = createSimpleDeviceWithName("PhysicalGateway");
        Device device = getDeviceService().newDevice(deviceConfiguration, "Slave", "SlaveMrid");
        device.save();
        getTopologyService().setPhysicalGateway(device, physicalGateway);
        Device reloadedDevice = getReloadedDevice(device);

        // Business method
        getTopologyService().setPhysicalGateway(reloadedDevice, reloadedDevice);
    }

    @Test
    @Transactional
    public void createWithPhysicalGatewayTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER");

        Device device = getDeviceService().newDevice(deviceConfiguration, "Slave", MRID);
        device.save();

        // Business method
        getTopologyService().setPhysicalGateway(device, masterDevice);

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        Optional<Device> reloadedPhysicalGateway = getTopologyService().getPhysicalGateway(reloadedDevice);
        assertThat(reloadedPhysicalGateway.isPresent()).isTrue();
        assertThat(reloadedPhysicalGateway.get().getId()).isEqualTo(masterDevice.getId());
    }

    @Test
    @Transactional
    public void updateMultipleSlavesWithSameMasterTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER", "m");
        Device slaveDevice1 = createSimpleDeviceWithName("SLAVE_1", "s1");
        Device slaveDevice2 = createSimpleDeviceWithName("SLAVE_2", "s2");

        // Business methods
        getTopologyService().setPhysicalGateway(slaveDevice1, masterDevice);
        getTopologyService().setPhysicalGateway(slaveDevice2, masterDevice);

        // Asserts
        Device reloadedSlave1 = getReloadedDevice(slaveDevice1);
        Device reloadedSlave2 = getReloadedDevice(slaveDevice2);
        Optional<Device> reloadedMaster1 = getTopologyService().getPhysicalGateway(reloadedSlave1);
        Optional<Device> reloadedMaster2 = getTopologyService().getPhysicalGateway(reloadedSlave2);
        assertThat(reloadedMaster1.isPresent()).isTrue();
        assertThat(reloadedMaster2.isPresent()).isTrue();
        assertThat(reloadedMaster1.get().getId()).isEqualTo(reloadedMaster2.get().getId()).isEqualTo(masterDevice.getId());
    }

    @Test
    @Transactional
    public void switchToAnotherMaster() {
        Device masterDevice1 = createSimpleDeviceWithName("Physical_MASTER_1", "m1");
        Device masterDevice2 = createSimpleDeviceWithName("Physical_MASTER_2", "m2");
        Device slave = createSimpleDeviceWithName("Origin", "o");
        getTopologyService().setPhysicalGateway(slave, masterDevice1);

        // Business method
        getTopologyService().setPhysicalGateway(slave, masterDevice2);

        // Asserts
        Device reloadedSlave = getReloadedDevice(slave);
        Optional<Device> reloadedPhysicalGateway = getTopologyService().getPhysicalGateway(reloadedSlave);
        assertThat(reloadedPhysicalGateway.isPresent()).isTrue();
        assertThat(reloadedPhysicalGateway.get().getId()).isEqualTo(masterDevice2.getId());
    }

    @Test
    @Transactional
    public void clearPhysicalGatewayTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER", "m");
        Device slave = createSimpleDeviceWithName("SLAVE_1", "s");
        getTopologyService().setPhysicalGateway(slave, masterDevice);

        // Business method
        getTopologyService().clearPhysicalGateway(slave);

        // Asserts
        Device reloadedSlave = getReloadedDevice(slave);
        Optional<Device> reloadedPhysicalGateway = getTopologyService().getPhysicalGateway(reloadedSlave);
        assertThat(reloadedPhysicalGateway.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void createWithSamePhysicalAndCommunicationGatewayTest() {
        Device gatewayForBoth = createSimpleDeviceWithName("GatewayForBoth");
        Device device = getDeviceService().newDevice(deviceConfiguration, "Origin", MRID);
        device.setPhysicalGateway(gatewayForBoth);
        device.setCommunicationGateway(gatewayForBoth);

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getPhysicalGateway().getId()).isEqualTo(reloadedDevice.getCommunicationGateway().getId()).isEqualTo(gatewayForBoth.getId());
    }

    @Test
    @Transactional
    public void getDefaultPhysicalGatewayNullTest() {
        Device simpleDevice = createSimpleDevice();
        assertThat(simpleDevice.getPhysicalGateway()).isNull();
    }

    private ServerDeviceService getDeviceService() {
        return inMemoryPersistence.getDeviceService();
    }

    private TopologyService getTopologyService() {
        return inMemoryPersistence.getTopologyService();
    }

}